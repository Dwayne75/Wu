package com.sun.tools.xjc.api.impl.s2j;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.FieldAccessor;
import com.sun.tools.xjc.outline.FieldOutline;

final class ElementSingleAdapter
  extends ElementAdapter
{
  public ElementSingleAdapter(FieldOutline core, CElementInfo ei)
  {
    super(core, ei);
  }
  
  public JType getRawType()
  {
    return this.ei.getContentInMemoryType().toType(outline(), Aspect.EXPOSED);
  }
  
  public FieldAccessor create(JExpression targetObject)
  {
    return new FieldAccessorImpl(targetObject);
  }
  
  final class FieldAccessorImpl
    extends ElementAdapter.FieldAccessorImpl
  {
    public FieldAccessorImpl(JExpression target)
    {
      super(target);
    }
    
    public void toRawValue(JBlock block, JVar $var)
    {
      JConditional cond = block._if(this.acc.hasSetValue());
      JVar $v = cond._then().decl(ElementSingleAdapter.this.core.getRawType(), "v" + hashCode());
      this.acc.toRawValue(cond._then(), $v);
      cond._then().assign($var, $v.invoke("getValue"));
      cond._else().assign($var, JExpr._null());
    }
    
    public void fromRawValue(JBlock block, String uniqueName, JExpression $var)
    {
      this.acc.fromRawValue(block, uniqueName, createJAXBElement($var));
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\impl\s2j\ElementSingleAdapter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */