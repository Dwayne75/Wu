package org.apache.http.cookie;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.params.HttpParams;

@ThreadSafe
public final class CookieSpecRegistry
{
  private final ConcurrentHashMap<String, CookieSpecFactory> registeredSpecs;
  
  public CookieSpecRegistry()
  {
    this.registeredSpecs = new ConcurrentHashMap();
  }
  
  public void register(String name, CookieSpecFactory factory)
  {
    if (name == null) {
      throw new IllegalArgumentException("Name may not be null");
    }
    if (factory == null) {
      throw new IllegalArgumentException("Cookie spec factory may not be null");
    }
    this.registeredSpecs.put(name.toLowerCase(Locale.ENGLISH), factory);
  }
  
  public void unregister(String id)
  {
    if (id == null) {
      throw new IllegalArgumentException("Id may not be null");
    }
    this.registeredSpecs.remove(id.toLowerCase(Locale.ENGLISH));
  }
  
  public CookieSpec getCookieSpec(String name, HttpParams params)
    throws IllegalStateException
  {
    if (name == null) {
      throw new IllegalArgumentException("Name may not be null");
    }
    CookieSpecFactory factory = (CookieSpecFactory)this.registeredSpecs.get(name.toLowerCase(Locale.ENGLISH));
    if (factory != null) {
      return factory.newInstance(params);
    }
    throw new IllegalStateException("Unsupported cookie spec: " + name);
  }
  
  public CookieSpec getCookieSpec(String name)
    throws IllegalStateException
  {
    return getCookieSpec(name, null);
  }
  
  public List<String> getSpecNames()
  {
    return new ArrayList(this.registeredSpecs.keySet());
  }
  
  public void setItems(Map<String, CookieSpecFactory> map)
  {
    if (map == null) {
      return;
    }
    this.registeredSpecs.clear();
    this.registeredSpecs.putAll(map);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\cookie\CookieSpecRegistry.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */