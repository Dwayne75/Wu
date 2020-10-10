package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.FieldBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSTerm;

public class ChoiceComplexTypeBuilder
  extends AbstractCTBuilder
{
  public ChoiceComplexTypeBuilder(ComplexTypeFieldBuilder _builder)
  {
    super(_builder);
  }
  
  public boolean isApplicable(XSComplexType ct)
  {
    if (!this.bgmBuilder.getGlobalBinding().isModelGroupBinding()) {
      return false;
    }
    if (ct.getBaseType() != this.bgmBuilder.schemas.getAnyType()) {
      return false;
    }
    XSParticle p = ct.getContentType().asParticle();
    if (p == null) {
      return false;
    }
    XSModelGroup mg = getTopLevelModelGroup(p);
    if (mg.getCompositor() != XSModelGroup.CHOICE) {
      return false;
    }
    if ((p.getMaxOccurs() > 1) || (p.getMaxOccurs() == -1)) {
      return false;
    }
    return true;
  }
  
  private XSModelGroup getTopLevelModelGroup(XSParticle p)
  {
    XSModelGroup mg = p.getTerm().asModelGroup();
    if (p.getTerm().isModelGroupDecl()) {
      mg = p.getTerm().asModelGroupDecl().getModelGroup();
    }
    return mg;
  }
  
  public Expression build(XSComplexType ct)
  {
    XSModelGroup choice = getTopLevelModelGroup(ct.getContentType().asParticle());
    
    Expression body = this.bgmBuilder.fieldBuilder.build(choice);
    
    return this.pool.createSequence(this.bgmBuilder.fieldBuilder.attributeContainer(ct), body);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\ct\ChoiceComplexTypeBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */