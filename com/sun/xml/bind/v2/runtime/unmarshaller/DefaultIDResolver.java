package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.IDResolver;
import java.util.HashMap;
import java.util.concurrent.Callable;
import javax.xml.bind.ValidationEventHandler;
import org.xml.sax.SAXException;

final class DefaultIDResolver
  extends IDResolver
{
  private HashMap<String, Object> idmap = null;
  
  public void startDocument(ValidationEventHandler eventHandler)
    throws SAXException
  {
    if (this.idmap != null) {
      this.idmap.clear();
    }
  }
  
  public void bind(String id, Object obj)
  {
    if (this.idmap == null) {
      this.idmap = new HashMap();
    }
    this.idmap.put(id, obj);
  }
  
  public Callable resolve(final String id, Class targetType)
  {
    new Callable()
    {
      public Object call()
        throws Exception
      {
        if (DefaultIDResolver.this.idmap == null) {
          return null;
        }
        return DefaultIDResolver.this.idmap.get(id);
      }
    };
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\DefaultIDResolver.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */