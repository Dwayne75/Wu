package javax.mail;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;

public abstract class Transport
  extends Service
{
  public Transport(Session session, URLName urlname)
  {
    super(session, urlname);
  }
  
  public static void send(Message msg)
    throws MessagingException
  {
    msg.saveChanges();
    send0(msg, msg.getAllRecipients());
  }
  
  public static void send(Message msg, Address[] addresses)
    throws MessagingException
  {
    msg.saveChanges();
    send0(msg, addresses);
  }
  
  private static void send0(Message msg, Address[] addresses)
    throws MessagingException
  {
    if ((addresses == null) || (addresses.length == 0)) {
      throw new SendFailedException("No recipient addresses");
    }
    Hashtable protocols = new Hashtable();
    
    Vector invalid = new Vector();
    Vector validSent = new Vector();
    Vector validUnsent = new Vector();
    for (int i = 0; i < addresses.length; i++) {
      if (protocols.containsKey(addresses[i].getType()))
      {
        Vector v = (Vector)protocols.get(addresses[i].getType());
        v.addElement(addresses[i]);
      }
      else
      {
        Vector w = new Vector();
        w.addElement(addresses[i]);
        protocols.put(addresses[i].getType(), w);
      }
    }
    int dsize = protocols.size();
    if (dsize == 0) {
      throw new SendFailedException("No recipient addresses");
    }
    Session s = msg.session != null ? msg.session : Session.getDefaultInstance(System.getProperties(), null);
    if (dsize == 1)
    {
      Transport transport = s.getTransport(addresses[0]);
      try
      {
        transport.connect();
        transport.sendMessage(msg, addresses);
      }
      finally
      {
        transport.close();
      }
      return;
    }
    Object chainedEx = null;
    boolean sendFailed = false;
    
    Enumeration e = protocols.elements();
    while (e.hasMoreElements())
    {
      Vector v = (Vector)e.nextElement();
      Address[] protaddresses = new Address[v.size()];
      v.copyInto(protaddresses);
      Transport transport;
      if ((transport = s.getTransport(protaddresses[0])) == null) {
        for (int j = 0; j < protaddresses.length; j++) {
          invalid.addElement(protaddresses[j]);
        }
      } else {
        try
        {
          transport.connect();
          transport.sendMessage(msg, protaddresses);
        }
        catch (SendFailedException sex)
        {
          sendFailed = true;
          if (chainedEx == null) {
            chainedEx = sex;
          } else {
            ((MessagingException)chainedEx).setNextException(sex);
          }
          Address[] a = sex.getInvalidAddresses();
          if (a != null) {
            for (int j = 0; j < a.length; j++) {
              invalid.addElement(a[j]);
            }
          }
          a = sex.getValidSentAddresses();
          if (a != null) {
            for (int k = 0; k < a.length; k++) {
              validSent.addElement(a[k]);
            }
          }
          Address[] c = sex.getValidUnsentAddresses();
          if (c != null) {
            for (int l = 0; l < c.length; l++) {
              validUnsent.addElement(c[l]);
            }
          }
        }
        catch (MessagingException mex)
        {
          sendFailed = true;
          if (chainedEx == null) {
            chainedEx = mex;
          } else {
            ((MessagingException)chainedEx).setNextException(mex);
          }
        }
        finally
        {
          transport.close();
        }
      }
    }
    if ((sendFailed) || (invalid.size() != 0) || (validUnsent.size() != 0))
    {
      Address[] a = null;Address[] b = null;Address[] c = null;
      if (validSent.size() > 0)
      {
        a = new Address[validSent.size()];
        validSent.copyInto(a);
      }
      if (validUnsent.size() > 0)
      {
        b = new Address[validUnsent.size()];
        validUnsent.copyInto(b);
      }
      if (invalid.size() > 0)
      {
        c = new Address[invalid.size()];
        invalid.copyInto(c);
      }
      throw new SendFailedException("Sending failed", (Exception)chainedEx, a, b, c);
    }
  }
  
  private volatile Vector transportListeners = null;
  
  public abstract void sendMessage(Message paramMessage, Address[] paramArrayOfAddress)
    throws MessagingException;
  
  public synchronized void addTransportListener(TransportListener l)
  {
    if (this.transportListeners == null) {
      this.transportListeners = new Vector();
    }
    this.transportListeners.addElement(l);
  }
  
  public synchronized void removeTransportListener(TransportListener l)
  {
    if (this.transportListeners != null) {
      this.transportListeners.removeElement(l);
    }
  }
  
  protected void notifyTransportListeners(int type, Address[] validSent, Address[] validUnsent, Address[] invalid, Message msg)
  {
    if (this.transportListeners == null) {
      return;
    }
    TransportEvent e = new TransportEvent(this, type, validSent, validUnsent, invalid, msg);
    
    queueEvent(e, this.transportListeners);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\Transport.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */