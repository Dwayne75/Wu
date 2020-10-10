package com.sun.codemodel;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class JDefinedClass
  extends JClass
  implements JDeclaration, JClassContainer, JGenerifiable, JAnnotatable
{
  private String name = null;
  private JMods mods;
  private JClass superClass;
  private final Set<JClass> interfaces = new TreeSet();
  final Map<String, JFieldVar> fields = new LinkedHashMap();
  private JBlock init = null;
  private JDocComment jdoc = null;
  private final List<JMethod> constructors = new ArrayList();
  private final List<JMethod> methods = new ArrayList();
  private Map<String, JDefinedClass> classes;
  private boolean hideFile = false;
  public Object metadata;
  private String directBlock;
  private JClassContainer outer = null;
  private final ClassType classType;
  private final Map<String, JEnumConstant> enumConstantsByName = new LinkedHashMap();
  private List<JAnnotationUse> annotations = null;
  private final JGenerifiableImpl generifiable = new JGenerifiableImpl()
  {
    protected JCodeModel owner()
    {
      return JDefinedClass.this.owner();
    }
  };
  
  JDefinedClass(JClassContainer parent, int mods, String name, ClassType classTypeval)
  {
    this(mods, name, parent, parent.owner(), classTypeval);
  }
  
  JDefinedClass(JCodeModel owner, int mods, String name)
  {
    this(mods, name, null, owner);
  }
  
  private JDefinedClass(int mods, String name, JClassContainer parent, JCodeModel owner)
  {
    this(mods, name, parent, owner, ClassType.CLASS);
  }
  
  private JDefinedClass(int mods, String name, JClassContainer parent, JCodeModel owner, ClassType classTypeVal)
  {
    super(owner);
    if (name != null)
    {
      if (name.trim().length() == 0) {
        throw new IllegalArgumentException("JClass name empty");
      }
      if (!Character.isJavaIdentifierStart(name.charAt(0)))
      {
        String msg = "JClass name " + name + " contains illegal character" + " for beginning of identifier: " + name.charAt(0);
        
        throw new IllegalArgumentException(msg);
      }
      for (int i = 1; i < name.length(); i++) {
        if (!Character.isJavaIdentifierPart(name.charAt(i)))
        {
          String msg = "JClass name " + name + " contains illegal character " + name.charAt(i);
          
          throw new IllegalArgumentException(msg);
        }
      }
    }
    this.classType = classTypeVal;
    if (isInterface()) {
      this.mods = JMods.forInterface(mods);
    } else {
      this.mods = JMods.forClass(mods);
    }
    this.name = name;
    
    this.outer = parent;
  }
  
  public final boolean isAnonymous()
  {
    return this.name == null;
  }
  
  public JDefinedClass _extends(JClass superClass)
  {
    if (this.classType == ClassType.INTERFACE) {
      throw new IllegalArgumentException("unable to set the super class for an interface");
    }
    if (superClass == null) {
      throw new NullPointerException();
    }
    for (JClass o = superClass.outer(); o != null; o = o.outer()) {
      if (this == o) {
        throw new IllegalArgumentException("Illegal class inheritance loop.  Outer class " + this.name + " may not subclass from inner class: " + o.name());
      }
    }
    this.superClass = superClass;
    return this;
  }
  
  public JDefinedClass _extends(Class superClass)
  {
    return _extends(owner().ref(superClass));
  }
  
  public JClass _extends()
  {
    if (this.superClass == null) {
      this.superClass = owner().ref(Object.class);
    }
    return this.superClass;
  }
  
  public JDefinedClass _implements(JClass iface)
  {
    this.interfaces.add(iface);
    return this;
  }
  
  public JDefinedClass _implements(Class iface)
  {
    return _implements(owner().ref(iface));
  }
  
  public Iterator<JClass> _implements()
  {
    return this.interfaces.iterator();
  }
  
  public String name()
  {
    return this.name;
  }
  
  public JEnumConstant enumConstant(String name)
  {
    JEnumConstant ec = (JEnumConstant)this.enumConstantsByName.get(name);
    if (null == ec)
    {
      ec = new JEnumConstant(this, name);
      this.enumConstantsByName.put(name, ec);
    }
    return ec;
  }
  
  public String fullName()
  {
    if ((this.outer instanceof JDefinedClass)) {
      return ((JDefinedClass)this.outer).fullName() + '.' + name();
    }
    JPackage p = _package();
    if (p.isUnnamed()) {
      return name();
    }
    return p.name() + '.' + name();
  }
  
  public String binaryName()
  {
    if ((this.outer instanceof JDefinedClass)) {
      return ((JDefinedClass)this.outer).binaryName() + '$' + name();
    }
    return fullName();
  }
  
  public boolean isInterface()
  {
    return this.classType == ClassType.INTERFACE;
  }
  
  public boolean isAbstract()
  {
    return this.mods.isAbstract();
  }
  
  public JFieldVar field(int mods, JType type, String name)
  {
    return field(mods, type, name, null);
  }
  
  public JFieldVar field(int mods, Class type, String name)
  {
    return field(mods, owner()._ref(type), name);
  }
  
  public JFieldVar field(int mods, JType type, String name, JExpression init)
  {
    JFieldVar f = new JFieldVar(this, JMods.forField(mods), type, name, init);
    if (this.fields.put(name, f) != null) {
      throw new IllegalArgumentException("trying to create the same field twice: " + name);
    }
    return f;
  }
  
  public boolean isAnnotationTypeDeclaration()
  {
    return this.classType == ClassType.ANNOTATION_TYPE_DECL;
  }
  
  public JDefinedClass _annotationTypeDeclaration(String name)
    throws JClassAlreadyExistsException
  {
    return _class(1, name, ClassType.ANNOTATION_TYPE_DECL);
  }
  
  public JDefinedClass _enum(String name)
    throws JClassAlreadyExistsException
  {
    return _class(1, name, ClassType.ENUM);
  }
  
  public JDefinedClass _enum(int mods, String name)
    throws JClassAlreadyExistsException
  {
    return _class(mods, name, ClassType.ENUM);
  }
  
  public ClassType getClassType()
  {
    return this.classType;
  }
  
  public JFieldVar field(int mods, Class type, String name, JExpression init)
  {
    return field(mods, owner()._ref(type), name, init);
  }
  
  public Map<String, JFieldVar> fields()
  {
    return Collections.unmodifiableMap(this.fields);
  }
  
  public void removeField(JFieldVar field)
  {
    if (this.fields.remove(field.name()) != field) {
      throw new IllegalArgumentException();
    }
  }
  
  public JBlock init()
  {
    if (this.init == null) {
      this.init = new JBlock();
    }
    return this.init;
  }
  
  public JMethod constructor(int mods)
  {
    JMethod c = new JMethod(mods, this);
    this.constructors.add(c);
    return c;
  }
  
  public Iterator constructors()
  {
    return this.constructors.iterator();
  }
  
  public JMethod getConstructor(JType[] argTypes)
  {
    for (JMethod m : this.constructors) {
      if (m.hasSignature(argTypes)) {
        return m;
      }
    }
    return null;
  }
  
  public JMethod method(int mods, JType type, String name)
  {
    JMethod m = new JMethod(this, mods, type, name);
    this.methods.add(m);
    return m;
  }
  
  public JMethod method(int mods, Class type, String name)
  {
    return method(mods, owner()._ref(type), name);
  }
  
  public Collection<JMethod> methods()
  {
    return this.methods;
  }
  
  public JMethod getMethod(String name, JType[] argTypes)
  {
    for (JMethod m : this.methods) {
      if (m.name().equals(name)) {
        if (m.hasSignature(argTypes)) {
          return m;
        }
      }
    }
    return null;
  }
  
  public boolean isClass()
  {
    return true;
  }
  
  public boolean isPackage()
  {
    return false;
  }
  
  public JPackage getPackage()
  {
    return parentContainer().getPackage();
  }
  
  public JDefinedClass _class(int mods, String name)
    throws JClassAlreadyExistsException
  {
    return _class(mods, name, ClassType.CLASS);
  }
  
  /**
   * @deprecated
   */
  public JDefinedClass _class(int mods, String name, boolean isInterface)
    throws JClassAlreadyExistsException
  {
    return _class(mods, name, isInterface ? ClassType.INTERFACE : ClassType.CLASS);
  }
  
  public JDefinedClass _class(int mods, String name, ClassType classTypeVal)
    throws JClassAlreadyExistsException
  {
    String NAME;
    String NAME;
    if (JCodeModel.isCaseSensitiveFileSystem) {
      NAME = name.toUpperCase();
    } else {
      NAME = name;
    }
    if (getClasses().containsKey(NAME)) {
      throw new JClassAlreadyExistsException((JDefinedClass)getClasses().get(NAME));
    }
    JDefinedClass c = new JDefinedClass(this, mods, name, classTypeVal);
    getClasses().put(NAME, c);
    return c;
  }
  
  public JDefinedClass _class(String name)
    throws JClassAlreadyExistsException
  {
    return _class(1, name);
  }
  
  public JDefinedClass _interface(int mods, String name)
    throws JClassAlreadyExistsException
  {
    return _class(mods, name, ClassType.INTERFACE);
  }
  
  public JDefinedClass _interface(String name)
    throws JClassAlreadyExistsException
  {
    return _interface(1, name);
  }
  
  public JDocComment javadoc()
  {
    if (this.jdoc == null) {
      this.jdoc = new JDocComment(owner());
    }
    return this.jdoc;
  }
  
  public void hide()
  {
    this.hideFile = true;
  }
  
  public boolean isHidden()
  {
    return this.hideFile;
  }
  
  public final Iterator<JDefinedClass> classes()
  {
    if (this.classes == null) {
      return Collections.emptyList().iterator();
    }
    return this.classes.values().iterator();
  }
  
  private Map<String, JDefinedClass> getClasses()
  {
    if (this.classes == null) {
      this.classes = new TreeMap();
    }
    return this.classes;
  }
  
  public final JClass[] listClasses()
  {
    if (this.classes == null) {
      return new JClass[0];
    }
    return (JClass[])this.classes.values().toArray(new JClass[this.classes.values().size()]);
  }
  
  public JClass outer()
  {
    if (this.outer.isClass()) {
      return (JClass)this.outer;
    }
    return null;
  }
  
  public void declare(JFormatter f)
  {
    if (this.jdoc != null) {
      f.nl().g(this.jdoc);
    }
    if (this.annotations != null) {
      for (JAnnotationUse annotation : this.annotations) {
        f.g(annotation).nl();
      }
    }
    f.g(this.mods).p(this.classType.declarationToken).id(this.name).d(this.generifiable);
    if ((this.superClass != null) && (this.superClass != owner().ref(Object.class))) {
      f.nl().i().p("extends").g(this.superClass).nl().o();
    }
    if (!this.interfaces.isEmpty())
    {
      if (this.superClass == null) {
        f.nl();
      }
      f.i().p(this.classType == ClassType.INTERFACE ? "extends" : "implements");
      f.g(this.interfaces);
      f.nl().o();
    }
    declareBody(f);
  }
  
  protected void declareBody(JFormatter f)
  {
    f.p('{').nl().nl().i();
    boolean first = true;
    if (!this.enumConstantsByName.isEmpty())
    {
      for (JEnumConstant c : this.enumConstantsByName.values())
      {
        if (!first) {
          f.p(',').nl();
        }
        f.d(c);
        first = false;
      }
      f.p(';').nl();
    }
    for (JFieldVar field : this.fields.values()) {
      f.d(field);
    }
    if (this.init != null) {
      f.nl().p("static").s(this.init);
    }
    for (JMethod m : this.constructors) {
      f.nl().d(m);
    }
    for (JMethod m : this.methods) {
      f.nl().d(m);
    }
    if (this.classes != null) {
      for (JDefinedClass dc : this.classes.values()) {
        f.nl().d(dc);
      }
    }
    if (this.directBlock != null) {
      f.p(this.directBlock);
    }
    f.nl().o().p('}').nl();
  }
  
  public void direct(String string)
  {
    if (this.directBlock == null) {
      this.directBlock = string;
    } else {
      this.directBlock += string;
    }
  }
  
  public final JPackage _package()
  {
    JClassContainer p = this.outer;
    while (!(p instanceof JPackage)) {
      p = p.parentContainer();
    }
    return (JPackage)p;
  }
  
  public final JClassContainer parentContainer()
  {
    return this.outer;
  }
  
  public JTypeVar generify(String name)
  {
    return this.generifiable.generify(name);
  }
  
  public JTypeVar generify(String name, Class bound)
  {
    return this.generifiable.generify(name, bound);
  }
  
  public JTypeVar generify(String name, JClass bound)
  {
    return this.generifiable.generify(name, bound);
  }
  
  public JTypeVar[] typeParams()
  {
    return this.generifiable.typeParams();
  }
  
  protected JClass substituteParams(JTypeVar[] variables, List<JClass> bindings)
  {
    return this;
  }
  
  public JAnnotationUse annotate(Class<? extends Annotation> clazz)
  {
    return annotate(owner().ref(clazz));
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
  
  public <W extends JAnnotationWriter> W annotate2(Class<W> clazz)
  {
    return TypedAnnotationWriter.create(clazz, this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JDefinedClass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */