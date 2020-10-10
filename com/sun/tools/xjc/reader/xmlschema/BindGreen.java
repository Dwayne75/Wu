package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeFieldBuilder;
import com.sun.xml.xsom.XSAttContainer;
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
import java.util.Iterator;

public final class BindGreen
  extends ColorBinder
{
  private final ComplexTypeFieldBuilder ctBuilder = (ComplexTypeFieldBuilder)Ring.get(ComplexTypeFieldBuilder.class);
  
  public void attGroupDecl(XSAttGroupDecl ag)
  {
    attContainer(ag);
  }
  
  public void attContainer(XSAttContainer cont)
  {
    Iterator itr = cont.iterateDeclaredAttributeUses();
    while (itr.hasNext()) {
      this.builder.ying((XSAttributeUse)itr.next(), cont);
    }
    itr = cont.iterateAttGroups();
    while (itr.hasNext()) {
      this.builder.ying((XSAttGroupDecl)itr.next(), cont);
    }
    XSWildcard w = cont.getAttributeWildcard();
    if (w != null) {
      this.builder.ying(w, cont);
    }
  }
  
  public void complexType(XSComplexType ct)
  {
    this.ctBuilder.build(ct);
  }
  
  public void attributeDecl(XSAttributeDecl xsAttributeDecl)
  {
    throw new UnsupportedOperationException();
  }
  
  public void wildcard(XSWildcard xsWildcard)
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
  
  public void elementDecl(XSElementDecl xsElementDecl)
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
  
  public void simpleType(XSSimpleType xsSimpleType)
  {
    throw new IllegalStateException();
  }
  
  public void attributeUse(XSAttributeUse use)
  {
    throw new IllegalStateException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\BindGreen.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */