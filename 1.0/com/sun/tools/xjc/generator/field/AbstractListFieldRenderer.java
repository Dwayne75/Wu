package com.sun.tools.xjc.generator.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.grammar.DefaultValue;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.xml.bind.util.ListImpl;
import java.util.List;

abstract class AbstractListFieldRenderer
  extends AbstractFieldRenderer
{
  protected JVar $defValues = null;
  private final JClass coreList;
  protected JPrimitiveType primitiveType;
  private JBlock onSetHandler;
  private JExpression newListObjectExp;
  private JFieldVar field;
  private JMethod internalGetter;
  
  protected AbstractListFieldRenderer(ClassContext context, FieldUse fu, JClass coreList)
  {
    super(context, fu);
    this.coreList = coreList;
    if ((fu.type instanceof JPrimitiveType)) {
      this.primitiveType = ((JPrimitiveType)fu.type);
    }
  }
  
  protected final JExpression unbox(JExpression exp)
  {
    if (this.primitiveType == null) {
      return exp;
    }
    return this.primitiveType.unwrap(exp);
  }
  
  protected final JExpression box(JExpression exp)
  {
    if (this.primitiveType == null) {
      return exp;
    }
    return this.primitiveType.wrap(exp);
  }
  
  public JBlock getOnSetEventHandler()
  {
    if (this.onSetHandler != null) {
      return this.onSetHandler;
    }
    JDefinedClass anonymousClass = this.codeModel.newAnonymousClass(this.codeModel.ref(ListImpl.class));
    
    this.newListObjectExp = JExpr._new(anonymousClass).arg(JExpr._new(this.coreList));
    
    JMethod method = anonymousClass.method(1, this.codeModel.VOID, "setModified");
    JVar $f = method.param(this.codeModel.BOOLEAN, "f");
    
    method.body().invoke(JExpr._super(), "setModified").arg($f);
    this.onSetHandler = method.body()._if($f)._then();
    
    return this.onSetHandler;
  }
  
  public JClass getValueType()
  {
    return this.codeModel.ref(List.class);
  }
  
  public final void generate()
  {
    this.field = generateField();
    
    this.internalGetter = this.context.implClass.method(2, ListImpl.class, "_get" + this.fu.name);
    this.internalGetter.body()._if(this.field.eq(JExpr._null()))._then().assign(this.field, this.lazyInitializer);
    
    this.internalGetter.body()._return(this.field);
    
    generateAccessors();
  }
  
  private JExpression lazyInitializer = new AbstractListFieldRenderer.1(this);
  
  protected final JExpression ref(boolean canBeNull)
  {
    if (canBeNull) {
      return this.field;
    }
    return JExpr.invoke(this.internalGetter);
  }
  
  public abstract void generateAccessors();
  
  protected final JFieldVar generateField()
  {
    DefaultValue[] defaultValues = this.fu.getDefaultValues();
    
    JClass list = this.codeModel.ref(ListImpl.class);
    JFieldVar ref = generateField(list);
    this.newListObjectExp = JExpr._new(list).arg(JExpr._new(this.coreList));
    if (defaultValues != null)
    {
      JType arrayType = this.fu.type.array();
      JInvocation initializer;
      this.$defValues = this.context.implClass.field(26, arrayType, this.fu.name + "_defaultValues", initializer = JExpr._new(arrayType));
      for (int i = 0; i < defaultValues.length; i++) {
        initializer.arg(defaultValues[i].generateConstant());
      }
    }
    return ref;
  }
  
  public void setter(JBlock body, JExpression newValue)
  {
    if (this.primitiveType != null) {
      newValue = this.primitiveType.wrap(newValue);
    }
    body.invoke(ref(false), "add").arg(newValue);
  }
  
  public void toArray(JBlock block, JExpression $array)
  {
    block = block._if(this.field.ne(JExpr._null()))._then();
    if (this.primitiveType == null)
    {
      block.invoke(ref(true), "toArray").arg($array);
    }
    else
    {
      JForLoop $for = block._for();
      JVar $idx = $for.init(this.codeModel.INT, "q" + hashCode(), count().minus(JExpr.lit(1)));
      $for.test($idx.gte(JExpr.lit(0)));
      $for.update($idx.decr());
      
      $for.body().assign($array.component($idx), this.primitiveType.unwrap(JExpr.cast(this.primitiveType.getWrapperClass(), ref(true).invoke("get").arg($idx))));
    }
  }
  
  public JExpression count()
  {
    return JOp.cond(this.field.eq(JExpr._null()), JExpr.lit(0), this.field.invoke("size"));
  }
  
  public JExpression ifCountEqual(int i)
  {
    return count().eq(JExpr.lit(i));
  }
  
  public JExpression ifCountGte(int i)
  {
    return count().gte(JExpr.lit(i));
  }
  
  public JExpression ifCountLte(int i)
  {
    return count().lte(JExpr.lit(i));
  }
  
  public FieldMarshallerGenerator createMarshaller(JBlock block, String uniqueId)
  {
    JVar $idx = block.decl(this.codeModel.INT, "idx" + uniqueId, JExpr.lit(0));
    
    JVar $len = block.decl(8, this.codeModel.INT, "len" + uniqueId, this.$defValues != null ? JOp.cond(this.field.ne(JExpr._null()).cand(this.field.invoke("isModified")), this.field.invoke("size"), JExpr.lit(0)) : count());
    
    return new AbstractListFieldRenderer.FMGImpl(this, $idx, $len);
  }
  
  public void unsetValues(JBlock body)
  {
    body = body._if(this.field.ne(JExpr._null()))._then();
    
    body.invoke(this.field, "clear");
    
    body.invoke(this.field, "setModified").arg(JExpr.FALSE);
  }
  
  public JExpression hasSetValue()
  {
    return JOp.cond(this.field.eq(JExpr._null()), JExpr.FALSE, this.field.invoke("isModified"));
  }
  
  public JExpression getValue()
  {
    return ref(false);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\field\AbstractListFieldRenderer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */