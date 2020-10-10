package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.BindGreen;
import com.sun.tools.xjc.reader.xmlschema.ClassSelector;
import com.sun.tools.xjc.reader.xmlschema.SimpleTypeBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import java.util.Stack;

final class STDerivedComplexTypeBuilder
  extends CTBuilder
{
  public boolean isApplicable(XSComplexType ct)
  {
    return ct.getBaseType().isSimpleType();
  }
  
  public void build(XSComplexType ct)
  {
    assert (ct.getDerivationMethod() == 1);
    
    XSSimpleType baseType = ct.getBaseType().asSimpleType();
    
    this.builder.recordBindingMode(ct, ComplexTypeBindingMode.NORMAL);
    
    this.simpleTypeBuilder.refererStack.push(ct);
    TypeUse use = this.simpleTypeBuilder.build(baseType);
    this.simpleTypeBuilder.refererStack.pop();
    
    BIProperty prop = BIProperty.getCustomization(ct);
    CPropertyInfo p = prop.createValueProperty("Value", false, baseType, use, BGMBuilder.getName(baseType));
    this.selector.getCurrentBean().addProperty(p);
    
    this.green.attContainer(ct);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\ct\STDerivedComplexTypeBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */