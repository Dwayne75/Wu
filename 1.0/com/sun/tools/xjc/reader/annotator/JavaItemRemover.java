package com.sun.tools.xjc.reader.annotator;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ConcurExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

class JavaItemRemover
  extends ExpressionCloner
{
  private static PrintStream debug = null;
  private final Set targets;
  
  JavaItemRemover(ExpressionPool pool, Set targets)
  {
    super(pool);
    this.targets = targets;
  }
  
  public Expression onNullSet()
  {
    throw new Error();
  }
  
  public Expression onConcur(ConcurExp exp)
  {
    throw new Error();
  }
  
  public Expression onAttribute(AttributeExp exp)
  {
    Expression body = exp.exp.visit(this);
    if (body == exp.exp) {
      return exp;
    }
    return this.pool.createAttribute(exp.nameClass, body);
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
  
  public Expression onRef(ReferenceExp exp)
  {
    if (!this.visitedExps.add(exp)) {
      return exp;
    }
    exp.exp = exp.exp.visit(this);
    return exp;
  }
  
  public Expression onOther(OtherExp exp)
  {
    if (this.targets.contains(exp))
    {
      if (debug != null) {
        debug.println(" " + exp + ": found and removed");
      }
      return exp.exp.visit(this);
    }
    exp.exp = exp.exp.visit(this);
    return exp;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\annotator\JavaItemRemover.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */