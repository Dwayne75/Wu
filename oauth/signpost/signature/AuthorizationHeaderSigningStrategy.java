package oauth.signpost.signature;

import java.util.Iterator;
import java.util.Set;
import oauth.signpost.OAuth;
import oauth.signpost.http.HttpParameters;
import oauth.signpost.http.HttpRequest;

public class AuthorizationHeaderSigningStrategy
  implements SigningStrategy
{
  private static final long serialVersionUID = 1L;
  
  public String writeSignature(String signature, HttpRequest request, HttpParameters requestParameters)
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append("OAuth ");
    if (requestParameters.containsKey("realm"))
    {
      sb.append(requestParameters.getAsHeaderElement("realm"));
      sb.append(", ");
    }
    HttpParameters oauthParams = requestParameters.getOAuthParameters();
    oauthParams.put("oauth_signature", signature, true);
    
    Iterator<String> iter = oauthParams.keySet().iterator();
    while (iter.hasNext())
    {
      String key = (String)iter.next();
      sb.append(oauthParams.getAsHeaderElement(key));
      if (iter.hasNext()) {
        sb.append(", ");
      }
    }
    String header = sb.toString();
    OAuth.debugOut("Auth Header", header);
    request.setHeader("Authorization", header);
    
    return header;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\oauth\signpost\signature\AuthorizationHeaderSigningStrategy.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */