package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.om.Location;

final class LocationHost
  implements Location
{
  final Location lhs;
  final Location rhs;
  
  LocationHost(Location lhs, Location rhs)
  {
    this.lhs = lhs;
    this.rhs = rhs;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\host\LocationHost.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */