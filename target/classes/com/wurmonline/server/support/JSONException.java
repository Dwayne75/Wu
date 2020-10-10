package com.wurmonline.server.support;

public class JSONException
  extends RuntimeException
{
  private static final long serialVersionUID = 0L;
  
  public JSONException(String message)
  {
    super(message);
  }
  
  public JSONException(Throwable cause)
  {
    super(cause.getMessage(), cause);
  }
  
  public JSONException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\support\JSONException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */