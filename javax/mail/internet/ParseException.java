package javax.mail.internet;

import javax.mail.MessagingException;

public class ParseException
  extends MessagingException
{
  private static final long serialVersionUID = 7649991205183658089L;
  
  public ParseException() {}
  
  public ParseException(String s)
  {
    super(s);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\internet\ParseException.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */