package com.sun.codemodel;

class JReturn
  implements JStatement
{
  private JExpression expr;
  
  JReturn(JExpression expr)
  {
    this.expr = expr;
  }
  
  public void state(JFormatter f)
  {
    f.p("return ");
    if (this.expr != null) {
      f.g(this.expr);
    }
    f.p(';').nl();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JReturn.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */