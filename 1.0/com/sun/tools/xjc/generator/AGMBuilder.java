package com.sun.tools.xjc.generator;

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
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.xml.bind.GrammarImpl;
import com.sun.xml.bind.GrammarImpl.Plug;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AGMBuilder
  extends ExpressionCloner
{
  private final Map class2agm = new HashMap();
  private final Map ref2exp = new HashMap();
  private final Map elem2exp = new HashMap();
  private final ReferenceExp anyContent;
  private final ArrayList plugs = new ArrayList();
  private final GrammarImpl grammar = new GrammarImpl(new ExpressionPool());
  
  public static Grammar remove(AnnotatedGrammar src)
  {
    AGMBuilder builder = new AGMBuilder(src);
    
    builder.grammar.setTopLevel(src.getTopLevel().visit(builder));
    builder.grammar.setPlugs((GrammarImpl.Plug[])builder.plugs.toArray(new GrammarImpl.Plug[0]));
    
    return builder.grammar;
  }
  
  private AGMBuilder(AnnotatedGrammar grammar)
  {
    super(new ExpressionPool());
    
    this.anyContent = new ReferenceExp("anyContent");
    this.anyContent.exp = this.pool.createZeroOrMore(this.pool.createChoice(new ElementPattern(NameClass.ALL, this.anyContent), this.pool.createChoice(this.pool.createAttribute(NameClass.ALL), Expression.anyString)));
    
    ClassItem[] ci = grammar.getClasses();
    for (int i = 0; i < ci.length; i++)
    {
      if (ci[i].agm.exp == null) {
        ci[i].agm.exp = ci[i].exp;
      }
      this.class2agm.put(ci[i], new ReferenceExp(null, ci[i].agm));
    }
    for (int i = 0; i < ci.length; i++)
    {
      ReferenceExp e = (ReferenceExp)this.class2agm.get(ci[i]);
      e.exp = e.exp.visit(this);
    }
  }
  
  public Expression onRef(ReferenceExp exp)
  {
    Expression e = (Expression)this.ref2exp.get(exp);
    if (e != null) {
      return e;
    }
    e = exp.exp.visit(this);
    this.ref2exp.put(exp, e);
    
    return e;
  }
  
  public Expression onOther(OtherExp exp)
  {
    if ((exp instanceof ExternalItem))
    {
      Expression e = ((ExternalItem)exp).createAGM(this.pool);
      if ((e instanceof GrammarImpl.Plug)) {
        this.plugs.add(e);
      }
      return e;
    }
    if ((exp instanceof ClassItem)) {
      return (Expression)this.class2agm.get(exp);
    }
    return exp.exp.visit(this);
  }
  
  public Expression onAttribute(AttributeExp exp)
  {
    return this.pool.createAttribute(exp.nameClass, exp.exp.visit(this));
  }
  
  public Expression onElement(ElementExp exp)
  {
    ElementExp result = (ElementExp)this.elem2exp.get(exp);
    if (result == null)
    {
      result = this.grammar.createElement(exp.getNameClass(), Expression.nullSet);
      this.elem2exp.put(exp, result);
      result.contentModel = exp.getContentModel().visit(this);
      result.ignoreUndeclaredAttributes = exp.ignoreUndeclaredAttributes;
    }
    return result;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\AGMBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */