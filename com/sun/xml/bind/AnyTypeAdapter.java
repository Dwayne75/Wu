package com.sun.xml.bind;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public final class AnyTypeAdapter
  extends XmlAdapter<Object, Object>
{
  public Object unmarshal(Object v)
  {
    return v;
  }
  
  public Object marshal(Object v)
  {
    return v;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\AnyTypeAdapter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */