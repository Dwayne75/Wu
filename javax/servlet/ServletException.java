package javax.servlet;

public class ServletException
  extends Exception
{
  private Throwable rootCause;
  
  public ServletException() {}
  
  public ServletException(String message)
  {
    super(message);
  }
  
  public ServletException(String message, Throwable rootCause)
  {
    super(message, rootCause);
    this.rootCause = rootCause;
  }
  
  public ServletException(Throwable rootCause)
  {
    super(rootCause);
    this.rootCause = rootCause;
  }
  
  public Throwable getRootCause()
  {
    return this.rootCause;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\ServletException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */