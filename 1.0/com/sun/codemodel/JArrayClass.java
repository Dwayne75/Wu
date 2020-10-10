package com.sun.codemodel;

import java.util.Iterator;

public class JArrayClass
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
  
  public JPackage _package()
  {
    return owner().rootPackage();
  }
  
  public JClass _extends()
  {
    return null;
  }
  
  public Iterator _implements()
  {
    return emptyIterator;
  }
  
  public boolean isInterface()
  {
    return false;
  }
  
  public void generate(JFormatter f)
  {
    this.componentType.generate(f);
    f.p("[]");
  }
  
  private static Iterator emptyIterator = new JArrayClass.1();
  
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
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JArrayClass.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */