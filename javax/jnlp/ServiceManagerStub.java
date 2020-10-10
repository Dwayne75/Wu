package javax.jnlp;

public abstract interface ServiceManagerStub
{
  public abstract Object lookup(String paramString)
    throws UnavailableServiceException;
  
  public abstract String[] getServiceNames();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\jnlp\ServiceManagerStub.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */