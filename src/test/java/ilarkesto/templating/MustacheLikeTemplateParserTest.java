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
package ilarkesto.templating;

import ilarkesto.core.parsing.sax.ParseException;
import ilarkesto.testng.ATest;

import java.util.Arrays;

import org.testng.annotations.Test;

public class MustacheLikeTemplateParserTest extends ATest {

	@Test
	public void templating() throws ParseException {
		assertTemplateOutput("hello world", "hello world");
		assertTemplateOutput("hello {{nonExistingVariable}}", "hello ");
		assertTemplateOutput("hello {{a}}", "hello a-value");
		assertTemplateOutput("hello {{#rose}}existing{{/}}", "hello existing");
		assertTemplateOutput("hello {{#rose}}{{color}}{{/}}", "hello red");
		assertTemplateOutput("hello {{html}}", "hello &lt;html&gt;");
		assertTemplateOutput("hello {{{html}}}", "hello <html>");
		assertTemplateOutput("{{>> panel}}{{<< title}}hello{{/}}{{<< body}}world{{/}}{{/}}", "(hello!world)");
	}

	@Test
	public void includeComponent() throws ParseException {
		Template template = MustacheLikeTemplateParser
				.parseTemplate("{{>> panel}}{{<< title}}hello{{/}}{{<< body}}world{{/}}{{/}}");
		assertNotEmpty(template.children);
		ComponentIncludeElement componentInclude = (ComponentIncludeElement) template.children.get(0);
		assertEquals(componentInclude.getPath(), "panel");
		// assertEmpty(componentInclude.getChildren());
		Template titleTemplate = componentInclude.getInlineTemplates().get("title");
		assertNotNull(titleTemplate);
		assertEquals(((TextElement) titleTemplate.children.get(0)).getText(), "hello");
	}

	@Test
	public void loopWithContent() throws ParseException {
		Template template = MustacheLikeTemplateParser.parseTemplate("{{#flower}}hello world{{/}}");
		assertNotEmpty(template.children);

		LoopElement loop = (LoopElement) template.children.get(0);
		assertEquals(loop.getExpression(), "flower");

		assertNotEmpty(loop.children);
		TextElement text = (TextElement) loop.children.get(0);
		assertEquals(text.getText(), "hello world");
	}

	@Test
	public void loopEmpty() throws ParseException {
		Template template = MustacheLikeTemplateParser.parseTemplate("{{#flower}}{{/}}");
		assertNotEmpty(template.children);
		LoopElement loop = (LoopElement) template.children.iterator().next();
		assertEquals(loop.getExpression(), "flower");
	}

	@Test
	public void include() throws ParseException {
		Template template = MustacheLikeTemplateParser.parseTemplate("{{> incl}}");
		assertNotEmpty(template.children);
		IncludeElement variable = (IncludeElement) template.children.get(0);
		assertEquals(variable.getPath(), "incl");
	}

	@Test
	public void variableAndText() throws ParseException {
		Template template = MustacheLikeTemplateParser.parseTemplate("start {{a}}!");
		assertNotEmpty(template.children);
		assertSize(template.children, 3);
		assertEquals(((TextElement) template.children.get(0)).getText(), "start ");
		assertEquals(((VariableElement) template.children.get(1)).getExpression(), "a");
		assertEquals(((TextElement) template.children.get(2)).getText(), "!");
	}

	@Test
	public void variable() throws ParseException {
		Template template = MustacheLikeTemplateParser.parseTemplate("{{a}}");
		assertNotEmpty(template.children);
		VariableElement variable = (VariableElement) template.children.get(0);
		assertEquals(variable.getExpression(), "a");
		assertTrue(variable.isEscape());
	}

	@Test
	public void variableUnescaped() throws ParseException {
		Template template = MustacheLikeTemplateParser.parseTemplate("{{{html}}}");
		assertNotEmpty(template.children);
		VariableElement variable = (VariableElement) template.children.get(0);
		assertFalse(variable.isEscape());
		assertEquals(variable.getExpression(), "html");
	}

	@Test
	public void variableEscaped() throws ParseException {
		Template template = MustacheLikeTemplateParser.parseTemplate("{{html}}");
		assertNotEmpty(template.children);
		VariableElement variable = (VariableElement) template.children.get(0);
		assertTrue(variable.isEscape());
		assertEquals(variable.getExpression(), "html");
	}

	@Test
	public void text() throws ParseException {
		Template template = MustacheLikeTemplateParser.parseTemplate("hello world");
		assertNotEmpty(template.children);
		TextElement text = (TextElement) template.children.get(0);
		assertEquals(text.getText(), "hello world");
	}

	private static void assertTemplateOutput(String templateCode, String output) throws ParseException {
		Template template = MustacheLikeTemplateParser.parseTemplate(templateCode);
		Context context = new Context();
		context.put("a", "a-value");
		context.put("html", "<html>");
		context.put("rose", new Flower("rose", "red"));
		context.put("flowers",
			Arrays.asList(new Flower("rose", "white"), new Flower("rose", "red"), new Flower("tulip", "yellow")));
		context.setTemplateResolver(new TemplateResolver() {

			@Override
			public Template getTemplate(String path) {
				if (path.equals("panel")) try {
					return MustacheLikeTemplateParser.parseTemplate("({{> title}}!{{> body}})");
				} catch (ParseException ex) {
					throw new RuntimeException(ex);
				}
				return null;
			}
		});
		template.process(context);
		assertEquals(context.popOutput(), output);
	}
}
