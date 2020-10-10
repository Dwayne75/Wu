package org.seamless.util;

public class OS
{
  public static boolean checkForLinux()
  {
    return checkForPresence("os.name", "linux");
  }
  
  public static boolean checkForHp()
  {
    return checkForPresence("os.name", "hp");
  }
  
  public static boolean checkForSolaris()
  {
    return checkForPresence("os.name", "sun");
  }
  
  public static boolean checkForWindows()
  {
    return checkForPresence("os.name", "win");
  }
  
  public static boolean checkForMac()
  {
    return checkForPresence("os.name", "mac");
  }
  
  private static boolean checkForPresence(String key, String value)
  {
    try
    {
      String tmp = System.getProperty(key);
      return (tmp != null) && (tmp.trim().toLowerCase().startsWith(value));
    }
    catch (Throwable t) {}
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\OS.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */