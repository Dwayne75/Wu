package org.apache.http;

public class TruncatedChunkException
  extends MalformedChunkCodingException
{
  private static final long serialVersionUID = -23506263930279460L;
  
  public TruncatedChunkException(String message)
  {
    super(message);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\TruncatedChunkException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */