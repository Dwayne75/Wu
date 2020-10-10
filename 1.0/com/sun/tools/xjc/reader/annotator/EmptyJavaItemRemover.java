package com.sun.tools.xjc.reader.annotator;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import java.util.HashSet;
import java.util.Set;

public class EmptyJavaItemRemover
  extends ExpressionCloner
{
  public EmptyJavaItemRemover(ExpressionPool pool)
  {
    super(pool);
  }
  
  public Expression onRef(ReferenceExp exp)
  {
    if (!this.visitedExps.contains(exp))
    {
      exp.exp = exp.exp.visit(this);
      this.visitedExps.add(exp);
    }
    if (exp.exp == Expression.epsilon) {
      return Expression.epsilon;
    }
    return exp;
  }
  
  public Expression onOther(OtherExp exp)
  {
    if (!this.visitedExps.contains(exp))
    {
      exp.exp = exp.exp.visit(this);
      this.visitedExps.add(exp);
    }
    if ((((exp instanceof SuperClassItem)) || ((exp instanceof FieldItem))) && 
      (exp.exp == Expression.epsilon)) {
      return Expression.epsilon;
    }
    return exp;
  }
  
  private final Set visitedExps = new HashSet();
  
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
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\annotator\EmptyJavaItemRemover.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */