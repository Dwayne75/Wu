package com.sun.codemodel;

final class JCast
  extends JExpressionImpl
{
  private final JType type;
  private final JExpression object;
  
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JCast.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */