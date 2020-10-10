package com.sun.xml.txw2.output;

import com.sun.xml.txw2.TxwException;
import java.util.Stack;
import javax.xml.transform.sax.SAXResult;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

public class SaxSerializer
  implements XmlSerializer
{
  private final ContentHandler writer;
  private final LexicalHandler lexical;
  
  public SaxSerializer(ContentHandler handler)
  {
    this(handler, null, true);
  }
  
  public SaxSerializer(ContentHandler handler, LexicalHandler lex)
  {
    this(handler, lex, true);
  }
  
  public SaxSerializer(ContentHandler handler, LexicalHandler lex, boolean indenting)
  {
    if (!indenting)
    {
      this.writer = handler;
      this.lexical = lex;
    }
    else
    {
      IndentingXMLFilter indenter = new IndentingXMLFilter(handler, lex);
      this.writer = indenter;
      this.lexical = indenter;
    }
  }
  
  public SaxSerializer(SAXResult result)
  {
    this(result.getHandler(), result.getLexicalHandler());
  }
  
  public void startDocument()
  {
    try
    {
      this.writer.startDocument();
    }
    catch (SAXException e)
    {
      throw new TxwException(e);
    }
  }
  
  private final Stack<String> prefixBindings = new Stack();
  
  public void writeXmlns(String prefix, String uri)
  {
    if (prefix == null) {
      prefix = "";
    }
    if (prefix.equals("xml")) {
      return;
    }
    this.prefixBindings.add(uri);
    this.prefixBindings.add(prefix);
  }
  
  private final Stack<String> elementBindings = new Stack();
  
  public void beginStartTag(String uri, String localName, String prefix)
  {
    this.elementBindings.add(getQName(prefix, localName));
    this.elementBindings.add(localName);
    this.elementBindings.add(uri);
  }
  
  private final AttributesImpl attrs = new AttributesImpl();
  
  public void writeAttribute(String uri, String localName, String prefix, StringBuilder value)
  {
    this.attrs.addAttribute(uri, localName, getQName(prefix, localName), "CDATA", value.toString());
  }
  
  public void endStartTag(String uri, String localName, String prefix)
  {
    try
    {
      while (this.prefixBindings.size() != 0) {
        this.writer.startPrefixMapping((String)this.prefixBindings.pop(), (String)this.prefixBindings.pop());
      }
      this.writer.startElement(uri, localName, getQName(prefix, localName), this.attrs);
      
      this.attrs.clear();
    }
    catch (SAXException e)
    {
      throw new TxwException(e);
    }
  }
  
  public void endTag()
  {
    try
    {
      this.writer.endElement((String)this.elementBindings.pop(), (String)this.elementBindings.pop(), (String)this.elementBindings.pop());
    }
    catch (SAXException e)
    {
      throw new TxwException(e);
    }
  }
  
  public void text(StringBuilder text)
  {
    try
    {
      this.writer.characters(text.toString().toCharArray(), 0, text.length());
    }
    catch (SAXException e)
    {
      throw new TxwException(e);
    }
  }
  
  public void cdata(StringBuilder text)
  {
    if (this.lexical == null) {
      throw new UnsupportedOperationException("LexicalHandler is needed to write PCDATA");
    }
    try
    {
      this.lexical.startCDATA();
      text(text);
      this.lexical.endCDATA();
    }
    catch (SAXException e)
    {
      throw new TxwException(e);
    }
  }
  
  public void comment(StringBuilder comment)
  {
    try
    {
      if (this.lexical == null) {
        throw new UnsupportedOperationException("LexicalHandler is needed to write comments");
      }
      this.lexical.comment(comment.toString().toCharArray(), 0, comment.length());
    }
    catch (SAXException e)
    {
      throw new TxwException(e);
    }
  }
  
  public void endDocument()
  {
    try
    {
      this.writer.endDocument();
    }
    catch (SAXException e)
    {
      throw new TxwException(e);
    }
  }
  
  public void flush() {}
  
  private static String getQName(String prefix, String localName)
  {
    String qName;
    String qName;
    if ((prefix == null) || (prefix.length() == 0)) {
      qName = localName;
    } else {
      qName = prefix + ':' + localName;
    }
    return qName;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\output\SaxSerializer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */