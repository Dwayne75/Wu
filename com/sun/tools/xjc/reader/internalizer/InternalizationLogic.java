package com.sun.tools.xjc.reader.internalizer;

import org.w3c.dom.Element;
import org.xml.sax.helpers.XMLFilterImpl;

public abstract interface InternalizationLogic
{
  public abstract XMLFilterImpl createExternalReferenceFinder(DOMForest paramDOMForest);
  
  public abstract boolean checkIfValidTargetNode(DOMForest paramDOMForest, Element paramElement1, Element paramElement2);
  
  public abstract Element refineTarget(Element paramElement);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\internalizer\InternalizationLogic.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */