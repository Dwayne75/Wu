package com.sun.codemodel;

public class JStringLiteral
  extends JExpressionImpl
{
  public final String str;
  
  JStringLiteral(String what)
  {
    this.str = what;
  }
  
  public void generate(JFormatter f)
  {
    f.p(JExpr.quotify('"', this.str));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JStringLiteral.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */