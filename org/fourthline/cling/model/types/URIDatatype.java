package org.fourthline.cling.model.types;

import java.net.URI;
import java.net.URISyntaxException;

public class URIDatatype
  extends AbstractDatatype<URI>
{
  public URI valueOf(String s)
    throws InvalidValueException
  {
    if (s.equals("")) {
      return null;
    }
    try
    {
      return new URI(s);
    }
    catch (URISyntaxException ex)
    {
      throw new InvalidValueException(ex.getMessage(), ex);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\URIDatatype.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */