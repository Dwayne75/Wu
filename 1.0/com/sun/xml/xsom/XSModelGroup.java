package com.sun.xml.xsom;

public abstract interface XSModelGroup
  extends XSComponent, XSTerm
{
  public static final XSModelGroup.Compositor ALL = new XSModelGroup.Compositor("all", null);
  public static final XSModelGroup.Compositor SEQUENCE = new XSModelGroup.Compositor("sequence", null);
  public static final XSModelGroup.Compositor CHOICE = new XSModelGroup.Compositor("choice", null);
  
  public abstract XSModelGroup.Compositor getCompositor();
  
  public abstract XSParticle getChild(int paramInt);
  
  public abstract int getSize();
  
  public abstract XSParticle[] getChildren();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\XSModelGroup.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */