package javax.mail.search;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;

public final class RecipientStringTerm
  extends AddressStringTerm
{
  private Message.RecipientType type;
  private static final long serialVersionUID = -8293562089611618849L;
  
  public RecipientStringTerm(Message.RecipientType type, String pattern)
  {
    super(pattern);
    this.type = type;
  }
  
  public Message.RecipientType getRecipientType()
  {
    return this.type;
  }
  
  public boolean match(Message msg)
  {
    Address[] recipients;
    try
    {
      recipients = msg.getRecipients(this.type);
    }
    catch (Exception e)
    {
      return false;
    }
    if (recipients == null) {
      return false;
    }
    for (int i = 0; i < recipients.length; i++) {
      if (super.match(recipients[i])) {
        return true;
      }
    }
    return false;
  }
  
  public boolean equals(Object obj)
  {
    if (!(obj instanceof RecipientStringTerm)) {
      return false;
    }
    RecipientStringTerm rst = (RecipientStringTerm)obj;
    return (rst.type.equals(this.type)) && (super.equals(obj));
  }
  
  public int hashCode()
  {
    return this.type.hashCode() + super.hashCode();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\search\RecipientStringTerm.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */