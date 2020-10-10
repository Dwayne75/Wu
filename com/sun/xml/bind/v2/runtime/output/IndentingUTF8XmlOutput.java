package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.v2.runtime.Name;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public final class IndentingUTF8XmlOutput
  extends UTF8XmlOutput
{
  private final Encoded indent8;
  private final int unitLen;
  private int depth = 0;
  private boolean seenText = false;
  
  public IndentingUTF8XmlOutput(OutputStream out, String indentStr, Encoded[] localNames)
  {
    super(out, localNames);
    if (indentStr != null)
    {
      Encoded e = new Encoded(indentStr);
      this.indent8 = new Encoded();
      this.indent8.ensureSize(e.len * 8);
      this.unitLen = e.len;
      for (int i = 0; i < 8; i++) {
        System.arraycopy(e.buf, 0, this.indent8.buf, this.unitLen * i, this.unitLen);
      }
    }
    else
    {
      this.indent8 = null;
      this.unitLen = 0;
    }
  }
  
  public void beginStartTag(int prefix, String localName)
    throws IOException
  {
    indentStartTag();
    super.beginStartTag(prefix, localName);
  }
  
  public void beginStartTag(Name name)
    throws IOException
  {
    indentStartTag();
    super.beginStartTag(name);
  }
  
  private void indentStartTag()
    throws IOException
  {
    closeStartTag();
    if (!this.seenText) {
      printIndent();
    }
    this.depth += 1;
    this.seenText = false;
  }
  
  public void endTag(Name name)
    throws IOException
  {
    indentEndTag();
    super.endTag(name);
  }
  
  public void endTag(int prefix, String localName)
    throws IOException
  {
    indentEndTag();
    super.endTag(prefix, localName);
  }
  
  private void indentEndTag()
    throws IOException
  {
    this.depth -= 1;
    if ((!this.closeStartTagPending) && (!this.seenText)) {
      printIndent();
    }
    this.seenText = false;
  }
  
  private void printIndent()
    throws IOException
  {
    write(10);
    int i = this.depth % 8;
    
    write(this.indent8.buf, 0, i * this.unitLen);
    
    i >>= 3;
    for (; i > 0; i--) {
      this.indent8.write(this);
    }
  }
  
  public void text(String value, boolean needSP)
    throws IOException
  {
    this.seenText = true;
    super.text(value, needSP);
  }
  
  public void text(Pcdata value, boolean needSP)
    throws IOException
  {
    this.seenText = true;
    super.text(value, needSP);
  }
  
  public void endDocument(boolean fragment)
    throws IOException, SAXException, XMLStreamException
  {
    write(10);
    super.endDocument(fragment);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\output\IndentingUTF8XmlOutput.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */