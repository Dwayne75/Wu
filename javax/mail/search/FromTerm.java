package javax.mail.search;

import javax.mail.Address;
import javax.mail.Message;

public final class FromTerm
  extends AddressTerm
{
  private static final long serialVersionUID = 5214730291502658665L;
  
  public FromTerm(Address address)
  {
    super(address);
  }
  
  public boolean match(Message msg)
  {
    Address[] from;
    try
    {
      from = msg.getFrom();
    }
    catch (Exception e)
    {
      return false;
    }
    if (from == null) {
      return false;
    }
    for (int i = 0; i < from.length; i++) {
      if (super.match(from[i])) {
        return true;
      }
    }
    return false;
  }
  
  public boolean equals(Object obj)
  {
    if (!(obj instanceof FromTerm)) {
      return false;
    }
    return super.equals(obj);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\search\FromTerm.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */