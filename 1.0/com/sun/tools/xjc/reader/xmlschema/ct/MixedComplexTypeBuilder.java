package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.util.Multiplicity;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.FieldBuilder;
import com.sun.tools.xjc.reader.xmlschema.TypeBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSSchemaSet;

class MixedComplexTypeBuilder
  extends AbstractCTBuilder
{
  public MixedComplexTypeBuilder(ComplexTypeFieldBuilder _builder)
  {
    super(_builder);
  }
  
  public boolean isApplicable(XSComplexType ct)
  {
    return (ct.getBaseType() == this.bgmBuilder.schemas.getAnyType()) && (ct.isMixed());
  }
  
  public Expression build(XSComplexType ct)
  {
    BIProperty prop = BIProperty.getCustomization(this.bgmBuilder, ct);
    
    this.builder.recordBindingMode(ct, ComplexTypeBindingMode.FALLBACK_CONTENT);
    
    FieldItem fi = prop.createFieldItem("Content", false, this.pool.createInterleave(this.pool.createZeroOrMore(this.bgmBuilder.grammar.createPrimitiveItem(new IdentityTransducer(this.bgmBuilder.grammar.codeModel), StringType.theInstance, this.pool.createData(StringType.theInstance), ct.getLocator())), this.bgmBuilder.typeBuilder.build(ct.getContentType())), ct);
    
    fi.multiplicity = Multiplicity.star;
    fi.collisionExpected = true;
    
    return this.pool.createSequence(this.bgmBuilder.fieldBuilder.attributeContainer(ct), fi);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\ct\MixedComplexTypeBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */