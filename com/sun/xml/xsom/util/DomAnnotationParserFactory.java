package com.sun.xml.xsom.util;

import com.sun.xml.xsom.parser.AnnotationContext;
import com.sun.xml.xsom.parser.AnnotationParser;
import com.sun.xml.xsom.parser.AnnotationParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;

public class DomAnnotationParserFactory
  implements AnnotationParserFactory
{
  public AnnotationParser create()
  {
    return new AnnotationParserImpl();
  }
  
  private static final SAXTransformerFactory stf = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
  
  private static class AnnotationParserImpl
    extends AnnotationParser
  {
    private final TransformerHandler transformer;
    private DOMResult result;
    
    AnnotationParserImpl()
    {
      try
      {
        this.transformer = DomAnnotationParserFactory.stf.newTransformerHandler();
      }
      catch (TransformerConfigurationException e)
      {
        throw new Error(e);
      }
    }
    
    public ContentHandler getContentHandler(AnnotationContext context, String parentElementName, ErrorHandler errorHandler, EntityResolver entityResolver)
    {
      this.result = new DOMResult();
      this.transformer.setResult(this.result);
      return this.transformer;
    }
    
    public Object getResult(Object existing)
    {
      Document dom = (Document)this.result.getNode();
      Element e = dom.getDocumentElement();
      if ((existing instanceof Element))
      {
        Element prev = (Element)existing;
        Node anchor = e.getFirstChild();
        while (prev.getFirstChild() != null)
        {
          Node move = prev.getFirstChild();
          e.insertBefore(e.getOwnerDocument().adoptNode(move), anchor);
        }
      }
      return e;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\util\DomAnnotationParserFactory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */