package com.sun.codemodel;

public class JClassAlreadyExistsException
  extends Exception
{
  private final JDefinedClass existing;
  
  public JClassAlreadyExistsException(JDefinedClass _existing)
  {
    this.existing = _existing;
  }
  
  public JDefinedClass getExistingClass()
  {
    return this.existing;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JClassAlreadyExistsException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */