package com.sun.codemodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class JSwitch
  implements JStatement
{
  private JExpression test;
  private List<JCase> cases = new ArrayList();
  private JCase defaultCase = null;
  
  JSwitch(JExpression test)
  {
    this.test = test;
  }
  
  public JExpression test()
  {
    return this.test;
  }
  
  public Iterator cases()
  {
    return this.cases.iterator();
  }
  
  public JCase _case(JExpression label)
  {
    JCase c = new JCase(label);
    this.cases.add(c);
    return c;
  }
  
  public JCase _default()
  {
    this.defaultCase = new JCase(null, true);
    return this.defaultCase;
  }
  
  public void state(JFormatter f)
  {
    if (JOp.hasTopOp(this.test)) {
      f.p("switch ").g(this.test).p(" {").nl();
    } else {
      f.p("switch (").g(this.test).p(')').p(" {").nl();
    }
    for (JCase c : this.cases) {
      f.s(c);
    }
    if (this.defaultCase != null) {
      f.s(this.defaultCase);
    }
    f.p('}').nl();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JSwitch.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */