package com.sun.codemodel;

public class JConditional
  implements JStatement
{
  private JExpression test = null;
  private JBlock _then = new JBlock();
  private JBlock _else = null;
  
  JConditional(JExpression test)
  {
    this.test = test;
  }
  
  public JBlock _then()
  {
    return this._then;
  }
  
  public JBlock _else()
  {
    if (this._else == null) {
      this._else = new JBlock();
    }
    return this._else;
  }
  
  public void state(JFormatter f)
  {
    if (JOp.hasTopOp(this.test)) {
      f.p("if ").g(this.test);
    } else {
      f.p("if (").g(this.test).p(')');
    }
    f.g(this._then);
    if (this._else != null) {
      f.p("else").g(this._else);
    }
    f.nl();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JConditional.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */