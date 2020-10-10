package com.sun.tools.xjc.reader.internalizer;

import com.sun.xml.xsom.parser.JAXPParser;
import com.sun.xml.xsom.parser.XMLParser;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

public final class DOMForest
{
  private final Map core = new HashMap();
  public final LocatorTable locatorTable = new LocatorTable();
  public final Set outerMostBindings = new HashSet();
  private EntityResolver entityResolver = null;
  private ErrorHandler errorHandler = null;
  protected final InternalizationLogic logic;
  private final SAXParserFactory parserFactory;
  private final DocumentBuilder documentBuilder;
  
  public DOMForest(SAXParserFactory parserFactory, DocumentBuilder documentBuilder, InternalizationLogic logic)
  {
    this.parserFactory = parserFactory;
    this.documentBuilder = documentBuilder;
    this.logic = logic;
  }
  
  public DOMForest(InternalizationLogic logic)
    throws ParserConfigurationException
  {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    this.documentBuilder = dbf.newDocumentBuilder();
    
    this.parserFactory = SAXParserFactory.newInstance();
    this.parserFactory.setNamespaceAware(true);
    
    this.logic = logic;
  }
  
  public Document get(String systemId)
  {
    Document doc = (Document)this.core.get(systemId);
    if ((doc == null) && (systemId.startsWith("file:/")) && (!systemId.startsWith("file://"))) {
      doc = (Document)this.core.get("file://" + systemId.substring(5));
    }
    return doc;
  }
  
  public String getSystemId(Document dom)
  {
    for (Iterator itr = this.core.entrySet().iterator(); itr.hasNext();)
    {
      Map.Entry e = (Map.Entry)itr.next();
      if (e.getValue() == dom) {
        return (String)e.getKey();
      }
    }
    return null;
  }
  
  public Document parse(InputSource source)
    throws SAXException, IOException
  {
    if (source.getSystemId() == null) {
      throw new IllegalArgumentException();
    }
    return parse(source.getSystemId(), source);
  }
  
  public Document parse(String systemId)
    throws SAXException, IOException
  {
    if (this.core.containsKey(systemId)) {
      return (Document)this.core.get(systemId);
    }
    InputSource is = null;
    if (this.entityResolver != null) {
      is = this.entityResolver.resolveEntity(null, systemId);
    }
    if (is == null) {
      is = new InputSource(systemId);
    }
    return parse(systemId, is);
  }
  
  public Document parse(String systemId, InputSource inputSource)
    throws SAXException, IOException
  {
    Document dom = this.documentBuilder.newDocument();
    
    this.core.put(systemId, dom);
    try
    {
      XMLReader reader = this.parserFactory.newSAXParser().getXMLReader();
      XMLFilter f = this.logic.createExternalReferenceFinder(this);
      f.setParent(reader);
      reader = f;
      
      reader = new VersionChecker(reader);
      reader = new WhitespaceStripper(reader);
      reader.setContentHandler(new DOMBuilder(dom, this.locatorTable, this.outerMostBindings));
      if (this.errorHandler != null) {
        reader.setErrorHandler(this.errorHandler);
      }
      if (this.entityResolver != null) {
        reader.setEntityResolver(this.entityResolver);
      }
      reader.parse(inputSource);
    }
    catch (ParserConfigurationException e)
    {
      e.printStackTrace();
    }
    return dom;
  }
  
  public void transform()
    throws SAXException
  {
    Internalizer.transform(this);
  }
  
  public XMLParser createParser()
  {
    return new DOMForestParser(this, new JAXPParser());
  }
  
  public EntityResolver getEntityResolver()
  {
    return this.entityResolver;
  }
  
  public void setEntityResolver(EntityResolver entityResolver)
  {
    this.entityResolver = entityResolver;
  }
  
  public ErrorHandler getErrorHandler()
  {
    return this.errorHandler;
  }
  
  public void setErrorHandler(ErrorHandler errorHandler)
  {
    this.errorHandler = errorHandler;
  }
  
  public Document[] listDocuments()
  {
    return (Document[])this.core.values().toArray(new Document[this.core.size()]);
  }
  
  public String[] listSystemIDs()
  {
    return (String[])this.core.keySet().toArray(new String[this.core.keySet().size()]);
  }
  
  public void dump(OutputStream out)
    throws IOException
  {
    try
    {
      it = TransformerFactory.newInstance().newTransformer();
      for (itr = this.core.entrySet().iterator(); itr.hasNext();)
      {
        Map.Entry e = (Map.Entry)itr.next();
        
        out.write(("---<< " + e.getKey() + "\n").getBytes());
        
        it.transform(new DOMSource((Document)e.getValue()), new StreamResult(out));
        
        out.write("\n\n\n".getBytes());
      }
    }
    catch (TransformerException e)
    {
      Transformer it;
      Iterator itr;
      e.printStackTrace();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\internalizer\DOMForest.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */