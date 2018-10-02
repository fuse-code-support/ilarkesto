package ilarkesto.json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class JsonObject {

    public static void main(String[] args) {
        JsonObject json = new JsonObject();
        json.put("name", "Witek");
        json.put("gender", "male");

        json.putNewObject("subobject").put("a", "1");
        json.addToArray("array", "1");
        json.addToArray("array", "2");
        json.addToArray("array", new JsonObject());

        String s = json.toFormatedString();
        System.out.println(s);

        JsonObject.parse(s);
    }

    private final Map<String, Object> elements = new LinkedHashMap<String, Object>();
    private int idx = -1;
    private JsonObject parent;
    private File file;

    public JsonObject() {
    }

    private JsonObject(String json, int offset) {
        parse(json, offset);
    }

    public JsonObject(String json) {
        this(json, 0);
    }

    public JsonObject(Map<?, ?> map) {
        for (Map.Entry entry : map.entrySet()) {
            String name = entry.getKey().toString();
            put(name, entry.getValue());
        }
    }

    public Map<String, String> toMapOfStrings() {
        LinkedHashMap<String, String> ret = new LinkedHashMap<String, String>();
        for (Entry<String, Object> entry : elements.entrySet()) {
            Object value = entry.getValue();
            ret.put(entry.getKey(), value == null ? null : value.toString());
        }
        return ret;
    }

    public static JsonObject parse(String json) {
        if (json == null || json.length() == 0)
            return new JsonObject();
        return new JsonObject(json, 0);
    }

    public File getFile() {
        return file;
    }

    public static JsonObject loadFile(File file) {
        return loadFile(file, false);
    }

    public static JsonObject loadFile(File file, boolean createEmptyIfNoFile) {
        if (!file.exists()) {
            if (createEmptyIfNoFile) {
                JsonObject json = new JsonObject();
                json.assignFile(file);
                return json;
            }
            return null;
        }
        JsonObject object;
        try {
            object = parse(load(file));
        } catch (ParseException ex) {
            if (!createEmptyIfNoFile) throw ex;
            object = new JsonObject();
        }
        object.assignFile(file);
        return object;
    }

    public static JsonObject loadResource(String resourceName, Class<?> resourcePackageClass,
                                          boolean createemptyIfNoResource) {
        InputStream is = resourcePackageClass == null ? ClassLoader.getSystemResourceAsStream(resourceName)
                : resourcePackageClass.getResourceAsStream(resourceName);
        if (is == null) {
            if (createemptyIfNoResource) return new JsonObject();
            return null;
        }
        return parse(load(is));
    }

    public static JsonObject loadFromUrl(String url) {
        InputStream is = null;
        try {
            is = new URL(url).openStream();
            return parse(load(is));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            if (is != null) try {
                is.close();
            } catch (IOException ex) {
            }
        }
    }

    public static JsonObject loadFromStream(InputStream is) {
        return parse(load(is));
    }

    public void assignFile(File file) {
        this.file = file;
    }

    public void save(boolean formated) {
        if (file == null) throw new IllegalStateException("file == null");
        write(file, formated);
    }

    public void save() {
        save(true);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JsonObject)) return false;
        return elements.equals(((JsonObject) obj).elements);
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    // --- inspecting ---

    public Set<String> getProperties() {
        return elements.keySet();
    }

    public Object get(String name) {
        return elements.get(name);
    }

    public boolean contains(String name) {
        return elements.containsKey(name);
    }

    public boolean rename(String oldName, String newName) {
        if (!contains(oldName)) return false;
        if (contains(newName))
            throw new IllegalStateException("Element already exists: " + newName);
        Object value = get(oldName);
        remove(oldName);
        put(newName, value);
        return true;
    }

    public boolean isSet(String name) {
        if (!elements.containsKey(name)) return false;
        return get(name) != null;
    }

    public boolean containsString(String name, String expected) {
        String value = getString(name);
        return value == expected || (value != null && value.equals(expected));
    }

    public String getString(String name) {
        return (String) get(name);
    }

    public String getString(String name, String defaultValue) {
        String value = getString(name);
        return value == null ? defaultValue : value;
    }

    public JsonObject getObject(String name) {
        return (JsonObject) get(name);
    }

    public JsonObject getObjectOrCreate(String name) {
        JsonObject ret = getObject(name);
        if (ret == null) ret = putNewObject(name);
        return ret;
    }

    public boolean isObject(String property) {
        return get(property) instanceof JsonObject;
    }

    public List getArray(String name) {
        return (List) get(name);
    }

    public <T> List<T> getArray(String name, Class<T> type) {
        return (List<T>) get(name);
    }

    public Number getNumber(String name) {
        return (Number) get(name);
    }

    public Integer getInteger(String name) {
        Number value = getNumber(name);
        return toInteger(value);
    }

    private Integer toInteger(Number value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        return value.intValue();
    }

    public Long getLong(String name) {
        Number value = getNumber(name);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        return value.longValue();
    }

    public Double getDouble(String name) {
        Number value = getNumber(name);
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        return value.doubleValue();
    }

    public Float getFloat(String name) {
        Number value = getNumber(name);
        if (value == null) return null;
        if (value instanceof Float) return (Float) value;
        return value.floatValue();
    }

    public Byte getByte(String name) {
        Number value = getNumber(name);
        if (value == null) return null;
        if (value instanceof Byte) return (Byte) value;
        return value.byteValue();
    }

    public Boolean getBoolean(String name) {
        return (Boolean) get(name);
    }

    public boolean isTrue(String name) {
        Boolean value = getBoolean(name);
        if (value == null) return false;
        return value.booleanValue();
    }

    public List<String> getArrayOfStrings(String name) {
        return getArray(name, String.class);
    }

    public List<JsonObject> getArrayOfObjects(String name) {
        return getArray(name, JsonObject.class);
    }

    public List<Integer> getArrayOfIntegers(String name) {
        List<Number> values = (List<Number>) get(name);
        List<Integer> ret = new ArrayList<Integer>(values.size());
        for (Number value : values) {
            ret.add(toInteger(value));
        }
        return ret;
    }

    public JsonObject getParent() {
        return parent;
    }

    public String getDeepString(String... path) {
        JsonObject json = getDeepParent(path);
        if (json == null) return null;
        return json.getString(path[path.length - 1]);
    }

    public JsonObject getDeepObject(String... path) {
        JsonObject json = getDeepParent(path);
        if (json == null) return null;
        return json.getObject(path[path.length - 1]);
    }

    public List<JsonObject> getDeepArrayOfObjects(String... path) {
        JsonObject json = getDeepParent(path);
        if (json == null) return null;
        return json.getArrayOfObjects(path[path.length - 1]);
    }

    private JsonObject getDeepParent(String... path) {
        JsonObject json = this;
        for (int i = 0; i < path.length - 1; i++) {
            json = json.getObject(path[i]);
            if (json == null) return null;
        }
        return json;
    }

    // --- manipulating ---

    /**
     * @return true, if property did not exist before
     */
    public boolean putIfNull(String name, Object value) {
        if (name == null || name.length() == 0) throw new RuntimeException("name required");
        Object currentValue = get(name);
        if (currentValue != null) return false;
        elements.put(name, adopt(value));
        return true;
    }

    public <V> V put(String name, V value) {
        if (name == null || name.length() == 0) throw new RuntimeException("name required");
        elements.put(name, adopt(value));
        return value;
    }

    public List addToArray(String name, Object value) {
        List array = getArray(name);
        if (array == null) {
            array = new ArrayList();
            put(name, array);
            return addToArray(name, value);
        }
        array.add(adopt(value));
        return array;
    }

    public List addToArray(String name, Collection values) {
        List array = getArray(name);
        if (array == null) {
            array = new ArrayList();
            put(name, array);
            return addToArray(name, values);
        }
        for (Object value : values) {
            array.add(adopt(value));
        }
        return array;
    }

    public boolean removeFromArray(String name, Object value) {
        List array = getArray(name);
        if (array == null) return false;
        return array.remove(value);
    }

    public Object remove(String name) {
        if (name == null || name.length() == 0) throw new RuntimeException("name required");
        return elements.remove(name);
    }

    public JsonObject putNewObject(String name) {
        return put(name, new JsonObject());
    }

    private Object adopt(Object childToAdopt) {
        Object child = Json.convertValue(childToAdopt);
        if (child instanceof JsonObject) {
            ((JsonObject) child).parent = this;
            return child;
        }
        if (child instanceof Iterable) {
            List list = new ArrayList();
            for (Object item : ((Iterable) child)) {
                list.add(adopt(item));
            }
            return list;
        }
        return child;
    }

    // --- formating ---

    void print(PrintWriter out, int indentation) {
        if (Json.isShort(elements.values())) indentation = -1;
        out.print('{');
        if (indentation >= 0) indentation++;
        boolean first = true;
        for (Map.Entry<String, Object> element : elements.entrySet()) {
            if (first) {
                first = false;
            } else {
                out.print(',');
            }
            if (indentation >= 0) out.print('\n');
            Json.indent(out, indentation);
            out.print('"');
            out.print(Json.escapeString(element.getKey()));
            out.print("\":");
            if (indentation >= 0) out.print(' ');
            Json.printValue(element.getValue(), out, indentation);
        }
        if (indentation >= 0) {
            indentation--;
            out.print('\n');
            Json.indent(out, indentation);
        }
        out.print('}');
    }

    public String toString(int indentation) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter out = new PrintWriter(stringWriter);
        print(out, indentation);
        out.flush();
        return stringWriter.toString();
    }

    public String toFormatedString() {
        return toString(0);
    }

    @Override
    public String toString() {
        return toString(-1);
    }

    // --- parsing ---

    private int parse(String json, int offset) {
        if (json.length() < 2) throw new ParseException("Empty string is invalid", json, 0);
        idx = offset;
        parseWhitespace(json, "'{'");
        if (json.charAt(idx) != '{') throw new ParseException("Expecting '{'", json, idx);
        idx++;
        parseWhitespace(json, "elements or '}'");
        boolean first = true;
        while (json.charAt(idx) != '}') {
            if (first) {
                first = false;
            } else {
                if (json.charAt(idx) == ',') {
                    idx++;
                } else {
                    throw new ParseException("Expecting ','", json, idx);
                }
            }
            parseElement(json);
            parseWhitespace(json, "',' or '}'");
        }
        idx++;
        return idx;
    }

    private void parseWhitespace(String json, String expectation) {
        idx = Json.getFirstNonWhitespaceIndex(json, idx);
        if (idx < 0) throw new ParseException("Expecting " + expectation, json, idx);
    }

    private void parseElement(String json) {
        parseWhitespace(json, "'\"'");
        if (json.charAt(idx) == '}') return;
        if (json.charAt(idx) != '"') throw new ParseException("Expecting '\"'", json, idx);
        idx++;
        int nameEndIdx = Json.getFirstQuoting(json, idx);
        if (nameEndIdx < 0) throw new ParseException("Unclosed element name", json, idx);
        String name = json.substring(idx, nameEndIdx);
        idx = nameEndIdx + 1;
        parseWhitespace(json, "':'");
        if (json.charAt(idx) != ':')
            throw new ParseException("Expecting ':' after element name \"" + name + "\"", json, idx);
        idx++;
        parseWhitespace(json, "element value");
        Object value = parseValue(json);
        put(name, value);
    }

    private Object parseValue(String json) {
        if (json.startsWith("null", idx)) {
            idx += 4;
            return null;
        } else if (json.startsWith("true", idx)) {
            idx += 4;
            return true;
        } else if (json.startsWith("false", idx)) {
            idx += 5;
            return false;
        } else if (json.charAt(idx) == '"') {
            idx++;
            int valueEndIdx = Json.getFirstQuoting(json, idx);
            if (valueEndIdx < 0)
                throw new ParseException("Unclosed element string value", json, idx);
            String value = json.substring(idx, valueEndIdx);
            idx = valueEndIdx + 1;
            return Json.parseString(value);
        } else if (json.charAt(idx) == '{') {
            JsonObject value = new JsonObject(json, idx);
            idx = value.idx;
            return value;
        } else if (json.charAt(idx) == '[') {
            List list = new ArrayList();
            idx++;
            while (true) {
                parseWhitespace(json, "array");
                if (json.charAt(idx) == ']') break;
                Object value = parseValue(json);
                list.add(value);
                parseWhitespace(json, "array");
                if (json.charAt(idx) == ']') break;
                if (json.charAt(idx) != ',')
                    throw new ParseException("Expecting array separator ','", json, idx);
                idx++;
            }
            idx++;
            return list;
        } else {
            int len = json.length();
            int endIdx = idx;
            while (endIdx < len) {
                endIdx++;
                char endCh = json.charAt(endIdx);
                if (endCh == ',' || endCh == '}' || endCh == ']' || Json.isWhitespace(endCh)) {
                    break;
                }
            }
            String sNumber = json.substring(idx, endIdx);
            Number number;
            try {
                number = Json.parseNumber(sNumber);
            } catch (NumberFormatException ex) {
                throw new ParseException("Expecting number in <" + sNumber + ">", json, idx);
            }
            idx = endIdx;
            return number;
        }
    }

    // --- IO ---

    public void write(OutputStream out, boolean formated) {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        write(writer, formated);
        try {
            writer.flush();
        } catch (IOException ex) {
            throw new RuntimeException("Writing failed", ex);
        }
    }

    public void write(Writer out, boolean formated) {
        write(new PrintWriter(out), formated);
    }

    public void write(File file, boolean formated) {
        File dir = file.getParentFile();
        if (!dir.exists()) {
            if (!dir.mkdirs())
                throw new RuntimeException("Creating directory failed: " + dir.getAbsolutePath());
        }
        PrintWriter out;
        try {
            out = new PrintWriter(new FileWriter(file));
        } catch (IOException ex) {
            throw new RuntimeException("Writing file failed: " + file.getAbsolutePath(), ex);
        }
        write(out, formated);
        out.close();
    }

    public void write(PrintWriter out, boolean formated) {
        int indentation = formated ? 0 : -1;
        print(out, indentation);
        out.flush();
    }

    private static String load(File file) {
        if (!file.exists()) return null;
        try {
            return load(new FileInputStream(file));
        } catch (Exception ex) {
            throw new RuntimeException("Loading file failed: +" + file.getAbsolutePath(), ex);
        }
    }

    private static String load(InputStream is) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = in.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Loading JSON failed", ex);
        } finally {
            if (in != null) try {
                in.close();
            } catch (Exception e) {
            }
        }
    }

}