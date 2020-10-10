package org.kohsuke.rngom.digested;

import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.w3c.dom.Element;

final class ElementWrapper
  implements ParsedElementAnnotation
{
  final Element element;
  
  public ElementWrapper(Element e)
  {
    this.element = e;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\ElementWrapper.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */