package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public class UTF8XmlOutput
  extends XmlOutputAbstractImpl
{
  protected final OutputStream out;
  private Encoded[] prefixes = new Encoded[8];
  private int prefixCount;
  private final Encoded[] localNames;
  private final Encoded textBuffer = new Encoded();
  protected final byte[] octetBuffer = new byte['Ѐ'];
  protected int octetBufferIndex;
  protected boolean closeStartTagPending = false;
  private String header;
  
  public UTF8XmlOutput(OutputStream out, Encoded[] localNames)
  {
    this.out = out;
    this.localNames = localNames;
    for (int i = 0; i < this.prefixes.length; i++) {
      this.prefixes[i] = new Encoded();
    }
  }
  
  public void setHeader(String header)
  {
    this.header = header;
  }
  
  public void startDocument(XMLSerializer serializer, boolean fragment, int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext)
    throws IOException, SAXException, XMLStreamException
  {
    super.startDocument(serializer, fragment, nsUriIndex2prefixIndex, nsContext);
    
    this.octetBufferIndex = 0;
    if (!fragment) {
      write(this.XML_DECL);
    }
    if (this.header != null)
    {
      this.textBuffer.set(this.header);
      this.textBuffer.write(this);
    }
  }
  
  public void endDocument(boolean fragment)
    throws IOException, SAXException, XMLStreamException
  {
    flushBuffer();
    super.endDocument(fragment);
  }
  
  protected final void closeStartTag()
    throws IOException
  {
    if (this.closeStartTagPending)
    {
      write(62);
      this.closeStartTagPending = false;
    }
  }
  
  public void beginStartTag(int prefix, String localName)
    throws IOException
  {
    closeStartTag();
    int base = pushNsDecls();
    write(60);
    writeName(prefix, localName);
    writeNsDecls(base);
  }
  
  public void beginStartTag(Name name)
    throws IOException
  {
    closeStartTag();
    int base = pushNsDecls();
    write(60);
    writeName(name);
    writeNsDecls(base);
  }
  
  private int pushNsDecls()
  {
    int total = this.nsContext.count();
    NamespaceContextImpl.Element ns = this.nsContext.getCurrent();
    if (total > this.prefixes.length)
    {
      int m = Math.max(total, this.prefixes.length * 2);
      Encoded[] buf = new Encoded[m];
      System.arraycopy(this.prefixes, 0, buf, 0, this.prefixes.length);
      for (int i = this.prefixes.length; i < buf.length; i++) {
        buf[i] = new Encoded();
      }
      this.prefixes = buf;
    }
    int base = Math.min(this.prefixCount, ns.getBase());
    int size = this.nsContext.count();
    for (int i = base; i < size; i++)
    {
      String p = this.nsContext.getPrefix(i);
      
      Encoded e = this.prefixes[i];
      if (p.length() == 0)
      {
        e.buf = EMPTY_BYTE_ARRAY;
        e.len = 0;
      }
      else
      {
        e.set(p);
        e.append(':');
      }
    }
    this.prefixCount = size;
    return base;
  }
  
  protected void writeNsDecls(int base)
    throws IOException
  {
    NamespaceContextImpl.Element ns = this.nsContext.getCurrent();
    int size = this.nsContext.count();
    for (int i = ns.getBase(); i < size; i++) {
      writeNsDecl(i);
    }
  }
  
  protected final void writeNsDecl(int prefixIndex)
    throws IOException
  {
    String p = this.nsContext.getPrefix(prefixIndex);
    if (p.length() == 0)
    {
      if ((this.nsContext.getCurrent().isRootElement()) && (this.nsContext.getNamespaceURI(prefixIndex).length() == 0)) {
        return;
      }
      write(this.XMLNS_EQUALS);
    }
    else
    {
      Encoded e = this.prefixes[prefixIndex];
      write(this.XMLNS_COLON);
      write(e.buf, 0, e.len - 1);
      write(this.EQUALS);
    }
    doText(this.nsContext.getNamespaceURI(prefixIndex), true);
    write(34);
  }
  
  private void writePrefix(int prefix)
    throws IOException
  {
    this.prefixes[prefix].write(this);
  }
  
  private void writeName(Name name)
    throws IOException
  {
    writePrefix(this.nsUriIndex2prefixIndex[name.nsUriIndex]);
    this.localNames[name.localNameIndex].write(this);
  }
  
  private void writeName(int prefix, String localName)
    throws IOException
  {
    writePrefix(prefix);
    this.textBuffer.set(localName);
    this.textBuffer.write(this);
  }
  
  public void attribute(Name name, String value)
    throws IOException
  {
    write(32);
    if (name.nsUriIndex == -1) {
      this.localNames[name.localNameIndex].write(this);
    } else {
      writeName(name);
    }
    write(this.EQUALS);
    doText(value, true);
    write(34);
  }
  
  public void attribute(int prefix, String localName, String value)
    throws IOException
  {
    write(32);
    if (prefix == -1)
    {
      this.textBuffer.set(localName);
      this.textBuffer.write(this);
    }
    else
    {
      writeName(prefix, localName);
    }
    write(this.EQUALS);
    doText(value, true);
    write(34);
  }
  
  public void endStartTag()
    throws IOException
  {
    this.closeStartTagPending = true;
  }
  
  public void endTag(Name name)
    throws IOException
  {
    if (this.closeStartTagPending)
    {
      write(this.EMPTY_TAG);
      this.closeStartTagPending = false;
    }
    else
    {
      write(this.CLOSE_TAG);
      writeName(name);
      write(62);
    }
  }
  
  public void endTag(int prefix, String localName)
    throws IOException
  {
    if (this.closeStartTagPending)
    {
      write(this.EMPTY_TAG);
      this.closeStartTagPending = false;
    }
    else
    {
      write(this.CLOSE_TAG);
      writeName(prefix, localName);
      write(62);
    }
  }
  
  public void text(String value, boolean needSP)
    throws IOException
  {
    closeStartTag();
    if (needSP) {
      write(32);
    }
    doText(value, false);
  }
  
  public void text(Pcdata value, boolean needSP)
    throws IOException
  {
    closeStartTag();
    if (needSP) {
      write(32);
    }
    value.writeTo(this);
  }
  
  private void doText(String value, boolean isAttribute)
    throws IOException
  {
    this.textBuffer.setEscape(value, isAttribute);
    this.textBuffer.write(this);
  }
  
  public final void text(int value)
    throws IOException
  {
    closeStartTag();
    
    boolean minus = value < 0;
    this.textBuffer.ensureSize(11);
    byte[] buf = this.textBuffer.buf;
    int idx = 11;
    do
    {
      int r = value % 10;
      if (r < 0) {
        r = -r;
      }
      buf[(--idx)] = ((byte)(0x30 | r));
      value /= 10;
    } while (value != 0);
    if (minus) {
      buf[(--idx)] = 45;
    }
    write(buf, idx, 11 - idx);
  }
  
  public void text(byte[] data, int dataLen)
    throws IOException
  {
    closeStartTag();
    
    int start = 0;
    while (dataLen > 0)
    {
      int batchSize = Math.min((this.octetBuffer.length - this.octetBufferIndex) / 4 * 3, dataLen);
      
      this.octetBufferIndex = DatatypeConverterImpl._printBase64Binary(data, start, batchSize, this.octetBuffer, this.octetBufferIndex);
      if (batchSize < dataLen) {
        flushBuffer();
      }
      start += batchSize;
      dataLen -= batchSize;
    }
  }
  
  public final void write(int i)
    throws IOException
  {
    if (this.octetBufferIndex < this.octetBuffer.length)
    {
      this.octetBuffer[(this.octetBufferIndex++)] = ((byte)i);
    }
    else
    {
      this.out.write(this.octetBuffer);
      this.octetBufferIndex = 1;
      this.octetBuffer[0] = ((byte)i);
    }
  }
  
  protected final void write(byte[] b)
    throws IOException
  {
    write(b, 0, b.length);
  }
  
  protected final void write(byte[] b, int start, int length)
    throws IOException
  {
    if (this.octetBufferIndex + length < this.octetBuffer.length)
    {
      System.arraycopy(b, start, this.octetBuffer, this.octetBufferIndex, length);
      this.octetBufferIndex += length;
    }
    else
    {
      this.out.write(this.octetBuffer, 0, this.octetBufferIndex);
      this.out.write(b, start, length);
      this.octetBufferIndex = 0;
    }
  }
  
  protected final void flushBuffer()
    throws IOException
  {
    this.out.write(this.octetBuffer, 0, this.octetBufferIndex);
    this.octetBufferIndex = 0;
  }
  
  static byte[] toBytes(String s)
  {
    byte[] buf = new byte[s.length()];
    for (int i = s.length() - 1; i >= 0; i--) {
      buf[i] = ((byte)s.charAt(i));
    }
    return buf;
  }
  
  private final byte[] XMLNS_EQUALS = (byte[])_XMLNS_EQUALS.clone();
  private final byte[] XMLNS_COLON = (byte[])_XMLNS_COLON.clone();
  private final byte[] EQUALS = (byte[])_EQUALS.clone();
  private final byte[] CLOSE_TAG = (byte[])_CLOSE_TAG.clone();
  private final byte[] EMPTY_TAG = (byte[])_EMPTY_TAG.clone();
  private final byte[] XML_DECL = (byte[])_XML_DECL.clone();
  private static final byte[] _XMLNS_EQUALS = toBytes(" xmlns=\"");
  private static final byte[] _XMLNS_COLON = toBytes(" xmlns:");
  private static final byte[] _EQUALS = toBytes("=\"");
  private static final byte[] _CLOSE_TAG = toBytes("</");
  private static final byte[] _EMPTY_TAG = toBytes("/>");
  private static final byte[] _XML_DECL = toBytes("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
  private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\output\UTF8XmlOutput.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */