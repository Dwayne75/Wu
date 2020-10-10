package javax.xml.bind;

public abstract interface ValidationEvent
{
  public static final int WARNING = 0;
  public static final int ERROR = 1;
  public static final int FATAL_ERROR = 2;
  
  public abstract int getSeverity();
  
  public abstract String getMessage();
  
  public abstract Throwable getLinkedException();
  
  public abstract ValidationEventLocator getLocator();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\ValidationEvent.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */