package com.sun.codemodel;

import com.sun.codemodel.util.ClassNameComparator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class JMethod
  implements JDeclaration
{
  private JMods mods;
  private JType type = null;
  private String name = null;
  private final List params = new ArrayList();
  private final Set _throws = new TreeSet(ClassNameComparator.theInstance);
  private JBlock body = null;
  private JDefinedClass outer;
  private JDocComment jdoc = null;
  
  private boolean isConstructor()
  {
    return this.type == null;
  }
  
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
  
  public JMethod _throws(JClass exception)
  {
    this._throws.add(exception);
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
    return param(mods, this.outer.owner().ref(type), name);
  }
  
  public JVar param(Class type, String name)
  {
    return param(this.outer.owner().ref(type), name);
  }
  
  public String name()
  {
    return this.name;
  }
  
  public JType type()
  {
    return this.type;
  }
  
  public JType[] listParamTypes()
  {
    JType[] r = new JType[this.params.size()];
    for (int i = 0; i < r.length; i++) {
      r[i] = ((JVar)this.params.get(i)).type();
    }
    return r;
  }
  
  public JVar[] listParams()
  {
    return (JVar[])this.params.toArray(new JVar[this.params.size()]);
  }
  
  public boolean hasSignature(JType[] argTypes)
  {
    JVar[] p = listParams();
    if (p.length != argTypes.length) {
      return false;
    }
    for (int i = 0; i < p.length; i++) {
      if (!p[i].type.equals(argTypes[i])) {
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
  
  public JDocComment javadoc()
  {
    if (this.jdoc == null) {
      this.jdoc = new JDocComment();
    }
    return this.jdoc;
  }
  
  public void declare(JFormatter f)
  {
    if (this.jdoc != null) {
      f.g(this.jdoc);
    }
    f.g(this.mods);
    if (!isConstructor()) {
      f.g(this.type);
    }
    f.p(this.name).p('(');
    boolean first = true;
    for (Iterator i = this.params.iterator(); i.hasNext();)
    {
      if (!first) {
        f.p(',');
      }
      f.b((JVar)i.next());
      first = false;
    }
    f.p(')');
    if (!this._throws.isEmpty())
    {
      f.nl().i().p("throws");
      first = true;
      for (Iterator i = this._throws.iterator(); i.hasNext();)
      {
        if (!first) {
          f.p(',');
        }
        f.g((JClass)i.next());
        first = false;
      }
      f.nl().o();
    }
    if (this.body != null) {
      f.s(this.body);
    } else if ((!this.outer.isInterface()) && (!this.mods.isAbstract()) && (!this.mods.isNative())) {
      f.s(new JBlock());
    } else {
      f.p(';').nl();
    }
  }
  
  public JMods getMods()
  {
    return this.mods;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JMethod.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */