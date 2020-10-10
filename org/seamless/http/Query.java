package org.seamless.http;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class Query
{
  protected final Map<String, List<String>> parameters = new LinkedHashMap();
  
  public static Query newInstance(Map<String, List<String>> parameters)
  {
    Query query = new Query();
    query.parameters.putAll(parameters);
    return query;
  }
  
  public Query() {}
  
  public Query(Map<String, String[]> parameters)
  {
    for (Map.Entry<String, String[]> entry : parameters.entrySet())
    {
      List<String> list = Arrays.asList(entry.getValue() != null ? (String[])entry.getValue() : new String[0]);
      
      this.parameters.put(entry.getKey(), list);
    }
  }
  
  public Query(URL url)
  {
    this(url.getQuery());
  }
  
  public Query(String qs)
  {
    if (qs == null) {
      return;
    }
    String[] pairs = qs.split("&");
    for (String pair : pairs)
    {
      int pos = pair.indexOf('=');
      String value;
      String name;
      String value;
      if (pos == -1)
      {
        String name = pair;
        value = null;
      }
      else
      {
        try
        {
          name = URLDecoder.decode(pair.substring(0, pos), "UTF-8");
          value = URLDecoder.decode(pair.substring(pos + 1, pair.length()), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
          throw new IllegalStateException("Query string is not UTF-8");
        }
      }
      List<String> list = (List)this.parameters.get(name);
      if (list == null)
      {
        list = new ArrayList();
        this.parameters.put(name, list);
      }
      list.add(value);
    }
  }
  
  public String get(String name)
  {
    List<String> values = (List)this.parameters.get(name);
    if (values == null) {
      return "";
    }
    if (values.size() == 0) {
      return "";
    }
    return (String)values.get(0);
  }
  
  public String[] getValues(String name)
  {
    List<String> values = (List)this.parameters.get(name);
    if (values == null) {
      return null;
    }
    return (String[])values.toArray(new String[values.size()]);
  }
  
  public List<String> getValuesAsList(String name)
  {
    return this.parameters.containsKey(name) ? Collections.unmodifiableList((List)this.parameters.get(name)) : null;
  }
  
  public Enumeration<String> getNames()
  {
    return Collections.enumeration(this.parameters.keySet());
  }
  
  public Map<String, String[]> getMap()
  {
    Map<String, String[]> map = new TreeMap();
    for (Map.Entry<String, List<String>> entry : this.parameters.entrySet())
    {
      List<String> list = (List)entry.getValue();
      String[] values;
      String[] values;
      if (list == null) {
        values = null;
      } else {
        values = (String[])list.toArray(new String[list.size()]);
      }
      map.put(entry.getKey(), values);
    }
    return map;
  }
  
  public Map<String, List<String>> getMapWithLists()
  {
    return Collections.unmodifiableMap(this.parameters);
  }
  
  public boolean isEmpty()
  {
    return this.parameters.size() == 0;
  }
  
  public Query cloneAndAdd(String name, String... values)
  {
    Map<String, List<String>> params = new HashMap(getMapWithLists());
    List<String> existingValues = (List)params.get(name);
    if (existingValues == null)
    {
      existingValues = new ArrayList();
      params.put(name, existingValues);
    }
    existingValues.addAll(Arrays.asList(values));
    return newInstance(params);
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    for (Iterator i$ = this.parameters.entrySet().iterator(); i$.hasNext();)
    {
      entry = (Map.Entry)i$.next();
      for (String v : (List)entry.getValue()) {
        if ((v != null) && (v.length() != 0))
        {
          if (sb.length() > 0) {
            sb.append("&");
          }
          sb.append((String)entry.getKey());
          sb.append("=");
          sb.append(v);
        }
      }
    }
    Map.Entry<String, List<String>> entry;
    return sb.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\http\Query.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */