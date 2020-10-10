package com.sun.codemodel;

public final class JForEach
  implements JStatement
{
  private final JType type;
  private final String var;
  private JBlock body = null;
  private final JExpression collection;
  private final JVar loopVar;
  
  public JForEach(JType vartype, String variable, JExpression collection)
  {
    this.type = vartype;
    this.var = variable;
    this.collection = collection;
    this.loopVar = new JVar(JMods.forVar(0), this.type, this.var, collection);
  }
  
  public JVar var()
  {
    return this.loopVar;
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
    f.p("for (");
    f.g(this.type).id(this.var).p(": ").g(this.collection);
    f.p(')');
    if (this.body != null) {
      f.g(this.body).nl();
    } else {
      f.p(';').nl();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JForEach.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */