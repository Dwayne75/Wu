package com.sun.xml.bind.v2.runtime.unmarshaller;

import org.xml.sax.SAXException;

public abstract class ProxyLoader
  extends Loader
{
  public ProxyLoader()
  {
    super(false);
  }
  
  public final void startElement(UnmarshallingContext.State state, TagName ea)
    throws SAXException
  {
    Loader loader = selectLoader(state, ea);
    state.loader = loader;
    loader.startElement(state, ea);
  }
  
  protected abstract Loader selectLoader(UnmarshallingContext.State paramState, TagName paramTagName)
    throws SAXException;
  
  public final void leaveElement(UnmarshallingContext.State state, TagName ea)
  {
    throw new IllegalStateException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\ProxyLoader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */