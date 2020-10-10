package com.sun.xml.bind.marshaller;

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class DataWriter
  extends XMLWriter
{
  public DataWriter(Writer writer, String encoding, CharacterEscapeHandler _escapeHandler)
  {
    super(writer, encoding, _escapeHandler);
  }
  
  public DataWriter(Writer writer, String encoding)
  {
    this(writer, encoding, DumbEscapeHandler.theInstance);
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
    StringBuilder buf = new StringBuilder();
    for (; indentStep > 0; indentStep--) {
      buf.append(' ');
    }
    setIndentStep(buf.toString());
  }
  
  public void setIndentStep(String s)
  {
    this.indentStep = s;
  }
  
  public void reset()
  {
    this.depth = 0;
    this.state = SEEN_NOTHING;
    this.stateStack = new Stack();
    super.reset();
  }
  
  protected void writeXmlDecl(String decl)
    throws IOException
  {
    super.writeXmlDecl(decl);
    write('\n');
  }
  
  public void startElement(String uri, String localName, String qName, Attributes atts)
    throws SAXException
  {
    this.stateStack.push(SEEN_ELEMENT);
    this.state = SEEN_NOTHING;
    if (this.depth > 0) {
      super.characters("\n");
    }
    doIndent();
    super.startElement(uri, localName, qName, atts);
    this.depth += 1;
  }
  
  public void endElement(String uri, String localName, String qName)
    throws SAXException
  {
    this.depth -= 1;
    if (this.state == SEEN_ELEMENT)
    {
      super.characters("\n");
      doIndent();
    }
    super.endElement(uri, localName, qName);
    this.state = this.stateStack.pop();
  }
  
  public void endDocument()
    throws SAXException
  {
    try
    {
      write('\n');
    }
    catch (IOException e)
    {
      throw new SAXException(e);
    }
    super.endDocument();
  }
  
  public void characters(char[] ch, int start, int length)
    throws SAXException
  {
    this.state = SEEN_DATA;
    super.characters(ch, start, length);
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\marshaller\DataWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */