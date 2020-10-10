package com.sun.tools.xjc.generator.util;

import com.sun.codemodel.JBlock;

public class ExistingBlockReference
  implements BlockReference
{
  private final JBlock block;
  
  public ExistingBlockReference(JBlock _block)
  {
    this.block = _block;
  }
  
  public JBlock get(boolean create)
  {
    return this.block;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\util\ExistingBlockReference.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */