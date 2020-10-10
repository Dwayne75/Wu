package com.google.common.io;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

@GwtCompatible(emulated=true)
final class GwtWorkarounds
{
  @GwtIncompatible("Reader")
  static CharInput asCharInput(Reader reader)
  {
    Preconditions.checkNotNull(reader);
    new CharInput()
    {
      public int read()
        throws IOException
      {
        return this.val$reader.read();
      }
      
      public void close()
        throws IOException
      {
        this.val$reader.close();
      }
    };
  }
  
  static CharInput asCharInput(CharSequence chars)
  {
    Preconditions.checkNotNull(chars);
    new CharInput()
    {
      int index = 0;
      
      public int read()
      {
        if (this.index < this.val$chars.length()) {
          return this.val$chars.charAt(this.index++);
        }
        return -1;
      }
      
      public void close()
      {
        this.index = this.val$chars.length();
      }
    };
  }
  
  @GwtIncompatible("InputStream")
  static InputStream asInputStream(ByteInput input)
  {
    Preconditions.checkNotNull(input);
    new InputStream()
    {
      public int read()
        throws IOException
      {
        return this.val$input.read();
      }
      
      public int read(byte[] b, int off, int len)
        throws IOException
      {
        Preconditions.checkNotNull(b);
        Preconditions.checkPositionIndexes(off, off + len, b.length);
        if (len == 0) {
          return 0;
        }
        int firstByte = read();
        if (firstByte == -1) {
          return -1;
        }
        b[off] = ((byte)firstByte);
        for (int dst = 1; dst < len; dst++)
        {
          int readByte = read();
          if (readByte == -1) {
            return dst;
          }
          b[(off + dst)] = ((byte)readByte);
        }
        return len;
      }
      
      public void close()
        throws IOException
      {
        this.val$input.close();
      }
    };
  }
  
  @GwtIncompatible("OutputStream")
  static OutputStream asOutputStream(ByteOutput output)
  {
    Preconditions.checkNotNull(output);
    new OutputStream()
    {
      public void write(int b)
        throws IOException
      {
        this.val$output.write((byte)b);
      }
      
      public void flush()
        throws IOException
      {
        this.val$output.flush();
      }
      
      public void close()
        throws IOException
      {
        this.val$output.close();
      }
    };
  }
  
  @GwtIncompatible("Writer")
  static CharOutput asCharOutput(Writer writer)
  {
    Preconditions.checkNotNull(writer);
    new CharOutput()
    {
      public void write(char c)
        throws IOException
      {
        this.val$writer.append(c);
      }
      
      public void flush()
        throws IOException
      {
        this.val$writer.flush();
      }
      
      public void close()
        throws IOException
      {
        this.val$writer.close();
      }
    };
  }
  
  static CharOutput stringBuilderOutput(int initialSize)
  {
    StringBuilder builder = new StringBuilder(initialSize);
    new CharOutput()
    {
      public void write(char c)
      {
        this.val$builder.append(c);
      }
      
      public void flush() {}
      
      public void close() {}
      
      public String toString()
      {
        return this.val$builder.toString();
      }
    };
  }
  
  static abstract interface CharOutput
  {
    public abstract void write(char paramChar)
      throws IOException;
    
    public abstract void flush()
      throws IOException;
    
    public abstract void close()
      throws IOException;
  }
  
  static abstract interface ByteOutput
  {
    public abstract void write(byte paramByte)
      throws IOException;
    
    public abstract void flush()
      throws IOException;
    
    public abstract void close()
      throws IOException;
  }
  
  static abstract interface ByteInput
  {
    public abstract int read()
      throws IOException;
    
    public abstract void close()
      throws IOException;
  }
  
  static abstract interface CharInput
  {
    public abstract int read()
      throws IOException;
    
    public abstract void close()
      throws IOException;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\io\GwtWorkarounds.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */