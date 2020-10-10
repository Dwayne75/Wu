package com.sun.codemodel;

public abstract interface JGenerifiable
{
  public abstract JTypeVar generify(String paramString);
  
  public abstract JTypeVar generify(String paramString, Class paramClass);
  
  public abstract JTypeVar generify(String paramString, JClass paramJClass);
  
  public abstract JTypeVar[] typeParams();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JGenerifiable.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */