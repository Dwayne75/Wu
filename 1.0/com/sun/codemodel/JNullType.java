package com.sun.codemodel;

import java.util.Iterator;

public final class JNullType
  extends JClass
{
  JNullType(JCodeModel _owner)
  {
    super(_owner);
  }
  
  public String name()
  {
    return "null";
  }
  
  public JPackage _package()
  {
    return owner()._package("");
  }
  
  public JClass _extends()
  {
    return null;
  }
  
  public Iterator _implements()
  {
    return new JNullType.1(this);
  }
  
  public boolean isInterface()
  {
    return false;
  }
  
  public void generate(JFormatter f)
  {
    throw new IllegalStateException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JNullType.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */