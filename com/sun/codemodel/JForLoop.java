package com.sun.codemodel;

import java.util.ArrayList;
import java.util.List;

public class JForLoop
  implements JStatement
{
  private List<Object> inits = new ArrayList();
  private JExpression test = null;
  private List<JExpression> updates = new ArrayList();
  private JBlock body = null;
  
  public JVar init(int mods, JType type, String var, JExpression e)
  {
    JVar v = new JVar(JMods.forVar(mods), type, var, e);
    this.inits.add(v);
    return v;
  }
  
  public JVar init(JType type, String var, JExpression e)
  {
    return init(0, type, var, e);
  }
  
  public void init(JVar v, JExpression e)
  {
    this.inits.add(JExpr.assign(v, e));
  }
  
  public void test(JExpression e)
  {
    this.test = e;
  }
  
  public void update(JExpression e)
  {
    this.updates.add(e);
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
    boolean first = true;
    for (Object o : this.inits)
    {
      if (!first) {
        f.p(',');
      }
      if ((o instanceof JVar)) {
        f.b((JVar)o);
      } else {
        f.g((JExpression)o);
      }
      first = false;
    }
    f.p(';').g(this.test).p(';').g(this.updates).p(')');
    if (this.body != null) {
      f.g(this.body).nl();
    } else {
      f.p(';').nl();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JForLoop.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */