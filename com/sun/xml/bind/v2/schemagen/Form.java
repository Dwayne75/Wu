package com.sun.xml.bind.v2.schemagen;

import com.sun.xml.bind.v2.schemagen.xmlschema.LocalAttribute;
import com.sun.xml.bind.v2.schemagen.xmlschema.LocalElement;
import com.sun.xml.bind.v2.schemagen.xmlschema.Schema;
import com.sun.xml.txw2.TypedXmlWriter;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.namespace.QName;

 enum Form
{
  QUALIFIED(XmlNsForm.QUALIFIED, true),  UNQUALIFIED(XmlNsForm.UNQUALIFIED, false),  UNSET(XmlNsForm.UNSET, false);
  
  private final XmlNsForm xnf;
  public final boolean isEffectivelyQualified;
  
  private Form(XmlNsForm xnf, boolean effectivelyQualified)
  {
    this.xnf = xnf;
    this.isEffectivelyQualified = effectivelyQualified;
  }
  
  abstract void declare(String paramString, Schema paramSchema);
  
  public void writeForm(LocalElement e, QName tagName)
  {
    _writeForm(e, tagName);
  }
  
  public void writeForm(LocalAttribute a, QName tagName)
  {
    _writeForm(a, tagName);
  }
  
  private void _writeForm(TypedXmlWriter e, QName tagName)
  {
    boolean qualified = tagName.getNamespaceURI().length() > 0;
    if ((qualified) && (this != QUALIFIED)) {
      e._attribute("form", "qualified");
    } else if ((!qualified) && (this == QUALIFIED)) {
      e._attribute("form", "unqualified");
    }
  }
  
  public static Form get(XmlNsForm xnf)
  {
    for (Form v : ) {
      if (v.xnf == xnf) {
        return v;
      }
    }
    throw new IllegalArgumentException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\Form.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */