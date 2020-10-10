package com.sun.xml.xsom;

public abstract interface XSComplexType
  extends XSType, XSAttContainer
{
  public abstract boolean isAbstract();
  
  public abstract boolean isFinal(int paramInt);
  
  public abstract boolean isSubstitutionProhibited(int paramInt);
  
  public abstract XSElementDecl getScope();
  
  public abstract XSContentType getContentType();
  
  public abstract XSContentType getExplicitContent();
  
  public abstract boolean isMixed();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\XSComplexType.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */