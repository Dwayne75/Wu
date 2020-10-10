package com.sun.xml.txw2.output;

import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

public class IndentingXMLFilter
  extends XMLFilterImpl
  implements LexicalHandler
{
  private LexicalHandler lexical;
  
  public IndentingXMLFilter() {}
  
  public IndentingXMLFilter(ContentHandler handler)
  {
    setContentHandler(handler);
  }
  
  public IndentingXMLFilter(ContentHandler handler, LexicalHandler lexical)
  {
    setContentHandler(handler);
    setLexicalHandler(lexical);
  }
  
  public LexicalHandler getLexicalHandler()
  {
    return this.lexical;
  }
  
  public void setLexicalHandler(LexicalHandler lexical)
  {
    this.lexical = lexical;
  }
  
  /**
   * @deprecated
   */
  public int getIndentStep()
  {
    return this.indentStep.length();
  }
  
  /**
   * @deprecated
   */
  public void setIndentStep(int indentStep)
  {
    StringBuilder s = new StringBuilder();
    for (; indentStep > 0; indentStep--) {
      s.append(' ');
    }
    setIndentStep(s.toString());
  }
  
  public void setIndentStep(String s)
  {
    this.indentStep = s;
  }
  
  public void startElement(String uri, String localName, String qName, Attributes atts)
    throws SAXException
  {
    this.stateStack.push(SEEN_ELEMENT);
    this.state = SEEN_NOTHING;
    if (this.depth > 0) {
      writeNewLine();
    }
    doIndent();
    super.startElement(uri, localName, qName, atts);
    this.depth += 1;
  }
  
  private void writeNewLine()
    throws SAXException
  {
    super.characters(NEWLINE, 0, NEWLINE.length);
  }
  
  private static final char[] NEWLINE = { '\n' };
  
  public void endElement(String uri, String localName, String qName)
    throws SAXException
  {
    this.depth -= 1;
    if (this.state == SEEN_ELEMENT)
    {
      writeNewLine();
      doIndent();
    }
    super.endElement(uri, localName, qName);
    this.state = this.stateStack.pop();
  }
  
  public void characters(char[] ch, int start, int length)
    throws SAXException
  {
    this.state = SEEN_DATA;
    super.characters(ch, start, length);
  }
  
  public void comment(char[] ch, int start, int length)
    throws SAXException
  {
    if (this.depth > 0) {
      writeNewLine();
    }
    doIndent();
    if (this.lexical != null) {
      this.lexical.comment(ch, start, length);
    }
  }
  
  public void startDTD(String name, String publicId, String systemId)
    throws SAXException
  {
    if (this.lexical != null) {
      this.lexical.startDTD(name, publicId, systemId);
    }
  }
  
  public void endDTD()
    throws SAXException
  {
    if (this.lexical != null) {
      this.lexical.endDTD();
    }
  }
  
  public void startEntity(String name)
    throws SAXException
  {
    if (this.lexical != null) {
      this.lexical.startEntity(name);
    }
  }
  
  public void endEntity(String name)
    throws SAXException
  {
    if (this.lexical != null) {
      this.lexical.endEntity(name);
    }
  }
  
  public void startCDATA()
    throws SAXException
  {
    if (this.lexical != null) {
      this.lexical.startCDATA();
    }
  }
  
  public void endCDATA()
    throws SAXException
  {
    if (this.lexical != null) {
      this.lexical.endCDATA();
    }
  }
  
  private void doIndent()
    throws SAXException
  {
    if (this.depth > 0)
    {
      char[] ch = this.indentStep.toCharArray();
      for (int i = 0; i < this.depth; i++) {
        characters(ch, 0, ch.length);
      }
    }
  }
  
  private static final Object SEEN_NOTHING = new Object();
  private static final Object SEEN_ELEMENT = new Object();
  private static final Object SEEN_DATA = new Object();
  private Object state = SEEN_NOTHING;
  private Stack<Object> stateStack = new Stack();
  private String indentStep = "";
  private int depth = 0;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\output\IndentingXMLFilter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */