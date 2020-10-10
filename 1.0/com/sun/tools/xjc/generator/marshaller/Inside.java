package com.sun.tools.xjc.generator.marshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCast;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.LookupTable;
import com.sun.tools.xjc.generator.LookupTableBuilder;
import com.sun.tools.xjc.generator.LookupTableUse;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.tools.xjc.grammar.util.TypeItemCollector;
import com.sun.tools.xjc.runtime.Util;
import com.sun.tools.xjc.runtime.ValidatableObject;
import com.sun.xml.bind.JAXBObject;
import com.sun.xml.bind.ProxyGroup;
import javax.xml.bind.Element;

final class Inside
  extends AbstractSideImpl
{
  public Inside(Context _context)
  {
    super(_context);
  }
  
  private boolean isImplentingElement(TypeItem t)
  {
    JType jt = t.getType();
    if (jt.isPrimitive()) {
      return false;
    }
    JClass jc = (JClass)jt;
    
    return this.context.codeModel.ref(Element.class).isAssignableFrom(jc);
  }
  
  private boolean tryOptimizedChoice1(Expression[] children, TypeItem[][] types)
  {
    FieldMarshallerGenerator fmg = this.context.getCurrentFieldMarshaller();
    
    Expression rest = Expression.nullSet;
    int count = 0;
    for (int i = 0; i < children.length; i++)
    {
      if (types[i].length != 1) {
        return false;
      }
      if (isImplentingElement(types[i][0]))
      {
        if (!(children[i] instanceof ClassItem)) {
          return false;
        }
        count++;
      }
      else
      {
        rest = this.context.pool.createChoice(rest, children[i]);
      }
    }
    if (count == 0) {
      return false;
    }
    if (rest == Expression.nullSet)
    {
      onMarshallableObject();
    }
    else
    {
      IfThenElseBlockReference ifb = new IfThenElseBlockReference(this.context, fmg.peek(false)._instanceof(this.context.codeModel.ref(Element.class)));
      
      this.context.pushNewBlock(ifb.createThenProvider());
      onMarshallableObject();
      this.context.popBlock();
      
      this.context.pushNewBlock(ifb.createElseProvider());
      this.context.build(rest);
      this.context.popBlock();
    }
    return true;
  }
  
  private boolean tryOptimizedChoice2(ChoiceExp exp, Expression[] children)
  {
    LookupTableUse tableUse = this.context.genContext.getLookupTableBuilder().buildTable(exp);
    if (tableUse == null) {
      return false;
    }
    NestedIfBlockProvider nib = null;
    FieldMarshallerGenerator fmg = this.context.getCurrentFieldMarshaller();
    if (tableUse.anomaly != null)
    {
      if (!(tableUse.anomaly instanceof ClassItem)) {
        return false;
      }
      JClass vo = this.context.getRuntime(ValidatableObject.class);
      JExpression test = JExpr.cast(vo, this.context.codeModel.ref(ProxyGroup.class).staticInvoke("blindWrap").arg(fmg.peek(false)).arg(vo.dotclass()).arg(JExpr._null())).invoke("getPrimaryInterface");
      
      ClassItem ancls = (ClassItem)tableUse.anomaly;
      nib = new NestedIfBlockProvider(this.context);
      nib.startBlock(test.ne(ancls.getTypeAsDefined().dotclass()));
    }
    if (this.context.currentPass != this.context.skipPass) {
      if (this.context.currentPass == this.context.uriPass)
      {
        getBlock(true).invoke(this.context.$serializer.invoke("getNamespaceContext"), "declareNamespace").arg(JExpr.lit(tableUse.switchAttName.namespaceURI)).arg(JExpr._null()).arg(JExpr.FALSE);
        
        tableUse.table.declareNamespace(this.context.getCurrentBlock(), fmg.peek(false), this.context);
      }
      else if (this.context.currentPass != this.context.bodyPass)
      {
        if (this.context.currentPass == this.context.attPass)
        {
          JBlock block = getBlock(true);
          
          block.invoke(this.context.$serializer, "startAttribute").arg(JExpr.lit(tableUse.switchAttName.namespaceURI)).arg(JExpr.lit(tableUse.switchAttName.localName));
          
          block.invoke(this.context.$serializer, "text").arg(tableUse.table.reverseLookup(fmg.peek(false), this.context)).arg(JExpr.lit(fmg.owner().getFieldUse().name));
          
          block.invoke(this.context.$serializer, "endAttribute");
        }
        else
        {
          _assert(false);
        }
      }
    }
    if (nib != null) {
      nib.end();
    }
    onMarshallableObject();
    
    return true;
  }
  
  public void onChoice(ChoiceExp exp)
  {
    FieldMarshallerGenerator fmg = this.context.getCurrentFieldMarshaller();
    
    Expression[] children = exp.getChildren();
    TypeItem[][] types = new TypeItem[children.length][];
    for (int i = 0; i < children.length; i++) {
      types[i] = TypeItemCollector.collect(children[i]);
    }
    if (tryOptimizedChoice1(children, types)) {
      return;
    }
    if (tryOptimizedChoice2(exp, children)) {
      return;
    }
    NestedIfBlockProvider nib = new NestedIfBlockProvider(this.context);
    for (int i = 0; i < children.length; i++)
    {
      if (types[i].length == 0)
      {
        nib.startBlock(fmg.hasMore().not());
      }
      else
      {
        JExpression testExp = null;
        for (int j = 0; j < types[i].length; j++)
        {
          JType t = types[i][j].getType();
          JExpression e = instanceOf(fmg.peek(false), t);
          if (testExp == null) {
            testExp = e;
          } else {
            testExp = testExp.cor(e);
          }
        }
        nib.startBlock(testExp);
      }
      this.context.build(children[i]);
    }
    nib.startElse();
    if (getBlock(false) != null) {
      getBlock(false).staticInvoke(this.context.getRuntime(Util.class), "handleTypeMismatchError").arg(this.context.$serializer).arg(JExpr._this()).arg(JExpr.lit(fmg.owner().getFieldUse().name)).arg(fmg.peek(false));
    }
    nib.end();
  }
  
  public void onZeroOrMore(Expression exp)
  {
    JExpression expr = this.context.getCurrentFieldMarshaller().hasMore();
    
    this.context.pushNewBlock(createWhileBlock(this.context.getCurrentBlock(), expr));
    
    this.context.build(exp);
    this.context.popBlock();
  }
  
  public void onMarshallableObject()
  {
    FieldMarshallerGenerator fm = this.context.getCurrentFieldMarshaller();
    if (this.context.currentPass == this.context.skipPass)
    {
      fm.increment(this.context.getCurrentBlock());
      return;
    }
    JClass joRef = this.context.codeModel.ref(JAXBObject.class);
    
    getBlock(true).invoke(this.context.$serializer, "childAs" + this.context.currentPass.getName()).arg(JExpr.cast(joRef, fm.peek(true))).arg(JExpr.lit(fm.owner().getFieldUse().name));
  }
  
  public void onField(FieldItem item)
  {
    _assert(false);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\marshaller\Inside.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */