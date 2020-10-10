package com.sun.tools.xjc.grammar.util;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.DataExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.relax.NoneType;
import java.util.HashSet;
import java.util.Set;

public class NotAllowedRemover
  extends ExpressionCloner
{
  public NotAllowedRemover(ExpressionPool pool)
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
    if (exp.exp == Expression.nullSet) {
      return Expression.nullSet;
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
    if (exp.exp == Expression.nullSet) {
      return Expression.nullSet;
    }
    return exp;
  }
  
  private final Set visitedExps = new HashSet();
  
  public Expression onElement(ElementExp exp)
  {
    if (!this.visitedExps.add(exp)) {
      return exp;
    }
    Expression body = exp.contentModel.visit(this);
    if (body == Expression.nullSet) {
      return Expression.nullSet;
    }
    exp.contentModel = body;
    return exp;
  }
  
  public Expression onAttribute(AttributeExp exp)
  {
    if (!this.visitedExps.add(exp)) {
      return exp;
    }
    Expression body = exp.exp.visit(this);
    if (body == Expression.nullSet) {
      return Expression.nullSet;
    }
    return this.pool.createAttribute(exp.nameClass, body);
  }
  
  public Expression onData(DataExp exp)
  {
    if ((exp.dt instanceof NoneType)) {
      return Expression.nullSet;
    }
    return exp;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\util\NotAllowedRemover.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */