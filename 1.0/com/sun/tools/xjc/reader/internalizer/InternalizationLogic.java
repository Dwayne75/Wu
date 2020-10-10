package com.sun.tools.xjc.reader.internalizer;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

public abstract interface InternalizationLogic
{
  public abstract XMLFilter createExternalReferenceFinder(DOMForest paramDOMForest);
  
  public abstract boolean checkIfValidTargetNode(DOMForest paramDOMForest, Element paramElement1, Element paramElement2)
    throws SAXException;
  
  public abstract Element refineTarget(Element paramElement);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\internalizer\InternalizationLogic.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */