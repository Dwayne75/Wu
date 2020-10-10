package com.sun.codemodel;

class JAnonymousClass
  extends JDefinedClass
{
  private final JClass base;
  
  JAnonymousClass(JClass _base)
  {
    super(_base.owner(), 0, null);
    this.base = _base;
  }
  
  public String fullName()
  {
    return this.base.fullName();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JAnonymousClass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */