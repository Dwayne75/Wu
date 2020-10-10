package com.sun.tools.xjc.reader.xmlschema;

import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;

final class BindBlue
  extends ColorBinder
{
  public void complexType(XSComplexType ct)
  {
    throw new UnsupportedOperationException();
  }
  
  public void elementDecl(XSElementDecl e)
  {
    throw new UnsupportedOperationException();
  }
  
  public void wildcard(XSWildcard xsWildcard)
  {
    throw new UnsupportedOperationException();
  }
  
  public void attGroupDecl(XSAttGroupDecl xsAttGroupDecl)
  {
    throw new UnsupportedOperationException();
  }
  
  public void attributeDecl(XSAttributeDecl xsAttributeDecl)
  {
    throw new UnsupportedOperationException();
  }
  
  public void attributeUse(XSAttributeUse use)
  {
    throw new UnsupportedOperationException();
  }
  
  public void modelGroupDecl(XSModelGroupDecl xsModelGroupDecl)
  {
    throw new UnsupportedOperationException();
  }
  
  public void modelGroup(XSModelGroup xsModelGroup)
  {
    throw new UnsupportedOperationException();
  }
  
  public void particle(XSParticle xsParticle)
  {
    throw new UnsupportedOperationException();
  }
  
  public void empty(XSContentType xsContentType)
  {
    throw new UnsupportedOperationException();
  }
  
  public void simpleType(XSSimpleType type)
  {
    throw new IllegalStateException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\BindBlue.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */