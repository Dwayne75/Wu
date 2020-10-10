package org.seamless.util.mail;

public class Email
{
  protected String sender;
  protected String recipient;
  protected String subject;
  protected String plaintext;
  protected String html;
  
  public Email(String sender, String recipient, String subject, String plaintext)
  {
    this(sender, recipient, subject, plaintext, null);
  }
  
  public Email(String sender, String recipient, String subject, String plaintext, String html)
  {
    this.sender = sender;
    this.recipient = recipient;
    this.subject = subject;
    this.plaintext = plaintext;
    this.html = html;
  }
  
  public String getSender()
  {
    return this.sender;
  }
  
  public void setSender(String sender)
  {
    this.sender = sender;
  }
  
  public String getRecipient()
  {
    return this.recipient;
  }
  
  public void setRecipient(String recipient)
  {
    this.recipient = recipient;
  }
  
  public String getSubject()
  {
    return this.subject;
  }
  
  public void setSubject(String subject)
  {
    this.subject = subject;
  }
  
  public String getPlaintext()
  {
    return this.plaintext;
  }
  
  public void setPlaintext(String plaintext)
  {
    this.plaintext = plaintext;
  }
  
  public String getHtml()
  {
    return this.html;
  }
  
  public void setHtml(String html)
  {
    this.html = html;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\mail\Email.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */