package com.sun.codemodel;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
  
  public String fullName()
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
  
  public Iterator<JClass> _implements()
  {
    return Collections.emptyList().iterator();
  }
  
  public boolean isInterface()
  {
    return false;
  }
  
  public boolean isAbstract()
  {
    return false;
  }
  
  protected JClass substituteParams(JTypeVar[] variables, List<JClass> bindings)
  {
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JNullType.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */