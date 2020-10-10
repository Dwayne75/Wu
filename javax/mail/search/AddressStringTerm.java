package javax.mail.search;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public abstract class AddressStringTerm
  extends StringTerm
{
  private static final long serialVersionUID = 3086821234204980368L;
  
  protected AddressStringTerm(String pattern)
  {
    super(pattern, true);
  }
  
  protected boolean match(Address a)
  {
    if ((a instanceof InternetAddress))
    {
      InternetAddress ia = (InternetAddress)a;
      
      return super.match(ia.toUnicodeString());
    }
    return super.match(a.toString());
  }
  
  public boolean equals(Object obj)
  {
    if (!(obj instanceof AddressStringTerm)) {
      return false;
    }
    return super.equals(obj);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\search\AddressStringTerm.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */