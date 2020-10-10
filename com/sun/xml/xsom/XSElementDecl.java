package com.sun.xml.xsom;

import java.util.List;
import java.util.Set;

public abstract interface XSElementDecl
  extends XSDeclaration, XSTerm
{
  public abstract XSType getType();
  
  public abstract boolean isNillable();
  
  public abstract XSElementDecl getSubstAffiliation();
  
  public abstract List<XSIdentityConstraint> getIdentityConstraints();
  
  public abstract boolean isSubstitutionExcluded(int paramInt);
  
  public abstract boolean isSubstitutionDisallowed(int paramInt);
  
  public abstract boolean isAbstract();
  
  /**
   * @deprecated
   */
  public abstract XSElementDecl[] listSubstitutables();
  
  public abstract Set<? extends XSElementDecl> getSubstitutables();
  
  public abstract boolean canBeSubstitutedBy(XSElementDecl paramXSElementDecl);
  
  public abstract XmlString getDefaultValue();
  
  public abstract XmlString getFixedValue();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\XSElementDecl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */