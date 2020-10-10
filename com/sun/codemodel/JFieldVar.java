package com.sun.codemodel;

import java.util.Map;

public class JFieldVar
  extends JVar
{
  private JDocComment jdoc = null;
  private final JDefinedClass owner;
  
  JFieldVar(JDefinedClass owner, JMods mods, JType type, String name, JExpression init)
  {
    super(mods, type, name, init);
    this.owner = owner;
  }
  
  public void name(String name)
  {
    if (this.owner.fields.containsKey(name)) {
      throw new IllegalArgumentException("name " + name + " is already in use");
    }
    String oldName = name();
    super.name(name);
    this.owner.fields.remove(oldName);
    this.owner.fields.put(name, this);
  }
  
  public JDocComment javadoc()
  {
    if (this.jdoc == null) {
      this.jdoc = new JDocComment(this.owner.owner());
    }
    return this.jdoc;
  }
  
  public void declare(JFormatter f)
  {
    if (this.jdoc != null) {
      f.g(this.jdoc);
    }
    super.declare(f);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JFieldVar.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */