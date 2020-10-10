package com.sun.codemodel;

class JNestedClass
  extends JDefinedClass
  implements JDeclaration
{
  private JDefinedClass outer = null;
  
  public JPackage _package()
  {
    return this.outer._package();
  }
  
  JNestedClass(JDefinedClass outer, int mods, String name)
  {
    this(outer, mods, name, false);
  }
  
  JNestedClass(JDefinedClass outer, int mods, String name, boolean isInterface)
  {
    super(mods, name, isInterface, outer.owner());
    this.outer = outer;
  }
  
  public String fullName()
  {
    return this.outer.fullName() + '.' + name();
  }
  
  public JClass outer()
  {
    return this.outer;
  }
  
  public JClassContainer parentContainer()
  {
    return this.outer;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JNestedClass.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */