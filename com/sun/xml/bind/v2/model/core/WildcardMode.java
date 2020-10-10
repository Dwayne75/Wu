package com.sun.xml.bind.v2.model.core;

public enum WildcardMode
{
  STRICT(false, true),  SKIP(true, false),  LAX(true, true);
  
  public final boolean allowDom;
  public final boolean allowTypedObject;
  
  private WildcardMode(boolean allowDom, boolean allowTypedObject)
  {
    this.allowDom = allowDom;
    this.allowTypedObject = allowTypedObject;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\WildcardMode.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */