package com.sun.xml.xsom;

public abstract interface XSModelGroup
  extends XSComponent, XSTerm, Iterable<XSParticle>
{
  public abstract Compositor getCompositor();
  
  public abstract XSParticle getChild(int paramInt);
  
  public abstract int getSize();
  
  public abstract XSParticle[] getChildren();
  
  public static enum Compositor
  {
    ALL("all"),  CHOICE("choice"),  SEQUENCE("sequence");
    
    private final String value;
    
    private Compositor(String _value)
    {
      this.value = _value;
    }
    
    public String toString()
    {
      return this.value;
    }
  }
  
  public static final Compositor ALL = Compositor.ALL;
  public static final Compositor SEQUENCE = Compositor.SEQUENCE;
  public static final Compositor CHOICE = Compositor.CHOICE;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\XSModelGroup.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */