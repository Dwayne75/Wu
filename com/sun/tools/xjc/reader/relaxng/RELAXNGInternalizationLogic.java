package com.sun.tools.xjc.reader.relaxng;

import com.sun.tools.xjc.reader.internalizer.AbstractReferenceFinderImpl;
import com.sun.tools.xjc.reader.internalizer.DOMForest;
import com.sun.tools.xjc.reader.internalizer.InternalizationLogic;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.XMLFilterImpl;

public class RELAXNGInternalizationLogic
  implements InternalizationLogic
{
  private static final class ReferenceFinder
    extends AbstractReferenceFinderImpl
  {
    ReferenceFinder(DOMForest parent)
    {
      super();
    }
    
    protected String findExternalResource(String nsURI, String localName, Attributes atts)
    {
      if (("http://relaxng.org/ns/structure/1.0".equals(nsURI)) && (("include".equals(localName)) || ("externalRef".equals(localName)))) {
        return atts.getValue("href");
      }
      return null;
    }
  }
  
  public XMLFilterImpl createExternalReferenceFinder(DOMForest parent)
  {
    return new ReferenceFinder(parent);
  }
  
  public boolean checkIfValidTargetNode(DOMForest parent, Element bindings, Element target)
  {
    return "http://relaxng.org/ns/structure/1.0".equals(target.getNamespaceURI());
  }
  
  public Element refineTarget(Element target)
  {
    return target;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\relaxng\RELAXNGInternalizationLogic.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */