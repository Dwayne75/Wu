package org.seamless.http;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Headers
  implements Map<String, List<String>>
{
  static final byte CR = 13;
  static final byte LF = 10;
  final Map<String, List<String>> map = new HashMap(32);
  private boolean normalizeHeaders = true;
  
  public Headers() {}
  
  public Headers(Map<String, List<String>> map)
  {
    putAll(map);
  }
  
  public Headers(ByteArrayInputStream inputStream)
  {
    StringBuilder sb = new StringBuilder(256);
    Headers headers = new Headers();
    String line = readLine(sb, inputStream);
    String lastHeader = null;
    if (line.length() != 0) {
      do
      {
        char firstChar = line.charAt(0);
        if ((lastHeader != null) && ((firstChar == ' ') || (firstChar == '\t')))
        {
          List<String> current = headers.get(lastHeader);
          int lastPos = current.size() - 1;
          String newString = (String)current.get(lastPos) + line.trim();
          current.set(lastPos, newString);
        }
        else
        {
          String[] header = splitHeader(line);
          headers.add(header[0], header[1]);
          lastHeader = header[0];
        }
        sb.delete(0, sb.length());
        line = readLine(sb, inputStream);
      } while (line.length() != 0);
    }
    putAll(headers);
  }
  
  public Headers(boolean normalizeHeaders)
  {
    this.normalizeHeaders = normalizeHeaders;
  }
  
  public int size()
  {
    return this.map.size();
  }
  
  public boolean isEmpty()
  {
    return this.map.isEmpty();
  }
  
  public boolean containsKey(Object key)
  {
    return (key != null) && ((key instanceof String)) && (this.map.containsKey(normalize((String)key)));
  }
  
  public boolean containsValue(Object value)
  {
    return this.map.containsValue(value);
  }
  
  public List<String> get(Object key)
  {
    return (List)this.map.get(normalize((String)key));
  }
  
  public List<String> put(String key, List<String> value)
  {
    return (List)this.map.put(normalize(key), value);
  }
  
  public List<String> remove(Object key)
  {
    return (List)this.map.remove(normalize((String)key));
  }
  
  public void putAll(Map<? extends String, ? extends List<String>> t)
  {
    for (Map.Entry<? extends String, ? extends List<String>> entry : t.entrySet()) {
      put((String)entry.getKey(), (List)entry.getValue());
    }
  }
  
  public void clear()
  {
    this.map.clear();
  }
  
  public Set<String> keySet()
  {
    return this.map.keySet();
  }
  
  public Collection<List<String>> values()
  {
    return this.map.values();
  }
  
  public Set<Map.Entry<String, List<String>>> entrySet()
  {
    return this.map.entrySet();
  }
  
  public boolean equals(Object o)
  {
    return this.map.equals(o);
  }
  
  public int hashCode()
  {
    return this.map.hashCode();
  }
  
  public String getFirstHeader(String key)
  {
    List<String> l = (List)this.map.get(normalize(key));
    return (l != null) && (l.size() > 0) ? (String)l.get(0) : null;
  }
  
  public void add(String key, String value)
  {
    String k = normalize(key);
    List<String> l = (List)this.map.get(k);
    if (l == null)
    {
      l = new LinkedList();
      this.map.put(k, l);
    }
    l.add(value);
  }
  
  public void set(String key, String value)
  {
    LinkedList<String> l = new LinkedList();
    l.add(value);
    put(key, l);
  }
  
  private String normalize(String key)
  {
    String result = key;
    if (this.normalizeHeaders)
    {
      if (key == null) {
        return null;
      }
      if (key.length() == 0) {
        return key;
      }
      char[] b = key.toCharArray();
      int caseDiff = 32;
      if ((b[0] >= 'a') && (b[0] <= 'z')) {
        b[0] = ((char)(b[0] - ' '));
      }
      int length = key.length();
      for (int i = 1; i < length; i++) {
        if ((b[i] >= 'A') && (b[i] <= 'Z')) {
          b[i] = ((char)(b[i] + ' '));
        }
      }
      result = new String(b);
    }
    return result;
  }
  
  public static String readLine(ByteArrayInputStream is)
  {
    return readLine(new StringBuilder(256), is);
  }
  
  public static String readLine(StringBuilder sb, ByteArrayInputStream is)
  {
    int nextByte;
    while ((nextByte = is.read()) != -1)
    {
      char nextChar = (char)nextByte;
      if (nextChar == '\r')
      {
        nextByte = (char)is.read();
        if (nextByte == 10) {
          break;
        }
      }
      else
      {
        if (nextChar == '\n') {
          break;
        }
      }
      sb.append(nextChar);
    }
    return sb.toString();
  }
  
  protected String[] splitHeader(String sb)
  {
    int nameStart = findNonWhitespace(sb, 0);
    for (int nameEnd = nameStart; nameEnd < sb.length(); nameEnd++)
    {
      char ch = sb.charAt(nameEnd);
      if ((ch == ':') || (Character.isWhitespace(ch))) {
        break;
      }
    }
    for (int colonEnd = nameEnd; colonEnd < sb.length(); colonEnd++) {
      if (sb.charAt(colonEnd) == ':')
      {
        colonEnd++;
        break;
      }
    }
    int valueStart = findNonWhitespace(sb, colonEnd);
    int valueEnd = findEndOfString(sb);
    
    return new String[] { sb.substring(nameStart, nameEnd), (sb.length() >= valueStart) && (sb.length() >= valueEnd) && (valueStart < valueEnd) ? sb.substring(valueStart, valueEnd) : null };
  }
  
  protected int findNonWhitespace(String sb, int offset)
  {
    for (int result = offset; result < sb.length(); result++) {
      if (!Character.isWhitespace(sb.charAt(result))) {
        break;
      }
    }
    return result;
  }
  
  protected int findEndOfString(String sb)
  {
    for (int result = sb.length(); result > 0; result--) {
      if (!Character.isWhitespace(sb.charAt(result - 1))) {
        break;
      }
    }
    return result;
  }
  
  public String toString()
  {
    StringBuilder headerString = new StringBuilder(512);
    for (Map.Entry<String, List<String>> headerEntry : entrySet())
    {
      headerString.append((String)headerEntry.getKey()).append(": ");
      for (String v : (List)headerEntry.getValue()) {
        headerString.append(v).append(",");
      }
      headerString.delete(headerString.length() - 1, headerString.length());
      headerString.append("\r\n");
    }
    return headerString.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\http\Headers.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */