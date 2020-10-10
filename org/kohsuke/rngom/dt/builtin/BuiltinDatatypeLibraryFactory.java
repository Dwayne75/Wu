package org.kohsuke.rngom.dt.builtin;

import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;

public class BuiltinDatatypeLibraryFactory
  implements DatatypeLibraryFactory
{
  private final DatatypeLibrary builtinDatatypeLibrary;
  private final DatatypeLibrary compatibilityDatatypeLibrary;
  private final DatatypeLibraryFactory core;
  
  public BuiltinDatatypeLibraryFactory(DatatypeLibraryFactory coreFactory)
  {
    this.builtinDatatypeLibrary = new BuiltinDatatypeLibrary(coreFactory);
    this.compatibilityDatatypeLibrary = new CompatibilityDatatypeLibrary(coreFactory);
    this.core = coreFactory;
  }
  
  public DatatypeLibrary createDatatypeLibrary(String uri)
  {
    if (uri.equals("")) {
      return this.builtinDatatypeLibrary;
    }
    if (uri.equals("http://relaxng.org/ns/compatibility/datatypes/1.0")) {
      return this.compatibilityDatatypeLibrary;
    }
    return this.core.createDatatypeLibrary(uri);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\dt\builtin\BuiltinDatatypeLibraryFactory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */