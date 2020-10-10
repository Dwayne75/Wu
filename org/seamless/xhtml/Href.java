package org.seamless.xhtml;

import java.net.URI;

public class Href
{
  private URI uri;
  
  public Href(URI uri)
  {
    this.uri = uri;
  }
  
  public URI getURI()
  {
    return this.uri;
  }
  
  public static Href fromString(String string)
  {
    if (string == null) {
      return null;
    }
    string = string.replaceAll(" ", "%20");
    return new Href(URI.create(string));
  }
  
  public String toString()
  {
    return getURI().toString();
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    Href href = (Href)o;
    if (!this.uri.equals(href.uri)) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    return this.uri.hashCode();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\xhtml\Href.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */