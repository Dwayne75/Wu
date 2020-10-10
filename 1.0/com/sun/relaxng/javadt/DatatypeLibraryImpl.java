package com.sun.relaxng.javadt;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeBuilder;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.relaxng.datatype.helpers.ParameterlessDatatypeBuilder;

public class DatatypeLibraryImpl
  implements DatatypeLibrary, DatatypeLibraryFactory
{
  public static final String NAMESPACE_URI = "http://java.sun.com/xml/ns/relaxng/java-datatypes";
  
  public DatatypeBuilder createDatatypeBuilder(String name)
    throws DatatypeException
  {
    return new ParameterlessDatatypeBuilder(createDatatype(name));
  }
  
  public Datatype createDatatype(String name)
    throws DatatypeException
  {
    if ("identifier".equals(name)) {
      return JavaIdentifierDatatype.theInstance;
    }
    if ("package".equals(name)) {
      return JavaPackageDatatype.theInstance;
    }
    throw new DatatypeException();
  }
  
  public DatatypeLibrary createDatatypeLibrary(String namespaceUri)
  {
    if ("http://java.sun.com/xml/ns/relaxng/java-datatypes".equals(namespaceUri)) {
      return this;
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\relaxng\javadt\DatatypeLibraryImpl.class
 * Java compiler version: 3 (47.0)
 * JD-Core Version:       0.7.1
 */