package javax.xml.bind.annotation;

import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class W3CDomHandler
  implements DomHandler<Element, DOMResult>
{
  private DocumentBuilder builder;
  
  public W3CDomHandler()
  {
    this.builder = null;
  }
  
  public W3CDomHandler(DocumentBuilder builder)
  {
    if (builder == null) {
      throw new IllegalArgumentException();
    }
    this.builder = builder;
  }
  
  public DocumentBuilder getBuilder()
  {
    return this.builder;
  }
  
  public void setBuilder(DocumentBuilder builder)
  {
    this.builder = builder;
  }
  
  public DOMResult createUnmarshaller(ValidationEventHandler errorHandler)
  {
    if (this.builder == null) {
      return new DOMResult();
    }
    return new DOMResult(this.builder.newDocument());
  }
  
  public Element getElement(DOMResult r)
  {
    Node n = r.getNode();
    if ((n instanceof Document)) {
      return ((Document)n).getDocumentElement();
    }
    if ((n instanceof Element)) {
      return (Element)n;
    }
    if ((n instanceof DocumentFragment)) {
      return (Element)n.getChildNodes().item(0);
    }
    throw new IllegalStateException(n.toString());
  }
  
  public Source marshal(Element element, ValidationEventHandler errorHandler)
  {
    return new DOMSource(element);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\annotation\W3CDomHandler.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */