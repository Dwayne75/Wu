package com.sun.tools.xjc.model;

import com.sun.codemodel.JPackage;

public abstract interface CClassInfoParent
{
  public abstract String fullName();
  
  public abstract <T> T accept(Visitor<T> paramVisitor);
  
  public abstract JPackage getOwnerPackage();
  
  public static final class Package
    implements CClassInfoParent
  {
    public final JPackage pkg;
    
    public Package(JPackage pkg)
    {
      this.pkg = pkg;
    }
    
    public String fullName()
    {
      return this.pkg.name();
    }
    
    public <T> T accept(CClassInfoParent.Visitor<T> visitor)
    {
      return (T)visitor.onPackage(this.pkg);
    }
    
    public JPackage getOwnerPackage()
    {
      return this.pkg;
    }
  }
  
  public static abstract interface Visitor<T>
  {
    public abstract T onBean(CClassInfo paramCClassInfo);
    
    public abstract T onPackage(JPackage paramJPackage);
    
    public abstract T onElement(CElementInfo paramCElementInfo);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CClassInfoParent.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */