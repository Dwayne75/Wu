package com.sun.mail.handlers;

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessageAware;
import javax.mail.MessageContext;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class message_rfc822
  implements DataContentHandler
{
  ActivationDataFlavor ourDataFlavor = new ActivationDataFlavor(Message.class, "message/rfc822", "Message");
  
  public DataFlavor[] getTransferDataFlavors()
  {
    return new DataFlavor[] { this.ourDataFlavor };
  }
  
  public Object getTransferData(DataFlavor df, DataSource ds)
    throws IOException
  {
    if (this.ourDataFlavor.equals(df)) {
      return getContent(ds);
    }
    return null;
  }
  
  public Object getContent(DataSource ds)
    throws IOException
  {
    try
    {
      Session session;
      Session session;
      if ((ds instanceof MessageAware))
      {
        MessageContext mc = ((MessageAware)ds).getMessageContext();
        session = mc.getSession();
      }
      else
      {
        session = Session.getDefaultInstance(new Properties(), null);
      }
      return new MimeMessage(session, ds.getInputStream());
    }
    catch (MessagingException me)
    {
      throw new IOException("Exception creating MimeMessage in message/rfc822 DataContentHandler: " + me.toString());
    }
  }
  
  public void writeTo(Object obj, String mimeType, OutputStream os)
    throws IOException
  {
    if ((obj instanceof Message))
    {
      Message m = (Message)obj;
      try
      {
        m.writeTo(os);
      }
      catch (MessagingException me)
      {
        throw new IOException(me.toString());
      }
    }
    else
    {
      throw new IOException("unsupported object");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\handlers\message_rfc822.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */