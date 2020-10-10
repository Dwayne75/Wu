package com.sun.codemodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JTryBlock
  implements JStatement
{
  private JBlock body = new JBlock();
  private List catches = new ArrayList();
  private JBlock _finally = null;
  
  public JBlock body()
  {
    return this.body;
  }
  
  public JCatchBlock _catch(JClass exception)
  {
    JCatchBlock cb = new JCatchBlock(exception);
    this.catches.add(cb);
    return cb;
  }
  
  public JBlock _finally()
  {
    if (this._finally == null) {
      this._finally = new JBlock();
    }
    return this._finally;
  }
  
  public void state(JFormatter f)
  {
    f.p("try").g(this.body);
    for (Iterator i = this.catches.iterator(); i.hasNext();) {
      f.g((JCatchBlock)i.next());
    }
    if (this._finally != null) {
      f.p("finally").g(this._finally);
    }
    f.nl();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JTryBlock.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */