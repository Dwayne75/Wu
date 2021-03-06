package org.seamless.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class DOMParser<D extends DOM>
  implements ErrorHandler, EntityResolver
{
  private static Logger log = Logger.getLogger(DOMParser.class.getName());
  public static final URL XML_SCHEMA_RESOURCE = Thread.currentThread().getContextClassLoader().getResource("org/seamless/schemas/xml.xsd");
  protected Source[] schemaSources;
  protected Schema schema;
  
  public DOMParser()
  {
    this(null);
  }
  
  public DOMParser(Source[] schemaSources)
  {
    this.schemaSources = schemaSources;
  }
  
  public Schema getSchema()
  {
    if (this.schema == null) {
      try
      {
        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        
        schemaFactory.setResourceResolver(new CatalogResourceResolver(new HashMap() {}));
        if (this.schemaSources != null) {
          this.schema = schemaFactory.newSchema(this.schemaSources);
        } else {
          this.schema = schemaFactory.newSchema();
        }
      }
      catch (Exception ex)
      {
        throw new RuntimeException(ex);
      }
    }
    return this.schema;
  }
  
  protected abstract D createDOM(Document paramDocument);
  
  public DocumentBuilderFactory createFactory(boolean validating)
    throws ParserException
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try
    {
      factory.setNamespaceAware(true);
      if (validating)
      {
        factory.setXIncludeAware(true);
        
        factory.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
        factory.setFeature("http://apache.org/xml/features/xinclude/fixup-language", false);
        
        factory.setSchema(getSchema());
        
        factory.setFeature("http://apache.org/xml/features/validation/dynamic", true);
      }
    }
    catch (ParserConfigurationException ex)
    {
      throw new ParserException(ex);
    }
    return factory;
  }
  
  public Transformer createTransformer(String method, int indent, boolean standalone)
    throws ParserException
  {
    try
    {
      TransformerFactory transFactory = TransformerFactory.newInstance();
      if (indent > 0) {
        try
        {
          transFactory.setAttribute("indent-number", Integer.valueOf(indent));
        }
        catch (IllegalArgumentException ex) {}
      }
      Transformer transformer = transFactory.newTransformer();
      transformer.setOutputProperty("omit-xml-declaration", standalone ? "no" : "yes");
      if (standalone) {
        try
        {
          transformer.setOutputProperty("http://www.oracle.com/xml/is-standalone", "yes");
        }
        catch (IllegalArgumentException e) {}
      }
      transformer.setOutputProperty("indent", indent > 0 ? "yes" : "no");
      if (indent > 0) {
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));
      }
      transformer.setOutputProperty("method", method);
      
      return transformer;
    }
    catch (Exception ex)
    {
      throw new ParserException(ex);
    }
  }
  
  public D createDocument()
  {
    try
    {
      return createDOM(createFactory(false).newDocumentBuilder().newDocument());
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public D parse(URL url)
    throws ParserException
  {
    return parse(url, true);
  }
  
  public D parse(String string)
    throws ParserException
  {
    return parse(string, true);
  }
  
  public D parse(File file)
    throws ParserException
  {
    return parse(file, true);
  }
  
  public D parse(InputStream stream)
    throws ParserException
  {
    return parse(stream, true);
  }
  
  public D parse(URL url, boolean validate)
    throws ParserException
  {
    if (url == null) {
      throw new IllegalArgumentException("Can't parse null URL");
    }
    try
    {
      return parse(url.openStream(), validate);
    }
    catch (Exception ex)
    {
      throw new ParserException("Parsing URL failed: " + url, ex);
    }
  }
  
  public D parse(String string, boolean validate)
    throws ParserException
  {
    if (string == null) {
      throw new IllegalArgumentException("Can't parse null string");
    }
    return parse(new InputSource(new StringReader(string)), validate);
  }
  
  public D parse(File file, boolean validate)
    throws ParserException
  {
    if (file == null) {
      throw new IllegalArgumentException("Can't parse null file");
    }
    try
    {
      return parse(file.toURI().toURL(), validate);
    }
    catch (Exception ex)
    {
      throw new ParserException("Parsing file failed: " + file, ex);
    }
  }
  
  public D parse(InputStream stream, boolean validate)
    throws ParserException
  {
    return parse(new InputSource(stream), validate);
  }
  
  public D parse(InputSource source, boolean validate)
    throws ParserException
  {
    try
    {
      DocumentBuilder parser = createFactory(validate).newDocumentBuilder();
      
      parser.setEntityResolver(this);
      
      parser.setErrorHandler(this);
      
      Document dom = parser.parse(source);
      
      dom.normalizeDocument();
      
      return createDOM(dom);
    }
    catch (Exception ex)
    {
      throw unwrapException(ex);
    }
  }
  
  public void validate(URL url)
    throws ParserException
  {
    if (url == null) {
      throw new IllegalArgumentException("Can't validate null URL");
    }
    log.fine("Validating XML of URL: " + url);
    validate(new StreamSource(url.toString()));
  }
  
  public void validate(String string)
    throws ParserException
  {
    if (string == null) {
      throw new IllegalArgumentException("Can't validate null string");
    }
    log.fine("Validating XML string characters: " + string.length());
    validate(new SAXSource(new InputSource(new StringReader(string))));
  }
  
  public void validate(Document document)
    throws ParserException
  {
    validate(new DOMSource(document));
  }
  
  public void validate(DOM dom)
    throws ParserException
  {
    validate(new DOMSource(dom.getW3CDocument()));
  }
  
  public void validate(Source source)
    throws ParserException
  {
    try
    {
      Validator validator = getSchema().newValidator();
      validator.setErrorHandler(this);
      validator.validate(source);
    }
    catch (Exception ex)
    {
      throw unwrapException(ex);
    }
  }
  
  public XPathFactory createXPathFactory()
  {
    return XPathFactory.newInstance();
  }
  
  public XPath createXPath(NamespaceContext nsContext)
  {
    XPath xpath = createXPathFactory().newXPath();
    xpath.setNamespaceContext(nsContext);
    return xpath;
  }
  
  public XPath createXPath(XPathFactory factory, NamespaceContext nsContext)
  {
    XPath xpath = factory.newXPath();
    xpath.setNamespaceContext(nsContext);
    return xpath;
  }
  
  public Object getXPathResult(DOM dom, XPath xpath, String expr, QName result)
  {
    return getXPathResult(dom.getW3CDocument(), xpath, expr, result);
  }
  
  public Object getXPathResult(DOMElement element, XPath xpath, String expr, QName result)
  {
    return getXPathResult(element.getW3CElement(), xpath, expr, result);
  }
  
  public Object getXPathResult(Node context, XPath xpath, String expr, QName result)
  {
    try
    {
      log.fine("Evaluating xpath query: " + expr);
      return xpath.evaluate(expr, context, result);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public String print(DOM dom)
    throws ParserException
  {
    return print(dom, 4, true);
  }
  
  public String print(DOM dom, int indent)
    throws ParserException
  {
    return print(dom, indent, true);
  }
  
  public String print(DOM dom, boolean standalone)
    throws ParserException
  {
    return print(dom, 4, standalone);
  }
  
  public String print(DOM dom, int indent, boolean standalone)
    throws ParserException
  {
    return print(dom.getW3CDocument(), indent, standalone);
  }
  
  public String print(Document document, int indent, boolean standalone)
    throws ParserException
  {
    removeIgnorableWSNodes(document.getDocumentElement());
    return print(new DOMSource(document.getDocumentElement()), indent, standalone);
  }
  
  public String print(String string, int indent, boolean standalone)
    throws ParserException
  {
    return print(new StreamSource(new StringReader(string)), indent, standalone);
  }
  
  public String print(Source source, int indent, boolean standalone)
    throws ParserException
  {
    try
    {
      Transformer transformer = createTransformer("xml", indent, standalone);
      transformer.setOutputProperty("encoding", "utf-8");
      
      StringWriter out = new StringWriter();
      transformer.transform(source, new StreamResult(out));
      out.flush();
      
      return out.toString();
    }
    catch (Exception e)
    {
      throw new ParserException(e);
    }
  }
  
  public String printHTML(Document dom)
    throws ParserException
  {
    return printHTML(dom, 4, true, true);
  }
  
  public String printHTML(Document dom, int indent, boolean standalone, boolean doctype)
    throws ParserException
  {
    dom = (Document)dom.cloneNode(true);
    
    accept(dom.getDocumentElement(), new NodeVisitor((short)4)
    {
      public void visit(Node node)
      {
        CDATASection cdata = (CDATASection)node;
        cdata.getParentNode().setTextContent(cdata.getData());
      }
    });
    removeIgnorableWSNodes(dom.getDocumentElement());
    try
    {
      Transformer transformer = createTransformer("html", indent, standalone);
      if (doctype)
      {
        transformer.setOutputProperty("doctype-public", "-//W3C//DTD HTML 4.01 Transitional//EN");
        transformer.setOutputProperty("doctype-system", "http://www.w3.org/TR/html4/loose.dtd");
      }
      StringWriter out = new StringWriter();
      transformer.transform(new DOMSource(dom), new StreamResult(out));
      out.flush();
      String output = out.toString();
      
      String meta = "\\s*<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">";
      output = output.replaceFirst(meta, "");
      
      String xmlns = "<html xmlns=\"http://www.w3.org/1999/xhtml\">";
      return output.replaceFirst(xmlns, "<html>");
    }
    catch (Exception ex)
    {
      throw new ParserException(ex);
    }
  }
  
  public void removeIgnorableWSNodes(Element element)
  {
    Node nextNode = element.getFirstChild();
    while (nextNode != null)
    {
      Node child = nextNode;
      nextNode = child.getNextSibling();
      if (isIgnorableWSNode(child)) {
        element.removeChild(child);
      } else if (child.getNodeType() == 1) {
        removeIgnorableWSNodes((Element)child);
      }
    }
  }
  
  public boolean isIgnorableWSNode(Node node)
  {
    return (node.getNodeType() == 3) && (node.getTextContent().matches("[\\t\\n\\x0B\\f\\r\\s]+"));
  }
  
  public void warning(SAXParseException e)
    throws SAXException
  {
    log.warning(e.toString());
  }
  
  public void error(SAXParseException e)
    throws SAXException
  {
    throw new SAXException(new ParserException(e));
  }
  
  public void fatalError(SAXParseException e)
    throws SAXException
  {
    throw new SAXException(new ParserException(e));
  }
  
  protected ParserException unwrapException(Exception ex)
  {
    if ((ex.getCause() != null) && ((ex.getCause() instanceof ParserException))) {
      return (ParserException)ex.getCause();
    }
    return new ParserException(ex);
  }
  
  public InputSource resolveEntity(String publicId, String systemId)
    throws SAXException, IOException
  {
    InputSource is;
    InputSource is;
    if (systemId.startsWith("file://")) {
      is = new InputSource(new FileInputStream(new File(URI.create(systemId))));
    } else {
      is = new InputSource(new ByteArrayInputStream(new byte[0]));
    }
    is.setPublicId(publicId);
    is.setSystemId(systemId);
    return is;
  }
  
  public static String escape(String string)
  {
    return escape(string, false, false);
  }
  
  public static String escape(String string, boolean convertNewlines, boolean convertSpaces)
  {
    if (string == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < string.length(); i++)
    {
      String entity = null;
      char c = string.charAt(i);
      switch (c)
      {
      case '<': 
        entity = "&#60;";
        break;
      case '>': 
        entity = "&#62;";
        break;
      case '&': 
        entity = "&#38;";
        break;
      case '"': 
        entity = "&#34;";
      }
      if (entity != null) {
        sb.append(entity);
      } else {
        sb.append(c);
      }
    }
    String result = sb.toString();
    if (convertSpaces)
    {
      Matcher matcher = Pattern.compile("(\\n+)(\\s*)(.*)").matcher(result);
      StringBuffer temp = new StringBuffer();
      while (matcher.find())
      {
        String group = matcher.group(2);
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < group.length(); i++) {
          spaces.append("&#160;");
        }
        matcher.appendReplacement(temp, "$1" + spaces.toString() + "$3");
      }
      matcher.appendTail(temp);
      result = temp.toString();
    }
    if (convertNewlines) {
      result = result.replaceAll("\n", "<br/>");
    }
    return result;
  }
  
  public static String stripElements(String xml)
  {
    if (xml == null) {
      return null;
    }
    return xml.replaceAll("<([a-zA-Z]|/).*?>", "");
  }
  
  public static void accept(Node node, NodeVisitor visitor)
  {
    if (node == null) {
      return;
    }
    if (visitor.isHalted()) {
      return;
    }
    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++)
    {
      Node child = children.item(i);
      boolean cont = true;
      if (child.getNodeType() == visitor.nodeType)
      {
        visitor.visit(child);
        if (visitor.isHalted()) {
          break;
        }
      }
      accept(child, visitor);
    }
  }
  
  public static abstract class NodeVisitor
  {
    private short nodeType;
    
    protected NodeVisitor(short nodeType)
    {
      assert (nodeType < 12);
      this.nodeType = nodeType;
    }
    
    public boolean isHalted()
    {
      return false;
    }
    
    public abstract void visit(Node paramNode);
  }
  
  public static String wrap(String wrapperName, String fragment)
  {
    return wrap(wrapperName, null, fragment);
  }
  
  public static String wrap(String wrapperName, String xmlns, String fragment)
  {
    StringBuilder wrapper = new StringBuilder();
    wrapper.append("<").append(wrapperName);
    if (xmlns != null) {
      wrapper.append(" xmlns=\"").append(xmlns).append("\"");
    }
    wrapper.append(">");
    wrapper.append(fragment);
    wrapper.append("</").append(wrapperName).append(">");
    return wrapper.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\xml\DOMParser.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */