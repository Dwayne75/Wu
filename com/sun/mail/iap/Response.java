package com.sun.mail.iap;

import com.sun.mail.util.ASCIIUtility;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Vector;

public class Response
{
  protected int index;
  protected int pindex;
  protected int size;
  protected byte[] buffer = null;
  protected int type = 0;
  protected String tag = null;
  private static final int increment = 100;
  public static final int TAG_MASK = 3;
  public static final int CONTINUATION = 1;
  public static final int TAGGED = 2;
  public static final int UNTAGGED = 3;
  public static final int TYPE_MASK = 28;
  public static final int OK = 4;
  public static final int NO = 8;
  public static final int BAD = 12;
  public static final int BYE = 16;
  public static final int SYNTHETIC = 32;
  
  public Response(String s)
  {
    this.buffer = ASCIIUtility.getBytes(s);
    this.size = this.buffer.length;
    parse();
  }
  
  public Response(Protocol p)
    throws IOException, ProtocolException
  {
    ByteArray ba = p.getResponseBuffer();
    ByteArray response = p.getInputStream().readResponse(ba);
    this.buffer = response.getBytes();
    this.size = (response.getCount() - 2);
    
    parse();
  }
  
  public Response(Response r)
  {
    this.index = r.index;
    this.size = r.size;
    this.buffer = r.buffer;
    this.type = r.type;
    this.tag = r.tag;
  }
  
  public static Response byeResponse(Exception ex)
  {
    String err = "* BYE JavaMail Exception: " + ex.toString();
    err = err.replace('\r', ' ').replace('\n', ' ');
    Response r = new Response(err);
    r.type |= 0x20;
    return r;
  }
  
  private void parse()
  {
    this.index = 0;
    if (this.size == 0) {
      return;
    }
    if (this.buffer[this.index] == 43)
    {
      this.type |= 0x1;
      this.index += 1;
      return;
    }
    if (this.buffer[this.index] == 42)
    {
      this.type |= 0x3;
      this.index += 1;
    }
    else
    {
      this.type |= 0x2;
      this.tag = readAtom();
      if (this.tag == null) {
        this.tag = "";
      }
    }
    int mark = this.index;
    String s = readAtom();
    if (s == null) {
      s = "";
    }
    if (s.equalsIgnoreCase("OK")) {
      this.type |= 0x4;
    } else if (s.equalsIgnoreCase("NO")) {
      this.type |= 0x8;
    } else if (s.equalsIgnoreCase("BAD")) {
      this.type |= 0xC;
    } else if (s.equalsIgnoreCase("BYE")) {
      this.type |= 0x10;
    } else {
      this.index = mark;
    }
    this.pindex = this.index;
  }
  
  public void skipSpaces()
  {
    while ((this.index < this.size) && (this.buffer[this.index] == 32)) {
      this.index += 1;
    }
  }
  
  public void skipToken()
  {
    while ((this.index < this.size) && (this.buffer[this.index] != 32)) {
      this.index += 1;
    }
  }
  
  public void skip(int count)
  {
    this.index += count;
  }
  
  public byte peekByte()
  {
    if (this.index < this.size) {
      return this.buffer[this.index];
    }
    return 0;
  }
  
  public byte readByte()
  {
    if (this.index < this.size) {
      return this.buffer[(this.index++)];
    }
    return 0;
  }
  
  public String readAtom()
  {
    return readAtom('\000');
  }
  
  public String readAtom(char delim)
  {
    skipSpaces();
    if (this.index >= this.size) {
      return null;
    }
    int start = this.index;
    byte b;
    while ((this.index < this.size) && ((b = this.buffer[this.index]) > 32) && (b != 40) && (b != 41) && (b != 37) && (b != 42) && (b != 34) && (b != 92) && (b != Byte.MAX_VALUE) && ((delim == 0) || (b != delim))) {
      this.index += 1;
    }
    return ASCIIUtility.toString(this.buffer, start, this.index);
  }
  
  public String readString(char delim)
  {
    skipSpaces();
    if (this.index >= this.size) {
      return null;
    }
    int start = this.index;
    while ((this.index < this.size) && (this.buffer[this.index] != delim)) {
      this.index += 1;
    }
    return ASCIIUtility.toString(this.buffer, start, this.index);
  }
  
  public String[] readStringList()
  {
    return readStringList(false);
  }
  
  public String[] readAtomStringList()
  {
    return readStringList(true);
  }
  
  private String[] readStringList(boolean atom)
  {
    skipSpaces();
    if (this.buffer[this.index] != 40) {
      return null;
    }
    this.index += 1;
    
    Vector v = new Vector();
    do
    {
      v.addElement(atom ? readAtomString() : readString());
    } while (this.buffer[(this.index++)] != 41);
    int size = v.size();
    if (size > 0)
    {
      String[] s = new String[size];
      v.copyInto(s);
      return s;
    }
    return null;
  }
  
  public int readNumber()
  {
    skipSpaces();
    
    int start = this.index;
    while ((this.index < this.size) && (Character.isDigit((char)this.buffer[this.index]))) {
      this.index += 1;
    }
    if (this.index > start) {
      try
      {
        return ASCIIUtility.parseInt(this.buffer, start, this.index);
      }
      catch (NumberFormatException nex) {}
    }
    return -1;
  }
  
  public long readLong()
  {
    skipSpaces();
    
    int start = this.index;
    while ((this.index < this.size) && (Character.isDigit((char)this.buffer[this.index]))) {
      this.index += 1;
    }
    if (this.index > start) {
      try
      {
        return ASCIIUtility.parseLong(this.buffer, start, this.index);
      }
      catch (NumberFormatException nex) {}
    }
    return -1L;
  }
  
  public String readString()
  {
    return (String)parseString(false, true);
  }
  
  public ByteArrayInputStream readBytes()
  {
    ByteArray ba = readByteArray();
    if (ba != null) {
      return ba.toByteArrayInputStream();
    }
    return null;
  }
  
  public ByteArray readByteArray()
  {
    if (isContinuation())
    {
      skipSpaces();
      return new ByteArray(this.buffer, this.index, this.size - this.index);
    }
    return (ByteArray)parseString(false, false);
  }
  
  public String readAtomString()
  {
    return (String)parseString(true, true);
  }
  
  private Object parseString(boolean parseAtoms, boolean returnString)
  {
    skipSpaces();
    
    byte b = this.buffer[this.index];
    if (b == 34)
    {
      this.index += 1;
      int start = this.index;
      int copyto = this.index;
      while ((this.index < this.size) && ((b = this.buffer[this.index]) != 34))
      {
        if (b == 92) {
          this.index += 1;
        }
        if (this.index != copyto) {
          this.buffer[copyto] = this.buffer[this.index];
        }
        copyto++;
        this.index += 1;
      }
      if (this.index >= this.size) {
        return null;
      }
      this.index += 1;
      if (returnString) {
        return ASCIIUtility.toString(this.buffer, start, copyto);
      }
      return new ByteArray(this.buffer, start, copyto - start);
    }
    if (b == 123)
    {
      int start = ++this.index;
      while (this.buffer[this.index] != 125) {
        this.index += 1;
      }
      int count = 0;
      try
      {
        count = ASCIIUtility.parseInt(this.buffer, start, this.index);
      }
      catch (NumberFormatException nex)
      {
        return null;
      }
      start = this.index + 3;
      this.index = (start + count);
      if (returnString) {
        return ASCIIUtility.toString(this.buffer, start, start + count);
      }
      return new ByteArray(this.buffer, start, count);
    }
    if (parseAtoms)
    {
      int start = this.index;
      
      String s = readAtom();
      if (returnString) {
        return s;
      }
      return new ByteArray(this.buffer, start, this.index);
    }
    if ((b == 78) || (b == 110))
    {
      this.index += 3;
      return null;
    }
    return null;
  }
  
  public int getType()
  {
    return this.type;
  }
  
  public boolean isContinuation()
  {
    return (this.type & 0x3) == 1;
  }
  
  public boolean isTagged()
  {
    return (this.type & 0x3) == 2;
  }
  
  public boolean isUnTagged()
  {
    return (this.type & 0x3) == 3;
  }
  
  public boolean isOK()
  {
    return (this.type & 0x1C) == 4;
  }
  
  public boolean isNO()
  {
    return (this.type & 0x1C) == 8;
  }
  
  public boolean isBAD()
  {
    return (this.type & 0x1C) == 12;
  }
  
  public boolean isBYE()
  {
    return (this.type & 0x1C) == 16;
  }
  
  public boolean isSynthetic()
  {
    return (this.type & 0x20) == 32;
  }
  
  public String getTag()
  {
    return this.tag;
  }
  
  public String getRest()
  {
    skipSpaces();
    return ASCIIUtility.toString(this.buffer, this.index, this.size);
  }
  
  public void reset()
  {
    this.index = this.pindex;
  }
  
  public String toString()
  {
    return ASCIIUtility.toString(this.buffer, 0, this.size);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\iap\Response.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */