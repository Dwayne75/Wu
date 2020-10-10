package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.tools.xjc.model.CClass;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.ClassSelector;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSType;

final class RestrictedComplexTypeBuilder
  extends CTBuilder
{
  public boolean isApplicable(XSComplexType ct)
  {
    XSType baseType = ct.getBaseType();
    return (baseType != this.schemas.getAnyType()) && (baseType.isComplexType()) && (ct.getDerivationMethod() == 2);
  }
  
  public void build(XSComplexType ct)
  {
    if (this.bgmBuilder.getGlobalBinding().isRestrictionFreshType())
    {
      new FreshComplexTypeBuilder().build(ct);
      return;
    }
    XSComplexType baseType = ct.getBaseType().asComplexType();
    
    CClass baseClass = this.selector.bindToType(baseType, ct, true);
    assert (baseClass != null);
    
    this.selector.getCurrentBean().setBaseClass(baseClass);
    
    this.builder.recordBindingMode(ct, this.builder.getBindingMode(baseType));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\ct\RestrictedComplexTypeBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */