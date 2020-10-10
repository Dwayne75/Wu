package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeFieldBuilder;
import com.sun.xml.bind.v2.TODO;
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
import java.util.Stack;

public final class BindRed
  extends ColorBinder
{
  private final ComplexTypeFieldBuilder ctBuilder = (ComplexTypeFieldBuilder)Ring.get(ComplexTypeFieldBuilder.class);
  
  public void complexType(XSComplexType ct)
  {
    this.ctBuilder.build(ct);
  }
  
  public void wildcard(XSWildcard xsWildcard)
  {
    TODO.checkSpec();
    throw new UnsupportedOperationException();
  }
  
  public void elementDecl(XSElementDecl e)
  {
    SimpleTypeBuilder stb = (SimpleTypeBuilder)Ring.get(SimpleTypeBuilder.class);
    stb.refererStack.push(e);
    this.builder.ying(e.getType(), e);
    stb.refererStack.pop();
  }
  
  public void simpleType(XSSimpleType type)
  {
    SimpleTypeBuilder stb = (SimpleTypeBuilder)Ring.get(SimpleTypeBuilder.class);
    stb.refererStack.push(type);
    createSimpleTypeProperty(type, "Value");
    stb.refererStack.pop();
  }
  
  public void attGroupDecl(XSAttGroupDecl ag)
  {
    throw new IllegalStateException();
  }
  
  public void attributeDecl(XSAttributeDecl ad)
  {
    throw new IllegalStateException();
  }
  
  public void attributeUse(XSAttributeUse au)
  {
    throw new IllegalStateException();
  }
  
  public void empty(XSContentType xsContentType)
  {
    throw new IllegalStateException();
  }
  
  public void modelGroupDecl(XSModelGroupDecl xsModelGroupDecl)
  {
    throw new IllegalStateException();
  }
  
  public void modelGroup(XSModelGroup xsModelGroup)
  {
    throw new IllegalStateException();
  }
  
  public void particle(XSParticle p)
  {
    throw new IllegalStateException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\BindRed.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */