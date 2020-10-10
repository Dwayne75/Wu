package com.sun.codemodel;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

final class JDirectClass
  extends JClass
{
  private final String fullName;
  
  public JDirectClass(JCodeModel _owner, String fullName)
  {
    super(_owner);
    this.fullName = fullName;
  }
  
  public String name()
  {
    int i = this.fullName.lastIndexOf('.');
    if (i >= 0) {
      return this.fullName.substring(i + 1);
    }
    return this.fullName;
  }
  
  public String fullName()
  {
    return this.fullName;
  }
  
  public JPackage _package()
  {
    int i = this.fullName.lastIndexOf('.');
    if (i >= 0) {
      return owner()._package(this.fullName.substring(0, i));
    }
    return owner().rootPackage();
  }
  
  public JClass _extends()
  {
    return owner().ref(Object.class);
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JDirectClass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */