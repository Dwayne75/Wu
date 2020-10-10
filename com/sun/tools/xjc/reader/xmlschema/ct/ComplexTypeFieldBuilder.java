package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.tools.xjc.reader.xmlschema.BindingComponent;
import com.sun.xml.xsom.XSComplexType;
import java.util.HashMap;
import java.util.Map;

public final class ComplexTypeFieldBuilder
  extends BindingComponent
{
  private final CTBuilder[] complexTypeBuilders = { new MixedComplexTypeBuilder(), new FreshComplexTypeBuilder(), new ExtendedComplexTypeBuilder(), new RestrictedComplexTypeBuilder(), new STDerivedComplexTypeBuilder() };
  private final Map<XSComplexType, ComplexTypeBindingMode> complexTypeBindingModes = new HashMap();
  
  public void build(XSComplexType type)
  {
    for (CTBuilder ctb : this.complexTypeBuilders) {
      if (ctb.isApplicable(type))
      {
        ctb.build(type);
        return;
      }
    }
    if (!$assertionsDisabled) {
      throw new AssertionError();
    }
  }
  
  public void recordBindingMode(XSComplexType type, ComplexTypeBindingMode flag)
  {
    Object o = this.complexTypeBindingModes.put(type, flag);
    assert (o == null);
  }
  
  protected ComplexTypeBindingMode getBindingMode(XSComplexType type)
  {
    ComplexTypeBindingMode r = (ComplexTypeBindingMode)this.complexTypeBindingModes.get(type);
    assert (r != null);
    return r;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\ct\ComplexTypeFieldBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */