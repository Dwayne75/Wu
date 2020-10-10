package com.sun.xml.xsom;

public abstract interface XSParticle
  extends XSContentType
{
  public static final int UNBOUNDED = -1;
  
  public abstract int getMinOccurs();
  
  public abstract int getMaxOccurs();
  
  public abstract boolean isRepeated();
  
  public abstract XSTerm getTerm();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\XSParticle.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */