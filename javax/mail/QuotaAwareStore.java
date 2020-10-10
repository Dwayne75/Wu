package javax.mail;

public abstract interface QuotaAwareStore
{
  public abstract Quota[] getQuota(String paramString)
    throws MessagingException;
  
  public abstract void setQuota(Quota paramQuota)
    throws MessagingException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\QuotaAwareStore.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */