package com.sun.xml.bind.v2.model.core;

public abstract interface EnumConstant<T, C>
{
  public abstract EnumLeafInfo<T, C> getEnclosingClass();
  
  public abstract String getLexicalValue();
  
  public abstract String getName();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\EnumConstant.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */