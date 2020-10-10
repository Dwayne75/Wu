package com.sun.tools.xjc.api;

public enum SpecVersion
{
  V2_0,  V2_1;
  
  private SpecVersion() {}
  
  public boolean isLaterThan(SpecVersion t)
  {
    return ordinal() >= t.ordinal();
  }
  
  public static SpecVersion parse(String token)
  {
    if (token.equals("2.0")) {
      return V2_0;
    }
    if (token.equals("2.1")) {
      return V2_1;
    }
    return null;
  }
  
  public static final SpecVersion LATEST = V2_1;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\SpecVersion.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */