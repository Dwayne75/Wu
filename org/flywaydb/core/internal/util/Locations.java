package org.flywaydb.core.internal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class Locations
{
  private static final Log LOG = LogFactory.getLog(Locations.class);
  private final List<Location> locations = new ArrayList();
  
  public Locations(String... rawLocations)
  {
    List<Location> normalizedLocations = new ArrayList();
    for (String rawLocation : rawLocations) {
      normalizedLocations.add(new Location(rawLocation));
    }
    Collections.sort(normalizedLocations);
    for (??? = normalizedLocations.iterator(); ((Iterator)???).hasNext();)
    {
      Location normalizedLocation = (Location)((Iterator)???).next();
      if (this.locations.contains(normalizedLocation))
      {
        LOG.warn("Discarding duplicate location '" + normalizedLocation + "'");
      }
      else
      {
        Location parentLocation = getParentLocationIfExists(normalizedLocation, this.locations);
        if (parentLocation != null) {
          LOG.warn("Discarding location '" + normalizedLocation + "' as it is a sublocation of '" + parentLocation + "'");
        } else {
          this.locations.add(normalizedLocation);
        }
      }
    }
  }
  
  public List<Location> getLocations()
  {
    return this.locations;
  }
  
  private Location getParentLocationIfExists(Location location, List<Location> finalLocations)
  {
    for (Location finalLocation : finalLocations) {
      if (finalLocation.isParentOf(location)) {
        return finalLocation;
      }
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\Locations.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */