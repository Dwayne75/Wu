package com.sun.tools.xjc.reader.xmlschema;

import org.xml.sax.Locator;

final class CollisionInfo
{
  private final String name;
  private final Locator source1;
  private final Locator source2;
  
  public CollisionInfo(String name, Locator source1, Locator source2)
  {
    this.name = name;
    this.source1 = source1;
    this.source2 = source2;
  }
  
  public String toString()
  {
    return Messages.format("CollisionInfo.CollisionInfo", this.name, printLocator(this.source1), printLocator(this.source2));
  }
  
  private String printLocator(Locator loc)
  {
    if (loc == null) {
      return "";
    }
    int line = loc.getLineNumber();
    String sysId = loc.getSystemId();
    if (sysId == null) {
      sysId = Messages.format("CollisionInfo.UnknownFile");
    }
    if (line != -1) {
      return Messages.format("CollisionInfo.LineXOfY", Integer.toString(line), sysId);
    }
    return sysId;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\CollisionInfo.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */