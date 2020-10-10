package com.sun.tools.xjc.generator.bean;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.ElementOutline;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

final class ElementOutlineImpl
  extends ElementOutline
{
  private final BeanGenerator parent;
  
  public BeanGenerator parent()
  {
    return this.parent;
  }
  
  ElementOutlineImpl(BeanGenerator parent, CElementInfo ei)
  {
    super(ei, parent.getClassFactory().createClass(parent.getContainer(ei.parent, Aspect.EXPOSED), ei.shortName(), ei.getLocator()));
    
    this.parent = parent;
    parent.elements.put(ei, this);
    
    JCodeModel cm = parent.getCodeModel();
    
    this.implClass._extends(cm.ref(JAXBElement.class).narrow(this.target.getContentInMemoryType().toType(parent, Aspect.EXPOSED).boxify()));
    if (ei.hasClass())
    {
      JType implType = ei.getContentInMemoryType().toType(parent, Aspect.IMPLEMENTATION);
      JExpression declaredType = JExpr.cast(cm.ref(Class.class), implType.boxify().dotclass());
      JClass scope = null;
      if (ei.getScope() != null) {
        scope = parent.getClazz(ei.getScope()).implRef;
      }
      JExpression scopeClass = scope == null ? JExpr._null() : scope.dotclass();
      
      JMethod cons = this.implClass.constructor(1);
      cons.body().invoke("super").arg(this.implClass.field(26, QName.class, "NAME", createQName(cm, ei.getElementName()))).arg(declaredType).arg(scopeClass).arg(cons.param(implType, "value"));
    }
  }
  
  private JInvocation createQName(JCodeModel codeModel, QName name)
  {
    return JExpr._new(codeModel.ref(QName.class)).arg(name.getNamespaceURI()).arg(name.getLocalPart());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\ElementOutlineImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */