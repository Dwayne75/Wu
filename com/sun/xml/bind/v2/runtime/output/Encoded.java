package com.sun.xml.bind.v2.runtime.output;

import java.io.IOException;

public final class Encoded
{
  public byte[] buf;
  public int len;
  
  public Encoded(String text)
  {
    set(text);
  }
  
  public void ensureSize(int size)
  {
    if ((this.buf == null) || (this.buf.length < size)) {
      this.buf = new byte[size];
    }
  }
  
  public final void set(String text)
  {
    int length = text.length();
    
    ensureSize(length * 3 + 1);
    
    int ptr = 0;
    for (int i = 0; i < length; i++)
    {
      char chr = text.charAt(i);
      if (chr > '')
      {
        if (chr > '߿')
        {
          if ((55296 <= chr) && (chr <= 57343))
          {
            int uc = ((chr & 0x3FF) << '\n' | text.charAt(++i) & 0x3FF) + 65536;
            
            this.buf[(ptr++)] = ((byte)(0xF0 | uc >> 18));
            this.buf[(ptr++)] = ((byte)(0x80 | uc >> 12 & 0x3F));
            this.buf[(ptr++)] = ((byte)(0x80 | uc >> 6 & 0x3F));
            this.buf[(ptr++)] = ((byte)(128 + (uc & 0x3F)));
            continue;
          }
          this.buf[(ptr++)] = ((byte)(224 + (chr >> '\f')));
          this.buf[(ptr++)] = ((byte)(128 + (chr >> '\006' & 0x3F)));
        }
        else
        {
          this.buf[(ptr++)] = ((byte)(192 + (chr >> '\006')));
        }
        this.buf[(ptr++)] = ((byte)('' + (chr & 0x3F)));
      }
      else
      {
        this.buf[(ptr++)] = ((byte)chr);
      }
    }
    this.len = ptr;
  }
  
  public final void setEscape(String text, boolean isAttribute)
  {
    int length = text.length();
    ensureSize(length * 6 + 1);
    
    int ptr = 0;
    for (int i = 0; i < length; i++)
    {
      char chr = text.charAt(i);
      
      int ptr1 = ptr;
      if (chr > '')
      {
        if (chr > '߿')
        {
          if ((55296 <= chr) && (chr <= 57343))
          {
            int uc = ((chr & 0x3FF) << '\n' | text.charAt(++i) & 0x3FF) + 65536;
            
            this.buf[(ptr++)] = ((byte)(0xF0 | uc >> 18));
            this.buf[(ptr++)] = ((byte)(0x80 | uc >> 12 & 0x3F));
            this.buf[(ptr++)] = ((byte)(0x80 | uc >> 6 & 0x3F));
            this.buf[(ptr++)] = ((byte)(128 + (uc & 0x3F)));
            continue;
          }
          this.buf[(ptr1++)] = ((byte)(224 + (chr >> '\f')));
          this.buf[(ptr1++)] = ((byte)(128 + (chr >> '\006' & 0x3F)));
        }
        else
        {
          this.buf[(ptr1++)] = ((byte)(192 + (chr >> '\006')));
        }
        this.buf[(ptr1++)] = ((byte)('' + (chr & 0x3F)));
      }
      else
      {
        byte[] ent;
        if ((ent = attributeEntities[chr]) != null)
        {
          if ((isAttribute) || (entities[chr] != null)) {
            ptr1 = writeEntity(ent, ptr1);
          } else {
            this.buf[(ptr1++)] = ((byte)chr);
          }
        }
        else {
          this.buf[(ptr1++)] = ((byte)chr);
        }
      }
      ptr = ptr1;
    }
    this.len = ptr;
  }
  
  private int writeEntity(byte[] entity, int ptr)
  {
    System.arraycopy(entity, 0, this.buf, ptr, entity.length);
    return ptr + entity.length;
  }
  
  public final void write(UTF8XmlOutput out)
    throws IOException
  {
    out.write(this.buf, 0, this.len);
  }
  
  public void append(char b)
  {
    this.buf[(this.len++)] = ((byte)b);
  }
  
  public void compact()
  {
    byte[] b = new byte[this.len];
    System.arraycopy(this.buf, 0, b, 0, this.len);
    this.buf = b;
  }
  
  private static final byte[][] entities = new byte[''][];
  private static final byte[][] attributeEntities = new byte[''][];
  
  static
  {
    add('&', "&amp;", false);
    add('<', "&lt;", false);
    add('>', "&gt;", false);
    add('"', "&quot;", false);
    add('\t', "&#x9;", true);
    add('\r', "&#xD;", false);
    add('\n', "&#xA;", true);
  }
  
  private static void add(char c, String s, boolean attOnly)
  {
    byte[] image = UTF8XmlOutput.toBytes(s);
    attributeEntities[c] = image;
    if (!attOnly) {
      entities[c] = image;
    }
  }
  
  public Encoded() {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\output\Encoded.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */