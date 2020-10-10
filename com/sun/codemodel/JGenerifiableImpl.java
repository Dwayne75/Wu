package com.sun.codemodel;

import java.util.ArrayList;
import java.util.List;

abstract class JGenerifiableImpl
  implements JGenerifiable, JDeclaration
{
  private List<JTypeVar> typeVariables = null;
  
  protected abstract JCodeModel owner();
  
  public void declare(JFormatter f)
  {
    if (this.typeVariables != null)
    {
      f.p('<');
      for (int i = 0; i < this.typeVariables.size(); i++)
      {
        if (i != 0) {
          f.p(',');
        }
        f.d((JDeclaration)this.typeVariables.get(i));
      }
      f.p('>');
    }
  }
  
  public JTypeVar generify(String name)
  {
    JTypeVar v = new JTypeVar(owner(), name);
    if (this.typeVariables == null) {
      this.typeVariables = new ArrayList(3);
    }
    this.typeVariables.add(v);
    return v;
  }
  
  public JTypeVar generify(String name, Class bound)
  {
    return generify(name, owner().ref(bound));
  }
  
  public JTypeVar generify(String name, JClass bound)
  {
    return generify(name).bound(bound);
  }
  
  public JTypeVar[] typeParams()
  {
    if (this.typeVariables == null) {
      return JTypeVar.EMPTY_ARRAY;
    }
    return (JTypeVar[])this.typeVariables.toArray(new JTypeVar[this.typeVariables.size()]);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JGenerifiableImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */