package com.sun.xml.bind.v2.runtime.unmarshaller;

import org.xml.sax.SAXException;

public final class DefaultValueLoaderDecorator
  extends Loader
{
  private final Loader l;
  private final String defaultValue;
  
  public DefaultValueLoaderDecorator(Loader l, String defaultValue)
  {
    this.l = l;
    this.defaultValue = defaultValue;
  }
  
  public void startElement(UnmarshallingContext.State state, TagName ea)
    throws SAXException
  {
    if (state.elementDefaultValue == null) {
      state.elementDefaultValue = this.defaultValue;
    }
    state.loader = this.l;
    this.l.startElement(state, ea);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\DefaultValueLoaderDecorator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */