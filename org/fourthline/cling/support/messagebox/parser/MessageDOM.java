package org.fourthline.cling.support.messagebox.parser;

import javax.xml.xpath.XPath;
import org.seamless.xml.DOM;
import org.w3c.dom.Document;

public class MessageDOM
  extends DOM
{
  public static final String NAMESPACE_URI = "urn:samsung-com:messagebox-1-0";
  
  public MessageDOM(Document dom)
  {
    super(dom);
  }
  
  public String getRootElementNamespace()
  {
    return "urn:samsung-com:messagebox-1-0";
  }
  
  public MessageElement getRoot(XPath xPath)
  {
    return new MessageElement(xPath, getW3CDocument().getDocumentElement());
  }
  
  public MessageDOM copy()
  {
    return new MessageDOM((Document)getW3CDocument().cloneNode(true));
  }
  
  public MessageElement createRoot(XPath xpath, String element)
  {
    super.createRoot(element);
    return getRoot(xpath);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\messagebox\parser\MessageDOM.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */