package org.flywaydb.core.internal.util.scanner.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.FileCopyUtils;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.scanner.Resource;

public class FileSystemResource
  implements Resource, Comparable<FileSystemResource>
{
  private File location;
  
  public FileSystemResource(String location)
  {
    this.location = new File(location);
  }
  
  public String getLocation()
  {
    return StringUtils.replaceAll(this.location.getPath(), "\\", "/");
  }
  
  public String getLocationOnDisk()
  {
    return this.location.getAbsolutePath();
  }
  
  public String loadAsString(String encoding)
  {
    try
    {
      InputStream inputStream = new FileInputStream(this.location);
      Reader reader = new InputStreamReader(inputStream, Charset.forName(encoding));
      
      return FileCopyUtils.copyToString(reader);
    }
    catch (IOException e)
    {
      throw new FlywayException("Unable to load filesystem resource: " + this.location.getPath() + " (encoding: " + encoding + ")", e);
    }
  }
  
  public byte[] loadAsBytes()
  {
    try
    {
      InputStream inputStream = new FileInputStream(this.location);
      return FileCopyUtils.copyToByteArray(inputStream);
    }
    catch (IOException e)
    {
      throw new FlywayException("Unable to load filesystem resource: " + this.location.getPath(), e);
    }
  }
  
  public String getFilename()
  {
    return this.location.getName();
  }
  
  public int compareTo(FileSystemResource o)
  {
    return this.location.compareTo(o.location);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\scanner\filesystem\FileSystemResource.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */