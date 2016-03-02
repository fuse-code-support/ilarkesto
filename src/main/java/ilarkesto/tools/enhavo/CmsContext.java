/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package ilarkesto.tools.enhavo;

import ilarkesto.core.base.RuntimeTracker;
import ilarkesto.core.logging.Log;
import ilarkesto.core.time.DateAndTime;
import ilarkesto.integration.BeanshellExecutor;
import ilarkesto.io.DirChangeState;
import ilarkesto.io.IO;
import ilarkesto.protocol.AProtocolConsumer;
import ilarkesto.protocol.HtmlProtocolConsumer;
import ilarkesto.protocol.ProtocolWriter;
import ilarkesto.protocol.SysoutProtocolConsumer;
import ilarkesto.ui.web.HtmlBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CmsContext {

	public static final String SITE_BUILDREQUEST_FILENAME = "buildrequest.txt";

	protected final Log log = Log.get(getClass());

	private File dir;
	private File inputDir;
	private File sitesDir;
	private File dataDir;
	private File templatesDir;

	private File outputDir;
	private File sitesOutputDir;

	private BuildObserver buildObserver;
	private List<AProtocolConsumer> protocolConsumers = new ArrayList<AProtocolConsumer>();
	private ContentProvider contentProvider;
	private BeanshellExecutor beanshellExecutor;

	private ProtocolWriter prot;

	private Map<String, DirChangeState> dirChangeStatesBySiteName = new HashMap<String, DirChangeState>();
	private DirChangeState templatesDirChangeState;
	private DirChangeState dataDirChangeState;

	public CmsContext(File dir, ContentProvider additionalContentProvider) {
		this.dir = dir;

		outputDir = new File(dir.getPath() + "/output");
		sitesOutputDir = new File(outputDir.getPath() + "/sites");

		inputDir = new File(dir.getPath() + "/input");
		sitesDir = new File(inputDir.getPath() + "/sites");
		templatesDir = new File(inputDir.getPath() + "/templates");
		dataDir = new File(inputDir.getPath() + "/data");

		FilesContentProvider filesContentProvider = new FilesContentProvider(dataDir, additionalContentProvider)
				.setBeanshellExecutor(beanshellExecutor);
		contentProvider = new CmsStatusContentProvider(filesContentProvider);
	}

	public void requestSiteBuild(String siteName) {
		IO.touch(getSiteBuildRequestFile(siteName));
	}

	public synchronized void build() {
		if (buildObserver != null) buildObserver.onBuildStart();
		HtmlBuilder htmlProtocolBuilder;
		try {
			htmlProtocolBuilder = new HtmlBuilder(new File(outputDir.getPath() + "/build.html"), IO.UTF_8);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		htmlProtocolBuilder.SCRIPTjavascript(null, "setTimeout(function() {window.location.reload(1);}, 1000);");
		htmlProtocolBuilder.startDIV().setStyle("font-family: mono;");
		HtmlProtocolConsumer htmlProtocolConsumer = new HtmlProtocolConsumer(htmlProtocolBuilder);

		prot = new ProtocolWriter(new SysoutProtocolConsumer(), htmlProtocolConsumer);
		for (AProtocolConsumer consumer : protocolConsumers) {
			prot.addConsumers(consumer);
		}
		prot.pushContext(dir.getAbsolutePath());
		prot.info("Build", DateAndTime.now().format());
		RuntimeTracker rt = new RuntimeTracker();
		try {
			createDirs();
			buildSites();
		} finally {
			prot.popContext();
			prot.info(rt.getRuntimeFormated());
			htmlProtocolBuilder.close();
		}
		if (buildObserver != null) buildObserver.onBuildEnd(rt.getRuntime());
	}

	private void createDirs() {
		IO.createDirectory(outputDir);
		IO.createDirectory(sitesOutputDir);
		IO.createDirectory(sitesDir);
		IO.createDirectory(templatesDir);
		if (templatesDirChangeState == null) templatesDirChangeState = new DirChangeState(templatesDir);
		IO.createDirectory(dataDir);
		if (dataDirChangeState == null) dataDirChangeState = new DirChangeState(dataDir);
	}

	private void buildSites() {
		boolean forceAll = templatesDirChangeState.isChanged() | dataDirChangeState.isChanged();
		for (File siteDir : IO.listFiles(sitesDir)) {
			String siteName = siteDir.getName();
			File buildRequestFile = getSiteBuildRequestFile(siteName);
			boolean force = forceAll || buildRequestFile.exists();
			buildRequestFile.delete();
			buildSite(siteDir, force);
		}
	}

	private File getSiteBuildRequestFile(String siteName) {
		return new File(sitesDir.getPath() + "/" + siteName + "/" + SITE_BUILDREQUEST_FILENAME);
	}

	private void buildSite(File siteDir, boolean force) {
		if (!siteDir.isDirectory()) return;
		String siteName = siteDir.getName();

		boolean changed = checkAndUpdateDirChangeStateForSite(siteDir);
		if (!changed && !force) {
			prot.info("Skipping unchanged site:", siteName);
			return;
		}

		new SiteContext(this, siteDir).build();
	}

	private boolean checkAndUpdateDirChangeStateForSite(File siteDir) {
		String siteName = siteDir.getName();
		DirChangeState dirChangeState = dirChangeStatesBySiteName.get(siteName);
		if (dirChangeState == null) {
			dirChangeState = new DirChangeState(siteDir);
			dirChangeStatesBySiteName.put(siteName, dirChangeState);
			return true;
		}
		return dirChangeState.isChanged();
	}

	public File getDir() {
		return dir;
	}

	public ProtocolWriter getProt() {
		return prot;
	}

	public File getOutputDir() {
		return outputDir;
	}

	public File getSitesOutputDir() {
		return sitesOutputDir;
	}

	public File getInputDir() {
		return inputDir;
	}

	public BeanshellExecutor getBeanshellExecutor() {
		return beanshellExecutor;
	}

	public CmsContext setBeanshellExecutor(BeanshellExecutor beanshellExecutor) {
		this.beanshellExecutor = beanshellExecutor;
		return this;
	}

	public File findTemplateFile(String templatePath) {
		File file = new File(templatesDir.getPath() + "/" + templatePath);
		if (file.exists()) return file;
		return null;
	}

	public ContentProvider getContentProvider() {
		return contentProvider;
	}

	public List<String> getSiteNames() {
		ArrayList<String> ret = new ArrayList<String>();
		for (File siteDir : IO.listFiles(sitesDir)) {
			if (!siteDir.isDirectory()) continue;
			ret.add(siteDir.getName());
		}
		return ret;
	}

	public void setBuildObserver(BuildObserver buildObserver) {
		this.buildObserver = buildObserver;
	}

	public void addProtocolConsumers(AProtocolConsumer consumer) {
		protocolConsumers.add(consumer);
	}
}
