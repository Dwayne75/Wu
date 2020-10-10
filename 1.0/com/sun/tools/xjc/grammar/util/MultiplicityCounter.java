package com.sun.tools.xjc.grammar.util;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.ConcurExp;
import com.sun.msv.grammar.DataExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionVisitor;
import com.sun.msv.grammar.InterleaveExp;
import com.sun.msv.grammar.ListExp;
import com.sun.msv.grammar.MixedExp;
import com.sun.msv.grammar.OneOrMoreExp;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.SequenceExp;
import com.sun.msv.grammar.ValueExp;

public abstract class MultiplicityCounter
  implements ExpressionVisitor
{
  public static final MultiplicityCounter javaItemCounter = new MultiplicityCounter.1();
  
  protected abstract Multiplicity isChild(Expression paramExpression);
  
  public Object onSequence(SequenceExp exp)
  {
    Multiplicity m = isChild(exp);
    if (m != null) {
      return m;
    }
    return Multiplicity.group((Multiplicity)exp.exp1.visit(this), (Multiplicity)exp.exp2.visit(this));
  }
  
  public Object onInterleave(InterleaveExp exp)
  {
    Multiplicity m = isChild(exp);
    if (m != null) {
      return m;
    }
    return Multiplicity.group((Multiplicity)exp.exp1.visit(this), (Multiplicity)exp.exp2.visit(this));
  }
  
  public Object onChoice(ChoiceExp exp)
  {
    Multiplicity m = isChild(exp);
    if (m != null) {
      return m;
    }
    return Multiplicity.choice((Multiplicity)exp.exp1.visit(this), (Multiplicity)exp.exp2.visit(this));
  }
  
  public Object onOneOrMore(OneOrMoreExp exp)
  {
    Multiplicity m = isChild(exp);
    if (m != null) {
      return m;
    }
    return Multiplicity.oneOrMore((Multiplicity)exp.exp.visit(this));
  }
  
  public Object onMixed(MixedExp exp)
  {
    Multiplicity m = isChild(exp);
    if (m != null) {
      return m;
    }
    return exp.exp.visit(this);
  }
  
  public Object onList(ListExp exp)
  {
    Multiplicity m = isChild(exp);
    if (m != null) {
      return m;
    }
    return exp.exp.visit(this);
  }
  
  public Object onEpsilon()
  {
    Multiplicity m = isChild(Expression.epsilon);
    if (m == null) {
      m = Multiplicity.zero;
    }
    return m;
  }
  
  public Object onAnyString()
  {
    Multiplicity m = isChild(Expression.anyString);
    if (m == null) {
      m = Multiplicity.zero;
    }
    return m;
  }
  
  public Object onData(DataExp exp)
  {
    Multiplicity m = isChild(exp);
    if (m == null) {
      m = Multiplicity.zero;
    }
    return m;
  }
  
  public Object onValue(ValueExp exp)
  {
    Multiplicity m = isChild(exp);
    if (m == null) {
      m = Multiplicity.zero;
    }
    return m;
  }
  
  public Object onElement(ElementExp exp)
  {
    Multiplicity m = isChild(exp);
    if (m != null) {
      return m;
    }
    return exp.contentModel.visit(this);
  }
  
  public Object onAttribute(AttributeExp exp)
  {
    Multiplicity m = isChild(exp);
    if (m != null) {
      return m;
    }
    return exp.exp.visit(this);
  }
  
  public Object onRef(ReferenceExp exp)
  {
    Multiplicity m = isChild(exp);
    if (m != null) {
      return m;
    }
    return exp.exp.visit(this);
  }
  
  public Object onOther(OtherExp exp)
  {
    Multiplicity m = isChild(exp);
    if (m != null) {
      return m;
    }
    return exp.exp.visit(this);
  }
  
  public Object onConcur(ConcurExp exp)
  {
    throw new Error();
  }
  
  public Object onNullSet()
  {
    throw new Error();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\util\MultiplicityCounter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */