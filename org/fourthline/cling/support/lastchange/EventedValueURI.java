package org.fourthline.cling.support.lastchange;

import java.net.URI;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.Datatype.Builtin;
import org.fourthline.cling.model.types.InvalidValueException;
import org.seamless.util.Exceptions;

public class EventedValueURI
  extends EventedValue<URI>
{
  private static final Logger log = Logger.getLogger(EventedValueURI.class.getName());
  
  public EventedValueURI(URI value)
  {
    super(value);
  }
  
  public EventedValueURI(Map.Entry<String, String>[] attributes)
  {
    super(attributes);
  }
  
  protected URI valueOf(String s)
    throws InvalidValueException
  {
    try
    {
      return (URI)super.valueOf(s);
    }
    catch (InvalidValueException ex)
    {
      log.info("Ignoring invalid URI in evented value '" + s + "': " + Exceptions.unwrap(ex));
    }
    return null;
  }
  
  protected Datatype getDatatype()
  {
    return Datatype.Builtin.URI.getDatatype();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\lastchange\EventedValueURI.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */