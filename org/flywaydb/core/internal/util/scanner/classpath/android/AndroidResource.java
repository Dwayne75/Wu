package org.flywaydb.core.internal.util.scanner.classpath.android;

import android.content.res.AssetManager;
import java.io.IOException;
import java.io.InputStreamReader;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.FileCopyUtils;
import org.flywaydb.core.internal.util.scanner.Resource;

public class AndroidResource
  implements Resource
{
  private final AssetManager assetManager;
  private final String path;
  private final String name;
  
  public AndroidResource(AssetManager assetManager, String path, String name)
  {
    this.assetManager = assetManager;
    this.path = path;
    this.name = name;
  }
  
  public String getLocation()
  {
    return this.path + "/" + this.name;
  }
  
  public String getLocationOnDisk()
  {
    return null;
  }
  
  public String loadAsString(String encoding)
  {
    try
    {
      return FileCopyUtils.copyToString(new InputStreamReader(this.assetManager.open(getLocation()), encoding));
    }
    catch (IOException e)
    {
      throw new FlywayException("Unable to load asset: " + getLocation(), e);
    }
  }
  
  public byte[] loadAsBytes()
  {
    try
    {
      return FileCopyUtils.copyToByteArray(this.assetManager.open(getLocation()));
    }
    catch (IOException e)
    {
      throw new FlywayException("Unable to load asset: " + getLocation(), e);
    }
  }
  
  public String getFilename()
  {
    return this.name;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\scanner\classpath\android\AndroidResource.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */