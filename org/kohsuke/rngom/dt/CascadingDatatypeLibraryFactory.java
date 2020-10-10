package org.kohsuke.rngom.dt;

import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;

public class CascadingDatatypeLibraryFactory
  implements DatatypeLibraryFactory
{
  private final DatatypeLibraryFactory factory1;
  private final DatatypeLibraryFactory factory2;
  
  public CascadingDatatypeLibraryFactory(DatatypeLibraryFactory factory1, DatatypeLibraryFactory factory2)
  {
    this.factory1 = factory1;
    this.factory2 = factory2;
  }
  
  public DatatypeLibrary createDatatypeLibrary(String namespaceURI)
  {
    DatatypeLibrary lib = this.factory1.createDatatypeLibrary(namespaceURI);
    if (lib == null) {
      lib = this.factory2.createDatatypeLibrary(namespaceURI);
    }
    return lib;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\dt\CascadingDatatypeLibraryFactory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */