package org.apache.commons.codec.language.bm;

public enum NameType
{
  ASHKENAZI("ash"),  GENERIC("gen"),  SEPHARDIC("sep");
  
  private final String name;
  
  private NameType(String name)
  {
    this.name = name;
  }
  
  public String getName()
  {
    return this.name;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\commons\codec\language\bm\NameType.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */