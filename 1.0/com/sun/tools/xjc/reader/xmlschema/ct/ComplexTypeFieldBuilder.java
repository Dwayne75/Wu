package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSComplexType;
import java.util.HashMap;
import java.util.Map;

public class ComplexTypeFieldBuilder
{
  protected final BGMBuilder builder;
  private final CTBuilder[] complexTypeBuilders;
  private final Map complexTypeBindingModes = new HashMap();
  
  public ComplexTypeFieldBuilder(BGMBuilder _builder)
  {
    this.builder = _builder;
    
    this.complexTypeBuilders = new CTBuilder[] { new ChoiceComplexTypeBuilder(this), new MixedComplexTypeBuilder(this), new FreshComplexTypeBuilder(this), new ExtendedComplexTypeBuilder(this), new RestrictedComplexTypeBuilder(this), new STDerivedComplexTypeBuilder(this) };
  }
  
  public Expression build(XSComplexType type)
  {
    for (int i = 0; i < this.complexTypeBuilders.length; i++) {
      if (this.complexTypeBuilders[i].isApplicable(type)) {
        return this.complexTypeBuilders[i].build(type);
      }
    }
    _assert(false);
    return null;
  }
  
  protected void recordBindingMode(XSComplexType type, ComplexTypeBindingMode flag)
  {
    Object o = this.complexTypeBindingModes.put(type, flag);
    _assert(o == null);
  }
  
  protected ComplexTypeBindingMode getBindingMode(XSComplexType type)
  {
    Object r = this.complexTypeBindingModes.get(type);
    _assert(r != null);
    return (ComplexTypeBindingMode)r;
  }
  
  protected static void _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\ct\ComplexTypeFieldBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */