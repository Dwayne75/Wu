package com.sun.codemodel;

public class JWhileLoop
  implements JStatement
{
  private JExpression test;
  private JBlock body = null;
  
  JWhileLoop(JExpression test)
  {
    this.test = test;
  }
  
  public JExpression test()
  {
    return this.test;
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
    if (JOp.hasTopOp(this.test)) {
      f.p("while ").g(this.test);
    } else {
      f.p("while (").g(this.test).p(')');
    }
    if (this.body != null) {
      f.s(this.body);
    } else {
      f.p(';').nl();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JWhileLoop.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */