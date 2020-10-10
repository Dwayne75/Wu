package org.seamless.xhtml;

import javax.xml.xpath.XPath;
import org.w3c.dom.Element;

public class Link
  extends XHTMLElement
{
  public Link(XPath xpath, Element element)
  {
    super(xpath, element);
  }
  
  public Href getHref()
  {
    return Href.fromString(getAttribute(XHTML.ATTR.href));
  }
  
  public String getRel()
  {
    return getAttribute(XHTML.ATTR.rel);
  }
  
  public String getRev()
  {
    return getAttribute(XHTML.ATTR.rev);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\xhtml\Link.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */