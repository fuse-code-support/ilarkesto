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

import ilarkesto.base.Proc;
import ilarkesto.base.Proc.UnexpectedReturnCodeException;
import ilarkesto.core.base.Str;
import ilarkesto.core.parsing.sax.ParseException;
import ilarkesto.io.IO;
import ilarkesto.templating.MustacheLikeTemplateParser;
import ilarkesto.templating.Template;
import ilarkesto.templating.TemplateResolver;

import java.io.File;
import java.io.FileFilter;

public class SiteContext extends ABuilder implements TemplateResolver {

	private File dir;
	private File pagesDir;
	private File templatesDir;
	private File contentDir;
	private File resourcesDir;
	private File scriptsDir;

	private SiteConfig config;

	private File outputDir;
	private ContentProvider contentProvider;
	private SiteBuildlog siteBuildlog;

	public SiteContext(CmsContext cms, File dir) {
		super(cms);
		this.dir = dir;

		pagesDir = new File(dir.getPath() + "/pages");
		IO.createDirectory(pagesDir);

		templatesDir = new File(dir.getPath() + "/templates");
		IO.createDirectory(templatesDir);

		contentDir = new File(dir.getPath() + "/content");
		IO.createDirectory(contentDir);

		resourcesDir = new File(dir.getPath() + "/resources");
		IO.createDirectory(resourcesDir);

		scriptsDir = new File(dir.getPath() + "/scripts");
		IO.createDirectory(scriptsDir);

		config = new SiteConfig(this);

		contentProvider = new FilesContentProvider(contentDir, cms.getContentProvider())
				.setBeanshellExecutor(cms.getBeanshellExecutor());

		siteBuildlog = new SiteBuildlog(this);

		cms.getProt().addConsumers(siteBuildlog);
	}

	@Override
	protected void onBuild() {
		config = new SiteConfig(this);

		outputDir = new File(cms.getSitesOutputDir().getPath() + "/" + dir.getName());
		siteBuildlog.onBuildStart();

		clean();
		processPagesFiles(pagesDir);
		copyRessources();
		boolean success = runScripts();

		if (success) siteBuildlog.onBuildSuccess();
	}

	private void copyRessources() {
		IO.copyFilesIfDifferInTimeOrLength(resourcesDir.listFiles(), outputDir, new FileFilter() {

			@Override
			public boolean accept(File file) {
				if (file.getName().equals(IO.MAC_SYS_FILENAME)) return false;
				return true;
			}

		});
	}

	private void clean() {
		if (!config.isCleanOutputDir()) return;
		info("Cleaning output dir:", outputDir.getPath());
		IO.delete(outputDir.listFiles());
	}

	private boolean runScripts() {
		for (File file : IO.listFiles(scriptsDir)) {
			cms.getProt().pushContext("script> " + file.getName());
			boolean success;
			try {
				success = runScript(file);
			} finally {
				cms.getProt().popContext();
			}
			if (!success) return false;
		}
		return true;
	}

	private boolean runScript(File file) {
		if (file.isDirectory()) return true;
		String executorCommand = "bash";

		Proc proc = new Proc(executorCommand);
		proc.addParameter(file.getAbsolutePath());
		proc.setWorkingDir(outputDir);
		proc.addEnvironmentParameter("CMS_INPUT_DIR", cms.getInputDir().getAbsolutePath());
		proc.addEnvironmentParameter("CMS_OUTPUT_DIR", cms.getOutputDir().getAbsolutePath());
		proc.addEnvironmentParameter("CMS_SITE_INPUT_DIR", dir.getAbsolutePath());
		proc.addEnvironmentParameter("CMS_SITE_OUTPUT_DIR", outputDir.getAbsolutePath());
		String output;
		try {
			output = proc.execute();
		} catch (UnexpectedReturnCodeException ex) {
			error(ex.getRc(), ex.getOutput());
			return false;
		}

		info(output);

		return true;
	}

	private void processPagesFiles(File dir) {
		for (File file : IO.listFiles(dir)) {
			if (file.isDirectory()) {
				processPagesFiles(file);
				continue;
			}
			String name = file.getName();
			if (name.endsWith(".json")) {
				FilePageContext page = new FilePageContext(this, file);
				page.build();
				continue;
			}
			writeOutputFile(getRelativePath(file), file);
		}
	}

	public void writeOutputFile(String path, String text) {
		File file = getOutputFile(path);
		info("output>", file.getPath());
		IO.writeFile(file, text, IO.UTF_8);
	}

	public void writeOutputFile(String path, File source) {
		File file = getOutputFile(path);
		boolean copied = IO.copyFileIfDiffersInTimeOrLength(source, file);
		info("output>", file.getPath(), copied ? "" : "[unchanged]");
	}

	File getOutputFile(String path) {
		return new File(outputDir.getPath() + "/" + path);
	}

	public File getDir() {
		return dir;
	}

	public File getPagesDir() {
		return pagesDir;
	}

	@Override
	public String toString() {
		return dir.getName();
	}

	@Override
	public Template getTemplate(String templatePath) {
		if (!config.isProductionMode()) {
			if ("CMS_BUILD_INFO".equals(templatePath) || "cmsinfo.incl.mustache.html".equals(templatePath)) {
				try {
					return MustacheLikeTemplateParser
							.parseTemplate(IO.readResource("cmsinfo.incl.mustache.html", SiteContext.class));
				} catch (ParseException ex) {
					throw new RuntimeException(ex);
				}
			}
		}

		File file = findTemplateFile(templatePath);
		if (file == null) {
			error("Template not found:", templatePath);
			return null;
		}
		info("template:", templatePath, "->", file.getPath());
		try {
			return MustacheLikeTemplateParser.parseTemplate(file);
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
	}

	public File findTemplateFile(String templatePath) {
		File file = new File(templatesDir.getPath() + "/" + templatePath);
		if (file.exists()) return file;
		return cms.findTemplateFile(templatePath);
	}

	public String getRelativePath(File file) {
		String ret = Str.removePrefix(file.getPath(), getPagesDir().getPath());
		ret = Str.removePrefix(ret, "/");
		return ret;
	}

	public ContentProvider getContentProvider() {
		return contentProvider;
	}

}
