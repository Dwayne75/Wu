package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.v2.runtime.JAXBContextImpl;

public final class UnmarshallerChain
{
  private int offset = 0;
  public final JAXBContextImpl context;
  
  public UnmarshallerChain(JAXBContextImpl context)
  {
    this.context = context;
  }
  
  public int allocateOffset()
  {
    return this.offset++;
  }
  
  public int getScopeSize()
  {
    return this.offset;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\property\UnmarshallerChain.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */