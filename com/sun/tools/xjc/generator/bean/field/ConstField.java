package com.sun.tools.xjc.generator.bean.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CDefaultValue;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldAccessor;

final class ConstField
  extends AbstractField
{
  private final JFieldVar $ref;
  
  ConstField(ClassOutlineImpl outline, CPropertyInfo prop)
  {
    super(outline, prop);
    
    assert (!prop.isCollection());
    
    JPrimitiveType ptype = this.implType.boxify().getPrimitiveType();
    
    JExpression defaultValue = null;
    if (prop.defaultValue != null) {
      defaultValue = prop.defaultValue.compute(outline.parent());
    }
    this.$ref = outline.ref.field(25, ptype != null ? ptype : this.implType, prop.getName(true), defaultValue);
    
    this.$ref.javadoc().append(prop.javadoc);
    
    annotate(this.$ref);
  }
  
  public JType getRawType()
  {
    return this.exposedType;
  }
  
  public FieldAccessor create(JExpression target)
  {
    return new Accessor(target);
  }
  
  private class Accessor
    extends AbstractField.Accessor
  {
    Accessor(JExpression $target)
    {
      super($target);
    }
    
    public void unsetValues(JBlock body) {}
    
    public JExpression hasSetValue()
    {
      return null;
    }
    
    public void toRawValue(JBlock block, JVar $var)
    {
      throw new UnsupportedOperationException();
    }
    
    public void fromRawValue(JBlock block, String uniqueName, JExpression $var)
    {
      throw new UnsupportedOperationException();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\field\ConstField.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */