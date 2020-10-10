package com.sun.tools.xjc.generator.bean.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CPropertyInfo;
import java.util.List;

abstract class AbstractListField
  extends AbstractField
{
  protected JFieldVar field;
  private JMethod internalGetter;
  protected final JPrimitiveType primitiveType;
  protected final JClass listT = this.codeModel.ref(List.class).narrow(this.exposedType.boxify());
  private final boolean eagerInstanciation;
  
  protected AbstractListField(ClassOutlineImpl outline, CPropertyInfo prop, boolean eagerInstanciation)
  {
    super(outline, prop);
    this.eagerInstanciation = eagerInstanciation;
    if ((this.implType instanceof JPrimitiveType))
    {
      assert (this.implType == this.exposedType);
      this.primitiveType = ((JPrimitiveType)this.implType);
    }
    else
    {
      this.primitiveType = null;
    }
  }
  
  protected final void generate()
  {
    this.field = this.outline.implClass.field(2, this.listT, this.prop.getName(false));
    if (this.eagerInstanciation) {
      this.field.init(newCoreList());
    }
    annotate(this.field);
    
    generateAccessors();
  }
  
  private void generateInternalGetter()
  {
    this.internalGetter = this.outline.implClass.method(2, this.listT, "_get" + this.prop.getName(true));
    if (!this.eagerInstanciation) {
      fixNullRef(this.internalGetter.body());
    }
    this.internalGetter.body()._return(this.field);
  }
  
  protected final void fixNullRef(JBlock block)
  {
    block._if(this.field.eq(JExpr._null()))._then().assign(this.field, newCoreList());
  }
  
  public final JType getRawType()
  {
    return this.codeModel.ref(List.class).narrow(this.exposedType.boxify());
  }
  
  private JExpression newCoreList()
  {
    return JExpr._new(getCoreListType());
  }
  
  protected abstract JClass getCoreListType();
  
  protected abstract void generateAccessors();
  
  protected abstract class Accessor
    extends AbstractField.Accessor
  {
    protected final JFieldRef field;
    
    protected Accessor(JExpression $target)
    {
      super($target);
      this.field = $target.ref(AbstractListField.this.field);
    }
    
    protected final JExpression unbox(JExpression exp)
    {
      if (AbstractListField.this.primitiveType == null) {
        return exp;
      }
      return AbstractListField.this.primitiveType.unwrap(exp);
    }
    
    protected final JExpression box(JExpression exp)
    {
      if (AbstractListField.this.primitiveType == null) {
        return exp;
      }
      return AbstractListField.this.primitiveType.wrap(exp);
    }
    
    protected final JExpression ref(boolean canBeNull)
    {
      if (canBeNull) {
        return this.field;
      }
      if (AbstractListField.this.internalGetter == null) {
        AbstractListField.this.generateInternalGetter();
      }
      return this.$target.invoke(AbstractListField.this.internalGetter);
    }
    
    public JExpression count()
    {
      return JOp.cond(this.field.eq(JExpr._null()), JExpr.lit(0), this.field.invoke("size"));
    }
    
    public void unsetValues(JBlock body)
    {
      body.assign(this.field, JExpr._null());
    }
    
    public JExpression hasSetValue()
    {
      return this.field.ne(JExpr._null()).cand(this.field.invoke("isEmpty").not());
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\field\AbstractListField.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */