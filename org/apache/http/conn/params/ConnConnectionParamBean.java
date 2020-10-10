package org.apache.http.conn.params;

import org.apache.http.params.HttpAbstractParamBean;
import org.apache.http.params.HttpParams;

@Deprecated
public class ConnConnectionParamBean
  extends HttpAbstractParamBean
{
  public ConnConnectionParamBean(HttpParams params)
  {
    super(params);
  }
  
  /**
   * @deprecated
   */
  public void setMaxStatusLineGarbage(int maxStatusLineGarbage)
  {
    this.params.setIntParameter("http.connection.max-status-line-garbage", maxStatusLineGarbage);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\conn\params\ConnConnectionParamBean.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */