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
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSFunction;
import java.util.Locale;
import java.util.ResourceBundle;

public class NameGetter
  implements XSFunction<String>
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
  
  public String annotation(XSAnnotation ann)
  {
    return localize("annotation");
  }
  
  public String attGroupDecl(XSAttGroupDecl decl)
  {
    return localize("attGroupDecl");
  }
  
  public String attributeUse(XSAttributeUse use)
  {
    return localize("attributeUse");
  }
  
  public String attributeDecl(XSAttributeDecl decl)
  {
    return localize("attributeDecl");
  }
  
  public String complexType(XSComplexType type)
  {
    return localize("complexType");
  }
  
  public String schema(XSSchema schema)
  {
    return localize("schema");
  }
  
  public String facet(XSFacet facet)
  {
    return localize("facet");
  }
  
  public String simpleType(XSSimpleType simpleType)
  {
    return localize("simpleType");
  }
  
  public String particle(XSParticle particle)
  {
    return localize("particle");
  }
  
  public String empty(XSContentType empty)
  {
    return localize("empty");
  }
  
  public String wildcard(XSWildcard wc)
  {
    return localize("wildcard");
  }
  
  public String modelGroupDecl(XSModelGroupDecl decl)
  {
    return localize("modelGroupDecl");
  }
  
  public String modelGroup(XSModelGroup group)
  {
    return localize("modelGroup");
  }
  
  public String elementDecl(XSElementDecl decl)
  {
    return localize("elementDecl");
  }
  
  public String notation(XSNotation n)
  {
    return localize("notation");
  }
  
  public String identityConstraint(XSIdentityConstraint decl)
  {
    return localize("idConstraint");
  }
  
  public String xpath(XSXPath xpath)
  {
    return localize("xpath");
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\util\NameGetter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */