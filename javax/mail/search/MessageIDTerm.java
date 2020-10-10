package javax.mail.search;

import javax.mail.Message;

public final class MessageIDTerm
  extends StringTerm
{
  private static final long serialVersionUID = -2121096296454691963L;
  
  public MessageIDTerm(String msgid)
  {
    super(msgid);
  }
  
  public boolean match(Message msg)
  {
    String[] s;
    try
    {
      s = msg.getHeader("Message-ID");
    }
    catch (Exception e)
    {
      return false;
    }
    if (s == null) {
      return false;
    }
    for (int i = 0; i < s.length; i++) {
      if (super.match(s[i])) {
        return true;
      }
    }
    return false;
  }
  
  public boolean equals(Object obj)
  {
    if (!(obj instanceof MessageIDTerm)) {
      return false;
    }
    return super.equals(obj);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\search\MessageIDTerm.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */