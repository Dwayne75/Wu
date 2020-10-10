package javax.mail;

public class StoreClosedException
  extends MessagingException
{
  private transient Store store;
  private static final long serialVersionUID = -3145392336120082655L;
  
  public StoreClosedException(Store store)
  {
    this(store, null);
  }
  
  public StoreClosedException(Store store, String message)
  {
    super(message);
    this.store = store;
  }
  
  public Store getStore()
  {
    return this.store;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\StoreClosedException.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */