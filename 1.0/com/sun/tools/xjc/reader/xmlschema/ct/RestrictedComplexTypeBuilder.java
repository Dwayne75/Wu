package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.cs.ClassSelector;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSType;

class RestrictedComplexTypeBuilder
  extends AbstractCTBuilder
{
  public RestrictedComplexTypeBuilder(ComplexTypeFieldBuilder _builder)
  {
    super(_builder);
  }
  
  public boolean isApplicable(XSComplexType ct)
  {
    XSType baseType = ct.getBaseType();
    return (baseType != this.bgmBuilder.schemas.getAnyType()) && (baseType.isComplexType()) && (ct.getDerivationMethod() == 2);
  }
  
  public Expression build(XSComplexType ct)
  {
    XSComplexType baseType = ct.getBaseType().asComplexType();
    
    ClassItem baseClass = this.bgmBuilder.selector.bindToType(baseType);
    _assert(baseClass != null);
    
    this.builder.recordBindingMode(ct, this.builder.getBindingMode(baseType));
    
    return new SuperClassItem(baseClass, ct.getLocator());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\ct\RestrictedComplexTypeBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */