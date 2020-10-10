package javax.xml.bind;

import java.io.PrintStream;
import java.io.PrintWriter;

public class JAXBException
  extends Exception
{
  private String errorCode;
  private Throwable linkedException;
  static final long serialVersionUID = -5621384651494307979L;
  
  public JAXBException(String message)
  {
    this(message, null, null);
  }
  
  public JAXBException(String message, String errorCode)
  {
    this(message, errorCode, null);
  }
  
  public JAXBException(Throwable exception)
  {
    this(null, null, exception);
  }
  
  public JAXBException(String message, Throwable exception)
  {
    this(message, null, exception);
  }
  
  public JAXBException(String message, String errorCode, Throwable exception)
  {
    super(message);
    this.errorCode = errorCode;
    this.linkedException = exception;
  }
  
  public String getErrorCode()
  {
    return this.errorCode;
  }
  
  public Throwable getLinkedException()
  {
    return this.linkedException;
  }
  
  public synchronized void setLinkedException(Throwable exception)
  {
    this.linkedException = exception;
  }
  
  public String toString()
  {
    return super.toString() + "\n - with linked exception:\n[" + this.linkedException.toString() + "]";
  }
  
  public void printStackTrace(PrintStream s)
  {
    super.printStackTrace(s);
  }
  
  public void printStackTrace()
  {
    super.printStackTrace();
  }
  
  public void printStackTrace(PrintWriter s)
  {
    super.printStackTrace(s);
  }
  
  public Throwable getCause()
  {
    return this.linkedException;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\JAXBException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */