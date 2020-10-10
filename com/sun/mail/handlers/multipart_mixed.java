package com.sun.mail.handlers;

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.OutputStream;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

public class multipart_mixed
  implements DataContentHandler
{
  private ActivationDataFlavor myDF = new ActivationDataFlavor(MimeMultipart.class, "multipart/mixed", "Multipart");
  
  public DataFlavor[] getTransferDataFlavors()
  {
    return new DataFlavor[] { this.myDF };
  }
  
  public Object getTransferData(DataFlavor df, DataSource ds)
    throws IOException
  {
    if (this.myDF.equals(df)) {
      return getContent(ds);
    }
    return null;
  }
  
  public Object getContent(DataSource ds)
    throws IOException
  {
    try
    {
      return new MimeMultipart(ds);
    }
    catch (MessagingException e)
    {
      IOException ioex = new IOException("Exception while constructing MimeMultipart");
      
      ioex.initCause(e);
      throw ioex;
    }
  }
  
  public void writeTo(Object obj, String mimeType, OutputStream os)
    throws IOException
  {
    if ((obj instanceof MimeMultipart)) {
      try
      {
        ((MimeMultipart)obj).writeTo(os);
      }
      catch (MessagingException e)
      {
        throw new IOException(e.toString());
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\handlers\multipart_mixed.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */