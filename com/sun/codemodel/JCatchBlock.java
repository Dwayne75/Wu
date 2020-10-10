package com.sun.codemodel;

public class JCatchBlock
  implements JGenerable
{
  JClass exception;
  private JVar var = null;
  private JBlock body = new JBlock();
  
  JCatchBlock(JClass exception)
  {
    this.exception = exception;
  }
  
  public JVar param(String name)
  {
    if (this.var != null) {
      throw new IllegalStateException();
    }
    this.var = new JVar(JMods.forVar(0), this.exception, name, null);
    return this.var;
  }
  
  public JBlock body()
  {
    return this.body;
  }
  
  public void generate(JFormatter f)
  {
    if (this.var == null) {
      this.var = new JVar(JMods.forVar(0), this.exception, "_x", null);
    }
    f.p("catch (").b(this.var).p(')').g(this.body);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JCatchBlock.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */