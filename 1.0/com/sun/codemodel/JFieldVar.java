package com.sun.codemodel;

public class JFieldVar
  extends JVar
{
  private JDocComment jdoc = null;
  
  JFieldVar(JMods mods, JType type, String name, JExpression init)
  {
    super(mods, type, name, init);
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
    super.declare(f);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JFieldVar.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */