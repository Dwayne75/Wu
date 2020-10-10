package com.sun.tools.xjc.generator.marshaller;

import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.generator.util.BlockReference;

class IfThenElseBlockReference
{
  private final JExpression testExp;
  private final BlockReference parent;
  private JConditional cond;
  
  IfThenElseBlockReference(Context _context, JExpression exp)
  {
    this.testExp = exp;
    this.parent = _context.getCurrentBlock();
  }
  
  private boolean swapped = false;
  
  public BlockReference createThenProvider()
  {
    return new IfThenElseBlockReference.1(this);
  }
  
  public BlockReference createElseProvider()
  {
    return new IfThenElseBlockReference.2(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\marshaller\IfThenElseBlockReference.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */