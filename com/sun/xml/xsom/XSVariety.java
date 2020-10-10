package com.sun.xml.xsom;

public final class XSVariety
{
  public static final XSVariety ATOMIC = new XSVariety("atomic");
  public static final XSVariety UNION = new XSVariety("union");
  public static final XSVariety LIST = new XSVariety("list");
  private final String name;
  
  private XSVariety(String _name)
  {
    this.name = _name;
  }
  
  public String toString()
  {
    return this.name;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\XSVariety.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */