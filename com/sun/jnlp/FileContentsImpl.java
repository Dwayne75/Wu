package com.sun.jnlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.jnlp.FileContents;
import javax.jnlp.JNLPRandomAccessFile;

public final class FileContentsImpl
  implements FileContents
{
  private String _name = null;
  private File _file = null;
  private long _limit = Long.MAX_VALUE;
  private URL _url = null;
  private JNLPRandomAccessFile _raf = null;
  private PersistenceServiceImpl _psCallback = null;
  
  FileContentsImpl(File paramFile, long paramLong)
    throws IOException
  {
    this._file = paramFile;
    this._limit = paramLong;
    this._name = this._file.getName();
  }
  
  FileContentsImpl(File paramFile, PersistenceServiceImpl paramPersistenceServiceImpl, URL paramURL, long paramLong)
  {
    this._file = paramFile;
    this._url = paramURL;
    this._psCallback = paramPersistenceServiceImpl;
    this._limit = paramLong;
    
    int i = paramURL.getFile().lastIndexOf('/');
    this._name = (i != -1 ? paramURL.getFile().substring(i + 1) : paramURL.getFile());
  }
  
  public String getName()
  {
    return this._name;
  }
  
  public long getLength()
  {
    Long localLong = (Long)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return new Long(FileContentsImpl.this._file.length());
      }
    });
    return localLong.longValue();
  }
  
  public InputStream getInputStream()
    throws IOException
  {
    try
    {
      (InputStream)AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        public Object run()
          throws IOException
        {
          return new FileInputStream(FileContentsImpl.this._file);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw rethrowException(localPrivilegedActionException);
    }
  }
  
  public OutputStream getOutputStream(boolean paramBoolean)
    throws IOException
  {
    try
    {
      (OutputStream)AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final boolean val$append;
        
        public Object run()
          throws IOException
        {
          return new MeteredFileOutputStream(FileContentsImpl.this._file, !this.val$append, FileContentsImpl.this);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw rethrowException(localPrivilegedActionException);
    }
  }
  
  public boolean canRead()
    throws IOException
  {
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return new Boolean(FileContentsImpl.this._file.canRead());
      }
    });
    return localBoolean.booleanValue();
  }
  
  public boolean canWrite()
    throws IOException
  {
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return new Boolean(FileContentsImpl.this._file.canWrite());
      }
    });
    return localBoolean.booleanValue();
  }
  
  public JNLPRandomAccessFile getRandomAccessFile(String paramString)
    throws IOException
  {
    try
    {
      (JNLPRandomAccessFile)AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        private final String val$mode;
        
        public Object run()
          throws MalformedURLException, IOException
        {
          return new JNLPRandomAccessFileImpl(FileContentsImpl.this._file, this.val$mode, FileContentsImpl.this);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw rethrowException(localPrivilegedActionException);
    }
  }
  
  public long getMaxLength()
    throws IOException
  {
    return this._limit;
  }
  
  public long setMaxLength(long paramLong)
    throws IOException
  {
    if (this._psCallback != null)
    {
      this._limit = this._psCallback.setMaxLength(this._url, paramLong);
      return this._limit;
    }
    this._limit = paramLong;
    return this._limit;
  }
  
  private IOException rethrowException(PrivilegedActionException paramPrivilegedActionException)
    throws IOException
  {
    Exception localException = paramPrivilegedActionException.getException();
    if ((localException instanceof IOException)) {
      throw ((IOException)localException);
    }
    if ((localException instanceof RuntimeException)) {
      throw ((RuntimeException)localException);
    }
    throw new IOException(localException.getMessage());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\jnlp\FileContentsImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */