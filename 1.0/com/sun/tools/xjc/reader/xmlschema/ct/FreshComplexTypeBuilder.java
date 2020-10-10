package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.FieldBuilder;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSSchemaSet;

class FreshComplexTypeBuilder
  extends AbstractCTBuilder
{
  public FreshComplexTypeBuilder(ComplexTypeFieldBuilder _builder)
  {
    super(_builder);
  }
  
  public boolean isApplicable(XSComplexType ct)
  {
    return (ct.getBaseType() == this.bgmBuilder.schemas.getAnyType()) && (!ct.isMixed());
  }
  
  public Expression build(XSComplexType ct)
  {
    XSContentType contentType = ct.getContentType();
    
    Expression exp = (Expression)contentType.apply(new FreshComplexTypeBuilder.1(this, ct));
    
    return this.pool.createSequence(this.bgmBuilder.fieldBuilder.attributeContainer(ct), exp);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\ct\FreshComplexTypeBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */