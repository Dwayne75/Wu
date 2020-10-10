package com.sun.xml.txw2;

import com.sun.xml.txw2.annotation.XmlElement;
import com.sun.xml.txw2.annotation.XmlNamespace;
import com.sun.xml.txw2.output.XmlSerializer;
import javax.xml.namespace.QName;

public abstract class TXW
{
  static QName getTagName(Class<?> c)
  {
    String localName = "";
    String nsUri = "##default";
    
    XmlElement xe = (XmlElement)c.getAnnotation(XmlElement.class);
    if (xe != null)
    {
      localName = xe.value();
      nsUri = xe.ns();
    }
    if (localName.length() == 0)
    {
      localName = c.getName();
      int idx = localName.lastIndexOf('.');
      if (idx >= 0) {
        localName = localName.substring(idx + 1);
      }
      localName = Character.toLowerCase(localName.charAt(0)) + localName.substring(1);
    }
    if (nsUri.equals("##default"))
    {
      Package pkg = c.getPackage();
      if (pkg != null)
      {
        XmlNamespace xn = (XmlNamespace)pkg.getAnnotation(XmlNamespace.class);
        if (xn != null) {
          nsUri = xn.value();
        }
      }
    }
    if (nsUri.equals("##default")) {
      nsUri = "";
    }
    return new QName(nsUri, localName);
  }
  
  public static <T extends TypedXmlWriter> T create(Class<T> rootElement, XmlSerializer out)
  {
    Document doc = new Document(out);
    QName n = getTagName(rootElement);
    return new ContainerElement(doc, null, n.getNamespaceURI(), n.getLocalPart())._cast(rootElement);
  }
  
  public static <T extends TypedXmlWriter> T create(QName tagName, Class<T> rootElement, XmlSerializer out)
  {
    return new ContainerElement(new Document(out), null, tagName.getNamespaceURI(), tagName.getLocalPart())._cast(rootElement);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\TXW.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */