package com.sun.tools.xjc.grammar.util;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import java.util.HashSet;
import java.util.Set;

public class SuperClassBodyRemover
  extends ExpressionCloner
{
  private final Set visitedRefs = new HashSet();
  private ExpressionCloner remover;
  
  public static void remove(AnnotatedGrammar g)
  {
    SuperClassBodyRemover su = new SuperClassBodyRemover(g.getPool());
    
    ClassItem[] cls = g.getClasses();
    for (int i = 0; i < cls.length; i++) {
      cls[i].exp = cls[i].exp.visit(su);
    }
  }
  
  public Expression onAttribute(AttributeExp exp)
  {
    return this.pool.createAttribute(exp.nameClass, exp.exp.visit(this));
  }
  
  public Expression onElement(ElementExp exp)
  {
    if (this.visitedRefs.add(exp)) {
      exp.contentModel = exp.contentModel;
    }
    return exp;
  }
  
  public Expression onRef(ReferenceExp exp)
  {
    if (this.visitedRefs.add(exp)) {
      exp.exp = exp.exp.visit(this);
    }
    return exp;
  }
  
  public Expression onOther(OtherExp exp)
  {
    if ((exp instanceof SuperClassItem)) {
      return exp.exp.visit(this.remover);
    }
    if (this.visitedRefs.add(exp)) {
      exp.exp = exp.exp.visit(this);
    }
    return exp;
  }
  
  private SuperClassBodyRemover(ExpressionPool pool)
  {
    super(pool);
    this.remover = new SuperClassBodyRemover.1(this, pool);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\util\SuperClassBodyRemover.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */