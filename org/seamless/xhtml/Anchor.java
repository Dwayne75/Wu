package org.seamless.xhtml;

import javax.xml.xpath.XPath;
import org.w3c.dom.Element;

public class Anchor
  extends XHTMLElement
{
  public Anchor(XPath xpath, Element element)
  {
    super(xpath, element);
  }
  
  public String getType()
  {
    return getAttribute(XHTML.ATTR.type);
  }
  
  public Anchor setType(String type)
  {
    setAttribute(XHTML.ATTR.type, type);
    return this;
  }
  
  public Href getHref()
  {
    return Href.fromString(getAttribute(XHTML.ATTR.href));
  }
  
  public Anchor setHref(String href)
  {
    setAttribute(XHTML.ATTR.href, href);
    return this;
  }
  
  public String toString()
  {
    return "(Anchor) " + getAttribute(XHTML.ATTR.href);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\xhtml\Anchor.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */