package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.v2.runtime.Name;
import javax.xml.namespace.QName;
import org.xml.sax.Attributes;

public abstract class TagName
{
  public String uri;
  public String local;
  public Attributes atts;
  
  public final boolean matches(String nsUri, String local)
  {
    return (this.uri == nsUri) && (this.local == local);
  }
  
  public final boolean matches(Name name)
  {
    return (this.local == name.localName) && (this.uri == name.nsUri);
  }
  
  public String toString()
  {
    return '{' + this.uri + '}' + this.local;
  }
  
  public abstract String getQname();
  
  public String getPrefix()
  {
    String qname = getQname();
    int idx = qname.indexOf(':');
    if (idx < 0) {
      return "";
    }
    return qname.substring(0, idx);
  }
  
  public QName createQName()
  {
    return new QName(this.uri, this.local, getPrefix());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\TagName.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */