package org.fourthline.cling.support.messagebox.parser;

import javax.xml.xpath.XPath;
import org.seamless.xml.DOMParser;
import org.seamless.xml.NamespaceContextMap;
import org.w3c.dom.Document;

public class MessageDOMParser
  extends DOMParser<MessageDOM>
{
  protected MessageDOM createDOM(Document document)
  {
    return new MessageDOM(document);
  }
  
  public NamespaceContextMap createDefaultNamespaceContext(String... optionalPrefixes)
  {
    NamespaceContextMap ctx = new NamespaceContextMap()
    {
      protected String getDefaultNamespaceURI()
      {
        return "urn:samsung-com:messagebox-1-0";
      }
    };
    for (String optionalPrefix : optionalPrefixes) {
      ctx.put(optionalPrefix, "urn:samsung-com:messagebox-1-0");
    }
    return ctx;
  }
  
  public XPath createXPath()
  {
    return super.createXPath(createDefaultNamespaceContext(new String[] { "m" }));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\messagebox\parser\MessageDOMParser.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */