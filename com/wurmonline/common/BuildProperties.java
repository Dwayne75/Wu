package com.wurmonline.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BuildProperties
{
  private final Properties properties = new Properties();
  
  public static BuildProperties getPropertiesFor(String path)
    throws IOException
  {
    BuildProperties bp = new BuildProperties();
    InputStream inputStream = BuildProperties.class.getResourceAsStream(path);Throwable localThrowable3 = null;
    try
    {
      bp.properties.load(inputStream);
    }
    catch (Throwable localThrowable1)
    {
      localThrowable3 = localThrowable1;throw localThrowable1;
    }
    finally
    {
      if (inputStream != null) {
        if (localThrowable3 != null) {
          try
          {
            inputStream.close();
          }
          catch (Throwable localThrowable2)
          {
            localThrowable3.addSuppressed(localThrowable2);
          }
        } else {
          inputStream.close();
        }
      }
    }
    return bp;
  }
  
  public String getGitSha1Short()
  {
    String sha = getGitSha1();
    if (sha.length() < 7) {
      return sha;
    }
    return sha.substring(0, 7);
  }
  
  public String getGitBranch()
  {
    return this.properties.getProperty("git-branch");
  }
  
  public String getGitSha1()
  {
    return this.properties.getProperty("git-sha-1");
  }
  
  public String getVersion()
  {
    return this.properties.getProperty("version");
  }
  
  public String getBuildTimeString()
  {
    return this.properties.getProperty("build-time");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\common\BuildProperties.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */