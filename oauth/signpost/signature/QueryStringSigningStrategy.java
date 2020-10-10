package oauth.signpost.signature;

import java.util.Iterator;
import java.util.Set;
import oauth.signpost.OAuth;
import oauth.signpost.http.HttpParameters;
import oauth.signpost.http.HttpRequest;

public class QueryStringSigningStrategy
  implements SigningStrategy
{
  private static final long serialVersionUID = 1L;
  
  public String writeSignature(String signature, HttpRequest request, HttpParameters requestParameters)
  {
    HttpParameters oauthParams = requestParameters.getOAuthParameters();
    oauthParams.put("oauth_signature", signature, true);
    
    Iterator<String> iter = oauthParams.keySet().iterator();
    
    String firstKey = (String)iter.next();
    StringBuilder sb = new StringBuilder(OAuth.addQueryString(request.getRequestUrl(), oauthParams.getAsQueryString(firstKey)));
    while (iter.hasNext())
    {
      sb.append("&");
      String key = (String)iter.next();
      sb.append(oauthParams.getAsQueryString(key));
    }
    String signedUrl = sb.toString();
    
    request.setRequestUrl(signedUrl);
    
    return signedUrl;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\oauth\signpost\signature\QueryStringSigningStrategy.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */