package com.sun.codemodel;

public class JCast
  extends JExpressionImpl
{
  private JType type;
  private JExpression object;
  
  JCast(JType type, JExpression object)
  {
    this.type = type;
    this.object = object;
  }
  
  public void generate(JFormatter f)
  {
    f.p("((").g(this.type).p(')').g(this.object).p(')');
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JCast.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */