package com.sun.tools.xjc.grammar.util;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.xml.bind.JAXBAssertionError;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public abstract class BreadthFirstExpressionCloner
  extends ExpressionCloner
{
  private final Set visitedExps = new HashSet();
  private final Stack queue = new Stack();
  private boolean inLoop = false;
  
  protected BreadthFirstExpressionCloner(ExpressionPool pool)
  {
    super(pool);
  }
  
  public final Expression onElement(ElementExp exp)
  {
    if (this.visitedExps.add(exp))
    {
      this.queue.push(exp);
      processQueue();
    }
    return exp;
  }
  
  public final Expression onRef(ReferenceExp exp)
  {
    if (this.visitedExps.add(exp))
    {
      this.queue.push(exp);
      processQueue();
    }
    return exp;
  }
  
  public final Expression onOther(OtherExp exp)
  {
    if (this.visitedExps.add(exp))
    {
      this.queue.push(exp);
      processQueue();
    }
    return exp;
  }
  
  public final Expression onAttribute(AttributeExp exp)
  {
    if (this.visitedExps.contains(exp)) {
      return exp;
    }
    Expression e = this.pool.createAttribute(exp.nameClass, exp.exp.visit(this));
    this.visitedExps.add(e);
    return e;
  }
  
  private void processQueue()
  {
    if (this.inLoop) {
      return;
    }
    this.inLoop = true;
    while (!this.queue.isEmpty())
    {
      Expression e = (Expression)this.queue.pop();
      if ((e instanceof ElementExp))
      {
        ElementExp ee = (ElementExp)e;
        ee.contentModel = ee.contentModel.visit(this);
      }
      else if ((e instanceof ReferenceExp))
      {
        ReferenceExp re = (ReferenceExp)e;
        re.exp = re.exp.visit(this);
      }
      else if ((e instanceof OtherExp))
      {
        OtherExp oe = (OtherExp)e;
        oe.exp = oe.exp.visit(this);
      }
      else
      {
        throw new JAXBAssertionError();
      }
    }
    this.inLoop = false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\util\BreadthFirstExpressionCloner.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */