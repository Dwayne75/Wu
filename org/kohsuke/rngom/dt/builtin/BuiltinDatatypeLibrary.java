package org.kohsuke.rngom.dt.builtin;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeBuilder;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;

public class BuiltinDatatypeLibrary
  implements DatatypeLibrary
{
  private final DatatypeLibraryFactory factory;
  private DatatypeLibrary xsdDatatypeLibrary = null;
  
  BuiltinDatatypeLibrary(DatatypeLibraryFactory factory)
  {
    this.factory = factory;
  }
  
  public DatatypeBuilder createDatatypeBuilder(String type)
    throws DatatypeException
  {
    this.xsdDatatypeLibrary = this.factory.createDatatypeLibrary("http://www.w3.org/2001/XMLSchema-datatypes");
    if (this.xsdDatatypeLibrary == null) {
      throw new DatatypeException();
    }
    if ((type.equals("string")) || (type.equals("token"))) {
      return new BuiltinDatatypeBuilder(this.xsdDatatypeLibrary.createDatatype(type));
    }
    throw new DatatypeException();
  }
  
  public Datatype createDatatype(String type)
    throws DatatypeException
  {
    return createDatatypeBuilder(type).createDatatype();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\dt\builtin\BuiltinDatatypeLibrary.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */