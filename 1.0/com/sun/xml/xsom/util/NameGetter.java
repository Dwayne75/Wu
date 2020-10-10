package com.sun.xml.xsom.util;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSFunction;
import java.util.Locale;
import java.util.ResourceBundle;

public class NameGetter
  implements XSFunction
{
  private final Locale locale;
  
  public NameGetter(Locale _locale)
  {
    this.locale = _locale;
  }
  
  public static final XSFunction theInstance = new NameGetter(null);
  
  public static String get(XSComponent comp)
  {
    return (String)comp.apply(theInstance);
  }
  
  public Object annotation(XSAnnotation ann)
  {
    return localize("annotation");
  }
  
  public Object attGroupDecl(XSAttGroupDecl decl)
  {
    return localize("attGroupDecl");
  }
  
  public Object attributeUse(XSAttributeUse use)
  {
    return localize("attributeUse");
  }
  
  public Object attributeDecl(XSAttributeDecl decl)
  {
    return localize("attributeDecl");
  }
  
  public Object complexType(XSComplexType type)
  {
    return localize("complexType");
  }
  
  public Object schema(XSSchema schema)
  {
    return localize("schema");
  }
  
  public Object facet(XSFacet facet)
  {
    return localize("facet");
  }
  
  public Object simpleType(XSSimpleType simpleType)
  {
    return localize("simpleType");
  }
  
  public Object particle(XSParticle particle)
  {
    return localize("particle");
  }
  
  public Object empty(XSContentType empty)
  {
    return localize("empty");
  }
  
  public Object wildcard(XSWildcard wc)
  {
    return localize("wildcard");
  }
  
  public Object modelGroupDecl(XSModelGroupDecl decl)
  {
    return localize("modelGroupDecl");
  }
  
  public Object modelGroup(XSModelGroup group)
  {
    return localize("modelGroup");
  }
  
  public Object elementDecl(XSElementDecl decl)
  {
    return localize("elementDecl");
  }
  
  public Object notation(XSNotation n)
  {
    return localize("notation");
  }
  
  private String localize(String key)
  {
    ResourceBundle rb;
    ResourceBundle rb;
    if (this.locale == null) {
      rb = ResourceBundle.getBundle(NameGetter.class.getName());
    } else {
      rb = ResourceBundle.getBundle(NameGetter.class.getName(), this.locale);
    }
    return rb.getString(key);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\util\NameGetter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */