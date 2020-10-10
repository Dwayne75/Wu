package javax.mail.search;

import javax.mail.Message;

public final class SubjectTerm
  extends StringTerm
{
  private static final long serialVersionUID = 7481568618055573432L;
  
  public SubjectTerm(String pattern)
  {
    super(pattern);
  }
  
  public boolean match(Message msg)
  {
    String subj;
    try
    {
      subj = msg.getSubject();
    }
    catch (Exception e)
    {
      return false;
    }
    if (subj == null) {
      return false;
    }
    return super.match(subj);
  }
  
  public boolean equals(Object obj)
  {
    if (!(obj instanceof SubjectTerm)) {
      return false;
    }
    return super.equals(obj);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\search\SubjectTerm.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */