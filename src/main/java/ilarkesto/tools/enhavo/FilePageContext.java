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

import ilarkesto.base.Str;
import ilarkesto.core.base.Filename;
import ilarkesto.core.base.Filepath;
import ilarkesto.core.logging.Log;
import ilarkesto.json.JsonObject;
import ilarkesto.templating.Context;
import ilarkesto.templating.Template;
import ilarkesto.templating.TemplateResolver;

import java.io.File;
import java.util.List;

public class FilePageContext extends APageContext implements TemplateResolver {

	private File contentFile;
	private JsonObject jPage;
	private JsonObject content;
	private String templatePath;
	private Template template;

	public FilePageContext(SiteContext site, File contentFile) {
		super(site);
		this.contentFile = contentFile;
	}

	@Override
	protected void onBuild() {
		jPage = JsonObject.loadFile(contentFile, false);

		templatePath = jPage.getString("template");
		template = site.getTemplate(templatePath);
		if (template == null) {
			error("ABORTED");
			return;
		}
		String templateFilename = new File(templatePath).getName();

		JsonObject jMultipage = jPage.getObject("multipage-content");
		String relativeOutputPath = site.getRelativePath(contentFile.getParentFile());
		if (jMultipage == null) {
			// single page
			content = jPage.getObject("content");
			if (content != null) processContent(content);
			processTemplate(computeOutputPath(relativeOutputPath, templateFilename, contentFile.getName()));
		} else {
			// multiple pages
			processContent(jMultipage);
			List<JsonObject> contents = jMultipage.getArrayOfObjects("list");
			Log.TEST("******** multipage:", jMultipage.toFormatedString());
			if (contents == null || contents.isEmpty()) {
				info("Empty multipage list");
				return;
			}
			int i = 0;
			for (JsonObject content : contents) {
				this.content = content;
				String pageName = null;
				String pagenameProperty = jMultipage.getString("pagename-property");
				if (pagenameProperty != null)
					pageName = content.getDeepString(Str.tokenizeToArray(pagenameProperty, "/"));
				if (pageName == null) pageName = new Filename(contentFile.getName()).getPrefix() + "-" + i;
				cms.getProt().pushContext(pageName);
				try {
					processTemplate(computeOutputPath(relativeOutputPath, templateFilename, pageName));
				} finally {
					cms.getProt().popContext();
				}
				i++;
			}
		}

	}

	private static String computeOutputPath(String relativeOutputPath, String templateFilename, String contentFilename) {
		String filename = new Filename(new Filename(contentFilename).getPrefix(),
				new Filename(templateFilename).getSuffix()).toString();
		return new Filepath(relativeOutputPath).append(filename).toString();
	}

	private void processTemplate(String outputPath) {
		Context templateContext = createTemplateContext();
		template.process(templateContext);
		site.writeOutputFile(outputPath, templateContext.popOutput());
	}

	private Context createTemplateContext() {
		Context context = new Context();
		context.setTemplateResolver(this);

		context.put("content", content);
		context.setScope(content);

		return context;
	}

	@Override
	public Template getTemplate(String path) {
		int idx = templatePath.lastIndexOf('/');
		if (idx > 1) {
			path = templatePath.substring(0, idx) + "/" + path;
		}
		return site.getTemplate(path);
	}

	@Override
	public String toString() {
		return site.getRelativePath(contentFile);
	}

}
