package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.FieldBuilder;
import com.sun.tools.xjc.reader.xmlschema.SimpleTypeBuilder;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import java.util.Stack;

public class STDerivedComplexTypeBuilder
  extends AbstractCTBuilder
{
  public STDerivedComplexTypeBuilder(ComplexTypeFieldBuilder _builder)
  {
    super(_builder);
  }
  
  public boolean isApplicable(XSComplexType ct)
  {
    return ct.getBaseType().isSimpleType();
  }
  
  public Expression build(XSComplexType ct)
  {
    _assert(ct.getDerivationMethod() == 1);
    
    XSSimpleType baseType = ct.getBaseType().asSimpleType();
    
    this.builder.recordBindingMode(ct, ComplexTypeBindingMode.NORMAL);
    
    Expression att = this.bgmBuilder.fieldBuilder.attributeContainer(ct);
    
    this.bgmBuilder.simpleTypeBuilder.refererStack.push(ct);
    
    Expression exp = this.pool.createSequence(att, this.bgmBuilder.fieldBuilder.simpleType(baseType, ct));
    
    this.bgmBuilder.simpleTypeBuilder.refererStack.pop();
    
    return exp;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\ct\STDerivedComplexTypeBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */