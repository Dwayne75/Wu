package org.kohsuke.rngom.ast.util;

import org.kohsuke.rngom.ast.om.Location;
import org.xml.sax.Locator;

public class LocatorImpl
  implements Locator, Location
{
  private final String systemId;
  private final int lineNumber;
  private final int columnNumber;
  
  public LocatorImpl(String systemId, int lineNumber, int columnNumber)
  {
    this.systemId = systemId;
    this.lineNumber = lineNumber;
    this.columnNumber = columnNumber;
  }
  
  public String getPublicId()
  {
    return null;
  }
  
  public String getSystemId()
  {
    return this.systemId;
  }
  
  public int getLineNumber()
  {
    return this.lineNumber;
  }
  
  public int getColumnNumber()
  {
    return this.columnNumber;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\ast\util\LocatorImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */