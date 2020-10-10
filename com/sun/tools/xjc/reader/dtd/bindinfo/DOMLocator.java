package com.sun.tools.xjc.reader.dtd.bindinfo;

import org.w3c.dom.Element;
import org.xml.sax.Locator;

class DOMLocator
{
  private static final String locationNamespace = "http://www.sun.com/xmlns/jaxb/dom-location";
  private static final String systemId = "systemid";
  private static final String column = "column";
  private static final String line = "line";
  
  public static void setLocationInfo(Element e, Locator loc)
  {
    e.setAttributeNS("http://www.sun.com/xmlns/jaxb/dom-location", "loc:systemid", loc.getSystemId());
    e.setAttributeNS("http://www.sun.com/xmlns/jaxb/dom-location", "loc:column", Integer.toString(loc.getLineNumber()));
    e.setAttributeNS("http://www.sun.com/xmlns/jaxb/dom-location", "loc:line", Integer.toString(loc.getColumnNumber()));
  }
  
  public static Locator getLocationInfo(Element e)
  {
    if (DOMUtil.getAttribute(e, "http://www.sun.com/xmlns/jaxb/dom-location", "systemid") == null) {
      return null;
    }
    new Locator()
    {
      public int getLineNumber()
      {
        return Integer.parseInt(DOMUtil.getAttribute(this.val$e, "http://www.sun.com/xmlns/jaxb/dom-location", "line"));
      }
      
      public int getColumnNumber()
      {
        return Integer.parseInt(DOMUtil.getAttribute(this.val$e, "http://www.sun.com/xmlns/jaxb/dom-location", "column"));
      }
      
      public String getSystemId()
      {
        return DOMUtil.getAttribute(this.val$e, "http://www.sun.com/xmlns/jaxb/dom-location", "systemid");
      }
      
      public String getPublicId()
      {
        return null;
      }
    };
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\dtd\bindinfo\DOMLocator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */