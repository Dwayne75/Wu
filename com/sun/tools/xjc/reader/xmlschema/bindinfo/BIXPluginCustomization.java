package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.reader.Ring;
import java.util.Set;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;
import org.xml.sax.Locator;

public final class BIXPluginCustomization
  extends AbstractDeclarationImpl
{
  public final Element element;
  private QName name;
  
  public BIXPluginCustomization(Element e, Locator _loc)
  {
    super(_loc);
    this.element = e;
  }
  
  public void onSetOwner()
  {
    super.onSetOwner();
    if (!((Model)Ring.get(Model.class)).options.pluginURIs.contains(this.element.getNamespaceURI())) {
      markAsAcknowledged();
    }
  }
  
  public final QName getName()
  {
    if (this.name == null) {
      this.name = new QName(this.element.getNamespaceURI(), this.element.getLocalName());
    }
    return this.name;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIXPluginCustomization.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */