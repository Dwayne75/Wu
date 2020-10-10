package com.sun.codemodel;

class JAnonymousClass
  extends JDefinedClass
{
  private final JClass base;
  
  JAnonymousClass(JClass _base, JCodeModel owner)
  {
    super(0, _base.name(), false, owner);
    this.base = _base;
  }
  
  public JPackage _package()
  {
    return this.base._package();
  }
  
  public JClassContainer parentContainer()
  {
    throw new InternalError();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JAnonymousClass.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */