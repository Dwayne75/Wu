package winterwell.json;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class JSONObject
{
  private final HashMap myHashMap;
  
  private static final class Null
  {
    protected final Object clone()
    {
      return this;
    }
    
    public boolean equals(Object object)
    {
      return (object == null) || (object == this);
    }
    
    public String toString()
    {
      return "null";
    }
  }
  
  public static final Object NULL = new Null(null);
  
  public JSONObject()
  {
    this.myHashMap = new HashMap();
  }
  
  public JSONObject(JSONObject jo, String[] sa)
    throws JSONException
  {
    this();
    for (int i = 0; i < sa.length; i++) {
      putOpt(sa[i], jo.opt(sa[i]));
    }
  }
  
  public JSONObject(JSONTokener x)
    throws JSONException
  {
    this();
    if (x.nextClean() != '{') {
      throw x.syntaxError("A JSONObject text must begin with '{'");
    }
    for (;;)
    {
      char c = x.nextClean();
      switch (c)
      {
      case '\000': 
        throw x.syntaxError("A JSONObject text must end with '}'");
      case '}': 
        return;
      }
      x.back();
      String key = x.nextValue().toString();
      
      c = x.nextClean();
      if (c == '=')
      {
        if (x.next() != '>') {
          x.back();
        }
      }
      else if (c != ':') {
        throw x.syntaxError("Expected a ':' after a key");
      }
      put(key, x.nextValue());
      switch (x.nextClean())
      {
      case ',': 
      case ';': 
        if (x.nextClean() == '}') {
          return;
        }
        x.back();
      }
    }
    return;
    
    throw x.syntaxError("Expected a ',' or '}'");
  }
  
  public JSONObject(Map map)
  {
    this.myHashMap = (map == null ? 
      new HashMap() : 
      new HashMap(map));
  }
  
  public JSONObject(Object object, String[] names)
  {
    this();
    Class c = object.getClass();
    for (int i = 0; i < names.length; i++) {
      try
      {
        String name = names[i];
        Field field = c.getField(name);
        Object value = field.get(object);
        put(name, value);
      }
      catch (Exception localException) {}
    }
  }
  
  public JSONObject(String string)
    throws JSONException
  {
    this(new JSONTokener(string));
  }
  
  public JSONObject accumulate(String key, Object value)
    throws JSONException
  {
    testValidity(value);
    Object o = opt(key);
    if (o == null) {
      put(key, (value instanceof JSONArray) ? 
        new JSONArray().put(value) : 
        value);
    } else if ((o instanceof JSONArray)) {
      ((JSONArray)o).put(value);
    } else {
      put(key, new JSONArray().put(o).put(value));
    }
    return this;
  }
  
  public JSONObject append(String key, Object value)
    throws JSONException
  {
    testValidity(value);
    Object o = opt(key);
    if (o == null) {
      put(key, new JSONArray().put(value));
    } else if ((o instanceof JSONArray)) {
      put(key, ((JSONArray)o).put(value));
    } else {
      throw new JSONException("JSONObject[" + key + 
        "] is not a JSONArray. " + o.getClass());
    }
    return this;
  }
  
  public static String doubleToString(double d)
  {
    if ((Double.isInfinite(d)) || (Double.isNaN(d))) {
      return "null";
    }
    String s = Double.toString(d);
    if ((s.indexOf('.') > 0) && (s.indexOf('e') < 0) && (s.indexOf('E') < 0))
    {
      while (s.endsWith("0")) {
        s = s.substring(0, s.length() - 1);
      }
      if (s.endsWith(".")) {
        s = s.substring(0, s.length() - 1);
      }
    }
    return s;
  }
  
  public Object get(String key)
    throws JSONException
  {
    Object o = opt(key);
    if (o == null) {
      throw new JSONException("JSONObject[" + quote(key) + 
        "] not found.");
    }
    return o;
  }
  
  public boolean getBoolean(String key)
    throws JSONException
  {
    Object o = get(key);
    if ((o.equals(Boolean.FALSE)) || (
      ((o instanceof String)) && 
      (((String)o).equalsIgnoreCase("false")))) {
      return false;
    }
    if ((o.equals(Boolean.TRUE)) || (
      ((o instanceof String)) && 
      (((String)o).equalsIgnoreCase("true")))) {
      return true;
    }
    throw new JSONException("JSONObject[" + quote(key) + 
      "] is not a Boolean. " + o.getClass());
  }
  
  public double getDouble(String key)
    throws JSONException
  {
    Object o = get(key);
    try
    {
      return (o instanceof Number) ? 
        ((Number)o).doubleValue() : 
        Double.valueOf((String)o).doubleValue();
    }
    catch (Exception e)
    {
      throw new JSONException("JSONObject[" + quote(key) + 
        "] is not a number. " + o.getClass());
    }
  }
  
  public int getInt(String key)
    throws JSONException
  {
    Object o = get(key);
    return (o instanceof Number) ? 
      ((Number)o).intValue() : (int)getDouble(key);
  }
  
  public JSONArray getJSONArray(String key)
    throws JSONException
  {
    Object o = get(key);
    if ((o instanceof JSONArray)) {
      return (JSONArray)o;
    }
    throw new JSONException("JSONObject[" + quote(key) + 
      "] is not a JSONArray. " + o.getClass());
  }
  
  public JSONObject getJSONObject(String key)
    throws JSONException
  {
    Object o = get(key);
    if ((o instanceof JSONObject)) {
      return (JSONObject)o;
    }
    throw new JSONException("JSONObject[" + quote(key) + 
      "] is not a JSONObject. " + o.getClass());
  }
  
  public long getLong(String key)
    throws JSONException
  {
    Object o = get(key);
    return (o instanceof Number) ? 
      ((Number)o).longValue() : getDouble(key);
  }
  
  public String getString(String key)
    throws JSONException
  {
    return get(key).toString();
  }
  
  public boolean has(String key)
  {
    return this.myHashMap.containsKey(key);
  }
  
  public boolean isNull(String key)
  {
    return NULL.equals(opt(key));
  }
  
  public Iterator keys()
  {
    return this.myHashMap.keySet().iterator();
  }
  
  public int length()
  {
    return this.myHashMap.size();
  }
  
  public JSONArray names()
  {
    JSONArray ja = new JSONArray();
    Iterator keys = keys();
    while (keys.hasNext()) {
      ja.put(keys.next());
    }
    return ja.length() == 0 ? null : ja;
  }
  
  public static String numberToString(Number n)
    throws JSONException
  {
    if (n == null) {
      throw new JSONException("Null pointer");
    }
    testValidity(n);
    
    String s = n.toString();
    if ((s.indexOf('.') > 0) && (s.indexOf('e') < 0) && (s.indexOf('E') < 0))
    {
      while (s.endsWith("0")) {
        s = s.substring(0, s.length() - 1);
      }
      if (s.endsWith(".")) {
        s = s.substring(0, s.length() - 1);
      }
    }
    return s;
  }
  
  public Object opt(String key)
  {
    return key == null ? null : this.myHashMap.get(key);
  }
  
  public boolean optBoolean(String key)
  {
    return optBoolean(key, false);
  }
  
  public boolean optBoolean(String key, boolean defaultValue)
  {
    try
    {
      return getBoolean(key);
    }
    catch (Exception e) {}
    return defaultValue;
  }
  
  public JSONObject put(String key, Collection value)
    throws JSONException
  {
    put(key, new JSONArray(value));
    return this;
  }
  
  public double optDouble(String key)
  {
    return optDouble(key, NaN.0D);
  }
  
  public double optDouble(String key, double defaultValue)
  {
    try
    {
      Object o = opt(key);
      return (o instanceof Number) ? ((Number)o).doubleValue() : 
        new Double((String)o).doubleValue();
    }
    catch (Exception e) {}
    return defaultValue;
  }
  
  public int optInt(String key)
  {
    return optInt(key, 0);
  }
  
  public int optInt(String key, int defaultValue)
  {
    try
    {
      return getInt(key);
    }
    catch (Exception e) {}
    return defaultValue;
  }
  
  public JSONArray optJSONArray(String key)
  {
    Object o = opt(key);
    return (o instanceof JSONArray) ? (JSONArray)o : null;
  }
  
  public JSONObject optJSONObject(String key)
  {
    Object o = opt(key);
    return (o instanceof JSONObject) ? (JSONObject)o : null;
  }
  
  public long optLong(String key)
  {
    return optLong(key, 0L);
  }
  
  public long optLong(String key, long defaultValue)
  {
    try
    {
      return getLong(key);
    }
    catch (Exception e) {}
    return defaultValue;
  }
  
  public String optString(String key)
  {
    return optString(key, "");
  }
  
  public Map<String, ?> getMap()
  {
    return this.myHashMap;
  }
  
  public String optString(String key, String defaultValue)
  {
    Object o = opt(key);
    return o != null ? o.toString() : defaultValue;
  }
  
  public JSONObject put(String key, boolean value)
    throws JSONException
  {
    put(key, value ? Boolean.TRUE : Boolean.FALSE);
    return this;
  }
  
  public JSONObject put(String key, double value)
    throws JSONException
  {
    put(key, new Double(value));
    return this;
  }
  
  public JSONObject put(String key, int value)
    throws JSONException
  {
    put(key, new Integer(value));
    return this;
  }
  
  public JSONObject put(String key, long value)
    throws JSONException
  {
    put(key, new Long(value));
    return this;
  }
  
  public JSONObject put(String key, Map value)
    throws JSONException
  {
    put(key, new JSONObject(value));
    return this;
  }
  
  public JSONObject put(String key, Object value)
    throws JSONException
  {
    if (key == null) {
      throw new JSONException("Null key.");
    }
    if (value != null)
    {
      testValidity(value);
      this.myHashMap.put(key, value);
    }
    else
    {
      remove(key);
    }
    return this;
  }
  
  public JSONObject putOpt(String key, Object value)
    throws JSONException
  {
    if ((key != null) && (value != null)) {
      put(key, value);
    }
    return this;
  }
  
  public static String quote(String string)
  {
    if ((string == null) || (string.length() == 0)) {
      return "\"\"";
    }
    char c = '\000';
    
    int len = string.length();
    StringBuffer sb = new StringBuffer(len + 4);
    
    sb.append('"');
    for (int i = 0; i < len; i++)
    {
      char b = c;
      c = string.charAt(i);
      switch (c)
      {
      case '"': 
      case '\\': 
        sb.append('\\');
        sb.append(c);
        break;
      case '/': 
        if (b == '<') {
          sb.append('\\');
        }
        sb.append(c);
        break;
      case '\b': 
        sb.append("\\b");
        break;
      case '\t': 
        sb.append("\\t");
        break;
      case '\n': 
        sb.append("\\n");
        break;
      case '\f': 
        sb.append("\\f");
        break;
      case '\r': 
        sb.append("\\r");
        break;
      default: 
        if ((c < ' ') || ((c >= '') && (c < ' ')) || (
          (c >= ' ') && (c < '℀')))
        {
          String t = "000" + Integer.toHexString(c);
          sb.append("\\u" + t.substring(t.length() - 4));
        }
        else
        {
          sb.append(c);
        }
        break;
      }
    }
    sb.append('"');
    return sb.toString();
  }
  
  public Object remove(String key)
  {
    return this.myHashMap.remove(key);
  }
  
  static void testValidity(Object o)
    throws JSONException
  {
    if (o != null) {
      if ((o instanceof Double))
      {
        if ((((Double)o).isInfinite()) || (((Double)o).isNaN())) {
          throw new JSONException(
            "JSON does not allow non-finite numbers.");
        }
      }
      else if (((o instanceof Float)) && (
        (((Float)o).isInfinite()) || (((Float)o).isNaN()))) {
        throw new JSONException(
          "JSON does not allow non-finite numbers.");
      }
    }
  }
  
  public JSONArray toJSONArray(JSONArray names)
    throws JSONException
  {
    if ((names == null) || (names.length() == 0)) {
      return null;
    }
    JSONArray ja = new JSONArray();
    for (int i = 0; i < names.length(); i++) {
      ja.put(opt(names.getString(i)));
    }
    return ja;
  }
  
  public String toString()
  {
    try
    {
      Iterator keys = keys();
      StringBuffer sb = new StringBuffer("{");
      while (keys.hasNext())
      {
        if (sb.length() > 1) {
          sb.append(',');
        }
        Object o = keys.next();
        sb.append(quote(o.toString()));
        sb.append(':');
        sb.append(valueToString(this.myHashMap.get(o)));
      }
      sb.append('}');
      return sb.toString();
    }
    catch (Exception e) {}
    return null;
  }
  
  public String toString(int indentFactor)
    throws JSONException
  {
    return toString(indentFactor, 0);
  }
  
  String toString(int indentFactor, int indent)
    throws JSONException
  {
    int n = length();
    if (n == 0) {
      return "{}";
    }
    Iterator keys = keys();
    StringBuffer sb = new StringBuffer("{");
    int newindent = indent + indentFactor;
    if (n == 1)
    {
      o = keys.next();
      sb.append(quote(o.toString()));
      sb.append(": ");
      sb.append(valueToString(this.myHashMap.get(o), indentFactor, 
        indent));
    }
    else
    {
      while (keys.hasNext())
      {
        Object o;
        Object o = keys.next();
        if (sb.length() > 1) {
          sb.append(",\n");
        } else {
          sb.append('\n');
        }
        for (int i = 0; i < newindent; i++) {
          sb.append(' ');
        }
        sb.append(quote(o.toString()));
        sb.append(": ");
        sb.append(valueToString(this.myHashMap.get(o), indentFactor, 
          newindent));
      }
      if (sb.length() > 1)
      {
        sb.append('\n');
        for (int i = 0; i < indent; i++) {
          sb.append(' ');
        }
      }
    }
    sb.append('}');
    return sb.toString();
  }
  
  static String valueToString(Object value)
    throws JSONException
  {
    if ((value == null) || (value.equals(null))) {
      return "null";
    }
    if ((value instanceof JSONString))
    {
      try
      {
        o = ((JSONString)value).toJSONString();
      }
      catch (Exception e)
      {
        Object o;
        throw new JSONException(e);
      }
      Object o;
      if ((o instanceof String)) {
        return (String)o;
      }
      throw new JSONException("Bad value from toJSONString: " + o);
    }
    if ((value instanceof Number)) {
      return numberToString((Number)value);
    }
    if (((value instanceof Boolean)) || ((value instanceof JSONObject)) || 
      ((value instanceof JSONArray))) {
      return value.toString();
    }
    return quote(value.toString());
  }
  
  static String valueToString(Object value, int indentFactor, int indent)
    throws JSONException
  {
    if ((value == null) || (value.equals(null))) {
      return "null";
    }
    try
    {
      if ((value instanceof JSONString))
      {
        Object o = ((JSONString)value).toJSONString();
        if ((o instanceof String)) {
          return (String)o;
        }
      }
    }
    catch (Exception localException)
    {
      if ((value instanceof Number)) {
        return numberToString((Number)value);
      }
      if ((value instanceof Boolean)) {
        return value.toString();
      }
      if ((value instanceof JSONObject)) {
        return ((JSONObject)value).toString(indentFactor, indent);
      }
      if ((value instanceof JSONArray)) {
        return ((JSONArray)value).toString(indentFactor, indent);
      }
    }
    return quote(value.toString());
  }
  
  public Writer write(Writer writer)
    throws JSONException
  {
    try
    {
      boolean b = false;
      Iterator keys = keys();
      writer.write(123);
      while (keys.hasNext())
      {
        if (b) {
          writer.write(44);
        }
        Object k = keys.next();
        writer.write(quote(k.toString()));
        writer.write(58);
        Object v = this.myHashMap.get(k);
        if ((v instanceof JSONObject)) {
          ((JSONObject)v).write(writer);
        } else if ((v instanceof JSONArray)) {
          ((JSONArray)v).write(writer);
        } else {
          writer.write(valueToString(v));
        }
        b = true;
      }
      writer.write(125);
      return writer;
    }
    catch (IOException e)
    {
      throw new JSONException(e);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\json\JSONObject.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */