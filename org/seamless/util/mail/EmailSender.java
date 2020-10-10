package org.seamless.util.mail;

import java.util.Date;
import java.util.Properties;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailSender
{
  protected final Properties properties = new Properties();
  protected final String host;
  protected final String user;
  protected final String password;
  
  public EmailSender(String host, String user, String password)
  {
    if ((host == null) || (host.length() == 0)) {
      throw new IllegalArgumentException("Host is required");
    }
    this.host = host;
    this.user = user;
    this.password = password;
    
    this.properties.put("mail.smtp.port", "25");
    this.properties.put("mail.smtp.socketFactory.fallback", "false");
    this.properties.put("mail.smtp.quitwait", "false");
    this.properties.put("mail.smtp.host", host);
    this.properties.put("mail.smtp.starttls.enable", "true");
    if ((user != null) && (password != null)) {
      this.properties.put("mail.smtp.auth", "true");
    }
  }
  
  public Properties getProperties()
  {
    return this.properties;
  }
  
  public String getHost()
  {
    return this.host;
  }
  
  public String getUser()
  {
    return this.user;
  }
  
  public String getPassword()
  {
    return this.password;
  }
  
  public void send(Email email)
    throws MessagingException
  {
    Session session = createSession();
    
    MimeMessage msg = new MimeMessage(session);
    
    msg.setFrom(new InternetAddress(email.getSender()));
    
    InternetAddress[] receipients = { new InternetAddress(email.getRecipient()) };
    msg.setRecipients(Message.RecipientType.TO, receipients);
    
    msg.setSubject(email.getSubject());
    
    msg.setSentDate(new Date());
    
    msg.setContent(createContent(email));
    
    Transport transport = createConnectedTransport(session);
    transport.sendMessage(msg, msg.getAllRecipients());
    transport.close();
  }
  
  protected Multipart createContent(Email email)
    throws MessagingException
  {
    MimeBodyPart partOne = new MimeBodyPart();
    partOne.setText(email.getPlaintext());
    
    Multipart mp = new MimeMultipart("alternative");
    mp.addBodyPart(partOne);
    if (email.getHtml() != null)
    {
      MimeBodyPart partTwo = new MimeBodyPart();
      partTwo.setContent(email.getHtml(), "text/html");
      mp.addBodyPart(partTwo);
    }
    return mp;
  }
  
  protected Session createSession()
  {
    return Session.getInstance(this.properties, null);
  }
  
  protected Transport createConnectedTransport(Session session)
    throws MessagingException
  {
    Transport transport = session.getTransport("smtp");
    if ((this.user != null) && (this.password != null)) {
      transport.connect(this.user, this.password);
    } else {
      transport.connect();
    }
    return transport;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\mail\EmailSender.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */