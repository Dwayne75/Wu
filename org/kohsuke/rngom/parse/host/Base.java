package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.om.Location;

public class Base
{
  protected AnnotationsHost cast(Annotations ann)
  {
    if (ann == null) {
      return nullAnnotations;
    }
    return (AnnotationsHost)ann;
  }
  
  protected LocationHost cast(Location loc)
  {
    if (loc == null) {
      return nullLocation;
    }
    return (LocationHost)loc;
  }
  
  private static final AnnotationsHost nullAnnotations = new AnnotationsHost(null, null);
  private static final LocationHost nullLocation = new LocationHost(null, null);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\host\Base.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */