package org.kohsuke.rngom.parse.compact;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.util.Utf16;

public final class UCode_UCodeESC_CharStream
{
  public static final boolean staticFlag = false;
  
  static final int hexval(char c)
  {
    switch (c)
    {
    case '0': 
      return 0;
    case '1': 
      return 1;
    case '2': 
      return 2;
    case '3': 
      return 3;
    case '4': 
      return 4;
    case '5': 
      return 5;
    case '6': 
      return 6;
    case '7': 
      return 7;
    case '8': 
      return 8;
    case '9': 
      return 9;
    case 'A': 
    case 'a': 
      return 10;
    case 'B': 
    case 'b': 
      return 11;
    case 'C': 
    case 'c': 
      return 12;
    case 'D': 
    case 'd': 
      return 13;
    case 'E': 
    case 'e': 
      return 14;
    case 'F': 
    case 'f': 
      return 15;
    }
    return -1;
  }
  
  public int bufpos = -1;
  int bufsize;
  int available;
  int tokenBegin;
  private int[] bufline;
  private int[] bufcolumn;
  private int column = 0;
  private int line = 1;
  private Reader inputStream;
  private boolean closed = false;
  private boolean prevCharIsLF = false;
  private char[] nextCharBuf;
  private char[] buffer;
  private int maxNextCharInd = 0;
  private int nextCharInd = -1;
  private int inBuf = 0;
  
  private final void ExpandBuff(boolean wrapAround)
  {
    char[] newbuffer = new char[this.bufsize + 2048];
    int[] newbufline = new int[this.bufsize + 2048];
    int[] newbufcolumn = new int[this.bufsize + 2048];
    if (wrapAround)
    {
      System.arraycopy(this.buffer, this.tokenBegin, newbuffer, 0, this.bufsize - this.tokenBegin);
      System.arraycopy(this.buffer, 0, newbuffer, this.bufsize - this.tokenBegin, this.bufpos);
      
      this.buffer = newbuffer;
      
      System.arraycopy(this.bufline, this.tokenBegin, newbufline, 0, this.bufsize - this.tokenBegin);
      System.arraycopy(this.bufline, 0, newbufline, this.bufsize - this.tokenBegin, this.bufpos);
      this.bufline = newbufline;
      
      System.arraycopy(this.bufcolumn, this.tokenBegin, newbufcolumn, 0, this.bufsize - this.tokenBegin);
      System.arraycopy(this.bufcolumn, 0, newbufcolumn, this.bufsize - this.tokenBegin, this.bufpos);
      this.bufcolumn = newbufcolumn;
      
      this.bufpos += this.bufsize - this.tokenBegin;
    }
    else
    {
      System.arraycopy(this.buffer, this.tokenBegin, newbuffer, 0, this.bufsize - this.tokenBegin);
      this.buffer = newbuffer;
      
      System.arraycopy(this.bufline, this.tokenBegin, newbufline, 0, this.bufsize - this.tokenBegin);
      this.bufline = newbufline;
      
      System.arraycopy(this.bufcolumn, this.tokenBegin, newbufcolumn, 0, this.bufsize - this.tokenBegin);
      this.bufcolumn = newbufcolumn;
      
      this.bufpos -= this.tokenBegin;
    }
    this.available = (this.bufsize += 2048);
    this.tokenBegin = 0;
  }
  
  private final void FillBuff()
    throws EOFException
  {
    if (this.maxNextCharInd == 4096) {
      this.maxNextCharInd = (this.nextCharInd = 0);
    }
    if (this.closed) {
      throw new EOFException();
    }
    try
    {
      int i;
      if ((i = this.inputStream.read(this.nextCharBuf, this.maxNextCharInd, 4096 - this.maxNextCharInd)) == -1)
      {
        this.closed = true;
        this.inputStream.close();
        throw new EOFException();
      }
      this.maxNextCharInd += i;
    }
    catch (IOException e)
    {
      throw new BuildException(e);
    }
  }
  
  private final char ReadChar()
    throws EOFException
  {
    if (++this.nextCharInd >= this.maxNextCharInd) {
      FillBuff();
    }
    return this.nextCharBuf[this.nextCharInd];
  }
  
  private final char PeekChar()
    throws EOFException
  {
    char c = ReadChar();
    this.nextCharInd -= 1;
    return c;
  }
  
  public final char BeginToken()
    throws EOFException
  {
    if (this.inBuf > 0)
    {
      this.inBuf -= 1;
      return this.buffer[(this.tokenBegin = ++this.bufpos)];
    }
    this.tokenBegin = 0;
    this.bufpos = -1;
    
    return readChar();
  }
  
  private final void AdjustBuffSize()
  {
    if (this.available == this.bufsize)
    {
      if (this.tokenBegin > 2048)
      {
        this.bufpos = 0;
        this.available = this.tokenBegin;
      }
      else
      {
        ExpandBuff(false);
      }
    }
    else if (this.available > this.tokenBegin) {
      this.available = this.bufsize;
    } else if (this.tokenBegin - this.available < 2048) {
      ExpandBuff(true);
    } else {
      this.available = this.tokenBegin;
    }
  }
  
  private final void UpdateLineColumn(char c)
  {
    this.column += 1;
    if (this.prevCharIsLF)
    {
      this.prevCharIsLF = false;
      this.line += (this.column = 1);
    }
    switch (c)
    {
    case '\000': 
      this.prevCharIsLF = true;
      break;
    case '\t': 
      this.column -= 1;
      this.column += 8 - (this.column & 0x7);
      break;
    }
    this.bufline[this.bufpos] = this.line;
    this.bufcolumn[this.bufpos] = this.column;
  }
  
  private final char NEWLINE_MARKER = '\000';
  private static final char BOM = '﻿';
  
  public final char readChar()
    throws EOFException
  {
    if (this.inBuf > 0)
    {
      this.inBuf -= 1;
      return this.buffer[(++this.bufpos)];
    }
    char c;
    try
    {
      c = ReadChar();
      switch (c)
      {
      case '\r': 
        c = '\000';
        try
        {
          if (PeekChar() == '\n') {
            ReadChar();
          }
        }
        catch (EOFException e) {}
      case '\n': 
        c = '\000';
        break;
      case '\t': 
        break;
      default: 
        if (c >= ' ')
        {
          if (!Utf16.isSurrogate(c)) {
            break label291;
          }
          if (Utf16.isSurrogate2(c)) {
            throw new EscapeSyntaxException("illegal_surrogate_pair", this.line, this.column + 1);
          }
          if (++this.bufpos == this.available) {
            AdjustBuffSize();
          }
          this.buffer[this.bufpos] = c;
          try
          {
            c = ReadChar();
          }
          catch (EOFException e)
          {
            throw new EscapeSyntaxException("illegal_surrogate_pair", this.line, this.column + 1);
          }
          if (Utf16.isSurrogate2(c)) {
            break label291;
          }
          throw new EscapeSyntaxException("illegal_surrogate_pair", this.line, this.column + 2);
        }
        break;
      }
      throw new EscapeSyntaxException("illegal_char_code", this.line, this.column + 1);
    }
    catch (EOFException e)
    {
      label291:
      if (this.bufpos == -1)
      {
        if (++this.bufpos == this.available) {
          AdjustBuffSize();
        }
        this.bufline[this.bufpos] = this.line;
        this.bufcolumn[this.bufpos] = this.column;
      }
      throw e;
    }
    if (++this.bufpos == this.available) {
      AdjustBuffSize();
    }
    this.buffer[this.bufpos] = c;
    UpdateLineColumn(c);
    try
    {
      if ((c != '\\') || (PeekChar() != 'x')) {
        return c;
      }
    }
    catch (EOFException e)
    {
      return c;
    }
    int xCnt = 1;
    for (;;)
    {
      ReadChar();
      if (++this.bufpos == this.available) {
        AdjustBuffSize();
      }
      this.buffer[this.bufpos] = 'x';
      UpdateLineColumn('x');
      try
      {
        c = PeekChar();
      }
      catch (EOFException e)
      {
        backup(xCnt);
        return '\\';
      }
      if (c == '{')
      {
        ReadChar();
        this.column += 1;
        
        this.bufpos -= xCnt;
        if (this.bufpos >= 0) {
          break;
        }
        this.bufpos += this.bufsize; break;
      }
      if (c != 'x')
      {
        backup(xCnt);
        return '\\';
      }
      xCnt++;
    }
    try
    {
      int scalarValue = hexval(ReadChar());
      this.column += 1;
      if (scalarValue < 0) {
        throw new EscapeSyntaxException("illegal_hex_digit", this.line, this.column);
      }
      while ((c = ReadChar()) != '}')
      {
        this.column += 1;
        int n = hexval(c);
        if (n < 0) {
          throw new EscapeSyntaxException("illegal_hex_digit", this.line, this.column);
        }
        scalarValue <<= 4;
        scalarValue |= n;
        if (scalarValue >= 1114112) {
          throw new EscapeSyntaxException("char_code_too_big", this.line, this.column);
        }
      }
      this.column += 1;
      if (scalarValue <= 65535)
      {
        c = (char)scalarValue;
        switch (c)
        {
        case '\t': 
        case '\n': 
        case '\r': 
          break;
        default: 
          if ((c >= ' ') && (!Utf16.isSurrogate(c))) {
            break;
          }
        case '￾': 
        case '￿': 
          throw new EscapeSyntaxException("illegal_char_code_ref", this.line, this.column);
        }
        this.buffer[this.bufpos] = c;
        return c;
      }
      c = Utf16.surrogate1(scalarValue);
      this.buffer[this.bufpos] = c;
      int bufpos1 = this.bufpos;
      if (++this.bufpos == this.bufsize) {
        this.bufpos = 0;
      }
      this.buffer[this.bufpos] = Utf16.surrogate2(scalarValue);
      this.bufline[this.bufpos] = this.bufline[bufpos1];
      this.bufcolumn[this.bufpos] = this.bufcolumn[bufpos1];
      backup(1);
      return c;
    }
    catch (EOFException e)
    {
      throw new EscapeSyntaxException("incomplete_escape", this.line, this.column);
    }
  }
  
  /**
   * @deprecated
   */
  public final int getColumn()
  {
    return this.bufcolumn[this.bufpos];
  }
  
  /**
   * @deprecated
   */
  public final int getLine()
  {
    return this.bufline[this.bufpos];
  }
  
  public final int getEndColumn()
  {
    return this.bufcolumn[this.bufpos];
  }
  
  public final int getEndLine()
  {
    return this.bufline[this.bufpos];
  }
  
  public final int getBeginColumn()
  {
    return this.bufcolumn[this.tokenBegin];
  }
  
  public final int getBeginLine()
  {
    return this.bufline[this.tokenBegin];
  }
  
  public final void backup(int amount)
  {
    this.inBuf += amount;
    if (this.bufpos -= amount < 0) {
      this.bufpos += this.bufsize;
    }
  }
  
  public UCode_UCodeESC_CharStream(Reader dstream, int startline, int startcolumn, int buffersize)
  {
    this.inputStream = dstream;
    this.line = startline;
    this.column = (startcolumn - 1);
    
    this.available = (this.bufsize = buffersize);
    this.buffer = new char[buffersize];
    this.bufline = new int[buffersize];
    this.bufcolumn = new int[buffersize];
    this.nextCharBuf = new char['က'];
    skipBOM();
  }
  
  public UCode_UCodeESC_CharStream(Reader dstream, int startline, int startcolumn)
  {
    this(dstream, startline, startcolumn, 4096);
  }
  
  public void ReInit(Reader dstream, int startline, int startcolumn, int buffersize)
  {
    this.inputStream = dstream;
    this.closed = false;
    this.line = startline;
    this.column = (startcolumn - 1);
    if ((this.buffer == null) || (buffersize != this.buffer.length))
    {
      this.available = (this.bufsize = buffersize);
      this.buffer = new char[buffersize];
      this.bufline = new int[buffersize];
      this.bufcolumn = new int[buffersize];
      this.nextCharBuf = new char['က'];
    }
    this.prevCharIsLF = false;
    this.tokenBegin = (this.inBuf = this.maxNextCharInd = 0);
    this.nextCharInd = (this.bufpos = -1);
    skipBOM();
  }
  
  public void ReInit(Reader dstream, int startline, int startcolumn)
  {
    ReInit(dstream, startline, startcolumn, 4096);
  }
  
  public UCode_UCodeESC_CharStream(InputStream dstream, int startline, int startcolumn, int buffersize)
  {
    this(new InputStreamReader(dstream), startline, startcolumn, 4096);
  }
  
  public UCode_UCodeESC_CharStream(InputStream dstream, int startline, int startcolumn)
  {
    this(dstream, startline, startcolumn, 4096);
  }
  
  public void ReInit(InputStream dstream, int startline, int startcolumn, int buffersize)
  {
    ReInit(new InputStreamReader(dstream), startline, startcolumn, 4096);
  }
  
  public void ReInit(InputStream dstream, int startline, int startcolumn)
  {
    ReInit(dstream, startline, startcolumn, 4096);
  }
  
  private void skipBOM()
  {
    try
    {
      if (PeekChar() == 65279) {
        ReadChar();
      }
    }
    catch (EOFException e) {}
  }
  
  public final String GetImage()
  {
    if (this.bufpos >= this.tokenBegin) {
      return new String(this.buffer, this.tokenBegin, this.bufpos - this.tokenBegin + 1);
    }
    return new String(this.buffer, this.tokenBegin, this.bufsize - this.tokenBegin) + new String(this.buffer, 0, this.bufpos + 1);
  }
  
  public final char[] GetSuffix(int len)
  {
    char[] ret = new char[len];
    if (this.bufpos + 1 >= len)
    {
      System.arraycopy(this.buffer, this.bufpos - len + 1, ret, 0, len);
    }
    else
    {
      System.arraycopy(this.buffer, this.bufsize - (len - this.bufpos - 1), ret, 0, len - this.bufpos - 1);
      
      System.arraycopy(this.buffer, 0, ret, len - this.bufpos - 1, this.bufpos + 1);
    }
    return ret;
  }
  
  public void Done()
  {
    this.nextCharBuf = null;
    this.buffer = null;
    this.bufline = null;
    this.bufcolumn = null;
  }
  
  public void adjustBeginLineColumn(int newLine, int newCol)
  {
    int start = this.tokenBegin;
    int len;
    int len;
    if (this.bufpos >= this.tokenBegin) {
      len = this.bufpos - this.tokenBegin + this.inBuf + 1;
    } else {
      len = this.bufsize - this.tokenBegin + this.bufpos + 1 + this.inBuf;
    }
    int i = 0;int j = 0;int k = 0;
    int nextColDiff = 0;int columnDiff = 0;
    while ((i < len) && (this.bufline[(j = start % this.bufsize)] == this.bufline[(k = ++start % this.bufsize)]))
    {
      this.bufline[j] = newLine;
      nextColDiff = columnDiff + this.bufcolumn[k] - this.bufcolumn[j];
      this.bufcolumn[j] = (newCol + columnDiff);
      columnDiff = nextColDiff;
      i++;
    }
    if (i < len)
    {
      this.bufline[j] = (newLine++);
      this.bufcolumn[j] = (newCol + columnDiff);
      while (i++ < len) {
        if (this.bufline[(j = start % this.bufsize)] != this.bufline[(++start % this.bufsize)]) {
          this.bufline[j] = (newLine++);
        } else {
          this.bufline[j] = newLine;
        }
      }
    }
    this.line = this.bufline[j];
    this.column = this.bufcolumn[j];
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\compact\UCode_UCodeESC_CharStream.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */