package org.relaxng.datatype;

public abstract interface ValidationContext
{
  public abstract String resolveNamespacePrefix(String paramString);
  
  public abstract String getBaseUri();
  
  public abstract boolean isUnparsedEntity(String paramString);
  
  public abstract boolean isNotation(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\relaxng\datatype\ValidationContext.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */