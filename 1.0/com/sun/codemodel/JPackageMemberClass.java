package com.sun.codemodel;

class JPackageMemberClass
  extends JDefinedClass
  implements JDeclaration
{
  private JPackage pkg;
  
  public final JPackage _package()
  {
    return this.pkg;
  }
  
  public JClassContainer parentContainer()
  {
    return this.pkg;
  }
  
  JPackageMemberClass(JPackage pkg, int mods, String name)
  {
    this(pkg, mods, name, false);
  }
  
  JPackageMemberClass(JPackage pkg, int mods, String name, boolean isInterface)
  {
    super(mods, name, isInterface, pkg.owner());
    this.pkg = pkg;
  }
  
  public void declare(JFormatter f)
  {
    if (!this.pkg.isUnnamed())
    {
      f.nl().d(this.pkg);
      f.nl();
    }
    super.declare(f);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JPackageMemberClass.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */