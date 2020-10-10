package com.sun.tools.xjc.grammar.util;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.Grammar;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.msv.grammar.trex.TREXGrammar;
import com.sun.tools.xjc.grammar.ExternalItem;
import java.util.HashMap;
import java.util.Map;

public class AnnotationRemover
  extends ExpressionCloner
{
  private final Map bookmarks = new HashMap();
  private final Map elements = new HashMap();
  private final ReferenceExp anyContent;
  
  public static Grammar remove(Grammar src)
  {
    ExpressionPool newPool = new ExpressionPool();
    
    Expression newTop = src.getTopLevel().visit(new AnnotationRemover(newPool));
    
    TREXGrammar grammar = new TREXGrammar(newPool);
    grammar.exp = newTop;
    
    return grammar;
  }
  
  public static Expression remove(Expression exp, ExpressionPool pool)
  {
    return exp.visit(new AnnotationRemover(pool));
  }
  
  private AnnotationRemover(ExpressionPool pool)
  {
    super(pool);
    this.anyContent = new ReferenceExp("anyContent");
    this.anyContent.exp = pool.createZeroOrMore(pool.createChoice(new ElementPattern(NameClass.ALL, this.anyContent), pool.createChoice(pool.createAttribute(NameClass.ALL), Expression.anyString)));
  }
  
  public Expression onRef(ReferenceExp exp)
  {
    if (!this.bookmarks.containsKey(exp)) {
      return exp.exp.visit(this);
    }
    ReferenceExp target = (ReferenceExp)this.bookmarks.get(exp);
    if (target == null)
    {
      target = new ReferenceExp(exp.name);
      target.exp = exp.exp.visit(this);
      
      this.bookmarks.put(exp, target);
    }
    return target;
  }
  
  public Expression onOther(OtherExp exp)
  {
    if ((exp instanceof ExternalItem)) {
      return ((ExternalItem)exp).createAGM(this.pool);
    }
    return exp.exp.visit(this);
  }
  
  public Expression onAttribute(AttributeExp exp)
  {
    return this.pool.createAttribute(exp.nameClass, exp.exp.visit(this));
  }
  
  public Expression onElement(ElementExp exp)
  {
    ElementExp result = (ElementExp)this.elements.get(exp);
    if (result != null) {
      return result;
    }
    result = new ElementPattern(exp.getNameClass(), Expression.nullSet);
    this.elements.put(exp, result);
    
    result.contentModel = exp.contentModel.visit(this);
    result.ignoreUndeclaredAttributes = exp.ignoreUndeclaredAttributes;
    
    return result;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\util\AnnotationRemover.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */