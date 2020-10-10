package javax.xml.bind;

import java.net.URL;
import org.w3c.dom.Node;

public abstract interface ValidationEventLocator
{
  public abstract URL getURL();
  
  public abstract int getOffset();
  
  public abstract int getLineNumber();
  
  public abstract int getColumnNumber();
  
  public abstract Object getObject();
  
  public abstract Node getNode();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\ValidationEventLocator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */