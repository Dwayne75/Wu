package javax.jnlp;

public final class ServiceManager
{
  private static ServiceManagerStub _stub = null;
  
  public static Object lookup(String paramString)
    throws UnavailableServiceException
  {
    if (_stub != null) {
      return _stub.lookup(paramString);
    }
    throw new UnavailableServiceException("uninitialized");
  }
  
  public static String[] getServiceNames()
  {
    if (_stub != null) {
      return _stub.getServiceNames();
    }
    return null;
  }
  
  public static synchronized void setServiceManagerStub(ServiceManagerStub paramServiceManagerStub)
  {
    if (_stub == null) {
      _stub = paramServiceManagerStub;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\jnlp\ServiceManager.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */