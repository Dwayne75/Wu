package com.wurmonline.properties;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

public enum PropertiesRepository
{
  INSTANCE;
  
  private static final Logger logger = Logger.getLogger(PropertiesRepository.class.getName());
  private static final HashMap<URL, Properties> propertiesHashMap = new HashMap();
  
  private PropertiesRepository() {}
  
  public static PropertiesRepository getInstance()
  {
    return INSTANCE;
  }
  
  Properties getProperties(URL file)
  {
    if (propertiesHashMap.containsKey(file)) {
      return (Properties)propertiesHashMap.get(file);
    }
    Properties properties = new Properties();
    propertiesHashMap.put(file, properties);
    try
    {
      InputStream is = file.openStream();Throwable localThrowable3 = null;
      try
      {
        properties.load(is);
      }
      catch (Throwable localThrowable1)
      {
        localThrowable3 = localThrowable1;throw localThrowable1;
      }
      finally
      {
        if (is != null) {
          if (localThrowable3 != null) {
            try
            {
              is.close();
            }
            catch (Throwable localThrowable2)
            {
              localThrowable3.addSuppressed(localThrowable2);
            }
          } else {
            is.close();
          }
        }
      }
    }
    catch (IOException e)
    {
      logger.warning("Unable to open properties file " + file.toString());
    }
    return properties;
  }
  
  public String getValueFor(URL file, String key)
  {
    return getProperties(file).getProperty(key);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\properties\PropertiesRepository.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */