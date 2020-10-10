package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSVisitor;

abstract class ColorBinder
  extends BindingComponent
  implements XSVisitor
{
  protected final BGMBuilder builder = (BGMBuilder)Ring.get(BGMBuilder.class);
  protected final ClassSelector selector = getClassSelector();
  
  protected final CClassInfo getCurrentBean()
  {
    return this.selector.getCurrentBean();
  }
  
  protected final XSComponent getCurrentRoot()
  {
    return this.selector.getCurrentRoot();
  }
  
  protected final void createSimpleTypeProperty(XSSimpleType type, String propName)
  {
    BIProperty prop = BIProperty.getCustomization(type);
    
    SimpleTypeBuilder stb = (SimpleTypeBuilder)Ring.get(SimpleTypeBuilder.class);
    
    CPropertyInfo p = prop.createValueProperty(propName, false, type, stb.buildDef(type), BGMBuilder.getName(type));
    getCurrentBean().addProperty(p);
  }
  
  public final void annotation(XSAnnotation xsAnnotation)
  {
    throw new IllegalStateException();
  }
  
  public final void schema(XSSchema xsSchema)
  {
    throw new IllegalStateException();
  }
  
  public final void facet(XSFacet xsFacet)
  {
    throw new IllegalStateException();
  }
  
  public final void notation(XSNotation xsNotation)
  {
    throw new IllegalStateException();
  }
  
  public final void identityConstraint(XSIdentityConstraint xsIdentityConstraint)
  {
    throw new IllegalStateException();
  }
  
  public final void xpath(XSXPath xsxPath)
  {
    throw new IllegalStateException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\ColorBinder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */