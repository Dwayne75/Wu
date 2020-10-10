package com.sun.tools.xjc.model;

import java.util.ArrayList;
import java.util.Collection;
import org.w3c.dom.Element;

public final class CCustomizations
  extends ArrayList<CPluginCustomization>
{
  CCustomizations next;
  private CCustomizable owner;
  
  public CCustomizations() {}
  
  public CCustomizations(Collection<? extends CPluginCustomization> cPluginCustomizations)
  {
    super(cPluginCustomizations);
  }
  
  void setParent(Model model, CCustomizable owner)
  {
    if (this.owner != null) {
      return;
    }
    this.next = model.customizations;
    model.customizations = this;
    assert (owner != null);
    this.owner = owner;
  }
  
  public CCustomizable getOwner()
  {
    assert (this.owner != null);
    return this.owner;
  }
  
  public CPluginCustomization find(String nsUri)
  {
    for (CPluginCustomization p : this) {
      if (fixNull(p.element.getNamespaceURI()).equals(nsUri)) {
        return p;
      }
    }
    return null;
  }
  
  public CPluginCustomization find(String nsUri, String localName)
  {
    for (CPluginCustomization p : this) {
      if ((fixNull(p.element.getNamespaceURI()).equals(nsUri)) && (fixNull(p.element.getLocalName()).equals(localName))) {
        return p;
      }
    }
    return null;
  }
  
  private String fixNull(String s)
  {
    if (s == null) {
      return "";
    }
    return s;
  }
  
  public static final CCustomizations EMPTY = new CCustomizations();
  
  public static CCustomizations merge(CCustomizations lhs, CCustomizations rhs)
  {
    if ((lhs == null) || (lhs.isEmpty())) {
      return rhs;
    }
    if ((rhs == null) || (rhs.isEmpty())) {
      return lhs;
    }
    CCustomizations r = new CCustomizations(lhs);
    r.addAll(rhs);
    return r;
  }
  
  public boolean equals(Object o)
  {
    return this == o;
  }
  
  public int hashCode()
  {
    return System.identityHashCode(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CCustomizations.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */