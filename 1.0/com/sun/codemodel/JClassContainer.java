package com.sun.codemodel;

import java.util.Iterator;

public abstract interface JClassContainer
{
  public abstract JDefinedClass _class(int paramInt, String paramString)
    throws JClassAlreadyExistsException;
  
  public abstract JDefinedClass _class(String paramString)
    throws JClassAlreadyExistsException;
  
  public abstract JDefinedClass _interface(int paramInt, String paramString)
    throws JClassAlreadyExistsException;
  
  public abstract JDefinedClass _interface(String paramString)
    throws JClassAlreadyExistsException;
  
  public abstract JDefinedClass _class(int paramInt, String paramString, boolean paramBoolean)
    throws JClassAlreadyExistsException;
  
  public abstract Iterator classes();
  
  public abstract JClassContainer parentContainer();
  
  public abstract JCodeModel owner();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JClassContainer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */