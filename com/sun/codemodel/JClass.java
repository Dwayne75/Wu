package com.sun.codemodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class JClass
  extends JType
{
  private final JCodeModel _owner;
  
  protected JClass(JCodeModel _owner)
  {
    this._owner = _owner;
  }
  
  public abstract String name();
  
  public abstract JPackage _package();
  
  public JClass outer()
  {
    return null;
  }
  
  public final JCodeModel owner()
  {
    return this._owner;
  }
  
  public abstract JClass _extends();
  
  public abstract Iterator<JClass> _implements();
  
  public JTypeVar[] typeParams()
  {
    return EMPTY_ARRAY;
  }
  
  protected static final JTypeVar[] EMPTY_ARRAY = new JTypeVar[0];
  private JClass arrayClass;
  
  public abstract boolean isInterface();
  
  public abstract boolean isAbstract();
  
  public JPrimitiveType getPrimitiveType()
  {
    return null;
  }
  
  /**
   * @deprecated
   */
  public JClass boxify()
  {
    return this;
  }
  
  public JType unboxify()
  {
    JPrimitiveType pt = getPrimitiveType();
    return pt == null ? this : pt;
  }
  
  public JClass erasure()
  {
    return this;
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
  
  public final JClass getBaseClass(JClass baseType)
  {
    if (erasure().equals(baseType)) {
      return this;
    }
    JClass b = _extends();
    if (b != null)
    {
      JClass bc = b.getBaseClass(baseType);
      if (bc != null) {
        return bc;
      }
    }
    Iterator<JClass> itfs = _implements();
    while (itfs.hasNext())
    {
      JClass bc = ((JClass)itfs.next()).getBaseClass(baseType);
      if (bc != null) {
        return bc;
      }
    }
    return null;
  }
  
  public final JClass getBaseClass(Class baseType)
  {
    return getBaseClass(owner().ref(baseType));
  }
  
  public JClass array()
  {
    if (this.arrayClass == null) {
      this.arrayClass = new JArrayClass(owner(), this);
    }
    return this.arrayClass;
  }
  
  public JClass narrow(Class clazz)
  {
    return narrow(owner().ref(clazz));
  }
  
  public JClass narrow(Class... clazz)
  {
    JClass[] r = new JClass[clazz.length];
    for (int i = 0; i < clazz.length; i++) {
      r[i] = owner().ref(clazz[i]);
    }
    return narrow(r);
  }
  
  public JClass narrow(JClass clazz)
  {
    return new JNarrowedClass(this, clazz);
  }
  
  public JClass narrow(JClass... clazz)
  {
    return new JNarrowedClass(this, Arrays.asList((Object[])clazz.clone()));
  }
  
  public JClass narrow(List<? extends JClass> clazz)
  {
    return new JNarrowedClass(this, new ArrayList(clazz));
  }
  
  public List<JClass> getTypeParameters()
  {
    return Collections.emptyList();
  }
  
  public final boolean isParameterized()
  {
    return erasure() != this;
  }
  
  public final JClass wildcard()
  {
    return new JTypeWildcard(this);
  }
  
  protected abstract JClass substituteParams(JTypeVar[] paramArrayOfJTypeVar, List<JClass> paramList);
  
  public String toString()
  {
    return getClass().getName() + '(' + name() + ')';
  }
  
  public final JExpression dotclass()
  {
    return JExpr.dotclass(this);
  }
  
  public final JInvocation staticInvoke(JMethod method)
  {
    return new JInvocation(this, method);
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
    return new JFieldRef(this, field);
  }
  
  public void generate(JFormatter f)
  {
    f.t(this);
  }
  
  void printLink(JFormatter f)
  {
    f.p("{@link ").g(this).p('}');
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JClass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */