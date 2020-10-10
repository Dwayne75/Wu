package com.sun.xml.dtdparser;

import java.io.ByteArrayInputStream;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.util.Hashtable;

final class XmlReader
  extends Reader
{
  private static final int MAXPUSHBACK = 512;
  private Reader in;
  private String assignedEncoding;
  private boolean closed;
  
  public static Reader createReader(InputStream in)
    throws IOException
  {
    return new XmlReader(in);
  }
  
  public static Reader createReader(InputStream in, String encoding)
    throws IOException
  {
    if (encoding == null) {
      return new XmlReader(in);
    }
    if (("UTF-8".equalsIgnoreCase(encoding)) || ("UTF8".equalsIgnoreCase(encoding))) {
      return new Utf8Reader(in);
    }
    if (("US-ASCII".equalsIgnoreCase(encoding)) || ("ASCII".equalsIgnoreCase(encoding))) {
      return new AsciiReader(in);
    }
    if ("ISO-8859-1".equalsIgnoreCase(encoding)) {
      return new Iso8859_1Reader(in);
    }
    return new InputStreamReader(in, std2java(encoding));
  }
  
  private static final Hashtable charsets = new Hashtable(31);
  
  static
  {
    charsets.put("UTF-16", "Unicode");
    charsets.put("ISO-10646-UCS-2", "Unicode");
    
    charsets.put("EBCDIC-CP-US", "cp037");
    charsets.put("EBCDIC-CP-CA", "cp037");
    charsets.put("EBCDIC-CP-NL", "cp037");
    charsets.put("EBCDIC-CP-WT", "cp037");
    
    charsets.put("EBCDIC-CP-DK", "cp277");
    charsets.put("EBCDIC-CP-NO", "cp277");
    charsets.put("EBCDIC-CP-FI", "cp278");
    charsets.put("EBCDIC-CP-SE", "cp278");
    
    charsets.put("EBCDIC-CP-IT", "cp280");
    charsets.put("EBCDIC-CP-ES", "cp284");
    charsets.put("EBCDIC-CP-GB", "cp285");
    charsets.put("EBCDIC-CP-FR", "cp297");
    
    charsets.put("EBCDIC-CP-AR1", "cp420");
    charsets.put("EBCDIC-CP-HE", "cp424");
    charsets.put("EBCDIC-CP-BE", "cp500");
    charsets.put("EBCDIC-CP-CH", "cp500");
    
    charsets.put("EBCDIC-CP-ROECE", "cp870");
    charsets.put("EBCDIC-CP-YU", "cp870");
    charsets.put("EBCDIC-CP-IS", "cp871");
    charsets.put("EBCDIC-CP-AR2", "cp918");
  }
  
  private static String std2java(String encoding)
  {
    String temp = encoding.toUpperCase();
    temp = (String)charsets.get(temp);
    return temp != null ? temp : encoding;
  }
  
  public String getEncoding()
  {
    return this.assignedEncoding;
  }
  
  private XmlReader(InputStream stream)
    throws IOException
  {
    super(stream);
    PushbackInputStream pb;
    PushbackInputStream pb;
    if ((stream instanceof PushbackInputStream)) {
      pb = (PushbackInputStream)stream;
    } else {
      pb = new PushbackInputStream(stream, 512);
    }
    byte[] buf = new byte[4];
    int len = pb.read(buf);
    if (len > 0) {
      pb.unread(buf, 0, len);
    }
    if (len == 4) {
      switch (buf[0] & 0xFF)
      {
      case 0: 
        if ((buf[1] == 60) && (buf[2] == 0) && (buf[3] == 63))
        {
          setEncoding(pb, "UnicodeBig"); return;
        }
        break;
      case 60: 
        switch (buf[1] & 0xFF)
        {
        default: 
          break;
        case 0: 
          if ((buf[2] == 63) && (buf[3] == 0))
          {
            setEncoding(pb, "UnicodeLittle"); return;
          }
          break;
        case 63: 
          if ((buf[2] == 120) && (buf[3] == 109))
          {
            useEncodingDecl(pb, "UTF8"); return;
          }
          break;
        }
        break;
      case 76: 
        if ((buf[1] == 111) && ((0xFF & buf[2]) == 167) && ((0xFF & buf[3]) == 148))
        {
          useEncodingDecl(pb, "CP037"); return;
        }
        break;
      case 254: 
        if ((buf[1] & 0xFF) == 255)
        {
          setEncoding(pb, "UTF-16"); return;
        }
        break;
      case 255: 
        if ((buf[1] & 0xFF) == 254)
        {
          setEncoding(pb, "UTF-16"); return;
        }
        break;
      }
    }
    setEncoding(pb, "UTF-8");
  }
  
  private void useEncodingDecl(PushbackInputStream pb, String encoding)
    throws IOException
  {
    byte[] buffer = new byte['Ȁ'];
    
    int len = pb.read(buffer, 0, buffer.length);
    pb.unread(buffer, 0, len);
    Reader r = new InputStreamReader(new ByteArrayInputStream(buffer, 4, len), encoding);
    int c;
    if ((c = r.read()) != 108)
    {
      setEncoding(pb, "UTF-8");
      return;
    }
    StringBuffer buf = new StringBuffer();
    StringBuffer keyBuf = null;
    String key = null;
    boolean sawEq = false;
    char quoteChar = '\000';
    boolean sawQuestion = false;
    for (int i = 0; i < 507; i++)
    {
      if ((c = r.read()) == -1) {
        break;
      }
      if ((c != 32) && (c != 9) && (c != 10) && (c != 13))
      {
        if (i == 0) {
          break;
        }
        if (c == 63)
        {
          sawQuestion = true;
        }
        else if (sawQuestion)
        {
          if (c == 62) {
            break;
          }
          sawQuestion = false;
        }
        if ((key == null) || (!sawEq))
        {
          if (keyBuf == null)
          {
            if (!Character.isWhitespace((char)c))
            {
              keyBuf = buf;
              buf.setLength(0);
              buf.append((char)c);
              sawEq = false;
            }
          }
          else if (Character.isWhitespace((char)c))
          {
            key = keyBuf.toString();
          }
          else if (c == 61)
          {
            if (key == null) {
              key = keyBuf.toString();
            }
            sawEq = true;
            keyBuf = null;
            quoteChar = '\000';
          }
          else
          {
            keyBuf.append((char)c);
          }
        }
        else if (!Character.isWhitespace((char)c))
        {
          if ((c == 34) || (c == 39))
          {
            if (quoteChar == 0)
            {
              quoteChar = (char)c;
              buf.setLength(0);
              continue;
            }
            if (c == quoteChar)
            {
              if ("encoding".equals(key))
              {
                this.assignedEncoding = buf.toString();
                for (i = 0; i < this.assignedEncoding.length(); i++)
                {
                  c = this.assignedEncoding.charAt(i);
                  if (((c < 65) || (c > 90)) && ((c < 97) || (c > 122)))
                  {
                    if (i == 0) {
                      break label513;
                    }
                    if ((i <= 0) || ((c != 45) && ((c < 48) || (c > 57)) && (c != 46) && (c != 95))) {
                      break label513;
                    }
                  }
                }
                setEncoding(pb, this.assignedEncoding);
                return;
              }
              key = null;
              continue;
            }
          }
          buf.append((char)c);
        }
      }
    }
    label513:
    setEncoding(pb, "UTF-8");
  }
  
  private void setEncoding(InputStream stream, String encoding)
    throws IOException
  {
    this.assignedEncoding = encoding;
    this.in = createReader(stream, encoding);
  }
  
  public int read(char[] buf, int off, int len)
    throws IOException
  {
    if (this.closed) {
      return -1;
    }
    int val = this.in.read(buf, off, len);
    if (val == -1) {
      close();
    }
    return val;
  }
  
  public int read()
    throws IOException
  {
    if (this.closed) {
      throw new IOException("closed");
    }
    int val = this.in.read();
    if (val == -1) {
      close();
    }
    return val;
  }
  
  public boolean markSupported()
  {
    return this.in == null ? false : this.in.markSupported();
  }
  
  public void mark(int value)
    throws IOException
  {
    if (this.in != null) {
      this.in.mark(value);
    }
  }
  
  public void reset()
    throws IOException
  {
    if (this.in != null) {
      this.in.reset();
    }
  }
  
  public long skip(long value)
    throws IOException
  {
    return this.in == null ? 0L : this.in.skip(value);
  }
  
  public boolean ready()
    throws IOException
  {
    return this.in == null ? false : this.in.ready();
  }
  
  public void close()
    throws IOException
  {
    if (this.closed) {
      return;
    }
    this.in.close();
    this.in = null;
    this.closed = true;
  }
  
  static abstract class BaseReader
    extends Reader
  {
    protected InputStream instream;
    protected byte[] buffer;
    protected int start;
    protected int finish;
    
    BaseReader(InputStream stream)
    {
      super();
      
      this.instream = stream;
      this.buffer = new byte[' '];
    }
    
    public boolean ready()
      throws IOException
    {
      return (this.instream == null) || (this.finish - this.start > 0) || (this.instream.available() != 0);
    }
    
    public void close()
      throws IOException
    {
      if (this.instream != null)
      {
        this.instream.close();
        this.start = (this.finish = 0);
        this.buffer = null;
        this.instream = null;
      }
    }
  }
  
  static final class Utf8Reader
    extends XmlReader.BaseReader
  {
    private char nextChar;
    
    Utf8Reader(InputStream stream)
    {
      super();
    }
    
    public int read(char[] buf, int offset, int len)
      throws IOException
    {
      int i = 0;int c = 0;
      if (len <= 0) {
        return 0;
      }
      if (this.nextChar != 0)
      {
        buf[(offset + i++)] = this.nextChar;
        this.nextChar = '\000';
      }
      while (i < len)
      {
        if (this.finish <= this.start)
        {
          if (this.instream == null)
          {
            c = -1;
            break;
          }
          this.start = 0;
          this.finish = this.instream.read(this.buffer, 0, this.buffer.length);
          if (this.finish <= 0)
          {
            close();
            c = -1;
            break;
          }
        }
        c = this.buffer[this.start] & 0xFF;
        if ((c & 0x80) == 0)
        {
          this.start += 1;
          buf[(offset + i++)] = ((char)c);
        }
        else
        {
          int off = this.start;
          try
          {
            if ((this.buffer[off] & 0xE0) == 192)
            {
              c = (this.buffer[(off++)] & 0x1F) << 6;
              c += (this.buffer[(off++)] & 0x3F);
            }
            else if ((this.buffer[off] & 0xF0) == 224)
            {
              c = (this.buffer[(off++)] & 0xF) << 12;
              c += ((this.buffer[(off++)] & 0x3F) << 6);
              c += (this.buffer[(off++)] & 0x3F);
            }
            else if ((this.buffer[off] & 0xF8) == 240)
            {
              c = (this.buffer[(off++)] & 0x7) << 18;
              c += ((this.buffer[(off++)] & 0x3F) << 12);
              c += ((this.buffer[(off++)] & 0x3F) << 6);
              c += (this.buffer[(off++)] & 0x3F);
              if (c > 1114111) {
                throw new CharConversionException("UTF-8 encoding of character 0x00" + Integer.toHexString(c) + " can't be converted to Unicode.");
              }
              c -= 65536;
              this.nextChar = ((char)(56320 + (c & 0x3FF)));
              c = 55296 + (c >> 10);
            }
            else
            {
              throw new CharConversionException("Unconvertible UTF-8 character beginning with 0x" + Integer.toHexString(this.buffer[this.start] & 0xFF));
            }
          }
          catch (ArrayIndexOutOfBoundsException e)
          {
            c = 0;
          }
          if (off > this.finish)
          {
            System.arraycopy(this.buffer, this.start, this.buffer, 0, this.finish - this.start);
            
            this.finish -= this.start;
            this.start = 0;
            off = this.instream.read(this.buffer, this.finish, this.buffer.length - this.finish);
            if (off < 0)
            {
              close();
              throw new CharConversionException("Partial UTF-8 char");
            }
            this.finish += off;
          }
          else
          {
            for (this.start += 1; this.start < off; this.start += 1) {
              if ((this.buffer[this.start] & 0xC0) != 128)
              {
                close();
                throw new CharConversionException("Malformed UTF-8 char -- is an XML encoding declaration missing?");
              }
            }
            buf[(offset + i++)] = ((char)c);
            if ((this.nextChar != 0) && (i < len))
            {
              buf[(offset + i++)] = this.nextChar;
              this.nextChar = '\000';
            }
          }
        }
      }
      if (i > 0) {
        return i;
      }
      return c == -1 ? -1 : 0;
    }
  }
  
  static final class AsciiReader
    extends XmlReader.BaseReader
  {
    AsciiReader(InputStream in)
    {
      super();
    }
    
    public int read(char[] buf, int offset, int len)
      throws IOException
    {
      if (this.instream == null) {
        return -1;
      }
      for (int i = 0; i < len; i++)
      {
        if (this.start >= this.finish)
        {
          this.start = 0;
          this.finish = this.instream.read(this.buffer, 0, this.buffer.length);
          if (this.finish <= 0)
          {
            if (this.finish > 0) {
              break;
            }
            close(); break;
          }
        }
        int c = this.buffer[(this.start++)];
        if ((c & 0x80) != 0) {
          throw new CharConversionException("Illegal ASCII character, 0x" + Integer.toHexString(c & 0xFF));
        }
        buf[(offset + i)] = ((char)c);
      }
      if ((i == 0) && (this.finish <= 0)) {
        return -1;
      }
      return i;
    }
  }
  
  static final class Iso8859_1Reader
    extends XmlReader.BaseReader
  {
    Iso8859_1Reader(InputStream in)
    {
      super();
    }
    
    public int read(char[] buf, int offset, int len)
      throws IOException
    {
      if (this.instream == null) {
        return -1;
      }
      for (int i = 0; i < len; i++)
      {
        if (this.start >= this.finish)
        {
          this.start = 0;
          this.finish = this.instream.read(this.buffer, 0, this.buffer.length);
          if (this.finish <= 0)
          {
            if (this.finish > 0) {
              break;
            }
            close(); break;
          }
        }
        buf[(offset + i)] = ((char)(0xFF & this.buffer[(this.start++)]));
      }
      if ((i == 0) && (this.finish <= 0)) {
        return -1;
      }
      return i;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\dtdparser\XmlReader.class
 * Java compiler version: 3 (47.0)
 * JD-Core Version:       0.7.1
 */