package com.google.common.io;

import java.io.IOException;

abstract class LineBuffer
{
  private StringBuilder line = new StringBuilder();
  private boolean sawReturn;
  
  protected void add(char[] cbuf, int off, int len)
    throws IOException
  {
    int pos = off;
    if ((this.sawReturn) && (len > 0)) {
      if (finishLine(cbuf[pos] == '\n')) {
        pos++;
      }
    }
    int start = pos;
    for (int end = off + len; pos < end; pos++) {
      switch (cbuf[pos])
      {
      case '\r': 
        this.line.append(cbuf, start, pos - start);
        this.sawReturn = true;
        if (pos + 1 < end) {
          if (finishLine(cbuf[(pos + 1)] == '\n')) {
            pos++;
          }
        }
        start = pos + 1;
        break;
      case '\n': 
        this.line.append(cbuf, start, pos - start);
        finishLine(true);
        start = pos + 1;
      }
    }
    this.line.append(cbuf, start, off + len - start);
  }
  
  private boolean finishLine(boolean sawNewline)
    throws IOException
  {
    handleLine(this.line.toString(), sawNewline ? "\n" : this.sawReturn ? "\r" : sawNewline ? "\r\n" : "");
    
    this.line = new StringBuilder();
    this.sawReturn = false;
    return sawNewline;
  }
  
  protected void finish()
    throws IOException
  {
    if ((this.sawReturn) || (this.line.length() > 0)) {
      finishLine(false);
    }
  }
  
  protected abstract void handleLine(String paramString1, String paramString2)
    throws IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\io\LineBuffer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */