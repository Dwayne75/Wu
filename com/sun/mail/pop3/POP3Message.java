package com.sun.mail.pop3;

import com.sun.mail.util.MailLogger;
import com.sun.mail.util.ReadableMime;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.Enumeration;
import java.util.logging.Level;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.IllegalWriteException;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.SharedInputStream;

public class POP3Message
  extends MimeMessage
  implements ReadableMime
{
  static final String UNKNOWN = "UNKNOWN";
  private POP3Folder folder;
  private int hdrSize = -1;
  private int msgSize = -1;
  String uid = "UNKNOWN";
  private SoftReference rawData = new SoftReference(null);
  
  public POP3Message(Folder folder, int msgno)
    throws MessagingException
  {
    super(folder, msgno);
    this.folder = ((POP3Folder)folder);
  }
  
  public synchronized void setFlags(Flags newFlags, boolean set)
    throws MessagingException
  {
    Flags oldFlags = (Flags)this.flags.clone();
    super.setFlags(newFlags, set);
    if (!this.flags.equals(oldFlags)) {
      this.folder.notifyMessageChangedListeners(1, this);
    }
  }
  
  /* Error */
  public int getSize()
    throws MessagingException
  {
    // Byte code:
    //   0: aload_0
    //   1: dup
    //   2: astore_1
    //   3: monitorenter
    //   4: aload_0
    //   5: getfield 8	com/sun/mail/pop3/POP3Message:msgSize	I
    //   8: ifle +10 -> 18
    //   11: aload_0
    //   12: getfield 8	com/sun/mail/pop3/POP3Message:msgSize	I
    //   15: aload_1
    //   16: monitorexit
    //   17: ireturn
    //   18: aload_1
    //   19: monitorexit
    //   20: goto +8 -> 28
    //   23: astore_2
    //   24: aload_1
    //   25: monitorexit
    //   26: aload_2
    //   27: athrow
    //   28: aload_0
    //   29: getfield 22	com/sun/mail/pop3/POP3Message:headers	Ljavax/mail/internet/InternetHeaders;
    //   32: ifnonnull +7 -> 39
    //   35: aload_0
    //   36: invokespecial 23	com/sun/mail/pop3/POP3Message:loadHeaders	()V
    //   39: aload_0
    //   40: dup
    //   41: astore_1
    //   42: monitorenter
    //   43: aload_0
    //   44: getfield 8	com/sun/mail/pop3/POP3Message:msgSize	I
    //   47: ifge +26 -> 73
    //   50: aload_0
    //   51: aload_0
    //   52: getfield 15	com/sun/mail/pop3/POP3Message:folder	Lcom/sun/mail/pop3/POP3Folder;
    //   55: invokevirtual 24	com/sun/mail/pop3/POP3Folder:getProtocol	()Lcom/sun/mail/pop3/Protocol;
    //   58: aload_0
    //   59: getfield 25	com/sun/mail/pop3/POP3Message:msgnum	I
    //   62: invokevirtual 26	com/sun/mail/pop3/Protocol:list	(I)I
    //   65: aload_0
    //   66: getfield 7	com/sun/mail/pop3/POP3Message:hdrSize	I
    //   69: isub
    //   70: putfield 8	com/sun/mail/pop3/POP3Message:msgSize	I
    //   73: aload_0
    //   74: getfield 8	com/sun/mail/pop3/POP3Message:msgSize	I
    //   77: aload_1
    //   78: monitorexit
    //   79: ireturn
    //   80: astore_3
    //   81: aload_1
    //   82: monitorexit
    //   83: aload_3
    //   84: athrow
    //   85: astore_1
    //   86: aload_0
    //   87: getfield 15	com/sun/mail/pop3/POP3Message:folder	Lcom/sun/mail/pop3/POP3Folder;
    //   90: iconst_0
    //   91: invokevirtual 28	com/sun/mail/pop3/POP3Folder:close	(Z)V
    //   94: new 29	javax/mail/FolderClosedException
    //   97: dup
    //   98: aload_0
    //   99: getfield 15	com/sun/mail/pop3/POP3Message:folder	Lcom/sun/mail/pop3/POP3Folder;
    //   102: aload_1
    //   103: invokevirtual 30	java/io/EOFException:toString	()Ljava/lang/String;
    //   106: invokespecial 31	javax/mail/FolderClosedException:<init>	(Ljavax/mail/Folder;Ljava/lang/String;)V
    //   109: athrow
    //   110: astore_1
    //   111: new 33	javax/mail/MessagingException
    //   114: dup
    //   115: ldc 34
    //   117: aload_1
    //   118: invokespecial 35	javax/mail/MessagingException:<init>	(Ljava/lang/String;Ljava/lang/Exception;)V
    //   121: athrow
    // Line number table:
    //   Java source line #111	-> byte code offset #0
    //   Java source line #113	-> byte code offset #4
    //   Java source line #114	-> byte code offset #11
    //   Java source line #115	-> byte code offset #18
    //   Java source line #132	-> byte code offset #28
    //   Java source line #133	-> byte code offset #35
    //   Java source line #135	-> byte code offset #39
    //   Java source line #136	-> byte code offset #43
    //   Java source line #137	-> byte code offset #50
    //   Java source line #138	-> byte code offset #73
    //   Java source line #139	-> byte code offset #80
    //   Java source line #140	-> byte code offset #85
    //   Java source line #141	-> byte code offset #86
    //   Java source line #142	-> byte code offset #94
    //   Java source line #143	-> byte code offset #110
    //   Java source line #144	-> byte code offset #111
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	122	0	this	POP3Message
    //   85	18	1	eex	EOFException
    //   110	8	1	ex	IOException
    //   23	4	2	localObject1	Object
    //   80	4	3	localObject2	Object
    // Exception table:
    //   from	to	target	type
    //   4	17	23	finally
    //   18	20	23	finally
    //   23	26	23	finally
    //   43	79	80	finally
    //   80	83	80	finally
    //   0	17	85	java/io/EOFException
    //   18	79	85	java/io/EOFException
    //   80	85	85	java/io/EOFException
    //   0	17	110	java/io/IOException
    //   18	79	110	java/io/IOException
    //   80	85	110	java/io/IOException
  }
  
  private InputStream getRawStream(boolean skipHeader)
    throws MessagingException
  {
    InputStream rawcontent = null;
    try
    {
      synchronized (this)
      {
        rawcontent = (InputStream)this.rawData.get();
        if (rawcontent == null)
        {
          TempFile cache = this.folder.getFileCache();
          if (cache != null)
          {
            Session s = ((POP3Store)this.folder.getStore()).getSession();
            if (this.folder.logger.isLoggable(Level.FINE)) {
              this.folder.logger.fine("caching message #" + this.msgnum + " in temp file");
            }
            AppendStream os = cache.getAppendStream();
            BufferedOutputStream bos = new BufferedOutputStream(os);
            try
            {
              this.folder.getProtocol().retr(this.msgnum, bos);
            }
            finally
            {
              bos.close();
            }
            rawcontent = os.getInputStream();
          }
          else
          {
            rawcontent = this.folder.getProtocol().retr(this.msgnum, this.msgSize > 0 ? this.msgSize + this.hdrSize : 0);
          }
          if (rawcontent == null)
          {
            this.expunged = true;
            throw new MessageRemovedException("can't retrieve message #" + this.msgnum + " in POP3Message.getContentStream");
          }
          if ((this.headers == null) || (((POP3Store)this.folder.getStore()).forgetTopHeaders))
          {
            this.headers = new InternetHeaders(rawcontent);
            this.hdrSize = ((int)((SharedInputStream)rawcontent).getPosition());
          }
          else
          {
            int offset = 0;
            for (;;)
            {
              int len = 0;
              int c1;
              while (((c1 = rawcontent.read()) >= 0) && 
                (c1 != 10))
              {
                if (c1 == 13)
                {
                  if (rawcontent.available() <= 0) {
                    break;
                  }
                  rawcontent.mark(1);
                  if (rawcontent.read() == 10) {
                    break;
                  }
                  rawcontent.reset(); break;
                }
                len++;
              }
              if (rawcontent.available() == 0) {
                break;
              }
              if (len == 0) {
                break;
              }
            }
            this.hdrSize = ((int)((SharedInputStream)rawcontent).getPosition());
          }
          this.msgSize = rawcontent.available();
          
          this.rawData = new SoftReference(rawcontent);
        }
      }
    }
    catch (EOFException eex)
    {
      this.folder.close(false);
      throw new FolderClosedException(this.folder, eex.toString());
    }
    catch (IOException ex)
    {
      throw new MessagingException("error fetching POP3 content", ex);
    }
    rawcontent = ((SharedInputStream)rawcontent).newStream(skipHeader ? this.hdrSize : 0L, -1L);
    
    return rawcontent;
  }
  
  protected synchronized InputStream getContentStream()
    throws MessagingException
  {
    if (this.contentStream != null) {
      return ((SharedInputStream)this.contentStream).newStream(0L, -1L);
    }
    InputStream cstream = getRawStream(true);
    
    TempFile cache = this.folder.getFileCache();
    if ((cache != null) || (((POP3Store)this.folder.getStore()).keepMessageContent)) {
      this.contentStream = ((SharedInputStream)cstream).newStream(0L, -1L);
    }
    return cstream;
  }
  
  public InputStream getMimeStream()
    throws MessagingException
  {
    return getRawStream(false);
  }
  
  public synchronized void invalidate(boolean invalidateHeaders)
  {
    this.content = null;
    InputStream rstream = (InputStream)this.rawData.get();
    if (rstream != null)
    {
      try
      {
        rstream.close();
      }
      catch (IOException ex) {}
      this.rawData = new SoftReference(null);
    }
    if (this.contentStream != null)
    {
      try
      {
        this.contentStream.close();
      }
      catch (IOException ex) {}
      this.contentStream = null;
    }
    this.msgSize = -1;
    if (invalidateHeaders)
    {
      this.headers = null;
      this.hdrSize = -1;
    }
  }
  
  /* Error */
  public InputStream top(int n)
    throws MessagingException
  {
    // Byte code:
    //   0: aload_0
    //   1: dup
    //   2: astore_2
    //   3: monitorenter
    //   4: aload_0
    //   5: getfield 15	com/sun/mail/pop3/POP3Message:folder	Lcom/sun/mail/pop3/POP3Folder;
    //   8: invokevirtual 24	com/sun/mail/pop3/POP3Folder:getProtocol	()Lcom/sun/mail/pop3/Protocol;
    //   11: aload_0
    //   12: getfield 25	com/sun/mail/pop3/POP3Message:msgnum	I
    //   15: iload_1
    //   16: invokevirtual 83	com/sun/mail/pop3/Protocol:top	(II)Ljava/io/InputStream;
    //   19: aload_2
    //   20: monitorexit
    //   21: areturn
    //   22: astore_3
    //   23: aload_2
    //   24: monitorexit
    //   25: aload_3
    //   26: athrow
    //   27: astore_2
    //   28: aload_0
    //   29: getfield 15	com/sun/mail/pop3/POP3Message:folder	Lcom/sun/mail/pop3/POP3Folder;
    //   32: iconst_0
    //   33: invokevirtual 28	com/sun/mail/pop3/POP3Folder:close	(Z)V
    //   36: new 29	javax/mail/FolderClosedException
    //   39: dup
    //   40: aload_0
    //   41: getfield 15	com/sun/mail/pop3/POP3Message:folder	Lcom/sun/mail/pop3/POP3Folder;
    //   44: aload_2
    //   45: invokevirtual 30	java/io/EOFException:toString	()Ljava/lang/String;
    //   48: invokespecial 31	javax/mail/FolderClosedException:<init>	(Ljavax/mail/Folder;Ljava/lang/String;)V
    //   51: athrow
    //   52: astore_2
    //   53: new 33	javax/mail/MessagingException
    //   56: dup
    //   57: ldc 34
    //   59: aload_2
    //   60: invokespecial 35	javax/mail/MessagingException:<init>	(Ljava/lang/String;Ljava/lang/Exception;)V
    //   63: athrow
    // Line number table:
    //   Java source line #340	-> byte code offset #0
    //   Java source line #341	-> byte code offset #4
    //   Java source line #342	-> byte code offset #22
    //   Java source line #343	-> byte code offset #27
    //   Java source line #344	-> byte code offset #28
    //   Java source line #345	-> byte code offset #36
    //   Java source line #346	-> byte code offset #52
    //   Java source line #347	-> byte code offset #53
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	64	0	this	POP3Message
    //   0	64	1	n	int
    //   2	22	2	Ljava/lang/Object;	Object
    //   27	18	2	eex	EOFException
    //   52	8	2	ex	IOException
    //   22	4	3	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   4	21	22	finally
    //   22	25	22	finally
    //   0	21	27	java/io/EOFException
    //   22	27	27	java/io/EOFException
    //   0	21	52	java/io/IOException
    //   22	27	52	java/io/IOException
  }
  
  public String[] getHeader(String name)
    throws MessagingException
  {
    if (this.headers == null) {
      loadHeaders();
    }
    return this.headers.getHeader(name);
  }
  
  public String getHeader(String name, String delimiter)
    throws MessagingException
  {
    if (this.headers == null) {
      loadHeaders();
    }
    return this.headers.getHeader(name, delimiter);
  }
  
  public void setHeader(String name, String value)
    throws MessagingException
  {
    throw new IllegalWriteException("POP3 messages are read-only");
  }
  
  public void addHeader(String name, String value)
    throws MessagingException
  {
    throw new IllegalWriteException("POP3 messages are read-only");
  }
  
  public void removeHeader(String name)
    throws MessagingException
  {
    throw new IllegalWriteException("POP3 messages are read-only");
  }
  
  public Enumeration getAllHeaders()
    throws MessagingException
  {
    if (this.headers == null) {
      loadHeaders();
    }
    return this.headers.getAllHeaders();
  }
  
  public Enumeration getMatchingHeaders(String[] names)
    throws MessagingException
  {
    if (this.headers == null) {
      loadHeaders();
    }
    return this.headers.getMatchingHeaders(names);
  }
  
  public Enumeration getNonMatchingHeaders(String[] names)
    throws MessagingException
  {
    if (this.headers == null) {
      loadHeaders();
    }
    return this.headers.getNonMatchingHeaders(names);
  }
  
  public void addHeaderLine(String line)
    throws MessagingException
  {
    throw new IllegalWriteException("POP3 messages are read-only");
  }
  
  public Enumeration getAllHeaderLines()
    throws MessagingException
  {
    if (this.headers == null) {
      loadHeaders();
    }
    return this.headers.getAllHeaderLines();
  }
  
  public Enumeration getMatchingHeaderLines(String[] names)
    throws MessagingException
  {
    if (this.headers == null) {
      loadHeaders();
    }
    return this.headers.getMatchingHeaderLines(names);
  }
  
  public Enumeration getNonMatchingHeaderLines(String[] names)
    throws MessagingException
  {
    if (this.headers == null) {
      loadHeaders();
    }
    return this.headers.getNonMatchingHeaderLines(names);
  }
  
  public void saveChanges()
    throws MessagingException
  {
    throw new IllegalWriteException("POP3 messages are read-only");
  }
  
  public synchronized void writeTo(OutputStream os, String[] ignoreList)
    throws IOException, MessagingException
  {
    InputStream rawcontent = (InputStream)this.rawData.get();
    if ((rawcontent == null) && (ignoreList == null) && (!((POP3Store)this.folder.getStore()).cacheWriteTo))
    {
      Session s = ((POP3Store)this.folder.getStore()).getSession();
      if (this.folder.logger.isLoggable(Level.FINE)) {
        this.folder.logger.fine("streaming msg " + this.msgnum);
      }
      if (!this.folder.getProtocol().retr(this.msgnum, os))
      {
        this.expunged = true;
        throw new MessageRemovedException("can't retrieve message #" + this.msgnum + " in POP3Message.writeTo");
      }
    }
    else if ((rawcontent != null) && (ignoreList == null))
    {
      InputStream in = ((SharedInputStream)rawcontent).newStream(0L, -1L);
      try
      {
        byte[] buf = new byte['䀀'];
        int len;
        while ((len = in.read(buf)) > 0) {
          os.write(buf, 0, len);
        }
      }
      finally
      {
        try
        {
          if (in != null) {
            in.close();
          }
        }
        catch (IOException ex) {}
      }
    }
    else
    {
      super.writeTo(os, ignoreList);
    }
  }
  
  private void loadHeaders()
    throws MessagingException
  {
    assert (!Thread.holdsLock(this));
    try
    {
      boolean fetchContent = false;
      synchronized (this)
      {
        if (this.headers != null) {
          return;
        }
        InputStream hdrs = null;
        if ((((POP3Store)this.folder.getStore()).disableTop) || ((hdrs = this.folder.getProtocol().top(this.msgnum, 0)) == null)) {
          fetchContent = true;
        } else {
          try
          {
            this.hdrSize = hdrs.available();
            this.headers = new InternetHeaders(hdrs);
          }
          finally
          {
            hdrs.close();
          }
        }
      }
      if (fetchContent)
      {
        InputStream cs = null;
        try
        {
          cs = getContentStream();
        }
        finally
        {
          if (cs != null) {
            cs.close();
          }
        }
      }
    }
    catch (EOFException eex)
    {
      this.folder.close(false);
      throw new FolderClosedException(this.folder, eex.toString());
    }
    catch (IOException ex)
    {
      throw new MessagingException("error loading POP3 headers", ex);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\pop3\POP3Message.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */