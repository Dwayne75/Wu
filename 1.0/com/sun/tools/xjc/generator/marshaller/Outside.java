package com.sun.tools.xjc.generator.marshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.util.ExpressionFinder;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.tools.xjc.grammar.util.FieldItemCollector;
import com.sun.tools.xjc.grammar.util.FieldMultiplicityCounter;
import com.sun.tools.xjc.grammar.util.Multiplicity;
import com.sun.tools.xjc.runtime.Util;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.bind.JAXBObject;

class Outside
  extends AbstractSideImpl
{
  protected Outside(Context _context)
  {
    super(_context);
  }
  
  private static final ExpressionFinder isNonOptimizable = new Outside.NonOptimizabilityChecker(null);
  
  public void onChoice(ChoiceExp exp)
  {
    Expression[] children = exp.getChildren();
    
    NestedIfBlockProvider nib = new NestedIfBlockProvider(this.context);
    
    Expression defaultBranch = null;
    
    boolean strong = true;
    if (this.context.inOneOrMore) {
      strong = false;
    }
    FieldItem[] allFi = FieldItemCollector.collect(exp);
    for (int i = 0; i < children.length; i++)
    {
      Expression e = children[i];
      FieldItem[] fi = FieldItemCollector.collect(e);
      if (fi.length == 0)
      {
        if (defaultBranch == null) {
          defaultBranch = children[i];
        }
      }
      else
      {
        nib.startBlock(strong ? createStrongTest(children[i], allFi) : createWeakTest(fi));
        
        this.context.build(children[i]);
      }
    }
    if (defaultBranch != null)
    {
      nib.startElse();
      this.context.build(defaultBranch);
    }
    nib.end();
  }
  
  public void onZeroOrMore(Expression exp)
  {
    JExpression expr = createWeakTest(FieldItemCollector.collect(exp));
    if (expr == null)
    {
      this.context.build(exp);
      return;
    }
    this.context.pushNewBlock(createWhileBlock(this.context.getCurrentBlock(), expr));
    
    this.context.build(exp);
    this.context.popBlock();
  }
  
  public void onMarshallableObject()
  {
    _assert(false);
  }
  
  public void onField(FieldItem item)
  {
    FieldMarshallerGenerator fmg = this.context.getMarshaller(item);
    if (fmg == null) {
      return;
    }
    this.context.pushFieldItem(item);
    if (item.exp.visit(isNonOptimizable))
    {
      this.context.build(item.exp);
    }
    else if (item.multiplicity.max == null)
    {
      this.context.pushNewBlock(createWhileBlock(this.context.getCurrentBlock(), fmg.hasMore()));
      
      onTypeItem(item);
      this.context.popBlock();
    }
    else
    {
      for (int i = 0; i < item.multiplicity.min; i++) {
        onTypeItem(item);
      }
      int repeatCount = item.multiplicity.max.intValue() - item.multiplicity.min;
      if (repeatCount > 0)
      {
        BlockReference parent = this.context.getCurrentBlock();
        this.context.pushNewBlock(new Outside.1(this, parent, repeatCount, fmg));
        
        onTypeItem(item);
        this.context.popBlock();
      }
    }
    this.context.popFieldItem(item);
  }
  
  private void onTypeItem(FieldItem parent)
  {
    TypeItem[] types = parent.listTypes();
    TypeItem.sort(types);
    
    boolean haveSerializableObject = false;
    boolean haveOtherObject = false;
    for (int i = 0; i < types.length; i++) {
      if ((types[i] instanceof PrimitiveItem)) {
        haveOtherObject = true;
      } else if ((types[i] instanceof InterfaceItem)) {
        haveSerializableObject = true;
      } else if ((types[i] instanceof ClassItem)) {
        haveSerializableObject = true;
      } else if ((types[i] instanceof ExternalItem)) {
        haveOtherObject = true;
      } else {
        throw new JAXBAssertionError();
      }
    }
    if ((haveSerializableObject) && (!haveOtherObject))
    {
      this.context.currentSide.onMarshallableObject();
      return;
    }
    if ((!haveSerializableObject) && (types.length == 1))
    {
      this.context.build(types[0]);
      return;
    }
    JBlock block = getBlock(true).block();
    this.context.pushNewBlock(block);
    
    JCodeModel codeModel = this.context.codeModel;
    
    FieldMarshallerGenerator fmg = this.context.getCurrentFieldMarshaller();
    
    JVar $o = block.decl(codeModel.ref(Object.class), "o", fmg.peek(false));
    
    NestedIfBlockProvider nib = new NestedIfBlockProvider(this.context);
    if (haveSerializableObject)
    {
      nib.startBlock($o._instanceof(codeModel.ref(JAXBObject.class)));
      this.context.currentSide.onMarshallableObject();
    }
    for (int i = 0; i < types.length; i++) {
      if (((types[i] instanceof PrimitiveItem)) || ((types[i] instanceof ExternalItem)))
      {
        nib.startBlock(instanceOf($o, types[i].getType()));
        this.context.build(types[i]);
      }
    }
    nib.startElse();
    if (getBlock(false) != null) {
      getBlock(false).staticInvoke(this.context.getRuntime(Util.class), "handleTypeMismatchError").arg(this.context.$serializer).arg(JExpr._this()).arg(JExpr.lit(fmg.owner().getFieldUse().name)).arg($o);
    }
    nib.end();
    this.context.popBlock();
  }
  
  private JExpression createWeakTest(FieldItem[] fi)
  {
    JExpression expr = JExpr.FALSE;
    for (int i = 0; i < fi.length; i++)
    {
      FieldMarshallerGenerator fmg = this.context.getMarshaller(fi[i]);
      if (fmg != null) {
        expr = expr.cor(fmg.hasMore());
      }
    }
    return expr;
  }
  
  private JExpression createStrongTest(Expression branch, FieldItem[] fi)
  {
    JExpression expr = JExpr.TRUE;
    for (int i = 0; i < fi.length; i++)
    {
      FieldRenderer fr = this.context.getMarshaller(fi[i]).owner();
      
      Multiplicity m = FieldMultiplicityCounter.count(branch, fi[i]);
      
      JExpression e = JExpr.TRUE;JExpression f = JExpr.TRUE;
      if ((m.max != null) && (m.min == m.max.intValue()))
      {
        e = fr.ifCountEqual(m.min);
      }
      else
      {
        if (m.min != 0) {
          e = fr.ifCountGte(m.min);
        }
        if (m.max != null) {
          f = fr.ifCountLte(m.max.intValue());
        }
      }
      expr = expr.cand(e).cand(f);
    }
    return expr;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\marshaller\Outside.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */