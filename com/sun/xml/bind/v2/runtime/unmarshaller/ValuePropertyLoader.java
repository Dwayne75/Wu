package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;
import org.xml.sax.SAXException;

public class ValuePropertyLoader
  extends Loader
{
  private final TransducedAccessor xacc;
  
  public ValuePropertyLoader(TransducedAccessor xacc)
  {
    super(true);
    this.xacc = xacc;
  }
  
  public void text(UnmarshallingContext.State state, CharSequence text)
    throws SAXException
  {
    try
    {
      this.xacc.parse(state.target, text);
    }
    catch (AccessorException e)
    {
      handleGenericException(e, true);
    }
    catch (RuntimeException e)
    {
      handleParseConversionException(state, e);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\ValuePropertyLoader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */