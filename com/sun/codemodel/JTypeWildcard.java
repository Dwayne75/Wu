package com.sun.codemodel;

import java.util.Iterator;
import java.util.List;

final class JTypeWildcard
  extends JClass
{
  private final JClass bound;
  
  JTypeWildcard(JClass bound)
  {
    super(bound.owner());
    this.bound = bound;
  }
  
  public String name()
  {
    return "? extends " + this.bound.name();
  }
  
  public String fullName()
  {
    return "? extends " + this.bound.fullName();
  }
  
  public JPackage _package()
  {
    return null;
  }
  
  public JClass _extends()
  {
    if (this.bound != null) {
      return this.bound;
    }
    return owner().ref(Object.class);
  }
  
  public Iterator<JClass> _implements()
  {
    return this.bound._implements();
  }
  
  public boolean isInterface()
  {
    return false;
  }
  
  public boolean isAbstract()
  {
    return false;
  }
  
  protected JClass substituteParams(JTypeVar[] variables, List<JClass> bindings)
  {
    JClass nb = this.bound.substituteParams(variables, bindings);
    if (nb == this.bound) {
      return this;
    }
    return new JTypeWildcard(nb);
  }
  
  public void generate(JFormatter f)
  {
    if (this.bound._extends() == null) {
      f.p("?");
    } else {
      f.p("? extends").g(this.bound);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JTypeWildcard.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */