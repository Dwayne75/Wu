package javax.mail.search;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Message;

public final class FlagTerm
  extends SearchTerm
{
  protected boolean set;
  protected Flags flags;
  private static final long serialVersionUID = -142991500302030647L;
  
  public FlagTerm(Flags flags, boolean set)
  {
    this.flags = flags;
    this.set = set;
  }
  
  public Flags getFlags()
  {
    return (Flags)this.flags.clone();
  }
  
  public boolean getTestSet()
  {
    return this.set;
  }
  
  public boolean match(Message msg)
  {
    try
    {
      Flags f = msg.getFlags();
      if (this.set)
      {
        if (f.contains(this.flags)) {
          return true;
        }
        return false;
      }
      Flags.Flag[] sf = this.flags.getSystemFlags();
      for (int i = 0; i < sf.length; i++) {
        if (f.contains(sf[i])) {
          return false;
        }
      }
      String[] s = this.flags.getUserFlags();
      for (int i = 0; i < s.length; i++) {
        if (f.contains(s[i])) {
          return false;
        }
      }
      return true;
    }
    catch (Exception e) {}
    return false;
  }
  
  public boolean equals(Object obj)
  {
    if (!(obj instanceof FlagTerm)) {
      return false;
    }
    FlagTerm ft = (FlagTerm)obj;
    return (ft.set == this.set) && (ft.flags.equals(this.flags));
  }
  
  public int hashCode()
  {
    return this.set ? this.flags.hashCode() : this.flags.hashCode() ^ 0xFFFFFFFF;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\search\FlagTerm.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */