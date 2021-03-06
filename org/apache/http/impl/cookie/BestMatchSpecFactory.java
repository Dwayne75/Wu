package org.apache.http.impl.cookie;

import java.util.Collection;
import org.apache.http.annotation.Immutable;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.params.HttpParams;

@Immutable
public class BestMatchSpecFactory
  implements CookieSpecFactory
{
  public CookieSpec newInstance(HttpParams params)
  {
    if (params != null)
    {
      String[] patterns = null;
      Collection<?> param = (Collection)params.getParameter("http.protocol.cookie-datepatterns");
      if (param != null)
      {
        patterns = new String[param.size()];
        patterns = (String[])param.toArray(patterns);
      }
      boolean singleHeader = params.getBooleanParameter("http.protocol.single-cookie-header", false);
      
      return new BestMatchSpec(patterns, singleHeader);
    }
    return new BestMatchSpec();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\cookie\BestMatchSpecFactory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */