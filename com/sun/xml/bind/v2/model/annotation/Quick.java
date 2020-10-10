package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.runtime.Location;
import java.lang.annotation.Annotation;

public abstract class Quick
  implements Annotation, Locatable, Location
{
  private final Locatable upstream;
  
  protected Quick(Locatable upstream)
  {
    this.upstream = upstream;
  }
  
  protected abstract Annotation getAnnotation();
  
  protected abstract Quick newInstance(Locatable paramLocatable, Annotation paramAnnotation);
  
  public final Location getLocation()
  {
    return this;
  }
  
  public final Locatable getUpstream()
  {
    return this.upstream;
  }
  
  public final String toString()
  {
    return getAnnotation().toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\annotation\Quick.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */