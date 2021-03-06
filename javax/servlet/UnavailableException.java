package javax.servlet;

public class UnavailableException
  extends ServletException
{
  private Servlet servlet;
  private boolean permanent;
  private int seconds;
  
  /**
   * @deprecated
   */
  public UnavailableException(Servlet servlet, String msg)
  {
    super(msg);
    this.servlet = servlet;
    this.permanent = true;
  }
  
  /**
   * @deprecated
   */
  public UnavailableException(int seconds, Servlet servlet, String msg)
  {
    super(msg);
    this.servlet = servlet;
    if (seconds <= 0) {
      this.seconds = -1;
    } else {
      this.seconds = seconds;
    }
    this.permanent = false;
  }
  
  public UnavailableException(String msg)
  {
    super(msg);
    
    this.permanent = true;
  }
  
  public UnavailableException(String msg, int seconds)
  {
    super(msg);
    if (seconds <= 0) {
      this.seconds = -1;
    } else {
      this.seconds = seconds;
    }
    this.permanent = false;
  }
  
  public boolean isPermanent()
  {
    return this.permanent;
  }
  
  /**
   * @deprecated
   */
  public Servlet getServlet()
  {
    return this.servlet;
  }
  
  public int getUnavailableSeconds()
  {
    return this.permanent ? -1 : this.seconds;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\UnavailableException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */