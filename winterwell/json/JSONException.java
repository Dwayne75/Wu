package winterwell.json;

public class JSONException
  extends RuntimeException
{
  private Throwable cause;
  
  public JSONException(String message)
  {
    super(message);
  }
  
  public JSONException(Throwable t)
  {
    super(t.getMessage());
    this.cause = t;
  }
  
  public Throwable getCause()
  {
    return this.cause;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\json\JSONException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */