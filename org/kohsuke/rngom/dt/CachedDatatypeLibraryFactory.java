package org.kohsuke.rngom.dt;

import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;

public class CachedDatatypeLibraryFactory
  implements DatatypeLibraryFactory
{
  private String lastUri;
  private DatatypeLibrary lastLib;
  private final DatatypeLibraryFactory core;
  
  public CachedDatatypeLibraryFactory(DatatypeLibraryFactory core)
  {
    this.core = core;
  }
  
  public DatatypeLibrary createDatatypeLibrary(String namespaceURI)
  {
    if (this.lastUri == namespaceURI) {
      return this.lastLib;
    }
    this.lastUri = namespaceURI;
    this.lastLib = this.core.createDatatypeLibrary(namespaceURI);
    return this.lastLib;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\dt\CachedDatatypeLibraryFactory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */