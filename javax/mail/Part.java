package javax.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.activation.DataHandler;

public abstract interface Part
{
  public static final String ATTACHMENT = "attachment";
  public static final String INLINE = "inline";
  
  public abstract int getSize()
    throws MessagingException;
  
  public abstract int getLineCount()
    throws MessagingException;
  
  public abstract String getContentType()
    throws MessagingException;
  
  public abstract boolean isMimeType(String paramString)
    throws MessagingException;
  
  public abstract String getDisposition()
    throws MessagingException;
  
  public abstract void setDisposition(String paramString)
    throws MessagingException;
  
  public abstract String getDescription()
    throws MessagingException;
  
  public abstract void setDescription(String paramString)
    throws MessagingException;
  
  public abstract String getFileName()
    throws MessagingException;
  
  public abstract void setFileName(String paramString)
    throws MessagingException;
  
  public abstract InputStream getInputStream()
    throws IOException, MessagingException;
  
  public abstract DataHandler getDataHandler()
    throws MessagingException;
  
  public abstract Object getContent()
    throws IOException, MessagingException;
  
  public abstract void setDataHandler(DataHandler paramDataHandler)
    throws MessagingException;
  
  public abstract void setContent(Object paramObject, String paramString)
    throws MessagingException;
  
  public abstract void setText(String paramString)
    throws MessagingException;
  
  public abstract void setContent(Multipart paramMultipart)
    throws MessagingException;
  
  public abstract void writeTo(OutputStream paramOutputStream)
    throws IOException, MessagingException;
  
  public abstract String[] getHeader(String paramString)
    throws MessagingException;
  
  public abstract void setHeader(String paramString1, String paramString2)
    throws MessagingException;
  
  public abstract void addHeader(String paramString1, String paramString2)
    throws MessagingException;
  
  public abstract void removeHeader(String paramString)
    throws MessagingException;
  
  public abstract Enumeration getAllHeaders()
    throws MessagingException;
  
  public abstract Enumeration getMatchingHeaders(String[] paramArrayOfString)
    throws MessagingException;
  
  public abstract Enumeration getNonMatchingHeaders(String[] paramArrayOfString)
    throws MessagingException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\Part.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */