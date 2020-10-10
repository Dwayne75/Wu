package com.sun.tools.xjc.reader.internalizer;

import com.sun.xml.xsom.parser.XMLParser;
import java.io.IOException;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class DOMForestParser
  implements XMLParser
{
  private final DOMForest forest;
  private final DOMForestScanner scanner;
  private final XMLParser fallbackParser;
  
  DOMForestParser(DOMForest forest, XMLParser fallbackParser)
  {
    this.forest = forest;
    this.scanner = new DOMForestScanner(forest);
    this.fallbackParser = fallbackParser;
  }
  
  public void parse(InputSource source, ContentHandler contentHandler, ErrorHandler errorHandler, EntityResolver entityResolver)
    throws SAXException, IOException
  {
    String systemId = source.getSystemId();
    Document dom = this.forest.get(systemId);
    if (dom == null)
    {
      this.fallbackParser.parse(source, contentHandler, errorHandler, entityResolver);
      return;
    }
    this.scanner.scan(dom, contentHandler);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\internalizer\DOMForestParser.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */