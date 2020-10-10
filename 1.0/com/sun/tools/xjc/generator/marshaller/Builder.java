package com.sun.tools.xjc.generator.marshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JInvocation;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.ConcurExp;
import com.sun.msv.grammar.DataExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.MixedExp;
import com.sun.msv.grammar.OneOrMoreExp;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ValueExp;
import com.sun.msv.grammar.xmlschema.OccurrenceExp;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.BGMWalker;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.xml.bind.JAXBAssertionError;
import org.xml.sax.SAXException;

final class Builder
  extends BGMWalker
{
  private final Context context;
  
  protected Builder(Context _context)
  {
    this.context = _context;
  }
  
  public final void onChoice(ChoiceExp exp)
  {
    if ((exp.exp1 == Expression.epsilon) && ((exp.exp2 instanceof OneOrMoreExp)))
    {
      onOneOrMore((OneOrMoreExp)exp.exp2);
      return;
    }
    if ((exp.exp2 == Expression.epsilon) && ((exp.exp1 instanceof OneOrMoreExp)))
    {
      onOneOrMore((OneOrMoreExp)exp.exp1);
      return;
    }
    this.context.currentSide.onChoice(exp);
  }
  
  public final void onOneOrMore(OneOrMoreExp exp)
  {
    _onOneOrMore(exp.exp);
  }
  
  private void _onOneOrMore(Expression itemExp)
  {
    boolean oldOOM = this.context.inOneOrMore;
    this.context.inOneOrMore = true;
    
    this.context.currentSide.onZeroOrMore(itemExp);
    
    this.context.inOneOrMore = oldOOM;
  }
  
  public final void onNullSet()
  {
    getBlock(true)._throw(JExpr._new(this.context.codeModel.ref(SAXException.class)).arg(JExpr.lit("this object doesn't have any XML representation")));
  }
  
  public Object onIgnore(IgnoreItem exp)
  {
    if (exp.exp.isEpsilonReducible()) {
      return null;
    }
    exp.exp.visit(this);
    return null;
  }
  
  public void onConcur(ConcurExp exp)
  {
    throw new JAXBAssertionError();
  }
  
  public void onMixed(MixedExp exp)
  {
    throw new JAXBAssertionError();
  }
  
  public void onData(DataExp exp)
  {
    throw new JAXBAssertionError();
  }
  
  public void onAnyString() {}
  
  public final void onValue(ValueExp exp)
  {
    this.context.currentPass.onValue(exp);
  }
  
  public Object onSuper(SuperClassItem exp)
  {
    if (this.context.currentPass == this.context.skipPass) {
      return null;
    }
    getBlock(true).invoke(JExpr._super(), "serialize" + this.context.currentPass.getName()).arg(this.context.$serializer);
    
    return null;
  }
  
  public void onAttribute(AttributeExp exp)
  {
    this.context.currentPass.onAttribute(exp);
  }
  
  public void onElement(ElementExp exp)
  {
    this.context.currentPass.onElement(exp);
  }
  
  public final Object onInterface(InterfaceItem exp)
  {
    this.context.currentSide.onMarshallableObject();
    return null;
  }
  
  public final Object onClass(ClassItem exp)
  {
    this.context.currentSide.onMarshallableObject();
    return null;
  }
  
  public Object onField(FieldItem item)
  {
    this.context.currentSide.onField(item);
    return null;
  }
  
  public final Object onExternal(ExternalItem item)
  {
    this.context.currentPass.onExternal(item);
    return null;
  }
  
  public final Object onPrimitive(PrimitiveItem item)
  {
    this.context.pushNewBlock(new PrintExceptionTryCatchBlockReference(this.context));
    this.context.currentPass.onPrimitive(item);
    this.context.popBlock();
    return null;
  }
  
  public void onOther(OtherExp exp)
  {
    if ((exp instanceof OccurrenceExp)) {
      onOccurence((OccurrenceExp)exp);
    } else {
      super.onOther(exp);
    }
  }
  
  public void onOccurence(OccurrenceExp exp)
  {
    _onOneOrMore(exp.itemExp);
  }
  
  protected final JBlock getBlock(boolean create)
  {
    return this.context.getCurrentBlock().get(create);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\marshaller\Builder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */