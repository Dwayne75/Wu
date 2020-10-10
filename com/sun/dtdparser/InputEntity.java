package com.sun.dtdparser;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Locale;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class InputEntity
{
  private int start;
  private int finish;
  private char[] buf;
  private int lineNumber = 1;
  private boolean returnedFirstHalf = false;
  private boolean maybeInCRLF = false;
  private String name;
  private InputEntity next;
  private InputSource input;
  private Reader reader;
  private boolean isClosed;
  private DTDEventListener errHandler;
  private Locale locale;
  private StringBuffer rememberedText;
  private int startRemember;
  private boolean isPE;
  private static final int BUFSIZ = 8193;
  private static final char[] newline = { '\n' };
  
  public static InputEntity getInputEntity(DTDEventListener h, Locale l)
  {
    InputEntity retval = new InputEntity();
    retval.errHandler = h;
    retval.locale = l;
    return retval;
  }
  
  public boolean isInternal()
  {
    return this.reader == null;
  }
  
  public boolean isDocument()
  {
    return this.next == null;
  }
  
  public boolean isParameterEntity()
  {
    return this.isPE;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public void init(InputSource in, String name, InputEntity stack, boolean isPE)
    throws IOException, SAXException
  {
    this.input = in;
    this.isPE = isPE;
    this.reader = in.getCharacterStream();
    if (this.reader == null)
    {
      InputStream bytes = in.getByteStream();
      if (bytes == null) {
        this.reader = XmlReader.createReader(new URL(in.getSystemId()).openStream());
      } else if (in.getEncoding() != null) {
        this.reader = XmlReader.createReader(in.getByteStream(), in.getEncoding());
      } else {
        this.reader = XmlReader.createReader(in.getByteStream());
      }
    }
    this.next = stack;
    this.buf = new char[' '];
    this.name = name;
    checkRecursion(stack);
  }
  
  public void init(char[] b, String name, InputEntity stack, boolean isPE)
    throws SAXException
  {
    this.next = stack;
    this.buf = b;
    this.finish = b.length;
    this.name = name;
    this.isPE = isPE;
    checkRecursion(stack);
  }
  
  private void checkRecursion(InputEntity stack)
    throws SAXException
  {
    if (stack == null) {
      return;
    }
    for (stack = stack.next; stack != null; stack = stack.next) {
      if ((stack.name != null) && (stack.name.equals(this.name))) {
        fatal("P-069", new Object[] { this.name });
      }
    }
  }
  
  public InputEntity pop()
    throws IOException
  {
    close();
    return this.next;
  }
  
  public boolean isEOF()
    throws IOException, SAXException
  {
    if (this.start >= this.finish)
    {
      fillbuf();
      return this.start >= this.finish;
    }
    return false;
  }
  
  public String getEncoding()
  {
    if (this.reader == null) {
      return null;
    }
    if ((this.reader instanceof XmlReader)) {
      return ((XmlReader)this.reader).getEncoding();
    }
    if ((this.reader instanceof InputStreamReader)) {
      return ((InputStreamReader)this.reader).getEncoding();
    }
    return null;
  }
  
  public char getNameChar()
    throws IOException, SAXException
  {
    if (this.finish <= this.start) {
      fillbuf();
    }
    if (this.finish > this.start)
    {
      char c = this.buf[(this.start++)];
      if (XmlChars.isNameChar(c)) {
        return c;
      }
      this.start -= 1;
    }
    return '\000';
  }
  
  public char getc()
    throws IOException, SAXException
  {
    if (this.finish <= this.start) {
      fillbuf();
    }
    if (this.finish > this.start)
    {
      char c = this.buf[(this.start++)];
      if (this.returnedFirstHalf)
      {
        if ((c >= 56320) && (c <= 57343))
        {
          this.returnedFirstHalf = false;
          return c;
        }
        fatal("P-070", new Object[] { Integer.toHexString(c) });
      }
      if (((c >= ' ') && (c <= 55295)) || (c == '\t') || ((c >= 57344) && (c <= 65533))) {
        return c;
      }
      if ((c == '\r') && (!isInternal()))
      {
        this.maybeInCRLF = true;
        c = getc();
        if (c != '\n') {
          ungetc();
        }
        this.maybeInCRLF = false;
        
        this.lineNumber += 1;
        return '\n';
      }
      if ((c == '\n') || (c == '\r'))
      {
        if ((!isInternal()) && (!this.maybeInCRLF)) {
          this.lineNumber += 1;
        }
        return c;
      }
      if ((c >= 55296) && (c < 56320))
      {
        this.returnedFirstHalf = true;
        return c;
      }
      fatal("P-071", new Object[] { Integer.toHexString(c) });
    }
    throw new EndOfInputException();
  }
  
  public boolean peekc(char c)
    throws IOException, SAXException
  {
    if (this.finish <= this.start) {
      fillbuf();
    }
    if (this.finish > this.start)
    {
      if (this.buf[this.start] == c)
      {
        this.start += 1;
        return true;
      }
      return false;
    }
    return false;
  }
  
  public void ungetc()
  {
    if (this.start == 0) {
      throw new InternalError("ungetc");
    }
    this.start -= 1;
    if ((this.buf[this.start] == '\n') || (this.buf[this.start] == '\r'))
    {
      if (!isInternal()) {
        this.lineNumber -= 1;
      }
    }
    else if (this.returnedFirstHalf) {
      this.returnedFirstHalf = false;
    }
  }
  
  public boolean maybeWhitespace()
    throws IOException, SAXException
  {
    boolean isSpace = false;
    boolean sawCR = false;
    for (;;)
    {
      if (this.finish <= this.start) {
        fillbuf();
      }
      if (this.finish <= this.start) {
        return isSpace;
      }
      char c = this.buf[(this.start++)];
      if ((c != ' ') && (c != '\t') && (c != '\n') && (c != '\r')) {
        break;
      }
      isSpace = true;
      if (((c == '\n') || (c == '\r')) && (!isInternal()))
      {
        if ((c != '\n') || (!sawCR))
        {
          this.lineNumber += 1;
          sawCR = false;
        }
        if (c == '\r') {
          sawCR = true;
        }
      }
    }
    this.start -= 1;
    return isSpace;
  }
  
  public boolean parsedContent(DTDEventListener docHandler)
    throws IOException, SAXException
  {
    int last;
    int first = last = this.start;
    for (boolean sawContent = false;; last++) {
      if (last >= this.finish)
      {
        if (last > first)
        {
          docHandler.characters(this.buf, first, last - first);
          sawContent = true;
          this.start = last;
        }
        if (isEOF()) {
          return sawContent;
        }
        first = this.start;
        last = first - 1;
      }
      else
      {
        char c = this.buf[last];
        if (((c <= ']') || (c > 55295)) && ((c >= '&') || (c < ' ')) && ((c <= '<') || (c >= ']')) && ((c <= '&') || (c >= '<')) && (c != '\t') && ((c < 57344) || (c > 65533)))
        {
          if ((c == '<') || (c == '&')) {
            break;
          }
          if (c == '\n')
          {
            if (!isInternal()) {
              this.lineNumber += 1;
            }
          }
          else if (c == '\r')
          {
            if (!isInternal())
            {
              docHandler.characters(this.buf, first, last - first);
              docHandler.characters(newline, 0, 1);
              sawContent = true;
              this.lineNumber += 1;
              if ((this.finish > last + 1) && 
                (this.buf[(last + 1)] == '\n')) {
                last++;
              }
              first = this.start = last + 1;
            }
          }
          else if (c == ']') {
            switch (this.finish - last)
            {
            case 2: 
              if (this.buf[(last + 1)] != ']') {
                continue;
              }
            case 1: 
              if ((this.reader == null) || (this.isClosed)) {
                continue;
              }
              if (last == first) {
                throw new InternalError("fillbuf");
              }
              last--;
              if (last > first)
              {
                docHandler.characters(this.buf, first, last - first);
                sawContent = true;
                this.start = last;
              }
              fillbuf();
              first = last = this.start;
              break;
            default: 
              if ((this.buf[(last + 1)] != ']') || (this.buf[(last + 2)] != '>')) {
                continue;
              }
              fatal("P-072", null);break;
            }
          } else if ((c >= 55296) && (c <= 57343))
          {
            if (last + 1 >= this.finish)
            {
              if (last > first)
              {
                docHandler.characters(this.buf, first, last - first);
                sawContent = true;
                this.start = (last + 1);
              }
              if (isEOF()) {
                fatal("P-081", new Object[] { Integer.toHexString(c) });
              }
              first = this.start;
              last = first;
            }
            else if (checkSurrogatePair(last))
            {
              last++;
            }
            else
            {
              last--;
              
              break;
            }
          }
          else {
            fatal("P-071", new Object[] { Integer.toHexString(c) });
          }
        }
      }
    }
    char c;
    if (last == first) {
      return sawContent;
    }
    docHandler.characters(this.buf, first, last - first);
    this.start = last;
    return true;
  }
  
  public boolean unparsedContent(DTDEventListener docHandler, boolean ignorableWhitespace, String whitespaceInvalidMessage)
    throws IOException, SAXException
  {
    if (!peek("![CDATA[", null)) {
      return false;
    }
    docHandler.startCDATA();
    for (;;)
    {
      boolean done = false;
      
      boolean white = ignorableWhitespace;
      for (int last = this.start; last < this.finish; last++)
      {
        char c = this.buf[last];
        if (!XmlChars.isChar(c))
        {
          white = false;
          if ((c >= 55296) && (c <= 57343))
          {
            if (checkSurrogatePair(last))
            {
              last++;
            }
            else
            {
              last--;
              break;
            }
          }
          else {
            fatal("P-071", new Object[] { Integer.toHexString(this.buf[last]) });
          }
        }
        else if (c == '\n')
        {
          if (!isInternal()) {
            this.lineNumber += 1;
          }
        }
        else if (c == '\r')
        {
          if (!isInternal())
          {
            if (white)
            {
              if (whitespaceInvalidMessage != null) {
                this.errHandler.error(new SAXParseException(DTDParser.messages.getMessage(this.locale, whitespaceInvalidMessage), null));
              }
              docHandler.ignorableWhitespace(this.buf, this.start, last - this.start);
              
              docHandler.ignorableWhitespace(newline, 0, 1);
            }
            else
            {
              docHandler.characters(this.buf, this.start, last - this.start);
              docHandler.characters(newline, 0, 1);
            }
            this.lineNumber += 1;
            if ((this.finish > last + 1) && 
              (this.buf[(last + 1)] == '\n')) {
              last++;
            }
            this.start = (last + 1);
          }
        }
        else if (c != ']')
        {
          if ((c != ' ') && (c != '\t')) {
            white = false;
          }
        }
        else
        {
          if (last + 2 >= this.finish) {
            break;
          }
          if ((this.buf[(last + 1)] == ']') && (this.buf[(last + 2)] == '>'))
          {
            done = true;
            break;
          }
          white = false;
        }
      }
      if (white)
      {
        if (whitespaceInvalidMessage != null) {
          this.errHandler.error(new SAXParseException(DTDParser.messages.getMessage(this.locale, whitespaceInvalidMessage), null));
        }
        docHandler.ignorableWhitespace(this.buf, this.start, last - this.start);
      }
      else
      {
        docHandler.characters(this.buf, this.start, last - this.start);
      }
      if (done)
      {
        this.start = (last + 3);
        break;
      }
      this.start = last;
      if (isEOF()) {
        fatal("P-073", null);
      }
    }
    docHandler.endCDATA();
    return true;
  }
  
  private boolean checkSurrogatePair(int offset)
    throws SAXException
  {
    if (offset + 1 >= this.finish) {
      return false;
    }
    char c1 = this.buf[(offset++)];
    char c2 = this.buf[offset];
    if ((c1 >= 55296) && (c1 < 56320) && (c2 >= 56320) && (c2 <= 57343)) {
      return true;
    }
    fatal("P-074", new Object[] { Integer.toHexString(c1 & 0xFFFF), Integer.toHexString(c2 & 0xFFFF) });
    
    return false;
  }
  
  public boolean ignorableWhitespace(DTDEventListener handler)
    throws IOException, SAXException
  {
    boolean isSpace = false;
    
    int first = this.start;
    for (;;)
    {
      if (this.finish <= this.start)
      {
        if (isSpace) {
          handler.ignorableWhitespace(this.buf, first, this.start - first);
        }
        fillbuf();
        first = this.start;
      }
      if (this.finish <= this.start) {
        return isSpace;
      }
      char c = this.buf[(this.start++)];
      switch (c)
      {
      case '\n': 
        if (!isInternal()) {
          this.lineNumber += 1;
        }
      case '\t': 
      case ' ': 
        isSpace = true;
        break;
      case '\r': 
        isSpace = true;
        if (!isInternal()) {
          this.lineNumber += 1;
        }
        handler.ignorableWhitespace(this.buf, first, this.start - 1 - first);
        
        handler.ignorableWhitespace(newline, 0, 1);
        if ((this.start < this.finish) && (this.buf[this.start] == '\n')) {
          this.start += 1;
        }
        first = this.start;
      }
    }
    ungetc();
    if (isSpace) {
      handler.ignorableWhitespace(this.buf, first, this.start - first);
    }
    return isSpace;
  }
  
  public boolean peek(String next, char[] chars)
    throws IOException, SAXException
  {
    int len;
    int len;
    if (chars != null) {
      len = chars.length;
    } else {
      len = next.length();
    }
    if ((this.finish <= this.start) || (this.finish - this.start < len)) {
      fillbuf();
    }
    if (this.finish <= this.start) {
      return false;
    }
    if (chars != null) {
      for (int i = 0; (i < len) && (this.start + i < this.finish); i++) {
        if (this.buf[(this.start + i)] != chars[i]) {
          return false;
        }
      }
    }
    for (int i = 0; (i < len) && (this.start + i < this.finish); i++) {
      if (this.buf[(this.start + i)] != next.charAt(i)) {
        return false;
      }
    }
    if (i < len)
    {
      if ((this.reader == null) || (this.isClosed)) {
        return false;
      }
      if (len > this.buf.length) {
        fatal("P-077", new Object[] { new Integer(this.buf.length) });
      }
      fillbuf();
      return peek(next, chars);
    }
    this.start += len;
    return true;
  }
  
  public void startRemembering()
  {
    if (this.startRemember != 0) {
      throw new InternalError();
    }
    this.startRemember = this.start;
  }
  
  public String rememberText()
  {
    String retval;
    String retval;
    if (this.rememberedText != null)
    {
      this.rememberedText.append(this.buf, this.startRemember, this.start - this.startRemember);
      
      retval = this.rememberedText.toString();
    }
    else
    {
      retval = new String(this.buf, this.startRemember, this.start - this.startRemember);
    }
    this.startRemember = 0;
    this.rememberedText = null;
    return retval;
  }
  
  private InputEntity getTopEntity()
  {
    InputEntity current = this;
    while ((current != null) && (current.input == null)) {
      current = current.next;
    }
    return current == null ? this : current;
  }
  
  public String getPublicId()
  {
    InputEntity where = getTopEntity();
    if (where == this) {
      return this.input.getPublicId();
    }
    return where.getPublicId();
  }
  
  public String getSystemId()
  {
    InputEntity where = getTopEntity();
    if (where == this) {
      return this.input.getSystemId();
    }
    return where.getSystemId();
  }
  
  public int getLineNumber()
  {
    InputEntity where = getTopEntity();
    if (where == this) {
      return this.lineNumber;
    }
    return where.getLineNumber();
  }
  
  public int getColumnNumber()
  {
    return -1;
  }
  
  private void fillbuf()
    throws IOException, SAXException
  {
    if ((this.reader == null) || (this.isClosed)) {
      return;
    }
    if (this.startRemember != 0)
    {
      if (this.rememberedText == null) {
        this.rememberedText = new StringBuffer(this.buf.length);
      }
      this.rememberedText.append(this.buf, this.startRemember, this.start - this.startRemember);
    }
    boolean extra = (this.finish > 0) && (this.start > 0);
    if (extra) {
      this.start -= 1;
    }
    int len = this.finish - this.start;
    
    System.arraycopy(this.buf, this.start, this.buf, 0, len);
    this.start = 0;
    this.finish = len;
    try
    {
      len = this.buf.length - len;
      len = this.reader.read(this.buf, this.finish, len);
    }
    catch (UnsupportedEncodingException e)
    {
      fatal("P-075", new Object[] { e.getMessage() });
    }
    catch (CharConversionException e)
    {
      fatal("P-076", new Object[] { e.getMessage() });
    }
    if (len >= 0) {
      this.finish += len;
    } else {
      close();
    }
    if (extra) {
      this.start += 1;
    }
    if (this.startRemember != 0) {
      this.startRemember = 1;
    }
  }
  
  public void close()
  {
    try
    {
      if ((this.reader != null) && (!this.isClosed)) {
        this.reader.close();
      }
      this.isClosed = true;
    }
    catch (IOException e) {}
  }
  
  private void fatal(String messageId, Object[] params)
    throws SAXException
  {
    SAXParseException x = new SAXParseException(DTDParser.messages.getMessage(this.locale, messageId, params), null);
    
    close();
    this.errHandler.fatalError(x);
    throw x;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\dtdparser\InputEntity.class
 * Java compiler version: 3 (47.0)
 * JD-Core Version:       0.7.1
 */