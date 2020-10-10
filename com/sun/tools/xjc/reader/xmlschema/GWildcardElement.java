package com.sun.tools.xjc.reader.xmlschema;

import com.sun.xml.xsom.XSWildcard;

final class GWildcardElement
  extends GElement
{
  private boolean strict = true;
  
  public String toString()
  {
    return "#any";
  }
  
  String getPropertyNameSeed()
  {
    return "any";
  }
  
  public void merge(XSWildcard wc)
  {
    switch (wc.getMode())
    {
    case 1: 
    case 3: 
      this.strict = false;
    }
  }
  
  public boolean isStrict()
  {
    return this.strict;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\GWildcardElement.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */