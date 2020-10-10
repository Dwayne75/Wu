package com.sun.codemodel;

import java.util.Iterator;

public abstract class JClass
  extends JType
{
  private final JCodeModel _owner;
  
  protected JClass(JCodeModel _owner)
  {
    this._owner = _owner;
  }
  
  public abstract String name();
  
  public String fullName()
  {
    JPackage p = _package();
    if (p.isUnnamed()) {
      return name();
    }
    return p.name() + '.' + name();
  }
  
  public abstract JPackage _package();
  
  public final JCodeModel owner()
  {
    return this._owner;
  }
  
  public abstract JClass _extends();
  
  public abstract Iterator _implements();
  
  public abstract boolean isInterface();
  
  public JPrimitiveType getPrimitiveType()
  {
    return null;
  }
  
  public final boolean isAssignableFrom(JClass derived)
  {
    if ((derived instanceof JNullType)) {
      return true;
    }
    if (this == derived) {
      return true;
    }
    if (this == _package().owner().ref(Object.class)) {
      return true;
    }
    JClass b = derived._extends();
    if ((b != null) && (isAssignableFrom(b))) {
      return true;
    }
    if (isInterface())
    {
      Iterator itfs = derived._implements();
      while (itfs.hasNext()) {
        if (isAssignableFrom((JClass)itfs.next())) {
          return true;
        }
      }
    }
    return false;
  }
  
  public JClass array()
  {
    return new JArrayClass(owner(), this);
  }
  
  public String toString()
  {
    return getClass().getName() + "(" + name() + ")";
  }
  
  public final JExpression dotclass()
  {
    return JExpr.dotclass(this);
  }
  
  public final JInvocation staticInvoke(JMethod method)
  {
    return staticInvoke(method.name());
  }
  
  public final JInvocation staticInvoke(String method)
  {
    return new JInvocation(this, method);
  }
  
  public final JFieldRef staticRef(String field)
  {
    return new JFieldRef(this, field);
  }
  
  public final JFieldRef staticRef(JVar field)
  {
    return staticRef(field.name());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JClass.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */