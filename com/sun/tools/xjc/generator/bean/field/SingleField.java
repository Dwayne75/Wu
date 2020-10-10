package com.sun.tools.xjc.generator.bean.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JCommentPart;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.bean.BeanGenerator;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.generator.bean.MethodWriter;
import com.sun.tools.xjc.model.CDefaultValue;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.FieldAccessor;
import com.sun.xml.bind.api.impl.NameConverter;
import java.util.List;

public class SingleField
  extends AbstractFieldWithVar
{
  protected SingleField(ClassOutlineImpl context, CPropertyInfo prop)
  {
    this(context, prop, false);
  }
  
  protected SingleField(ClassOutlineImpl context, CPropertyInfo prop, boolean forcePrimitiveAccess)
  {
    super(context, prop);
    assert ((!this.exposedType.isPrimitive()) && (!this.implType.isPrimitive()));
    
    createField();
    
    MethodWriter writer = context.createMethodWriter();
    NameConverter nc = context.parent().getModel().getNameConverter();
    
    JExpression defaultValue = null;
    if (prop.defaultValue != null) {
      defaultValue = prop.defaultValue.compute(this.outline.parent());
    }
    JType getterType;
    JType getterType;
    if ((defaultValue != null) || (forcePrimitiveAccess)) {
      getterType = this.exposedType.unboxify();
    } else {
      getterType = this.exposedType;
    }
    JMethod $get = writer.declareMethod(getterType, getGetterMethod());
    String javadoc = prop.javadoc;
    if (javadoc.length() == 0) {
      javadoc = Messages.DEFAULT_GETTER_JAVADOC.format(new Object[] { nc.toVariableName(prop.getName(true)) });
    }
    writer.javadoc().append(javadoc);
    if (defaultValue == null)
    {
      $get.body()._return(ref());
    }
    else
    {
      JConditional cond = $get.body()._if(ref().eq(JExpr._null()));
      cond._then()._return(defaultValue);
      cond._else()._return(ref());
    }
    List<Object> possibleTypes = listPossibleTypes(prop);
    writer.javadoc().addReturn().append("possible object is\n").append(possibleTypes);
    
    JMethod $set = writer.declareMethod(this.codeModel.VOID, "set" + prop.getName(true));
    JType setterType = this.exposedType;
    if (forcePrimitiveAccess) {
      setterType = setterType.unboxify();
    }
    JVar $value = writer.addParameter(setterType, "value");
    JBlock body = $set.body();
    body.assign(JExpr._this().ref(ref()), castToImplType($value));
    
    writer.javadoc().append(Messages.DEFAULT_SETTER_JAVADOC.format(new Object[] { nc.toVariableName(prop.getName(true)) }));
    writer.javadoc().addParam($value).append("allowed object is\n").append(possibleTypes);
  }
  
  public final JType getFieldType()
  {
    return this.implType;
  }
  
  public FieldAccessor create(JExpression targetObject)
  {
    return new Accessor(targetObject);
  }
  
  protected class Accessor
    extends AbstractFieldWithVar.Accessor
  {
    protected Accessor(JExpression $target)
    {
      super($target);
    }
    
    public void unsetValues(JBlock body)
    {
      body.assign(this.$ref, JExpr._null());
    }
    
    public JExpression hasSetValue()
    {
      return this.$ref.ne(JExpr._null());
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\field\SingleField.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */