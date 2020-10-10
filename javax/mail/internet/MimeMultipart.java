package javax.mail.internet;

import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.LineOutputStream;
import com.sun.mail.util.PropUtil;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessageAware;
import javax.mail.MessageContext;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.MultipartDataSource;

public class MimeMultipart
  extends Multipart
{
  protected DataSource ds = null;
  protected boolean parsed = true;
  private boolean complete = true;
  private String preamble = null;
  private boolean ignoreMissingEndBoundary = true;
  private boolean ignoreMissingBoundaryParameter = true;
  private boolean ignoreExistingBoundaryParameter = false;
  private boolean allowEmpty = false;
  private boolean bmparse = true;
  
  public MimeMultipart()
  {
    this("mixed");
  }
  
  public MimeMultipart(String subtype)
  {
    String boundary = UniqueValue.getUniqueBoundaryValue();
    ContentType cType = new ContentType("multipart", subtype, null);
    cType.setParameter("boundary", boundary);
    this.contentType = cType.toString();
  }
  
  public MimeMultipart(DataSource ds)
    throws MessagingException
  {
    if ((ds instanceof MessageAware))
    {
      MessageContext mc = ((MessageAware)ds).getMessageContext();
      setParent(mc.getPart());
    }
    if ((ds instanceof MultipartDataSource))
    {
      setMultipartDataSource((MultipartDataSource)ds);
      return;
    }
    this.parsed = false;
    this.ds = ds;
    this.contentType = ds.getContentType();
  }
  
  public synchronized void setSubType(String subtype)
    throws MessagingException
  {
    ContentType cType = new ContentType(this.contentType);
    cType.setSubType(subtype);
    this.contentType = cType.toString();
  }
  
  public synchronized int getCount()
    throws MessagingException
  {
    parse();
    return super.getCount();
  }
  
  public synchronized BodyPart getBodyPart(int index)
    throws MessagingException
  {
    parse();
    return super.getBodyPart(index);
  }
  
  public synchronized BodyPart getBodyPart(String CID)
    throws MessagingException
  {
    parse();
    
    int count = getCount();
    for (int i = 0; i < count; i++)
    {
      MimeBodyPart part = (MimeBodyPart)getBodyPart(i);
      String s = part.getContentID();
      if ((s != null) && (s.equals(CID))) {
        return part;
      }
    }
    return null;
  }
  
  public boolean removeBodyPart(BodyPart part)
    throws MessagingException
  {
    parse();
    return super.removeBodyPart(part);
  }
  
  public void removeBodyPart(int index)
    throws MessagingException
  {
    parse();
    super.removeBodyPart(index);
  }
  
  public synchronized void addBodyPart(BodyPart part)
    throws MessagingException
  {
    parse();
    super.addBodyPart(part);
  }
  
  public synchronized void addBodyPart(BodyPart part, int index)
    throws MessagingException
  {
    parse();
    super.addBodyPart(part, index);
  }
  
  public synchronized boolean isComplete()
    throws MessagingException
  {
    parse();
    return this.complete;
  }
  
  public synchronized String getPreamble()
    throws MessagingException
  {
    parse();
    return this.preamble;
  }
  
  public synchronized void setPreamble(String preamble)
    throws MessagingException
  {
    this.preamble = preamble;
  }
  
  protected synchronized void updateHeaders()
    throws MessagingException
  {
    parse();
    for (int i = 0; i < this.parts.size(); i++) {
      ((MimeBodyPart)this.parts.elementAt(i)).updateHeaders();
    }
  }
  
  public synchronized void writeTo(OutputStream os)
    throws IOException, MessagingException
  {
    parse();
    
    String boundary = "--" + new ContentType(this.contentType).getParameter("boundary");
    
    LineOutputStream los = new LineOutputStream(os);
    if (this.preamble != null)
    {
      byte[] pb = ASCIIUtility.getBytes(this.preamble);
      los.write(pb);
      if ((pb.length > 0) && (pb[(pb.length - 1)] != 13) && (pb[(pb.length - 1)] != 10)) {
        los.writeln();
      }
    }
    if (this.parts.size() == 0)
    {
      this.allowEmpty = PropUtil.getBooleanSystemProperty("mail.mime.multipart.allowempty", false);
      if (this.allowEmpty)
      {
        los.writeln(boundary);
        los.writeln();
      }
      else
      {
        throw new MessagingException("Empty multipart: " + this.contentType);
      }
    }
    else
    {
      for (int i = 0; i < this.parts.size(); i++)
      {
        los.writeln(boundary);
        ((MimeBodyPart)this.parts.elementAt(i)).writeTo(os);
        los.writeln();
      }
    }
    los.writeln(boundary + "--");
  }
  
  protected synchronized void parse()
    throws MessagingException
  {
    if (this.parsed) {
      return;
    }
    this.ignoreMissingEndBoundary = PropUtil.getBooleanSystemProperty("mail.mime.multipart.ignoremissingendboundary", true);
    
    this.ignoreMissingBoundaryParameter = PropUtil.getBooleanSystemProperty("mail.mime.multipart.ignoremissingboundaryparameter", true);
    
    this.ignoreExistingBoundaryParameter = PropUtil.getBooleanSystemProperty("mail.mime.multipart.ignoreexistingboundaryparameter", false);
    
    this.allowEmpty = PropUtil.getBooleanSystemProperty("mail.mime.multipart.allowempty", false);
    
    this.bmparse = PropUtil.getBooleanSystemProperty("mail.mime.multipart.bmparse", true);
    if (this.bmparse)
    {
      parsebm();
      return;
    }
    InputStream in = null;
    SharedInputStream sin = null;
    long start = 0L;long end = 0L;
    try
    {
      in = this.ds.getInputStream();
      if ((!(in instanceof ByteArrayInputStream)) && (!(in instanceof BufferedInputStream)) && (!(in instanceof SharedInputStream))) {
        in = new BufferedInputStream(in);
      }
    }
    catch (Exception ex)
    {
      throw new MessagingException("No inputstream from datasource", ex);
    }
    if ((in instanceof SharedInputStream)) {
      sin = (SharedInputStream)in;
    }
    ContentType cType = new ContentType(this.contentType);
    String boundary = null;
    if (!this.ignoreExistingBoundaryParameter)
    {
      String bp = cType.getParameter("boundary");
      if (bp != null) {
        boundary = "--" + bp;
      }
    }
    if ((boundary == null) && (!this.ignoreMissingBoundaryParameter) && (!this.ignoreExistingBoundaryParameter)) {
      throw new MessagingException("Missing boundary parameter");
    }
    try
    {
      LineInputStream lin = new LineInputStream(in);
      StringBuffer preamblesb = null;
      
      String lineSeparator = null;
      String line;
      while ((line = lin.readLine()) != null)
      {
        for (int i = line.length() - 1; i >= 0; i--)
        {
          char c = line.charAt(i);
          if ((c != ' ') && (c != '\t')) {
            break;
          }
        }
        line = line.substring(0, i + 1);
        if (boundary != null)
        {
          if (line.equals(boundary)) {
            break;
          }
          if ((line.length() == boundary.length() + 2) && (line.startsWith(boundary)) && (line.endsWith("--")))
          {
            line = null;
            break;
          }
        }
        else if ((line.length() > 2) && (line.startsWith("--")) && (
          (line.length() <= 4) || (!allDashes(line))))
        {
          boundary = line;
          break;
        }
        if (line.length() > 0)
        {
          if (lineSeparator == null) {
            try
            {
              lineSeparator = System.getProperty("line.separator", "\n");
            }
            catch (SecurityException ex)
            {
              lineSeparator = "\n";
            }
          }
          if (preamblesb == null) {
            preamblesb = new StringBuffer(line.length() + 2);
          }
          preamblesb.append(line).append(lineSeparator);
        }
      }
      if (preamblesb != null) {
        this.preamble = preamblesb.toString();
      }
      if (line == null)
      {
        if (this.allowEmpty) {
          return;
        }
        throw new MessagingException("Missing start boundary");
      }
      byte[] bndbytes = ASCIIUtility.getBytes(boundary);
      int bl = bndbytes.length;
      
      boolean done = false;
      while (!done)
      {
        InternetHeaders headers = null;
        if (sin != null)
        {
          start = sin.getPosition();
          while (((line = lin.readLine()) != null) && (line.length() > 0)) {}
          if (line == null)
          {
            if (!this.ignoreMissingEndBoundary) {
              throw new MessagingException("missing multipart end boundary");
            }
            this.complete = false;
            break;
          }
        }
        else
        {
          headers = createInternetHeaders(in);
        }
        if (!in.markSupported()) {
          throw new MessagingException("Stream doesn't support mark");
        }
        ByteArrayOutputStream buf = null;
        if (sin == null) {
          buf = new ByteArrayOutputStream();
        } else {
          end = sin.getPosition();
        }
        boolean bol = true;
        
        int eol1 = -1;int eol2 = -1;
        for (;;)
        {
          if (bol)
          {
            in.mark(bl + 4 + 1000);
            for (int i = 0; i < bl; i++) {
              if (in.read() != (bndbytes[i] & 0xFF)) {
                break;
              }
            }
            if (i == bl)
            {
              int b2 = in.read();
              if ((b2 == 45) && 
                (in.read() == 45))
              {
                this.complete = true;
                done = true;
                break;
              }
              while ((b2 == 32) || (b2 == 9)) {
                b2 = in.read();
              }
              if (b2 == 10) {
                break;
              }
              if (b2 == 13)
              {
                in.mark(1);
                if (in.read() == 10) {
                  break;
                }
                in.reset(); break;
              }
            }
            in.reset();
            if ((buf != null) && (eol1 != -1))
            {
              buf.write(eol1);
              if (eol2 != -1) {
                buf.write(eol2);
              }
              eol1 = eol2 = -1;
            }
          }
          int b;
          if ((b = in.read()) < 0)
          {
            if (!this.ignoreMissingEndBoundary) {
              throw new MessagingException("missing multipart end boundary");
            }
            this.complete = false;
            done = true;
            break;
          }
          if ((b == 13) || (b == 10))
          {
            bol = true;
            if (sin != null) {
              end = sin.getPosition() - 1L;
            }
            eol1 = b;
            if (b == 13)
            {
              in.mark(1);
              if ((b = in.read()) == 10) {
                eol2 = b;
              } else {
                in.reset();
              }
            }
          }
          else
          {
            bol = false;
            if (buf != null) {
              buf.write(b);
            }
          }
        }
        MimeBodyPart part;
        MimeBodyPart part;
        if (sin != null) {
          part = createMimeBodyPartIs(sin.newStream(start, end));
        } else {
          part = createMimeBodyPart(headers, buf.toByteArray());
        }
        super.addBodyPart(part);
      }
    }
    catch (IOException ioex)
    {
      throw new MessagingException("IO Error", ioex);
    }
    finally
    {
      try
      {
        in.close();
      }
      catch (IOException cex) {}
    }
    this.parsed = true;
  }
  
  private synchronized void parsebm()
    throws MessagingException
  {
    if (this.parsed) {
      return;
    }
    InputStream in = null;
    SharedInputStream sin = null;
    long start = 0L;long end = 0L;
    try
    {
      in = this.ds.getInputStream();
      if ((!(in instanceof ByteArrayInputStream)) && (!(in instanceof BufferedInputStream)) && (!(in instanceof SharedInputStream))) {
        in = new BufferedInputStream(in);
      }
    }
    catch (Exception ex)
    {
      throw new MessagingException("No inputstream from datasource", ex);
    }
    if ((in instanceof SharedInputStream)) {
      sin = (SharedInputStream)in;
    }
    ContentType cType = new ContentType(this.contentType);
    String boundary = null;
    if (!this.ignoreExistingBoundaryParameter)
    {
      String bp = cType.getParameter("boundary");
      if (bp != null) {
        boundary = "--" + bp;
      }
    }
    if ((boundary == null) && (!this.ignoreMissingBoundaryParameter) && (!this.ignoreExistingBoundaryParameter)) {
      throw new MessagingException("Missing boundary parameter");
    }
    try
    {
      LineInputStream lin = new LineInputStream(in);
      StringBuffer preamblesb = null;
      
      String lineSeparator = null;
      String line;
      while ((line = lin.readLine()) != null)
      {
        for (int i = line.length() - 1; i >= 0; i--)
        {
          char c = line.charAt(i);
          if ((c != ' ') && (c != '\t')) {
            break;
          }
        }
        line = line.substring(0, i + 1);
        if (boundary != null)
        {
          if (line.equals(boundary)) {
            break;
          }
          if ((line.length() == boundary.length() + 2) && (line.startsWith(boundary)) && (line.endsWith("--")))
          {
            line = null;
            break;
          }
        }
        else if ((line.length() > 2) && (line.startsWith("--")) && (
          (line.length() <= 4) || (!allDashes(line))))
        {
          boundary = line;
          break;
        }
        if (line.length() > 0)
        {
          if (lineSeparator == null) {
            try
            {
              lineSeparator = System.getProperty("line.separator", "\n");
            }
            catch (SecurityException ex)
            {
              lineSeparator = "\n";
            }
          }
          if (preamblesb == null) {
            preamblesb = new StringBuffer(line.length() + 2);
          }
          preamblesb.append(line).append(lineSeparator);
        }
      }
      if (preamblesb != null) {
        this.preamble = preamblesb.toString();
      }
      if (line == null)
      {
        if (this.allowEmpty) {
          return;
        }
        throw new MessagingException("Missing start boundary");
      }
      byte[] bndbytes = ASCIIUtility.getBytes(boundary);
      int bl = bndbytes.length;
      
      int[] bcs = new int['Ā'];
      for (int i = 0; i < bl; i++) {
        bcs[(bndbytes[i] & 0xFF)] = (i + 1);
      }
      int[] gss = new int[bl];
      label600:
      for (int i = bl; i > 0; i--)
      {
        for (int j = bl - 1; j >= i; j--)
        {
          if (bndbytes[j] != bndbytes[(j - i)]) {
            break label600;
          }
          gss[(j - 1)] = i;
        }
        while (j > 0) {
          gss[(--j)] = i;
        }
      }
      gss[(bl - 1)] = 1;
      
      boolean done = false;
      while (!done)
      {
        InternetHeaders headers = null;
        if (sin != null)
        {
          start = sin.getPosition();
          while (((line = lin.readLine()) != null) && (line.length() > 0)) {}
          if (line == null)
          {
            if (!this.ignoreMissingEndBoundary) {
              throw new MessagingException("missing multipart end boundary");
            }
            this.complete = false;
            break;
          }
        }
        else
        {
          headers = createInternetHeaders(in);
        }
        if (!in.markSupported()) {
          throw new MessagingException("Stream doesn't support mark");
        }
        ByteArrayOutputStream buf = null;
        if (sin == null) {
          buf = new ByteArrayOutputStream();
        } else {
          end = sin.getPosition();
        }
        byte[] inbuf = new byte[bl];
        byte[] previnbuf = new byte[bl];
        int inSize = 0;
        int prevSize = 0;
        
        boolean first = true;
        int eolLen;
        for (;;)
        {
          in.mark(bl + 4 + 1000);
          eolLen = 0;
          inSize = readFully(in, inbuf, 0, bl);
          if (inSize < bl)
          {
            if (!this.ignoreMissingEndBoundary) {
              throw new MessagingException("missing multipart end boundary");
            }
            if (sin != null) {
              end = sin.getPosition();
            }
            this.complete = false;
            done = true;
            break;
          }
          for (int i = bl - 1; i >= 0; i--) {
            if (inbuf[i] != bndbytes[i]) {
              break;
            }
          }
          if (i < 0)
          {
            eolLen = 0;
            if (!first)
            {
              int b = previnbuf[(prevSize - 1)];
              if ((b == 13) || (b == 10))
              {
                eolLen = 1;
                if ((b == 10) && (prevSize >= 2))
                {
                  b = previnbuf[(prevSize - 2)];
                  if (b == 13) {
                    eolLen = 2;
                  }
                }
              }
            }
            if ((first) || (eolLen > 0))
            {
              if (sin != null) {
                end = sin.getPosition() - bl - eolLen;
              }
              int b2 = in.read();
              if ((b2 == 45) && 
                (in.read() == 45))
              {
                this.complete = true;
                done = true;
                break;
              }
              while ((b2 == 32) || (b2 == 9)) {
                b2 = in.read();
              }
              if (b2 == 10) {
                break;
              }
              if (b2 == 13)
              {
                in.mark(1);
                if (in.read() == 10) {
                  break;
                }
                in.reset(); break;
              }
            }
            i = 0;
          }
          int skip = Math.max(i + 1 - bcs[(inbuf[i] & 0x7F)], gss[i]);
          if (skip < 2)
          {
            if ((sin == null) && (prevSize > 1)) {
              buf.write(previnbuf, 0, prevSize - 1);
            }
            in.reset();
            skipFully(in, 1L);
            if (prevSize >= 1)
            {
              previnbuf[0] = previnbuf[(prevSize - 1)];
              previnbuf[1] = inbuf[0];
              prevSize = 2;
            }
            else
            {
              previnbuf[0] = inbuf[0];
              prevSize = 1;
            }
          }
          else
          {
            if ((prevSize > 0) && (sin == null)) {
              buf.write(previnbuf, 0, prevSize);
            }
            prevSize = skip;
            in.reset();
            skipFully(in, prevSize);
            
            byte[] tmp = inbuf;
            inbuf = previnbuf;
            previnbuf = tmp;
          }
          first = false;
        }
        MimeBodyPart part;
        MimeBodyPart part;
        if (sin != null)
        {
          part = createMimeBodyPartIs(sin.newStream(start, end));
        }
        else
        {
          if (prevSize - eolLen > 0) {
            buf.write(previnbuf, 0, prevSize - eolLen);
          }
          if ((!this.complete) && (inSize > 0)) {
            buf.write(inbuf, 0, inSize);
          }
          part = createMimeBodyPart(headers, buf.toByteArray());
        }
        super.addBodyPart(part);
      }
    }
    catch (IOException ioex)
    {
      throw new MessagingException("IO Error", ioex);
    }
    finally
    {
      try
      {
        in.close();
      }
      catch (IOException cex) {}
    }
    this.parsed = true;
  }
  
  private static boolean allDashes(String s)
  {
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) != '-') {
        return false;
      }
    }
    return true;
  }
  
  private static int readFully(InputStream in, byte[] buf, int off, int len)
    throws IOException
  {
    if (len == 0) {
      return 0;
    }
    int total = 0;
    while (len > 0)
    {
      int bsize = in.read(buf, off, len);
      if (bsize <= 0) {
        break;
      }
      off += bsize;
      total += bsize;
      len -= bsize;
    }
    return total > 0 ? total : -1;
  }
  
  private void skipFully(InputStream in, long offset)
    throws IOException
  {
    while (offset > 0L)
    {
      long cur = in.skip(offset);
      if (cur <= 0L) {
        throw new EOFException("can't skip");
      }
      offset -= cur;
    }
  }
  
  protected InternetHeaders createInternetHeaders(InputStream is)
    throws MessagingException
  {
    return new InternetHeaders(is);
  }
  
  protected MimeBodyPart createMimeBodyPart(InternetHeaders headers, byte[] content)
    throws MessagingException
  {
    return new MimeBodyPart(headers, content);
  }
  
  protected MimeBodyPart createMimeBodyPart(InputStream is)
    throws MessagingException
  {
    return new MimeBodyPart(is);
  }
  
  private MimeBodyPart createMimeBodyPartIs(InputStream is)
    throws MessagingException
  {
    try
    {
      return createMimeBodyPart(is);
    }
    finally
    {
      try
      {
        is.close();
      }
      catch (IOException ex) {}
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\internet\MimeMultipart.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */