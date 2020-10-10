package com.sun.xml.bind.v2.runtime.unmarshaller;

public final class ChildLoader
{
  public final Loader loader;
  public final Receiver receiver;
  
  public ChildLoader(Loader loader, Receiver receiver)
  {
    assert (loader != null);
    this.loader = loader;
    this.receiver = receiver;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\ChildLoader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */