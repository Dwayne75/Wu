package com.sun.tools.xjc.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public abstract class ReadOnlyAdapter<OnTheWire, InMemory>
  extends XmlAdapter<OnTheWire, InMemory>
{
  public final OnTheWire marshal(InMemory onTheWire)
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\util\ReadOnlyAdapter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */