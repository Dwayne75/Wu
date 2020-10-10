package com.sun.codemodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class JNarrowedClass
  extends JClass
{
  final JClass basis;
  private final List<JClass> args;
  
  JNarrowedClass(JClass basis, JClass arg)
  {
    this(basis, Collections.singletonList(arg));
  }
  
  JNarrowedClass(JClass basis, List<JClass> args)
  {
    super(basis.owner());
    this.basis = basis;
    assert (!(basis instanceof JNarrowedClass));
    this.args = args;
  }
  
  public JClass narrow(JClass clazz)
  {
    List<JClass> newArgs = new ArrayList(this.args);
    newArgs.add(clazz);
    return new JNarrowedClass(this.basis, newArgs);
  }
  
  public JClass narrow(JClass... clazz)
  {
    List<JClass> newArgs = new ArrayList(this.args);
    for (JClass c : clazz) {
      newArgs.add(c);
    }
    return new JNarrowedClass(this.basis, newArgs);
  }
  
  public String name()
  {
    StringBuffer buf = new StringBuffer();
    buf.append(this.basis.name());
    buf.append('<');
    boolean first = true;
    for (JClass c : this.args)
    {
      if (first) {
        first = false;
      } else {
        buf.append(',');
      }
      buf.append(c.name());
    }
    buf.append('>');
    return buf.toString();
  }
  
  public String fullName()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(this.basis.fullName());
    buf.append('<');
    boolean first = true;
    for (JClass c : this.args)
    {
      if (first) {
        first = false;
      } else {
        buf.append(',');
      }
      buf.append(c.fullName());
    }
    buf.append('>');
    return buf.toString();
  }
  
  public String binaryName()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(this.basis.binaryName());
    buf.append('<');
    boolean first = true;
    for (JClass c : this.args)
    {
      if (first) {
        first = false;
      } else {
        buf.append(',');
      }
      buf.append(c.binaryName());
    }
    buf.append('>');
    return buf.toString();
  }
  
  public void generate(JFormatter f)
  {
    f.t(this.basis).p('<').g(this.args).p(65535);
  }
  
  void printLink(JFormatter f)
  {
    this.basis.printLink(f);
    f.p("{@code <}");
    boolean first = true;
    for (JClass c : this.args)
    {
      if (first) {
        first = false;
      } else {
        f.p(',');
      }
      c.printLink(f);
    }
    f.p("{@code >}");
  }
  
  public JPackage _package()
  {
    return this.basis._package();
  }
  
  public JClass _extends()
  {
    JClass base = this.basis._extends();
    if (base == null) {
      return base;
    }
    return base.substituteParams(this.basis.typeParams(), this.args);
  }
  
  public Iterator<JClass> _implements()
  {
    new Iterator()
    {
      private final Iterator<JClass> core = JNarrowedClass.this.basis._implements();
      
      public void remove()
      {
        this.core.remove();
      }
      
      public JClass next()
      {
        return ((JClass)this.core.next()).substituteParams(JNarrowedClass.this.basis.typeParams(), JNarrowedClass.this.args);
      }
      
      public boolean hasNext()
      {
        return this.core.hasNext();
      }
    };
  }
  
  public JClass erasure()
  {
    return this.basis;
  }
  
  public boolean isInterface()
  {
    return this.basis.isInterface();
  }
  
  public boolean isAbstract()
  {
    return this.basis.isAbstract();
  }
  
  public boolean isArray()
  {
    return false;
  }
  
  public boolean equals(Object obj)
  {
    if (!(obj instanceof JNarrowedClass)) {
      return false;
    }
    return fullName().equals(((JClass)obj).fullName());
  }
  
  public int hashCode()
  {
    return fullName().hashCode();
  }
  
  protected JClass substituteParams(JTypeVar[] variables, List<JClass> bindings)
  {
    JClass b = this.basis.substituteParams(variables, bindings);
    boolean different = b != this.basis;
    
    List<JClass> clazz = new ArrayList(this.args.size());
    for (int i = 0; i < clazz.size(); i++)
    {
      JClass c = ((JClass)this.args.get(i)).substituteParams(variables, bindings);
      clazz.set(i, c);
      different |= c != this.args.get(i);
    }
    if (different) {
      return new JNarrowedClass(b, clazz);
    }
    return this;
  }
  
  public List<JClass> getTypeParameters()
  {
    return this.args;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JNarrowedClass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */