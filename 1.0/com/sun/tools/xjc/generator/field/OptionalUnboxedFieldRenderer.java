package com.sun.tools.xjc.generator.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.MethodWriter;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.grammar.DefaultValue;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.reader.NameConverter;

public class OptionalUnboxedFieldRenderer
  extends AbstractFieldRendererWithVar
{
  private JVar $has_flag;
  private JBlock onSetEvent;
  
  public OptionalUnboxedFieldRenderer(ClassContext context, FieldUse fu)
  {
    super(context, fu);
  }
  
  protected JFieldVar generateField()
  {
    this.$has_flag = this.context.implClass.field(2, this.codeModel.BOOLEAN, "has_" + this.fu.name);
    return generateField(this.fu.type);
  }
  
  public JClass getValueType()
  {
    return ((JPrimitiveType)this.fu.type).getWrapperClass();
  }
  
  public JExpression getValue()
  {
    return ((JPrimitiveType)this.fu.type).wrap(ref());
  }
  
  public void generateAccessors()
  {
    JMethod $get = this.writer.declareMethod(this.fu.type, (this.fu.type == this.codeModel.BOOLEAN ? "is" : "get") + this.fu.name);
    
    String javadoc = this.fu.getJavadoc();
    if (javadoc.length() == 0) {
      javadoc = Messages.format("SingleFieldRenderer.DefaultGetterJavadoc", NameConverter.standard.toVariableName(this.fu.name));
    }
    this.writer.javadoc().appendComment(javadoc);
    
    DefaultValue[] defaultValues = this.fu.getDefaultValues();
    if (defaultValues == null)
    {
      $get.body()._return(ref());
    }
    else
    {
      _assert(defaultValues.length == 1);
      
      JConditional cond = $get.body()._if(this.$has_flag.not());
      
      cond._then()._return(defaultValues[0].generateConstant());
      cond._else()._return(ref());
    }
    JMethod $set = this.writer.declareMethod(this.codeModel.VOID, "set" + this.fu.name);
    JVar $value = this.writer.addParameter(this.fu.type, "value");
    JBlock body = $set.body();
    body.assign(ref(), $value);
    body.assign(this.$has_flag, JExpr.TRUE);
    this.onSetEvent = body;
    javadoc = this.fu.getJavadoc();
    if (javadoc.length() == 0) {
      javadoc = Messages.format("SingleFieldRenderer.DefaultSetterJavadoc", NameConverter.standard.toVariableName(this.fu.name));
    }
    this.writer.javadoc().appendComment(javadoc);
  }
  
  public void toArray(JBlock block, JExpression $array)
  {
    block.assign($array.component(JExpr.lit(0)), ref());
  }
  
  public void unsetValues(JBlock body)
  {
    body.assign(this.$has_flag, JExpr.FALSE);
  }
  
  public JExpression hasSetValue()
  {
    return this.$has_flag;
  }
  
  public JBlock getOnSetEventHandler()
  {
    return this.onSetEvent;
  }
  
  public JExpression ifCountEqual(int i)
  {
    switch (i)
    {
    case 0: 
      return this.$has_flag.not();
    case 1: 
      return this.$has_flag;
    }
    return JExpr.FALSE;
  }
  
  public JExpression ifCountGte(int i)
  {
    if (i == 1) {
      return this.$has_flag;
    }
    return JExpr.FALSE;
  }
  
  public JExpression ifCountLte(int i)
  {
    if (i == 0) {
      return this.$has_flag.not();
    }
    return JExpr.TRUE;
  }
  
  public JExpression count()
  {
    return JOp.cond(this.$has_flag, JExpr.lit(1), JExpr.lit(0));
  }
  
  public void setter(JBlock block, JExpression newValue)
  {
    block.assign(ref(), newValue);
    block.assign(this.$has_flag, JExpr.TRUE);
  }
  
  public FieldMarshallerGenerator createMarshaller(JBlock block, String uniqueId)
  {
    return new OptionalUnboxedFieldRenderer.1(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\field\OptionalUnboxedFieldRenderer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */