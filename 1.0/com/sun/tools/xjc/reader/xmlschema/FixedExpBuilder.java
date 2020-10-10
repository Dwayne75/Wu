package com.sun.tools.xjc.reader.xmlschema;

import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.ConcurExp;
import com.sun.msv.grammar.DataExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.ExpressionVisitorExpression;
import com.sun.msv.grammar.InterleaveExp;
import com.sun.msv.grammar.ListExp;
import com.sun.msv.grammar.MixedExp;
import com.sun.msv.grammar.OneOrMoreExp;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.SequenceExp;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.xml.bind.JAXBAssertionError;
import java.util.StringTokenizer;
import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.ValidationContext;

class FixedExpBuilder
  implements ExpressionVisitorExpression
{
  private final AnnotatedGrammar grammar;
  private final ExpressionPool pool;
  private String token;
  private final ValidationContext context;
  
  public static Expression build(Expression exp, String token, AnnotatedGrammar grammar, ValidationContext context)
  {
    return exp.visit(new FixedExpBuilder(grammar, token, context));
  }
  
  private FixedExpBuilder(AnnotatedGrammar _grammar, String _token, ValidationContext _context)
  {
    this.grammar = _grammar;
    this.pool = this.grammar.getPool();
    this.token = _token;
    this.context = _context;
  }
  
  public Expression onOther(OtherExp exp)
  {
    if ((exp instanceof PrimitiveItem))
    {
      PrimitiveItem pi = (PrimitiveItem)exp;
      
      Expression body = exp.exp.visit(this);
      if (body == Expression.nullSet) {
        return body;
      }
      return this.grammar.createPrimitiveItem(pi.xducer, pi.guard, body, pi.locator);
    }
    return exp.exp.visit(this);
  }
  
  public Expression onList(ListExp exp)
  {
    String oldToken = this.token;
    
    Expression residual = exp.exp;
    Expression result = Expression.epsilon;
    StringTokenizer tokens = new StringTokenizer(this.token);
    while (tokens.hasMoreTokens())
    {
      this.token = tokens.nextToken();
      result = this.pool.createSequence(result, residual.visit(this));
    }
    result = this.pool.createList(result);
    
    this.token = oldToken;
    return result;
  }
  
  public Expression onRef(ReferenceExp exp)
  {
    return exp.exp.visit(this);
  }
  
  private static void _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
  }
  
  public Expression onAnyString()
  {
    return this.pool.createValue(StringType.theInstance, this.token);
  }
  
  public Expression onChoice(ChoiceExp exp)
  {
    Expression r = exp.exp1.visit(this);
    if (r != Expression.nullSet) {
      return r;
    }
    return exp.exp2.visit(this);
  }
  
  public Expression onEpsilon()
  {
    return Expression.nullSet;
  }
  
  public Expression onNullSet()
  {
    return Expression.nullSet;
  }
  
  public Expression onOneOrMore(OneOrMoreExp exp)
  {
    return exp.exp.visit(this);
  }
  
  public Expression onSequence(SequenceExp exp)
  {
    Expression r = exp.exp1.visit(this);
    if ((r == Expression.nullSet) && (exp.exp1.isEpsilonReducible())) {
      r = exp.exp2.visit(this);
    }
    return r;
  }
  
  public Expression onData(DataExp exp)
  {
    if (exp.dt.isValid(this.token, this.context)) {
      return this.pool.createValue(exp.dt, exp.name, exp.dt.createValue(this.token, this.context));
    }
    return Expression.nullSet;
  }
  
  public Expression onValue(ValueExp exp)
  {
    if (exp.dt.sameValue(exp.value, exp.dt.createValue(this.token, this.context))) {
      return exp;
    }
    return Expression.nullSet;
  }
  
  public Expression onAttribute(AttributeExp exp)
  {
    _assert(false);return null;
  }
  
  public Expression onElement(ElementExp exp)
  {
    _assert(false);return null;
  }
  
  public Expression onConcur(ConcurExp p)
  {
    _assert(false);return null;
  }
  
  public Expression onInterleave(InterleaveExp p)
  {
    _assert(false);return null;
  }
  
  public Expression onMixed(MixedExp exp)
  {
    _assert(false);return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\FixedExpBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */