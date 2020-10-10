package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.generator.bean.field.FieldRendererFactory;
import com.sun.tools.xjc.model.CClass;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CDefaultValue;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
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

public class BindPurple
  extends ColorBinder
{
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
    boolean hasFixedValue = use.getFixedValue() != null;
    BIProperty pc = BIProperty.getCustomization(use);
    
    boolean toConstant = (pc.isConstantProperty()) && (hasFixedValue);
    TypeUse attType = bindAttDecl(use.getDecl());
    
    CPropertyInfo prop = pc.createAttributeProperty(use, attType);
    if (toConstant)
    {
      prop.defaultValue = CDefaultValue.create(attType, use.getFixedValue());
      prop.realization = this.builder.fieldRendererFactory.getConst(prop.realization);
    }
    else if (!attType.isCollection())
    {
      if (use.getDefaultValue() != null) {
        prop.defaultValue = CDefaultValue.create(attType, use.getDefaultValue());
      } else if (use.getFixedValue() != null) {
        prop.defaultValue = CDefaultValue.create(attType, use.getFixedValue());
      }
    }
    getCurrentBean().addProperty(prop);
  }
  
  private TypeUse bindAttDecl(XSAttributeDecl decl)
  {
    SimpleTypeBuilder stb = (SimpleTypeBuilder)Ring.get(SimpleTypeBuilder.class);
    stb.refererStack.push(decl);
    try
    {
      return stb.build(decl.getType());
    }
    finally
    {
      stb.refererStack.pop();
    }
  }
  
  public void complexType(XSComplexType ct)
  {
    CClass ctBean = this.selector.bindToType(ct, null, false);
    if (getCurrentBean() != ctBean) {
      getCurrentBean().setBaseClass(ctBean);
    }
  }
  
  public void wildcard(XSWildcard xsWildcard)
  {
    getCurrentBean().hasAttributeWildcard(true);
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
  
  public void simpleType(XSSimpleType type)
  {
    createSimpleTypeProperty(type, "Value");
  }
  
  public void particle(XSParticle xsParticle)
  {
    throw new UnsupportedOperationException();
  }
  
  public void empty(XSContentType ct) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\BindPurple.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */