package com.sun.codemodel;

import java.util.Iterator;

public abstract interface JClassContainer
{
  public abstract boolean isClass();
  
  public abstract boolean isPackage();
  
  public abstract JDefinedClass _class(int paramInt, String paramString)
    throws JClassAlreadyExistsException;
  
  public abstract JDefinedClass _class(String paramString)
    throws JClassAlreadyExistsException;
  
  public abstract JDefinedClass _interface(int paramInt, String paramString)
    throws JClassAlreadyExistsException;
  
  public abstract JDefinedClass _interface(String paramString)
    throws JClassAlreadyExistsException;
  
  /**
   * @deprecated
   */
  public abstract JDefinedClass _class(int paramInt, String paramString, boolean paramBoolean)
    throws JClassAlreadyExistsException;
  
  public abstract JDefinedClass _class(int paramInt, String paramString, ClassType paramClassType)
    throws JClassAlreadyExistsException;
  
  public abstract Iterator<JDefinedClass> classes();
  
  public abstract JClassContainer parentContainer();
  
  public abstract JPackage getPackage();
  
  public abstract JCodeModel owner();
  
  public abstract JDefinedClass _annotationTypeDeclaration(String paramString)
    throws JClassAlreadyExistsException;
  
  public abstract JDefinedClass _enum(String paramString)
    throws JClassAlreadyExistsException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JClassContainer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */