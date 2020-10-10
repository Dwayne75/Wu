package javax.xml.bind;

import java.io.PrintStream;

public class TypeConstraintException
  extends RuntimeException
{
  private String errorCode;
  private Throwable linkedException;
  
  public TypeConstraintException(String message)
  {
    this(message, null, null);
  }
  
  public TypeConstraintException(String message, String errorCode)
  {
    this(message, errorCode, null);
  }
  
  public TypeConstraintException(Throwable exception)
  {
    this(null, null, exception);
  }
  
  public TypeConstraintException(String message, Throwable exception)
  {
    this(message, null, exception);
  }
  
  public TypeConstraintException(String message, String errorCode, Throwable exception)
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
    if (this.linkedException != null)
    {
      this.linkedException.printStackTrace(s);
      s.println("--------------- linked to ------------------");
    }
    super.printStackTrace(s);
  }
  
  public void printStackTrace()
  {
    printStackTrace(System.err);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\TypeConstraintException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */