package ilarkesto.json;

import ilarkesto.core.base.Utl;
import ilarkesto.testng.ATest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.List;

import org.testng.annotations.Test;

public class JsonTest extends ATest {

	@Test
	public void printDecimal() {
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		Json.printValue(new Double(23.42), out, 0);
		out.flush();
		assertEquals(sw.toString(), "23.42");
	}

	@Test
	public void parseNumber() {
		assertEquals(Json.parseNumber("23"), Long.valueOf(23));
		assertEquals(Json.parseNumber("23.42"), new BigDecimal("23.42"));
		assertEquals(Json.parseNumber("1E+1"), new BigDecimal("10"));
	}

	@Test
	public void parseString() {
		assertEquals(Json.parseString("\\\""), "\"");
		assertEquals(Json.parseString("\\/"), "/");
		assertEquals(Json.parseString("\\b"), "\b");
		assertEquals(Json.parseString("\\f"), "\f");
		assertEquals(Json.parseString("\\n"), "\n");
		assertEquals(Json.parseString("\\r"), "\r");
		assertEquals(Json.parseString("\\t"), "\t");
		assertEquals(Json.parseString("\\u20ac"), "€");

		assertEquals(Json.parseString("a \\n \\\\ \\/"), "a \n \\ /");
	}

	@Test
	public void escapeString() {
		assertEquals(Json.escapeString("\t"), "\\t");
	}

	@Test
	public void toStringBasics() {
		JsonObject jo = new JsonObject();
		assertEquals(jo.toString(), "{}");
		jo.put("a", "string");
		jo.put("b", null);
		jo.put("c", 23);
		jo.put("d", true);
		assertEquals(jo.toString(), "{\"a\":\"string\",\"b\":null,\"c\":23,\"d\":true}");
	}

	@Test
	public void toStringNested() {
		JsonObject subJo = new JsonObject();
		JsonObject jo = new JsonObject();
		jo.put("sub", subJo);
		assertEquals(jo.toString(), "{\"sub\":{}}");

		subJo.put("a", null);
		assertEquals(jo.toString(), "{\"sub\":{\"a\":null}}");
	}

	@Test
	public void toStringWithEscaping() {
		JsonObject jo = new JsonObject();
		jo.put("a", "this is \"a\"");
		jo.put("b", "new\nline");
		String s = jo.toString();
		assertEquals(s, "{\"a\":\"this is \\\"a\\\"\",\"b\":\"new\\nline\"}");
	}

	@Test
	void valueToString() {
		List<Integer> numbers = Utl.toList(1, 2, 3);
		assertEquals(Json.valueToString(numbers, -1), "[1,2,3]");
		// assertEquals(Json.valueToString(numbers, 0), "[\n\t1,\n\t2,\n\t3\n]");
	}

	@Test
	public void parseWithEscaping() {
		assertEquals(Json.parseString("new\\nline"), "new\nline");
		JsonObject jo = JsonObject.parse("{\"a\":\"new\\nline\"}");
		assertEquals(jo.get("a"), "new\nline");
	}

	@Test
	public void toStringArray() {
		JsonObject jo = new JsonObject();
		jo.put("list", Utl.toList(1, 2, 3));
		assertEquals(jo.toString(), "{\"list\":[1,2,3]}");
		JsonObject sub1 = new JsonObject();
		JsonObject sub2 = new JsonObject();
		jo.put("subs", Utl.toList(sub1, sub2));
		assertEquals(jo.toString(), "{\"list\":[1,2,3],\"subs\":[{},{}]}");
	}

	@Test
	public void parseEmpty() {
		assertEquals(JsonObject.parse("{}").toString(), "{}");
		assertEquals(JsonObject.parse(" { } ").toString(), "{}");
		assertEquals(JsonObject.parse(" \n\t\r {\n\t}\n } ").toString(), "{}");
	}

	@Test
	public void parseBasic() {
		assertEquals(JsonObject.parse("{\"a\":null}").toString(), "{\"a\":null}");
		assertEquals(JsonObject.parse(" {\t\"a\" :\nnull } ").toString(), "{\"a\":null}");
		assertEquals(JsonObject.parse("{\"a\":true}").toString(), "{\"a\":true}");
		assertEquals(JsonObject.parse("{\"a\":false}").toString(), "{\"a\":false}");
		assertEquals(JsonObject.parse("{\"a\":5}").toString(), "{\"a\":5}");
		assertEquals(JsonObject.parse("{\"a\":5,\"b\":7}").toString(), "{\"a\":5,\"b\":7}");
		assertEquals(JsonObject.parse("{\"a\":\"string\"}").toString(), "{\"a\":\"string\"}");
		assertEquals(JsonObject.parse("{\"a\":null,\"b\":null}").toString(), "{\"a\":null,\"b\":null}");
	}

	@Test
	public void parseNested() {
		assertEquals(JsonObject.parse("{\"sub\":{}}").toString(), "{\"sub\":{}}");
		assertEquals(JsonObject.parse(" { \"sub\" : { } } ").toString(), "{\"sub\":{}}");
		assertEquals(JsonObject.parse("{\"sub\":{\"a\":null}}").toString(), "{\"sub\":{\"a\":null}}");
	}

	@Test
	public void parseArray() {
		assertEquals(JsonObject.parse("{\"list\":[]}").toString(), "{\"list\":[]}");
		assertEquals(JsonObject.parse("{\"list\":[1,2]}").toString(), "{\"list\":[1,2]}");
		assertEquals(JsonObject.parse(" { \"list\" : [  ] }").toString(), "{\"list\":[]}");
		assertEquals(JsonObject.parse("{\"list\":[[]]}").toString(), "{\"list\":[[]]}");
	}

	@Test
	public void parseError() {
		assertEquals(JsonObject.parse("{\"a\":\"v\",}").toString(), "{\"a\":\"v\"}");
	}

	@Test
	public void getParent() {
		JsonObject witek = new JsonObject();
		JsonObject address = new JsonObject();
		witek.put("address", address);
		assertSame(witek, address.getParent());

		witek = JsonObject.parse(witek.toString());
		address = witek.getObject("address");
		assertSame(witek, address.getParent());
	}

	@Test
	public void equals() {
		assertEquals(JsonObject.parse("{\"list\":[[]]}"), JsonObject.parse("{\"list\":[[]]}"));
		assertNotEquals(JsonObject.parse("{\"list\":[[{}]]}"), JsonObject.parse("{\"list\":[[]]}"));
	}

	// --- helper ---

}
