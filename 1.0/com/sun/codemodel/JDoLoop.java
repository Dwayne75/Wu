package com.sun.codemodel;

public class JDoLoop
  implements JStatement
{
  private JExpression test;
  private JBlock body = null;
  
  JDoLoop(JExpression test)
  {
    this.test = test;
  }
  
  public JBlock body()
  {
    if (this.body == null) {
      this.body = new JBlock();
    }
    return this.body;
  }
  
  public void state(JFormatter f)
  {
    f.p("do");
    if (this.body != null) {
      f.g(this.body);
    } else {
      f.p("{ }");
    }
    if (JOp.hasTopOp(this.test)) {
      f.p("while ").g(this.test);
    } else {
      f.p("while (").g(this.test).p(')');
    }
    f.p(';').nl();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JDoLoop.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */