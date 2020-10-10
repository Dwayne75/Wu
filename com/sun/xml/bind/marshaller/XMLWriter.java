package com.sun.xml.bind.marshaller;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map.Entry;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

public class XMLWriter
  extends XMLFilterImpl
{
  public XMLWriter(Writer writer, String encoding, CharacterEscapeHandler _escapeHandler)
  {
    init(writer, encoding);
    this.escapeHandler = _escapeHandler;
  }
  
  public XMLWriter(Writer writer, String encoding)
  {
    this(writer, encoding, DumbEscapeHandler.theInstance);
  }
  
  private void init(Writer writer, String encoding)
  {
    setOutput(writer, encoding);
  }
  
  public void reset()
  {
    this.elementLevel = 0;
    this.startTagIsClosed = true;
  }
  
  public void flush()
    throws IOException
  {
    this.output.flush();
  }
  
  public void setOutput(Writer writer, String _encoding)
  {
    if (writer == null) {
      this.output = new OutputStreamWriter(System.out);
    } else {
      this.output = writer;
    }
    this.encoding = _encoding;
  }
  
  public void setXmlDecl(boolean _writeXmlDecl)
  {
    this.writeXmlDecl = _writeXmlDecl;
  }
  
  public void setHeader(String _header)
  {
    this.header = _header;
  }
  
  private final HashMap<String, String> locallyDeclaredPrefix = new HashMap();
  
  public void startPrefixMapping(String prefix, String uri)
    throws SAXException
  {
    this.locallyDeclaredPrefix.put(prefix, uri);
  }
  
  public void startDocument()
    throws SAXException
  {
    try
    {
      reset();
      if (this.writeXmlDecl)
      {
        String e = "";
        if (this.encoding != null) {
          e = " encoding=\"" + this.encoding + '"';
        }
        writeXmlDecl("<?xml version=\"1.0\"" + e + " standalone=\"yes\"?>");
      }
      if (this.header != null) {
        write(this.header);
      }
      super.startDocument();
    }
    catch (IOException e)
    {
      throw new SAXException(e);
    }
  }
  
  protected void writeXmlDecl(String decl)
    throws IOException
  {
    write(decl);
  }
  
  public void endDocument()
    throws SAXException
  {
    try
    {
      super.endDocument();
      flush();
    }
    catch (IOException e)
    {
      throw new SAXException(e);
    }
  }
  
  public void startElement(String uri, String localName, String qName, Attributes atts)
    throws SAXException
  {
    try
    {
      if (!this.startTagIsClosed) {
        write(">");
      }
      this.elementLevel += 1;
      
      write('<');
      write(qName);
      writeAttributes(atts);
      if (!this.locallyDeclaredPrefix.isEmpty())
      {
        for (Map.Entry<String, String> e : this.locallyDeclaredPrefix.entrySet())
        {
          String p = (String)e.getKey();
          String u = (String)e.getValue();
          if (u == null) {
            u = "";
          }
          write(' ');
          if ("".equals(p))
          {
            write("xmlns=\"");
          }
          else
          {
            write("xmlns:");
            write(p);
            write("=\"");
          }
          char[] ch = u.toCharArray();
          writeEsc(ch, 0, ch.length, true);
          write('"');
        }
        this.locallyDeclaredPrefix.clear();
      }
      super.startElement(uri, localName, qName, atts);
      this.startTagIsClosed = false;
    }
    catch (IOException e)
    {
      throw new SAXException(e);
    }
  }
  
  public void endElement(String uri, String localName, String qName)
    throws SAXException
  {
    try
    {
      if (this.startTagIsClosed)
      {
        write("</");
        write(qName);
        write('>');
      }
      else
      {
        write("/>");
        this.startTagIsClosed = true;
      }
      super.endElement(uri, localName, qName);
      
      this.elementLevel -= 1;
    }
    catch (IOException e)
    {
      throw new SAXException(e);
    }
  }
  
  public void characters(char[] ch, int start, int len)
    throws SAXException
  {
    try
    {
      if (!this.startTagIsClosed)
      {
        write('>');
        this.startTagIsClosed = true;
      }
      writeEsc(ch, start, len, false);
      super.characters(ch, start, len);
    }
    catch (IOException e)
    {
      throw new SAXException(e);
    }
  }
  
  public void ignorableWhitespace(char[] ch, int start, int length)
    throws SAXException
  {
    try
    {
      writeEsc(ch, start, length, false);
      super.ignorableWhitespace(ch, start, length);
    }
    catch (IOException e)
    {
      throw new SAXException(e);
    }
  }
  
  public void processingInstruction(String target, String data)
    throws SAXException
  {
    try
    {
      if (!this.startTagIsClosed)
      {
        write('>');
        this.startTagIsClosed = true;
      }
      write("<?");
      write(target);
      write(' ');
      write(data);
      write("?>");
      if (this.elementLevel < 1) {
        write('\n');
      }
      super.processingInstruction(target, data);
    }
    catch (IOException e)
    {
      throw new SAXException(e);
    }
  }
  
  public void startElement(String uri, String localName)
    throws SAXException
  {
    startElement(uri, localName, "", this.EMPTY_ATTS);
  }
  
  public void startElement(String localName)
    throws SAXException
  {
    startElement("", localName, "", this.EMPTY_ATTS);
  }
  
  public void endElement(String uri, String localName)
    throws SAXException
  {
    endElement(uri, localName, "");
  }
  
  public void endElement(String localName)
    throws SAXException
  {
    endElement("", localName, "");
  }
  
  public void dataElement(String uri, String localName, String qName, Attributes atts, String content)
    throws SAXException
  {
    startElement(uri, localName, qName, atts);
    characters(content);
    endElement(uri, localName, qName);
  }
  
  public void dataElement(String uri, String localName, String content)
    throws SAXException
  {
    dataElement(uri, localName, "", this.EMPTY_ATTS, content);
  }
  
  public void dataElement(String localName, String content)
    throws SAXException
  {
    dataElement("", localName, "", this.EMPTY_ATTS, content);
  }
  
  public void characters(String data)
    throws SAXException
  {
    try
    {
      if (!this.startTagIsClosed)
      {
        write('>');
        this.startTagIsClosed = true;
      }
      char[] ch = data.toCharArray();
      characters(ch, 0, ch.length);
    }
    catch (IOException e)
    {
      throw new SAXException(e);
    }
  }
  
  protected final void write(char c)
    throws IOException
  {
    this.output.write(c);
  }
  
  protected final void write(String s)
    throws IOException
  {
    this.output.write(s);
  }
  
  private void writeAttributes(Attributes atts)
    throws IOException
  {
    int len = atts.getLength();
    for (int i = 0; i < len; i++)
    {
      char[] ch = atts.getValue(i).toCharArray();
      write(' ');
      write(atts.getQName(i));
      write("=\"");
      writeEsc(ch, 0, ch.length, true);
      write('"');
    }
  }
  
  private void writeEsc(char[] ch, int start, int length, boolean isAttVal)
    throws IOException
  {
    this.escapeHandler.escape(ch, start, length, isAttVal, this.output);
  }
  
  private final Attributes EMPTY_ATTS = new AttributesImpl();
  private int elementLevel = 0;
  private Writer output;
  private String encoding;
  private boolean writeXmlDecl = true;
  private String header = null;
  private final CharacterEscapeHandler escapeHandler;
  private boolean startTagIsClosed = true;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\marshaller\XMLWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */