package com.sun.tools.xjc.generator.bean.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CPropertyInfo;

abstract class AbstractFieldWithVar
  extends AbstractField
{
  private JFieldVar field;
  
  AbstractFieldWithVar(ClassOutlineImpl outline, CPropertyInfo prop)
  {
    super(outline, prop);
  }
  
  protected final void createField()
  {
    this.field = this.outline.implClass.field(2, getFieldType(), this.prop.getName(false));
    
    annotate(this.field);
  }
  
  protected String getGetterMethod()
  {
    return (getFieldType().boxify().getPrimitiveType() == this.codeModel.BOOLEAN ? "is" : "get") + this.prop.getName(true);
  }
  
  protected abstract JType getFieldType();
  
  protected JFieldVar ref()
  {
    return this.field;
  }
  
  public final JType getRawType()
  {
    return this.exposedType;
  }
  
  protected abstract class Accessor
    extends AbstractField.Accessor
  {
    protected final JFieldRef $ref;
    
    protected Accessor(JExpression $target)
    {
      super($target);
      this.$ref = $target.ref(AbstractFieldWithVar.this.ref());
    }
    
    public final void toRawValue(JBlock block, JVar $var)
    {
      block.assign($var, this.$target.invoke(AbstractFieldWithVar.this.getGetterMethod()));
    }
    
    public final void fromRawValue(JBlock block, String uniqueName, JExpression $var)
    {
      block.invoke(this.$target, "set" + AbstractFieldWithVar.this.prop.getName(true)).arg($var);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\field\AbstractFieldWithVar.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */