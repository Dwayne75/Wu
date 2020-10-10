package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.reader.RawTypeSet;
import com.sun.tools.xjc.reader.xmlschema.BindGreen;
import com.sun.tools.xjc.reader.xmlschema.ClassSelector;
import com.sun.tools.xjc.reader.xmlschema.RawTypeSetBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSType;

final class MixedComplexTypeBuilder
  extends CTBuilder
{
  public boolean isApplicable(XSComplexType ct)
  {
    XSType bt = ct.getBaseType();
    if ((bt == this.schemas.getAnyType()) && (ct.isMixed())) {
      return true;
    }
    if ((bt.isComplexType()) && (!bt.asComplexType().isMixed()) && (ct.isMixed()) && (ct.getDerivationMethod() == 1) && (ct.getContentType().asParticle() != null)) {
      return true;
    }
    return false;
  }
  
  public void build(XSComplexType ct)
  {
    XSContentType contentType = ct.getContentType();
    
    this.builder.recordBindingMode(ct, ComplexTypeBindingMode.FALLBACK_CONTENT);
    
    BIProperty prop = BIProperty.getCustomization(ct);
    CPropertyInfo p;
    CPropertyInfo p;
    if (contentType.asEmpty() != null)
    {
      p = prop.createValueProperty("Content", false, ct, CBuiltinLeafInfo.STRING, null);
    }
    else
    {
      RawTypeSet ts = RawTypeSetBuilder.build(contentType.asParticle(), false);
      p = prop.createReferenceProperty("Content", false, ct, ts, true);
    }
    this.selector.getCurrentBean().addProperty(p);
    
    this.green.attContainer(ct);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\ct\MixedComplexTypeBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */