package com.sun.codemodel;

import com.sun.codemodel.util.ClassNameComparator;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class JMethod
  extends JGenerifiableImpl
  implements JDeclaration, JAnnotatable
{
  private JMods mods;
  private JType type = null;
  private String name = null;
  private final List<JVar> params = new ArrayList();
  private Set<JClass> _throws;
  private JBlock body = null;
  private JDefinedClass outer;
  private JDocComment jdoc = null;
  private JVar varParam = null;
  private List<JAnnotationUse> annotations = null;
  
  private boolean isConstructor()
  {
    return this.type == null;
  }
  
  private JExpression defaultValue = null;
  
  JMethod(JDefinedClass outer, int mods, JType type, String name)
  {
    this.mods = JMods.forMethod(mods);
    this.type = type;
    this.name = name;
    this.outer = outer;
  }
  
  JMethod(int mods, JDefinedClass _class)
  {
    this.mods = JMods.forMethod(mods);
    this.type = null;
    this.name = _class.name();
    this.outer = _class;
  }
  
  private Set<JClass> getThrows()
  {
    if (this._throws == null) {
      this._throws = new TreeSet(ClassNameComparator.theInstance);
    }
    return this._throws;
  }
  
  public JMethod _throws(JClass exception)
  {
    getThrows().add(exception);
    return this;
  }
  
  public JMethod _throws(Class exception)
  {
    return _throws(this.outer.owner().ref(exception));
  }
  
  public JVar param(int mods, JType type, String name)
  {
    JVar v = new JVar(JMods.forVar(mods), type, name, null);
    this.params.add(v);
    return v;
  }
  
  public JVar param(JType type, String name)
  {
    return param(0, type, name);
  }
  
  public JVar param(int mods, Class type, String name)
  {
    return param(mods, this.outer.owner()._ref(type), name);
  }
  
  public JVar param(Class type, String name)
  {
    return param(this.outer.owner()._ref(type), name);
  }
  
  public JVar varParam(Class type, String name)
  {
    return varParam(this.outer.owner()._ref(type), name);
  }
  
  public JVar varParam(JType type, String name)
  {
    if (!hasVarArgs())
    {
      this.varParam = new JVar(JMods.forVar(0), type.array(), name, null);
      
      return this.varParam;
    }
    throw new IllegalStateException("Cannot have two varargs in a method,\nCheck if varParam method of JMethod is invoked more than once");
  }
  
  public JAnnotationUse annotate(JClass clazz)
  {
    if (this.annotations == null) {
      this.annotations = new ArrayList();
    }
    JAnnotationUse a = new JAnnotationUse(clazz);
    this.annotations.add(a);
    return a;
  }
  
  public JAnnotationUse annotate(Class<? extends Annotation> clazz)
  {
    return annotate(owner().ref(clazz));
  }
  
  public <W extends JAnnotationWriter> W annotate2(Class<W> clazz)
  {
    return TypedAnnotationWriter.create(clazz, this);
  }
  
  public boolean hasVarArgs()
  {
    return this.varParam != null;
  }
  
  public String name()
  {
    return this.name;
  }
  
  public void name(String n)
  {
    this.name = n;
  }
  
  public JType type()
  {
    return this.type;
  }
  
  public void type(JType t)
  {
    this.type = t;
  }
  
  public JType[] listParamTypes()
  {
    JType[] r = new JType[this.params.size()];
    for (int i = 0; i < r.length; i++) {
      r[i] = ((JVar)this.params.get(i)).type();
    }
    return r;
  }
  
  public JType listVarParamType()
  {
    if (this.varParam != null) {
      return this.varParam.type();
    }
    return null;
  }
  
  public JVar[] listParams()
  {
    return (JVar[])this.params.toArray(new JVar[this.params.size()]);
  }
  
  public JVar listVarParam()
  {
    return this.varParam;
  }
  
  public boolean hasSignature(JType[] argTypes)
  {
    JVar[] p = listParams();
    if (p.length != argTypes.length) {
      return false;
    }
    for (int i = 0; i < p.length; i++) {
      if (!p[i].type().equals(argTypes[i])) {
        return false;
      }
    }
    return true;
  }
  
  public JBlock body()
  {
    if (this.body == null) {
      this.body = new JBlock();
    }
    return this.body;
  }
  
  public void declareDefaultValue(JExpression value)
  {
    this.defaultValue = value;
  }
  
  public JDocComment javadoc()
  {
    if (this.jdoc == null) {
      this.jdoc = new JDocComment(owner());
    }
    return this.jdoc;
  }
  
  public void declare(JFormatter f)
  {
    if (this.jdoc != null) {
      f.g(this.jdoc);
    }
    if (this.annotations != null) {
      for (JAnnotationUse a : this.annotations) {
        f.g(a).nl();
      }
    }
    super.declare(f);
    
    f.g(this.mods);
    if (!isConstructor()) {
      f.g(this.type);
    }
    f.id(this.name).p('(').i();
    
    boolean first = true;
    for (JVar var : this.params)
    {
      if (!first) {
        f.p(',');
      }
      if (var.isAnnotated()) {
        f.nl();
      }
      f.b(var);
      first = false;
    }
    if (hasVarArgs())
    {
      if (!first) {
        f.p(',');
      }
      f.g(this.varParam.type().elementType());
      f.p("... ");
      f.id(this.varParam.name());
    }
    f.o().p(')');
    if ((this._throws != null) && (!this._throws.isEmpty())) {
      f.nl().i().p("throws").g(this._throws).nl().o();
    }
    if (this.defaultValue != null)
    {
      f.p("default ");
      f.g(this.defaultValue);
    }
    if (this.body != null) {
      f.s(this.body);
    } else if ((!this.outer.isInterface()) && (!this.outer.isAnnotationTypeDeclaration()) && (!this.mods.isAbstract()) && (!this.mods.isNative())) {
      f.s(new JBlock());
    } else {
      f.p(';').nl();
    }
  }
  
  public JMods mods()
  {
    return this.mods;
  }
  
  /**
   * @deprecated
   */
  public JMods getMods()
  {
    return this.mods;
  }
  
  protected JCodeModel owner()
  {
    return this.outer.owner();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JMethod.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */