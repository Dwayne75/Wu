package com.sun.tools.xjc.model;

import com.sun.xml.xsom.XSComponent;
import javax.xml.bind.annotation.XmlTransient;
import org.xml.sax.Locator;

abstract class AbstractCElement
  extends AbstractCTypeInfoImpl
  implements CElement
{
  @XmlTransient
  private final Locator locator;
  private boolean isAbstract;
  
  protected AbstractCElement(Model model, XSComponent source, Locator locator, CCustomizations customizations)
  {
    super(model, source, customizations);
    this.locator = locator;
  }
  
  public Locator getLocator()
  {
    return this.locator;
  }
  
  public boolean isAbstract()
  {
    return this.isAbstract;
  }
  
  public void setAbstract()
  {
    this.isAbstract = true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\AbstractCElement.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */