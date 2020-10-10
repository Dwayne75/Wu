package com.sun.tools.xjc.reader.relaxng;

import com.sun.tools.xjc.reader.internalizer.DOMForest;
import com.sun.tools.xjc.reader.internalizer.InternalizationLogic;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

public class RELAXNGInternalizationLogic
  implements InternalizationLogic
{
  public XMLFilter createExternalReferenceFinder(DOMForest parent)
  {
    return new RELAXNGInternalizationLogic.ReferenceFinder(this, parent);
  }
  
  public boolean checkIfValidTargetNode(DOMForest parent, Element bindings, Element target)
    throws SAXException
  {
    return "http://relaxng.org/ns/structure/1.0".equals(target.getNamespaceURI());
  }
  
  public Element refineTarget(Element target)
  {
    return target;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\relaxng\RELAXNGInternalizationLogic.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */