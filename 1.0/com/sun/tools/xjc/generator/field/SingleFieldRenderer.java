package com.sun.tools.xjc.generator.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.JavadocBuilder;
import com.sun.tools.xjc.generator.MethodWriter;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.grammar.DefaultValue;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.reader.NameConverter;

public class SingleFieldRenderer
  extends AbstractFieldRendererWithVar
{
  private JBlock onSetEvent;
  
  public SingleFieldRenderer(ClassContext context, FieldUse fu)
  {
    super(context, fu);
    _assert(!fu.type.isPrimitive());
  }
  
  protected JFieldVar generateField()
  {
    return generateField(this.fu.type);
  }
  
  public JClass getValueType()
  {
    return (JClass)this.fu.type;
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
      
      JConditional cond = $get.body()._if(ref().eq(JExpr._null()));
      cond._then()._return(defaultValues[0].generateConstant());
      cond._else()._return(ref());
    }
    this.writer.javadoc().addReturn("possible object is\n" + JavadocBuilder.listPossibleTypes(this.fu));
    
    JMethod $set = this.writer.declareMethod(this.codeModel.VOID, "set" + this.fu.name);
    JVar $value = this.writer.addParameter(this.fu.type, "value");
    JBlock body = $set.body();
    body.assign(ref(), $value);
    this.onSetEvent = body;
    
    javadoc = this.fu.getJavadoc();
    if (javadoc.length() == 0) {
      javadoc = Messages.format("SingleFieldRenderer.DefaultSetterJavadoc", NameConverter.standard.toVariableName(this.fu.name));
    }
    this.writer.javadoc().appendComment(javadoc);
    this.writer.javadoc().addParam($value, "allowed object is\n" + JavadocBuilder.listPossibleTypes(this.fu));
  }
  
  public JBlock getOnSetEventHandler()
  {
    return this.onSetEvent;
  }
  
  public void setter(JBlock block, JExpression newValue)
  {
    block.assign(ref(), newValue);
  }
  
  public void toArray(JBlock block, JExpression $array)
  {
    block.assign($array.component(JExpr.lit(0)), ref());
  }
  
  public void unsetValues(JBlock body)
  {
    body.assign(ref(), JExpr._null());
  }
  
  public JExpression hasSetValue()
  {
    return ref().ne(JExpr._null());
  }
  
  public JExpression getValue()
  {
    return ref();
  }
  
  public JExpression ifCountEqual(int i)
  {
    switch (i)
    {
    case 0: 
      return ref().eq(JExpr._null());
    case 1: 
      return ref().ne(JExpr._null());
    }
    return JExpr.FALSE;
  }
  
  public JExpression ifCountGte(int i)
  {
    if (i == 1) {
      return ref().ne(JExpr._null());
    }
    return JExpr.FALSE;
  }
  
  public JExpression ifCountLte(int i)
  {
    if (i == 0) {
      return ref().eq(JExpr._null());
    }
    return JExpr.TRUE;
  }
  
  public JExpression count()
  {
    return JOp.cond(ref().ne(JExpr._null()), JExpr.lit(1), JExpr.lit(0));
  }
  
  public FieldMarshallerGenerator createMarshaller(JBlock block, String uniqueId)
  {
    return new SingleFieldRenderer.1(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\field\SingleFieldRenderer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */