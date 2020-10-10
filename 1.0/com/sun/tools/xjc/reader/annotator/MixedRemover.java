package com.sun.tools.xjc.reader.annotator;

import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.MixedExp;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;
import java.util.HashSet;
import java.util.Set;

public class MixedRemover
  extends ExpressionCloner
{
  private final Set visitedExps = new HashSet();
  private final AnnotatedGrammar grammar;
  
  public MixedRemover(AnnotatedGrammar g)
  {
    super(g.getPool());
    this.grammar = g;
  }
  
  public Expression onRef(ReferenceExp exp)
  {
    if (this.visitedExps.add(exp)) {
      exp.exp = exp.exp.visit(this);
    }
    return exp;
  }
  
  public Expression onOther(OtherExp exp)
  {
    if (this.visitedExps.add(exp)) {
      exp.exp = exp.exp.visit(this);
    }
    return exp;
  }
  
  public Expression onElement(ElementExp exp)
  {
    if (!this.visitedExps.add(exp)) {
      return exp;
    }
    exp.contentModel = exp.contentModel.visit(this);
    return exp;
  }
  
  public Expression onAttribute(AttributeExp exp)
  {
    if (!this.visitedExps.add(exp)) {
      return exp;
    }
    return this.pool.createAttribute(exp.nameClass, exp.exp.visit(this));
  }
  
  public Expression onMixed(MixedExp exp)
  {
    return this.pool.createInterleave(this.pool.createZeroOrMore(this.grammar.createPrimitiveItem(new IdentityTransducer(this.grammar.codeModel), StringType.theInstance, this.pool.createData(StringType.theInstance), null)), exp.exp.visit(this));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\annotator\MixedRemover.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */