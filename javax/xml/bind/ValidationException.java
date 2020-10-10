package javax.xml.bind;

public class ValidationException
  extends JAXBException
{
  public ValidationException(String message)
  {
    this(message, null, null);
  }
  
  public ValidationException(String message, String errorCode)
  {
    this(message, errorCode, null);
  }
  
  public ValidationException(Throwable exception)
  {
    this(null, null, exception);
  }
  
  public ValidationException(String message, Throwable exception)
  {
    this(message, null, exception);
  }
  
  public ValidationException(String message, String errorCode, Throwable exception)
  {
    super(message, errorCode, exception);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\ValidationException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */