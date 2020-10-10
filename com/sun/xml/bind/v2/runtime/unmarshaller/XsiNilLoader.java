package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class XsiNilLoader
  extends ProxyLoader
{
  private final Loader defaultLoader;
  
  public XsiNilLoader(Loader defaultLoader)
  {
    this.defaultLoader = defaultLoader;
    assert (defaultLoader != null);
  }
  
  protected Loader selectLoader(UnmarshallingContext.State state, TagName ea)
    throws SAXException
  {
    int idx = ea.atts.getIndex("http://www.w3.org/2001/XMLSchema-instance", "nil");
    if (idx != -1)
    {
      String value = ea.atts.getValue(idx);
      if (DatatypeConverterImpl._parseBoolean(value))
      {
        onNil(state);
        return Discarder.INSTANCE;
      }
    }
    return this.defaultLoader;
  }
  
  protected void onNil(UnmarshallingContext.State state)
    throws SAXException
  {}
  
  public static final class Single
    extends XsiNilLoader
  {
    private final Accessor acc;
    
    public Single(Loader l, Accessor acc)
    {
      super();
      this.acc = acc;
    }
    
    protected void onNil(UnmarshallingContext.State state)
      throws SAXException
    {
      try
      {
        this.acc.set(state.prev.target, null);
      }
      catch (AccessorException e)
      {
        handleGenericException(e, true);
      }
    }
  }
  
  public static final class Array
    extends XsiNilLoader
  {
    public Array(Loader core)
    {
      super();
    }
    
    protected void onNil(UnmarshallingContext.State state)
    {
      state.target = null;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\XsiNilLoader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */