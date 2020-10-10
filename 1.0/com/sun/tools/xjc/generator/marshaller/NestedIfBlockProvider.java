package com.sun.tools.xjc.generator.marshaller;

import com.sun.codemodel.JExpression;

class NestedIfBlockProvider
{
  private final Context context;
  
  NestedIfBlockProvider(Context _context)
  {
    this.context = _context;
  }
  
  private int nestLevel = 0;
  private IfThenElseBlockReference previous = null;
  
  public void startBlock(JExpression testExp)
  {
    startElse();
    
    this.nestLevel += 1;
    this.previous = new IfThenElseBlockReference(this.context, testExp);
    this.context.pushNewBlock(this.previous.createThenProvider());
  }
  
  public void startElse()
  {
    if (this.previous != null)
    {
      this.context.popBlock();
      this.context.pushNewBlock(this.previous.createElseProvider());
    }
  }
  
  public void end()
  {
    while (this.nestLevel-- > 0) {
      this.context.popBlock();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\marshaller\NestedIfBlockProvider.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */