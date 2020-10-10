package com.sun.javaws.jnl;

public abstract interface ResourceVisitor
{
  public abstract void visitJARDesc(JARDesc paramJARDesc);
  
  public abstract void visitPropertyDesc(PropertyDesc paramPropertyDesc);
  
  public abstract void visitPackageDesc(PackageDesc paramPackageDesc);
  
  public abstract void visitExtensionDesc(ExtensionDesc paramExtensionDesc);
  
  public abstract void visitJREDesc(JREDesc paramJREDesc);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\jnl\ResourceVisitor.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */