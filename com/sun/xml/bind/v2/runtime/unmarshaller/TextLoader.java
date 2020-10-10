package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.Transducer;
import org.xml.sax.SAXException;

public class TextLoader
  extends Loader
{
  private final Transducer xducer;
  
  public TextLoader(Transducer xducer)
  {
    super(true);
    this.xducer = xducer;
  }
  
  public void text(UnmarshallingContext.State state, CharSequence text)
    throws SAXException
  {
    try
    {
      state.target = this.xducer.parse(text);
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\TextLoader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */