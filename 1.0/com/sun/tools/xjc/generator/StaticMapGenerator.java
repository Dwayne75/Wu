package com.sun.tools.xjc.generator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.util.Util;

public abstract class StaticMapGenerator
{
  public final JVar $map;
  private JBlock block;
  private int cnt;
  private int ticketMaster = 1;
  private final int THRESHOLD;
  
  protected StaticMapGenerator(JVar $map, JBlock block)
  {
    this.$map = $map;
    this.block = block;
    
    String debug = Util.getSystemProperty(ObjectFactoryGenerator.class, "staticThreshold");
    if (debug == null) {
      this.THRESHOLD = 500;
    } else {
      this.THRESHOLD = Integer.parseInt(debug);
    }
  }
  
  protected final void add(JExpression key, JExpression value)
  {
    this.block.invoke(this.$map, "put").arg(key).arg(value);
    if (++this.cnt >= this.THRESHOLD)
    {
      JMethod m = createNewMethod(this.ticketMaster++);
      this.block.invoke(m);
      this.block = m.body();
      this.cnt = 0;
    }
  }
  
  protected abstract JMethod createNewMethod(int paramInt);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\StaticMapGenerator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */