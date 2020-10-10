package javax.activation;

public abstract class CommandMap
{
  private static CommandMap defaultCommandMap = null;
  
  public static CommandMap getDefaultCommandMap()
  {
    if (defaultCommandMap == null) {
      defaultCommandMap = new MailcapCommandMap();
    }
    return defaultCommandMap;
  }
  
  public static void setDefaultCommandMap(CommandMap commandMap)
  {
    SecurityManager security = System.getSecurityManager();
    if (security != null) {
      try
      {
        security.checkSetFactory();
      }
      catch (SecurityException ex)
      {
        if (CommandMap.class.getClassLoader() != commandMap.getClass().getClassLoader()) {
          throw ex;
        }
      }
    }
    defaultCommandMap = commandMap;
  }
  
  public abstract CommandInfo[] getPreferredCommands(String paramString);
  
  public CommandInfo[] getPreferredCommands(String mimeType, DataSource ds)
  {
    return getPreferredCommands(mimeType);
  }
  
  public abstract CommandInfo[] getAllCommands(String paramString);
  
  public CommandInfo[] getAllCommands(String mimeType, DataSource ds)
  {
    return getAllCommands(mimeType);
  }
  
  public abstract CommandInfo getCommand(String paramString1, String paramString2);
  
  public CommandInfo getCommand(String mimeType, String cmdName, DataSource ds)
  {
    return getCommand(mimeType, cmdName);
  }
  
  public abstract DataContentHandler createDataContentHandler(String paramString);
  
  public DataContentHandler createDataContentHandler(String mimeType, DataSource ds)
  {
    return createDataContentHandler(mimeType);
  }
  
  public String[] getMimeTypes()
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\activation\CommandMap.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */