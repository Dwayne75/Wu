package com.sun.tools.xjc.generator.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.FieldUse;
import java.util.ArrayList;

public final class ConstFieldRenderer
  implements FieldRenderer
{
  private final JCodeModel codeModel;
  private boolean isCollection = false;
  private final FieldUse use;
  private JFieldVar $ref;
  private int count;
  public static final FieldRendererFactory theFactory = new ConstFieldRenderer.1();
  
  public ConstFieldRenderer(ClassContext context, FieldUse _use)
  {
    this.use = _use;
    this.codeModel = this.use.codeModel;
    
    JExpression initializer = calcInitializer();
    
    this.$ref = context.ref.field(25, this.isCollection ? getType().array() : getType(), this.use.name, initializer);
    
    this.$ref.javadoc().appendComment(this.use.getJavadoc());
  }
  
  public void generate() {}
  
  public JBlock getOnSetEventHandler()
  {
    return JBlock.dummyInstance;
  }
  
  public void toArray(JBlock block, JExpression $array)
  {
    if (this.isCollection) {
      block.add(this.codeModel.ref(System.class).staticInvoke("arraycopy").arg(this.$ref).arg(JExpr.lit(0)).arg($array).arg(JExpr.lit(0)).arg(this.$ref.ref("length")));
    } else {
      block.assign($array.component(JExpr.lit(0)), this.$ref);
    }
  }
  
  public void unsetValues(JBlock body) {}
  
  public JExpression hasSetValue()
  {
    return null;
  }
  
  public JExpression getValue()
  {
    return this.$ref;
  }
  
  public JClass getValueType()
  {
    if (this.isCollection) {
      return getType().array();
    }
    if (getType().isPrimitive()) {
      return ((JPrimitiveType)getType()).getWrapperClass();
    }
    return (JClass)getType();
  }
  
  private JType getType()
  {
    return this.use.type;
  }
  
  public FieldUse getFieldUse()
  {
    return this.use;
  }
  
  public void setter(JBlock body, JExpression newValue) {}
  
  public JExpression ifCountEqual(int i)
  {
    if (i == this.count) {
      return JExpr.TRUE;
    }
    return JExpr.FALSE;
  }
  
  public JExpression ifCountGte(int i)
  {
    if (i <= this.count) {
      return JExpr.TRUE;
    }
    return JExpr.FALSE;
  }
  
  public JExpression ifCountLte(int i)
  {
    if (i >= this.count) {
      return JExpr.TRUE;
    }
    return JExpr.FALSE;
  }
  
  public JExpression count()
  {
    return JExpr.lit(this.count);
  }
  
  private JExpression calcInitializer()
  {
    FieldItem[] items = this.use.getItems();
    ArrayList result = new ArrayList();
    
    items[0].exp.visit(new ConstFieldRenderer.2(this, result));
    
    this.count = result.size();
    if (!this.isCollection) {
      return (JExpression)result.get(0);
    }
    JInvocation inv = JExpr._new(getType().array());
    for (int i = 0; i < result.size(); i++) {
      inv.arg((JExpression)result.get(i));
    }
    return inv;
  }
  
  public FieldMarshallerGenerator createMarshaller(JBlock block, String uniqueId)
  {
    if (!this.isCollection) {
      return new ConstFieldRenderer.SingleFMGImpl(this, null);
    }
    JVar $idx = block.decl(this.codeModel.INT, "idx" + uniqueId, JExpr.lit(0));
    
    return new ConstFieldRenderer.CollectionFMGImpl(this, $idx);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\field\ConstFieldRenderer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */