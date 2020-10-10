package com.sun.tools.xjc.generator.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
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
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.xml.bind.JAXBAssertionError;

public class XsiNilFieldRenderer
  extends AbstractFieldRendererWithVar
{
  public static final FieldRendererFactory theFactory = new XsiNilFieldRenderer.1();
  private BlockReference onSetEvent;
  
  public XsiNilFieldRenderer(ClassContext context, FieldUse fu)
  {
    super(context, fu);
  }
  
  protected JFieldVar generateField()
  {
    return generateField(this.codeModel.BOOLEAN);
  }
  
  public void generateAccessors()
  {
    JMethod $get = this.writer.declareMethod(this.codeModel.BOOLEAN, "is" + this.fu.name);
    this.writer.javadoc().appendComment(this.fu.getJavadoc());
    
    $get.body()._return(ref());
    
    JMethod $set = this.writer.declareMethod(this.codeModel.VOID, "set" + this.fu.name);
    JVar $value = this.writer.addParameter(this.codeModel.BOOLEAN, "value");
    JBlock body = $set.body();
    body.assign(ref(), $value);
    
    this.writer.javadoc().appendComment("Passing <code>true</code> will generate xsi:nil in the XML output");
    
    this.writer.javadoc().appendComment(this.fu.getJavadoc());
    
    this.onSetEvent = new XsiNilFieldRenderer.2(this, body, $value);
  }
  
  public JBlock getOnSetEventHandler()
  {
    return this.onSetEvent.get(true);
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
    throw new JAXBAssertionError();
  }
  
  public JExpression hasSetValue()
  {
    return null;
  }
  
  public JExpression getValue()
  {
    return this.codeModel.BOOLEAN.wrap(ref());
  }
  
  public JClass getValueType()
  {
    return this.codeModel.BOOLEAN.getWrapperClass();
  }
  
  public JExpression ifCountEqual(int i)
  {
    switch (i)
    {
    case 0: 
      return ref().not();
    case 1: 
      return ref();
    }
    return JExpr.FALSE;
  }
  
  public JExpression ifCountGte(int i)
  {
    if (i == 1) {
      return ref();
    }
    return JExpr.FALSE;
  }
  
  public JExpression ifCountLte(int i)
  {
    if (i == 0) {
      return ref().not();
    }
    return JExpr.TRUE;
  }
  
  public JExpression count()
  {
    return JOp.cond(ref(), JExpr.lit(1), JExpr.lit(0));
  }
  
  public FieldMarshallerGenerator createMarshaller(JBlock block, String uniqueId)
  {
    return new XsiNilFieldRenderer.3(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\field\XsiNilFieldRenderer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */