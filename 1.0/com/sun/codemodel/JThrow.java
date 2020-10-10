package com.sun.codemodel;

class JThrow
  implements JStatement
{
  private JExpression expr;
  
  JThrow(JExpression expr)
  {
    this.expr = expr;
  }
  
  public void state(JFormatter f)
  {
    f.p("throw");
    f.g(this.expr);
    f.p(';').nl();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JThrow.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */