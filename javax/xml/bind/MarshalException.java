package javax.xml.bind;

public class MarshalException
  extends JAXBException
{
  public MarshalException(String message)
  {
    this(message, null, null);
  }
  
  public MarshalException(String message, String errorCode)
  {
    this(message, errorCode, null);
  }
  
  public MarshalException(Throwable exception)
  {
    this(null, null, exception);
  }
  
  public MarshalException(String message, Throwable exception)
  {
    this(message, null, exception);
  }
  
  public MarshalException(String message, String errorCode, Throwable exception)
  {
    super(message, errorCode, exception);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\MarshalException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */