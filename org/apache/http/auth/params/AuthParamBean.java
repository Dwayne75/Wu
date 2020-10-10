package org.apache.http.auth.params;

import org.apache.http.params.HttpAbstractParamBean;
import org.apache.http.params.HttpParams;

public class AuthParamBean
  extends HttpAbstractParamBean
{
  public AuthParamBean(HttpParams params)
  {
    super(params);
  }
  
  public void setCredentialCharset(String charset)
  {
    AuthParams.setCredentialCharset(this.params, charset);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\auth\params\AuthParamBean.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */