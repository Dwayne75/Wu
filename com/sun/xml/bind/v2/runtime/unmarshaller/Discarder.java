package com.sun.xml.bind.v2.runtime.unmarshaller;

public final class Discarder
  extends Loader
{
  public static final Loader INSTANCE = new Discarder();
  
  private Discarder()
  {
    super(false);
  }
  
  public void childElement(UnmarshallingContext.State state, TagName ea)
  {
    state.target = null;
    
    state.loader = this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\Discarder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */