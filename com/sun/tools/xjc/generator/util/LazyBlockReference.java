package com.sun.tools.xjc.generator.util;

import com.sun.codemodel.JBlock;

public abstract class LazyBlockReference
  implements BlockReference
{
  private JBlock block = null;
  
  protected abstract JBlock create();
  
  public JBlock get(boolean create)
  {
    if (!create) {
      return this.block;
    }
    if (this.block == null) {
      this.block = create();
    }
    return this.block;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\util\LazyBlockReference.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */