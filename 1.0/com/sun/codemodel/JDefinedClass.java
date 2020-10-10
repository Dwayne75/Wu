package com.sun.codemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class JDefinedClass
  extends JClass
  implements JDeclaration, JClassContainer
{
  private String name = null;
  private final boolean isInterface;
  private JMods mods;
  private JClass superClass;
  private final List interfaces = new ArrayList();
  private final List fields = new ArrayList();
  private final Map fieldsByName = new HashMap();
  private JBlock init = null;
  private JDocComment jdoc = null;
  private final List constructors = new ArrayList();
  private final List methods = new ArrayList();
  private final Map classes = new TreeMap();
  private final Map upperCaseClassMap;
  private boolean hideFile = false;
  public Object metadata;
  private String directBlock;
  
  JDefinedClass(int mods, String name, boolean isInterface, JCodeModel owner)
  {
    super(owner);
    if (JCodeModel.isCaseSensitiveFileSystem) {
      this.upperCaseClassMap = null;
    } else {
      this.upperCaseClassMap = new HashMap();
    }
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
    this.mods = (isInterface ? JMods.forInterface(mods) : JMods.forClass(mods));
    
    this.name = name;
    this.isInterface = isInterface;
    if (!isInterface) {
      this.superClass = owner().ref(Object.class);
    }
  }
  
  public JDefinedClass _extends(JClass superClass)
  {
    if (isInterface()) {
      throw new IllegalArgumentException("unable to set the super class for an interface");
    }
    if (superClass == null) {
      throw new NullPointerException();
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
  
  public Iterator _implements()
  {
    return this.interfaces.iterator();
  }
  
  public String name()
  {
    return this.name;
  }
  
  public boolean isInterface()
  {
    return this.isInterface;
  }
  
  public JFieldVar field(int mods, JType type, String name)
  {
    return field(mods, type, name, null);
  }
  
  public JFieldVar field(int mods, Class type, String name)
  {
    return field(mods, owner().ref(type), name);
  }
  
  public JFieldVar field(int mods, JType type, String name, JExpression init)
  {
    JFieldVar f = new JFieldVar(JMods.forField(mods), type, name, init);
    this.fields.add(f);
    
    JFieldVar existing = (JFieldVar)this.fieldsByName.get(name);
    if (existing != null) {
      this.fields.remove(existing);
    }
    this.fieldsByName.put(name, f);
    
    return f;
  }
  
  public JFieldVar field(int mods, Class type, String name, JExpression init)
  {
    return field(mods, owner().ref(type), name, init);
  }
  
  public Iterator fields()
  {
    return this.fields.iterator();
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
    for (Iterator itr = this.constructors.iterator(); itr.hasNext();)
    {
      JMethod m = (JMethod)itr.next();
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
    return method(mods, owner().ref(type), name);
  }
  
  public Iterator methods()
  {
    return this.methods.iterator();
  }
  
  public JMethod getMethod(String name, JType[] argTypes)
  {
    for (Iterator itr = this.methods.iterator(); itr.hasNext();)
    {
      JMethod m = (JMethod)itr.next();
      if (m.name().equals(name)) {
        if (m.hasSignature(argTypes)) {
          return m;
        }
      }
    }
    return null;
  }
  
  public JDefinedClass _class(int mods, String name)
    throws JClassAlreadyExistsException
  {
    return _class(mods, name, false);
  }
  
  public JDefinedClass _class(int mods, String name, boolean isInterface)
    throws JClassAlreadyExistsException
  {
    if (this.classes.containsKey(name)) {
      throw new JClassAlreadyExistsException((JDefinedClass)this.classes.get(name));
    }
    JDefinedClass c = new JNestedClass(this, mods, name, isInterface);
    if (this.upperCaseClassMap != null)
    {
      JDefinedClass dc = (JDefinedClass)this.upperCaseClassMap.get(name.toUpperCase());
      if (dc != null) {
        throw new JClassAlreadyExistsException(dc);
      }
      this.upperCaseClassMap.put(name.toUpperCase(), c);
    }
    this.classes.put(name, c);
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
    return _class(mods, name, true);
  }
  
  public JDefinedClass _interface(String name)
    throws JClassAlreadyExistsException
  {
    return _interface(1, name);
  }
  
  public JDocComment javadoc()
  {
    if (this.jdoc == null) {
      this.jdoc = new JDocComment();
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
  
  public final Iterator classes()
  {
    return this.classes.values().iterator();
  }
  
  public final JClass[] listClasses()
  {
    return (JClass[])this.classes.values().toArray(new JClass[this.classes.values().size()]);
  }
  
  public JClass outer()
  {
    return null;
  }
  
  public void declare(JFormatter f)
  {
    if (this.jdoc != null) {
      f.nl().g(this.jdoc);
    }
    f.g(this.mods).p(this.isInterface ? "interface" : "class").p(this.name);
    if ((this.superClass != null) && (this.superClass != owner().ref(Object.class))) {
      f.nl().i().p("extends").g(this.superClass).nl().o();
    }
    if (!this.interfaces.isEmpty())
    {
      boolean first = true;
      if (this.superClass == null) {
        f.nl();
      }
      f.i().p(this.isInterface ? "extends" : "implements");
      for (Iterator i = this.interfaces.iterator(); i.hasNext();)
      {
        if (!first) {
          f.p(',');
        }
        f.g((JClass)i.next());
        first = false;
      }
      f.nl().o();
    }
    declareBody(f);
  }
  
  protected void declareBody(JFormatter f)
  {
    f.p('{').nl().nl().i();
    for (Iterator i = this.fields.iterator(); i.hasNext();) {
      f.d((JVar)i.next());
    }
    if (this.init != null) {
      f.nl().p("static").s(this.init);
    }
    for (Iterator i = this.constructors.iterator(); i.hasNext();) {
      f.nl().d((JMethod)i.next());
    }
    for (Iterator i = this.methods.iterator(); i.hasNext();) {
      f.nl().d((JMethod)i.next());
    }
    for (Iterator i = this.classes.values().iterator(); i.hasNext();) {
      f.nl().d((JDefinedClass)i.next());
    }
    if (this.directBlock != null) {
      f.p(this.directBlock);
    }
    f.nl().o().p('}').nl();
  }
  
  public void generate(JFormatter f)
  {
    f.p(fullName());
  }
  
  public void direct(String string)
  {
    if (this.directBlock == null) {
      this.directBlock = string;
    } else {
      this.directBlock += string;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JDefinedClass.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */