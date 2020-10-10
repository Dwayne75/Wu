package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.Location;

public class MethodLocatable<M>
  implements Locatable
{
  private final Locatable upstream;
  private final M method;
  private final Navigator<?, ?, ?, M> nav;
  
  public MethodLocatable(Locatable upstream, M method, Navigator<?, ?, ?, M> nav)
  {
    this.upstream = upstream;
    this.method = method;
    this.nav = nav;
  }
  
  public Locatable getUpstream()
  {
    return this.upstream;
  }
  
  public Location getLocation()
  {
    return this.nav.getMethodLocation(this.method);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\annotation\MethodLocatable.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */