package org.flywaydb.core.internal.util.scanner.classpath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.FileCopyUtils;
import org.flywaydb.core.internal.util.scanner.Resource;

public class ClassPathResource
  implements Comparable<ClassPathResource>, Resource
{
  private String location;
  private ClassLoader classLoader;
  
  public ClassPathResource(String location, ClassLoader classLoader)
  {
    this.location = location;
    this.classLoader = classLoader;
  }
  
  public String getLocation()
  {
    return this.location;
  }
  
  public String getLocationOnDisk()
  {
    URL url = getUrl();
    if (url == null) {
      throw new FlywayException("Unable to location resource on disk: " + this.location);
    }
    try
    {
      return new File(URLDecoder.decode(url.getPath(), "UTF-8")).getAbsolutePath();
    }
    catch (UnsupportedEncodingException e)
    {
      throw new FlywayException("Unknown encoding: UTF-8", e);
    }
  }
  
  private URL getUrl()
  {
    return this.classLoader.getResource(this.location);
  }
  
  public String loadAsString(String encoding)
  {
    try
    {
      InputStream inputStream = this.classLoader.getResourceAsStream(this.location);
      if (inputStream == null) {
        throw new FlywayException("Unable to obtain inputstream for resource: " + this.location);
      }
      Reader reader = new InputStreamReader(inputStream, Charset.forName(encoding));
      
      return FileCopyUtils.copyToString(reader);
    }
    catch (IOException e)
    {
      throw new FlywayException("Unable to load resource: " + this.location + " (encoding: " + encoding + ")", e);
    }
  }
  
  public byte[] loadAsBytes()
  {
    try
    {
      InputStream inputStream = this.classLoader.getResourceAsStream(this.location);
      if (inputStream == null) {
        throw new FlywayException("Unable to obtain inputstream for resource: " + this.location);
      }
      return FileCopyUtils.copyToByteArray(inputStream);
    }
    catch (IOException e)
    {
      throw new FlywayException("Unable to load resource: " + this.location, e);
    }
  }
  
  public String getFilename()
  {
    return this.location.substring(this.location.lastIndexOf("/") + 1);
  }
  
  public boolean exists()
  {
    return getUrl() != null;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    ClassPathResource that = (ClassPathResource)o;
    if (!this.location.equals(that.location)) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    return this.location.hashCode();
  }
  
  public int compareTo(ClassPathResource o)
  {
    return this.location.compareTo(o.location);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\scanner\classpath\ClassPathResource.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */