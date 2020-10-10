package javax.mail;

public abstract interface UIDFolder
{
  public static final long LASTUID = -1L;
  
  public abstract long getUIDValidity()
    throws MessagingException;
  
  public abstract Message getMessageByUID(long paramLong)
    throws MessagingException;
  
  public abstract Message[] getMessagesByUID(long paramLong1, long paramLong2)
    throws MessagingException;
  
  public abstract Message[] getMessagesByUID(long[] paramArrayOfLong)
    throws MessagingException;
  
  public abstract long getUID(Message paramMessage)
    throws MessagingException;
  
  public static class FetchProfileItem
    extends FetchProfile.Item
  {
    protected FetchProfileItem(String name)
    {
      super();
    }
    
    public static final FetchProfileItem UID = new FetchProfileItem("UID");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\UIDFolder.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */