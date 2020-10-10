package javax.activation;

import java.io.File;

public abstract class FileTypeMap
{
  private static FileTypeMap defaultMap = null;
  
  public abstract String getContentType(File paramFile);
  
  public abstract String getContentType(String paramString);
  
  public static void setDefaultFileTypeMap(FileTypeMap map)
  {
    SecurityManager security = System.getSecurityManager();
    if (security != null) {
      try
      {
        security.checkSetFactory();
      }
      catch (SecurityException ex)
      {
        if (FileTypeMap.class.getClassLoader() != map.getClass().getClassLoader()) {
          throw ex;
        }
      }
    }
    defaultMap = map;
  }
  
  public static FileTypeMap getDefaultFileTypeMap()
  {
    if (defaultMap == null) {
      defaultMap = new MimetypesFileTypeMap();
    }
    return defaultMap;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\activation\FileTypeMap.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */