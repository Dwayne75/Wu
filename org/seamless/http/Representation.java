package org.seamless.http;

import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

public class Representation<E>
  implements Serializable
{
  private URL url;
  private CacheControl cacheControl;
  private Integer contentLength;
  private String contentType;
  private Long lastModified;
  private String entityTag;
  private E entity;
  
  public Representation(CacheControl cacheControl, Integer contentLength, String contentType, Long lastModified, String entityTag, E entity)
  {
    this(null, cacheControl, contentLength, contentType, lastModified, entityTag, entity);
  }
  
  public Representation(URL url, CacheControl cacheControl, Integer contentLength, String contentType, Long lastModified, String entityTag, E entity)
  {
    this.url = url;
    this.cacheControl = cacheControl;
    this.contentLength = contentLength;
    this.contentType = contentType;
    this.lastModified = lastModified;
    this.entityTag = entityTag;
    this.entity = entity;
  }
  
  public Representation(URLConnection urlConnection, E entity)
  {
    this(urlConnection.getURL(), CacheControl.valueOf(urlConnection.getHeaderField("Cache-Control")), Integer.valueOf(urlConnection.getContentLength()), urlConnection.getContentType(), Long.valueOf(urlConnection.getLastModified()), urlConnection.getHeaderField("Etag"), entity);
  }
  
  public URL getUrl()
  {
    return this.url;
  }
  
  public CacheControl getCacheControl()
  {
    return this.cacheControl;
  }
  
  public Integer getContentLength()
  {
    return (this.contentLength == null) || (this.contentLength.intValue() == -1) ? null : this.contentLength;
  }
  
  public String getContentType()
  {
    return this.contentType;
  }
  
  public Long getLastModified()
  {
    return this.lastModified.longValue() == 0L ? null : this.lastModified;
  }
  
  public String getEntityTag()
  {
    return this.entityTag;
  }
  
  public E getEntity()
  {
    return (E)this.entity;
  }
  
  public Long getMaxAgeOrNull()
  {
    return (getCacheControl() == null) || (getCacheControl().getMaxAge() == -1) || (getCacheControl().getMaxAge() == 0) ? null : Long.valueOf(getCacheControl().getMaxAge());
  }
  
  public boolean isExpired(long storedOn, long maxAge)
  {
    return storedOn + maxAge * 1000L < new Date().getTime();
  }
  
  public boolean isExpired(long storedOn)
  {
    return (getMaxAgeOrNull() == null) || (isExpired(storedOn, getMaxAgeOrNull().longValue()));
  }
  
  public boolean isNoStore()
  {
    return (getCacheControl() != null) && (getCacheControl().isNoStore());
  }
  
  public boolean isNoCache()
  {
    return (getCacheControl() != null) && (getCacheControl().isNoCache());
  }
  
  public boolean mustRevalidate()
  {
    return (getCacheControl() != null) && (getCacheControl().isProxyRevalidate());
  }
  
  public boolean hasEntityTagChanged(String currentEtag)
  {
    return (getEntityTag() != null) && (!getEntityTag().equals(currentEtag));
  }
  
  public boolean hasBeenModified(long currentModificationTime)
  {
    return (getLastModified() == null) || (getLastModified().longValue() < currentModificationTime);
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ") CT: " + getContentType();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\http\Representation.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */