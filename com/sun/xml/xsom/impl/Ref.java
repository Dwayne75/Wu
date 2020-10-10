package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;

public abstract class Ref
{
  public static abstract interface IdentityConstraint
  {
    public abstract XSIdentityConstraint get();
  }
  
  public static abstract interface Element
    extends Ref.Term
  {
    public abstract XSElementDecl get();
  }
  
  public static abstract interface AttGroup
  {
    public abstract XSAttGroupDecl get();
  }
  
  public static abstract interface Attribute
  {
    public abstract XSAttributeDecl getAttribute();
  }
  
  public static abstract interface ComplexType
    extends Ref.Type
  {
    public abstract XSComplexType getType();
  }
  
  public static abstract interface SimpleType
    extends Ref.Type
  {
    public abstract XSSimpleType getType();
  }
  
  public static abstract interface ContentType
  {
    public abstract XSContentType getContentType();
  }
  
  public static abstract interface Type
  {
    public abstract XSType getType();
  }
  
  public static abstract interface Term
  {
    public abstract XSTerm getTerm();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\Ref.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */