package javax.mail.internet;

import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.PropUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.mail.Header;
import javax.mail.MessagingException;

public class InternetHeaders
{
  private static final boolean ignoreWhitespaceLines = PropUtil.getBooleanSystemProperty("mail.mime.ignorewhitespacelines", false);
  protected List headers;
  
  protected static final class InternetHeader
    extends Header
  {
    String line;
    
    public InternetHeader(String l)
    {
      super("");
      int i = l.indexOf(':');
      if (i < 0) {
        this.name = l.trim();
      } else {
        this.name = l.substring(0, i).trim();
      }
      this.line = l;
    }
    
    public InternetHeader(String n, String v)
    {
      super("");
      if (v != null) {
        this.line = (n + ": " + v);
      } else {
        this.line = null;
      }
    }
    
    public String getValue()
    {
      int i = this.line.indexOf(':');
      if (i < 0) {
        return this.line;
      }
      for (int j = i + 1; j < this.line.length(); j++)
      {
        char c = this.line.charAt(j);
        if ((c != ' ') && (c != '\t') && (c != '\r') && (c != '\n')) {
          break;
        }
      }
      return this.line.substring(j);
    }
  }
  
  static class matchEnum
    implements Enumeration
  {
    private Iterator e;
    private String[] names;
    private boolean match;
    private boolean want_line;
    private InternetHeaders.InternetHeader next_header;
    
    matchEnum(List v, String[] n, boolean m, boolean l)
    {
      this.e = v.iterator();
      this.names = n;
      this.match = m;
      this.want_line = l;
      this.next_header = null;
    }
    
    public boolean hasMoreElements()
    {
      if (this.next_header == null) {
        this.next_header = nextMatch();
      }
      return this.next_header != null;
    }
    
    public Object nextElement()
    {
      if (this.next_header == null) {
        this.next_header = nextMatch();
      }
      if (this.next_header == null) {
        throw new NoSuchElementException("No more headers");
      }
      InternetHeaders.InternetHeader h = this.next_header;
      this.next_header = null;
      if (this.want_line) {
        return h.line;
      }
      return new Header(h.getName(), h.getValue());
    }
    
    private InternetHeaders.InternetHeader nextMatch()
    {
      while (this.e.hasNext())
      {
        InternetHeaders.InternetHeader h = (InternetHeaders.InternetHeader)this.e.next();
        if (h.line != null)
        {
          if (this.names == null) {
            return this.match ? null : h;
          }
          for (int i = 0;; i++)
          {
            if (i >= this.names.length) {
              break label97;
            }
            if (this.names[i].equalsIgnoreCase(h.getName()))
            {
              if (!this.match) {
                break;
              }
              return h;
            }
          }
          label97:
          if (!this.match) {
            return h;
          }
        }
      }
      return null;
    }
  }
  
  public InternetHeaders()
  {
    this.headers = new ArrayList(40);
    this.headers.add(new InternetHeader("Return-Path", null));
    this.headers.add(new InternetHeader("Received", null));
    this.headers.add(new InternetHeader("Resent-Date", null));
    this.headers.add(new InternetHeader("Resent-From", null));
    this.headers.add(new InternetHeader("Resent-Sender", null));
    this.headers.add(new InternetHeader("Resent-To", null));
    this.headers.add(new InternetHeader("Resent-Cc", null));
    this.headers.add(new InternetHeader("Resent-Bcc", null));
    this.headers.add(new InternetHeader("Resent-Message-Id", null));
    this.headers.add(new InternetHeader("Date", null));
    this.headers.add(new InternetHeader("From", null));
    this.headers.add(new InternetHeader("Sender", null));
    this.headers.add(new InternetHeader("Reply-To", null));
    this.headers.add(new InternetHeader("To", null));
    this.headers.add(new InternetHeader("Cc", null));
    this.headers.add(new InternetHeader("Bcc", null));
    this.headers.add(new InternetHeader("Message-Id", null));
    this.headers.add(new InternetHeader("In-Reply-To", null));
    this.headers.add(new InternetHeader("References", null));
    this.headers.add(new InternetHeader("Subject", null));
    this.headers.add(new InternetHeader("Comments", null));
    this.headers.add(new InternetHeader("Keywords", null));
    this.headers.add(new InternetHeader("Errors-To", null));
    this.headers.add(new InternetHeader("MIME-Version", null));
    this.headers.add(new InternetHeader("Content-Type", null));
    this.headers.add(new InternetHeader("Content-Transfer-Encoding", null));
    this.headers.add(new InternetHeader("Content-MD5", null));
    this.headers.add(new InternetHeader(":", null));
    this.headers.add(new InternetHeader("Content-Length", null));
    this.headers.add(new InternetHeader("Status", null));
  }
  
  public InternetHeaders(InputStream is)
    throws MessagingException
  {
    this.headers = new ArrayList(40);
    load(is);
  }
  
  public void load(InputStream is)
    throws MessagingException
  {
    LineInputStream lis = new LineInputStream(is);
    String prevline = null;
    
    StringBuffer lineBuffer = new StringBuffer();
    try
    {
      String line;
      do
      {
        line = lis.readLine();
        if ((line != null) && ((line.startsWith(" ")) || (line.startsWith("\t"))))
        {
          if (prevline != null)
          {
            lineBuffer.append(prevline);
            prevline = null;
          }
          lineBuffer.append("\r\n");
          lineBuffer.append(line);
        }
        else
        {
          if (prevline != null)
          {
            addHeaderLine(prevline);
          }
          else if (lineBuffer.length() > 0)
          {
            addHeaderLine(lineBuffer.toString());
            lineBuffer.setLength(0);
          }
          prevline = line;
        }
        if (line == null) {
          break;
        }
      } while (!isEmpty(line));
    }
    catch (IOException ioex)
    {
      throw new MessagingException("Error in input stream", ioex);
    }
  }
  
  private static final boolean isEmpty(String line)
  {
    return (line.length() == 0) || ((ignoreWhitespaceLines) && (line.trim().length() == 0));
  }
  
  public String[] getHeader(String name)
  {
    Iterator e = this.headers.iterator();
    
    List v = new ArrayList();
    while (e.hasNext())
    {
      InternetHeader h = (InternetHeader)e.next();
      if ((name.equalsIgnoreCase(h.getName())) && (h.line != null)) {
        v.add(h.getValue());
      }
    }
    if (v.size() == 0) {
      return null;
    }
    String[] r = new String[v.size()];
    r = (String[])v.toArray(r);
    return r;
  }
  
  public String getHeader(String name, String delimiter)
  {
    String[] s = getHeader(name);
    if (s == null) {
      return null;
    }
    if ((s.length == 1) || (delimiter == null)) {
      return s[0];
    }
    StringBuffer r = new StringBuffer(s[0]);
    for (int i = 1; i < s.length; i++)
    {
      r.append(delimiter);
      r.append(s[i]);
    }
    return r.toString();
  }
  
  public void setHeader(String name, String value)
  {
    boolean found = false;
    for (int i = 0; i < this.headers.size(); i++)
    {
      InternetHeader h = (InternetHeader)this.headers.get(i);
      if (name.equalsIgnoreCase(h.getName())) {
        if (!found)
        {
          int j;
          if ((h.line != null) && ((j = h.line.indexOf(':')) >= 0)) {
            h.line = (h.line.substring(0, j + 1) + " " + value);
          } else {
            h.line = (name + ": " + value);
          }
          found = true;
        }
        else
        {
          this.headers.remove(i);
          i--;
        }
      }
    }
    if (!found) {
      addHeader(name, value);
    }
  }
  
  public void addHeader(String name, String value)
  {
    int pos = this.headers.size();
    boolean addReverse = (name.equalsIgnoreCase("Received")) || (name.equalsIgnoreCase("Return-Path"));
    if (addReverse) {
      pos = 0;
    }
    for (int i = this.headers.size() - 1; i >= 0; i--)
    {
      InternetHeader h = (InternetHeader)this.headers.get(i);
      if (name.equalsIgnoreCase(h.getName())) {
        if (addReverse)
        {
          pos = i;
        }
        else
        {
          this.headers.add(i + 1, new InternetHeader(name, value));
          return;
        }
      }
      if ((!addReverse) && (h.getName().equals(":"))) {
        pos = i;
      }
    }
    this.headers.add(pos, new InternetHeader(name, value));
  }
  
  public void removeHeader(String name)
  {
    for (int i = 0; i < this.headers.size(); i++)
    {
      InternetHeader h = (InternetHeader)this.headers.get(i);
      if (name.equalsIgnoreCase(h.getName())) {
        h.line = null;
      }
    }
  }
  
  public Enumeration getAllHeaders()
  {
    return new matchEnum(this.headers, null, false, false);
  }
  
  public Enumeration getMatchingHeaders(String[] names)
  {
    return new matchEnum(this.headers, names, true, false);
  }
  
  public Enumeration getNonMatchingHeaders(String[] names)
  {
    return new matchEnum(this.headers, names, false, false);
  }
  
  public void addHeaderLine(String line)
  {
    try
    {
      char c = line.charAt(0);
      if ((c == ' ') || (c == '\t'))
      {
        InternetHeader h = (InternetHeader)this.headers.get(this.headers.size() - 1); InternetHeader 
        
          tmp50_49 = h;tmp50_49.line = (tmp50_49.line + "\r\n" + line);
      }
      else
      {
        this.headers.add(new InternetHeader(line));
      }
    }
    catch (StringIndexOutOfBoundsException e) {}catch (NoSuchElementException e) {}
  }
  
  public Enumeration getAllHeaderLines()
  {
    return getNonMatchingHeaderLines(null);
  }
  
  public Enumeration getMatchingHeaderLines(String[] names)
  {
    return new matchEnum(this.headers, names, true, true);
  }
  
  public Enumeration getNonMatchingHeaderLines(String[] names)
  {
    return new matchEnum(this.headers, names, false, true);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\internet\InternetHeaders.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */