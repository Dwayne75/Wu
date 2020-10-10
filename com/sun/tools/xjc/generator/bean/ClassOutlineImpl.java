package com.sun.tools.xjc.generator.bean;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.ClassOutline;
import java.util.Set;

public final class ClassOutlineImpl
  extends ClassOutline
{
  private final BeanGenerator _parent;
  
  public MethodWriter createMethodWriter()
  {
    return this._parent.getModel().strategy.createMethodWriter(this);
  }
  
  public PackageOutlineImpl _package()
  {
    return (PackageOutlineImpl)super._package();
  }
  
  ClassOutlineImpl(BeanGenerator _parent, CClassInfo _target, JDefinedClass exposedClass, JDefinedClass _implClass, JClass _implRef)
  {
    super(_target, exposedClass, _implRef, _implClass);
    this._parent = _parent;
    _package().classes.add(this);
  }
  
  public BeanGenerator parent()
  {
    return this._parent;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\ClassOutlineImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */