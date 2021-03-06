package org.seamless.util.logging;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggingUtil
{
  public static final String DEFAULT_CONFIG = "default-logging.properties";
  
  public static void loadDefaultConfiguration()
    throws Exception
  {
    loadDefaultConfiguration(null);
  }
  
  public static void loadDefaultConfiguration(InputStream is)
    throws Exception
  {
    if (System.getProperty("java.util.logging.config.file") != null) {
      return;
    }
    if (is == null) {
      is = Thread.currentThread().getContextClassLoader().getResourceAsStream("default-logging.properties");
    }
    if (is == null) {
      return;
    }
    List<String> handlerNames = new ArrayList();
    
    LogManager.getLogManager().readConfiguration(spliceHandlers(is, handlerNames));
    
    Handler[] handlers = instantiateHandlers(handlerNames);
    resetRootHandler(handlers);
  }
  
  public static Handler[] instantiateHandlers(List<String> handlerNames)
    throws Exception
  {
    List<Handler> list = new ArrayList();
    for (String handlerName : handlerNames) {
      list.add((Handler)Thread.currentThread().getContextClassLoader().loadClass(handlerName).newInstance());
    }
    return (Handler[])list.toArray(new Handler[list.size()]);
  }
  
  public static InputStream spliceHandlers(InputStream is, List<String> handlers)
    throws IOException
  {
    Properties props = new Properties();
    props.load(is);
    
    StringBuilder sb = new StringBuilder();
    List<String> handlersProperties = new ArrayList();
    for (Map.Entry<Object, Object> entry : props.entrySet()) {
      if (entry.getKey().equals("handlers")) {
        handlersProperties.add(entry.getValue().toString());
      } else {
        sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
      }
    }
    for (String handlersProperty : handlersProperties)
    {
      String[] handlerClasses = handlersProperty.trim().split(" ");
      for (String handlerClass : handlerClasses) {
        handlers.add(handlerClass.trim());
      }
    }
    return new ByteArrayInputStream(sb.toString().getBytes("ISO-8859-1"));
  }
  
  public static void resetRootHandler(Handler... h)
  {
    Logger rootLogger = LogManager.getLogManager().getLogger("");
    Handler[] handlers = rootLogger.getHandlers();
    for (Handler handler : handlers) {
      rootLogger.removeHandler(handler);
    }
    for (Handler handler : h) {
      if (handler != null) {
        LogManager.getLogManager().getLogger("").addHandler(handler);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\logging\LoggingUtil.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */