package com.sun.codemodel;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

final class JArrayClass
  extends JClass
{
  private final JType componentType;
  
  JArrayClass(JCodeModel owner, JType component)
  {
    super(owner);
    this.componentType = component;
  }
  
  public String name()
  {
    return this.componentType.name() + "[]";
  }
  
  public String fullName()
  {
    return this.componentType.fullName() + "[]";
  }
  
  public String binaryName()
  {
    return this.componentType.binaryName() + "[]";
  }
  
  public void generate(JFormatter f)
  {
    f.g(this.componentType).p("[]");
  }
  
  public JPackage _package()
  {
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
  
  public JType elementType()
  {
    return this.componentType;
  }
  
  public boolean isArray()
  {
    return true;
  }
  
  public boolean equals(Object obj)
  {
    if (!(obj instanceof JArrayClass)) {
      return false;
    }
    if (this.componentType.equals(((JArrayClass)obj).componentType)) {
      return true;
    }
    return false;
  }
  
  public int hashCode()
  {
    return this.componentType.hashCode();
  }
  
  protected JClass substituteParams(JTypeVar[] variables, List<JClass> bindings)
  {
    if (this.componentType.isPrimitive()) {
      return this;
    }
    JClass c = ((JClass)this.componentType).substituteParams(variables, bindings);
    if (c == this.componentType) {
      return this;
    }
    return new JArrayClass(owner(), c);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JArrayClass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */