package org.flywaydb.core.internal.util.scanner;

public abstract interface Resource
{
  public abstract String getLocation();
  
  public abstract String getLocationOnDisk();
  
  public abstract String loadAsString(String paramString);
  
  public abstract byte[] loadAsBytes();
  
  public abstract String getFilename();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\scanner\Resource.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */