package javax.xml.bind;

import org.xml.sax.ContentHandler;

public abstract interface UnmarshallerHandler
  extends ContentHandler
{
  public abstract Object getResult()
    throws JAXBException, IllegalStateException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\UnmarshallerHandler.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */