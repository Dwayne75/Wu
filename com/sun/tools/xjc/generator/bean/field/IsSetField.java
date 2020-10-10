package com.sun.tools.xjc.generator.bean.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.bean.BeanGenerator;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.generator.bean.MethodWriter;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldAccessor;
import com.sun.tools.xjc.outline.FieldOutline;

public class IsSetField
  extends AbstractField
{
  private final FieldOutline core;
  private final boolean generateUnSetMethod;
  private final boolean generateIsSetMethod;
  
  protected IsSetField(ClassOutlineImpl outline, CPropertyInfo prop, FieldOutline core, boolean unsetMethod, boolean issetMethod)
  {
    super(outline, prop);
    this.core = core;
    this.generateIsSetMethod = issetMethod;
    this.generateUnSetMethod = unsetMethod;
    
    generate(outline, prop);
  }
  
  private void generate(ClassOutlineImpl outline, CPropertyInfo prop)
  {
    MethodWriter writer = outline.createMethodWriter();
    
    JCodeModel codeModel = outline.parent().getCodeModel();
    
    FieldAccessor acc = this.core.create(JExpr._this());
    if (this.generateIsSetMethod)
    {
      JExpression hasSetValue = acc.hasSetValue();
      if (hasSetValue == null) {
        throw new UnsupportedOperationException();
      }
      writer.declareMethod(codeModel.BOOLEAN, "isSet" + this.prop.getName(true)).body()._return(hasSetValue);
    }
    if (this.generateUnSetMethod) {
      acc.unsetValues(writer.declareMethod(codeModel.VOID, "unset" + this.prop.getName(true)).body());
    }
  }
  
  public JType getRawType()
  {
    return this.core.getRawType();
  }
  
  public FieldAccessor create(JExpression targetObject)
  {
    return new Accessor(targetObject);
  }
  
  private class Accessor
    extends AbstractField.Accessor
  {
    private final FieldAccessor core;
    
    Accessor(JExpression $target)
    {
      super($target);
      this.core = IsSetField.this.core.create($target);
    }
    
    public void unsetValues(JBlock body)
    {
      this.core.unsetValues(body);
    }
    
    public JExpression hasSetValue()
    {
      return this.core.hasSetValue();
    }
    
    public void toRawValue(JBlock block, JVar $var)
    {
      this.core.toRawValue(block, $var);
    }
    
    public void fromRawValue(JBlock block, String uniqueName, JExpression $var)
    {
      this.core.fromRawValue(block, uniqueName, $var);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\field\IsSetField.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */