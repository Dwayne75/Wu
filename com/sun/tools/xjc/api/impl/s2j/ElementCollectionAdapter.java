package com.sun.tools.xjc.api.impl.s2j;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.FieldAccessor;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import java.util.ArrayList;
import java.util.List;

final class ElementCollectionAdapter
  extends ElementAdapter
{
  public ElementCollectionAdapter(FieldOutline core, CElementInfo ei)
  {
    super(core, ei);
  }
  
  public JType getRawType()
  {
    return codeModel().ref(List.class).narrow(itemType().boxify());
  }
  
  private JType itemType()
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
      JCodeModel cm = ElementCollectionAdapter.this.outline().getCodeModel();
      JClass elementType = ElementCollectionAdapter.this.ei.toType(ElementCollectionAdapter.this.outline(), Aspect.EXPOSED).boxify();
      
      block.assign($var, JExpr._new(cm.ref(ArrayList.class).narrow(ElementCollectionAdapter.this.itemType().boxify())));
      JVar $col = block.decl(ElementCollectionAdapter.this.core.getRawType(), "col" + hashCode());
      this.acc.toRawValue(block, $col);
      JForEach loop = block.forEach(elementType, "v" + hashCode(), $col);
      
      JConditional cond = loop.body()._if(loop.var().eq(JExpr._null()));
      cond._then().invoke($var, "add").arg(JExpr._null());
      cond._else().invoke($var, "add").arg(loop.var().invoke("getValue"));
    }
    
    public void fromRawValue(JBlock block, String uniqueName, JExpression $var)
    {
      JCodeModel cm = ElementCollectionAdapter.this.outline().getCodeModel();
      JClass elementType = ElementCollectionAdapter.this.ei.toType(ElementCollectionAdapter.this.outline(), Aspect.EXPOSED).boxify();
      
      JClass col = cm.ref(ArrayList.class).narrow(elementType);
      JVar $t = block.decl(col, uniqueName + "_col", JExpr._new(col));
      
      JForEach loop = block.forEach(ElementCollectionAdapter.this.itemType(), uniqueName + "_i", $t);
      loop.body().invoke($var, "add").arg(createJAXBElement(loop.var()));
      
      this.acc.fromRawValue(block, uniqueName, $t);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\impl\s2j\ElementCollectionAdapter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */