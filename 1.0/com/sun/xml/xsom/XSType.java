package com.sun.xml.xsom;

public abstract interface XSType
  extends XSDeclaration
{
  public static final int EXTENSION = 1;
  public static final int RESTRICTION = 2;
  public static final int SUBSTITUTION = 4;
  
  public abstract XSType getBaseType();
  
  public abstract int getDerivationMethod();
  
  public abstract boolean isSimpleType();
  
  public abstract boolean isComplexType();
  
  public abstract XSType[] listSubstitutables();
  
  public abstract XSSimpleType asSimpleType();
  
  public abstract XSComplexType asComplexType();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\XSType.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */