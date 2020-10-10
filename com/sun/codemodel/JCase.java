package com.sun.codemodel;

public final class JCase
  implements JStatement
{
  private JExpression label;
  private JBlock body = null;
  private boolean isDefaultCase = false;
  
  JCase(JExpression label)
  {
    this(label, false);
  }
  
  JCase(JExpression label, boolean isDefaultCase)
  {
    this.label = label;
    this.isDefaultCase = isDefaultCase;
  }
  
  public JExpression label()
  {
    return this.label;
  }
  
  public JBlock body()
  {
    if (this.body == null) {
      this.body = new JBlock(false, true);
    }
    return this.body;
  }
  
  public void state(JFormatter f)
  {
    f.i();
    if (!this.isDefaultCase) {
      f.p("case ").g(this.label).p(':').nl();
    } else {
      f.p("default:").nl();
    }
    if (this.body != null) {
      f.s(this.body);
    }
    f.o();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JCase.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */