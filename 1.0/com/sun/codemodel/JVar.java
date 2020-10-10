package com.sun.codemodel;

public class JVar
  extends JExpressionImpl
  implements JDeclaration, JAssignmentTarget
{
  private JMods mods;
  JType type;
  String name;
  JExpression init;
  
  JVar(JMods mods, JType type, String name, JExpression init)
  {
    this.mods = mods;
    this.type = type;
    this.name = name;
    this.init = init;
  }
  
  public JVar init(JExpression init)
  {
    this.init = init;
    return this;
  }
  
  public String name()
  {
    return this.name;
  }
  
  public JType type()
  {
    return this.type;
  }
  
  public void bind(JFormatter f)
  {
    f.g(this.mods).g(this.type).p(this.name);
    if (this.init != null) {
      f.p('=').g(this.init);
    }
  }
  
  public void declare(JFormatter f)
  {
    f.b(this).p(';').nl();
  }
  
  public void generate(JFormatter f)
  {
    f.p(this.name);
  }
  
  public JExpression assign(JExpression rhs)
  {
    return JExpr.assign(this, rhs);
  }
  
  public JExpression assignPlus(JExpression rhs)
  {
    return JExpr.assignPlus(this, rhs);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JVar.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */