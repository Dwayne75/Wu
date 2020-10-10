package org.kohsuke.rngom.xml.sax;

import org.kohsuke.rngom.util.Uri;
import org.xml.sax.Locator;

public class XmlBaseHandler
{
  private int depth;
  private Locator loc;
  private Entry stack;
  
  public XmlBaseHandler()
  {
    this.depth = 0;
    
    this.stack = null;
  }
  
  public void setLocator(Locator loc)
  {
    this.loc = loc;
  }
  
  public void startElement()
  {
    this.depth += 1;
  }
  
  public void endElement()
  {
    if ((this.stack != null) && (this.stack.depth == this.depth)) {
      this.stack = this.stack.parent;
    }
    this.depth -= 1;
  }
  
  public void xmlBaseAttribute(String value)
  {
    Entry entry = new Entry(null);
    entry.parent = this.stack;
    this.stack = entry;
    entry.attValue = Uri.escapeDisallowedChars(value);
    entry.systemId = getSystemId();
    entry.depth = this.depth;
  }
  
  private String getSystemId()
  {
    return this.loc == null ? null : this.loc.getSystemId();
  }
  
  public String getBaseUri()
  {
    return getBaseUri1(getSystemId(), this.stack);
  }
  
  private static String getBaseUri1(String baseUri, Entry stack)
  {
    if ((stack == null) || ((baseUri != null) && (!baseUri.equals(stack.systemId)))) {
      return baseUri;
    }
    baseUri = stack.attValue;
    if (Uri.isAbsolute(baseUri)) {
      return baseUri;
    }
    return Uri.resolve(getBaseUri1(stack.systemId, stack.parent), baseUri);
  }
  
  private static class Entry
  {
    private Entry parent;
    private String attValue;
    private String systemId;
    private int depth;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\xml\sax\XmlBaseHandler.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */