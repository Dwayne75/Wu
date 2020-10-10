package javax.xml.bind;

import javax.xml.namespace.QName;

public abstract class JAXBIntrospector
{
  public abstract boolean isElement(Object paramObject);
  
  public abstract QName getElementName(Object paramObject);
  
  public static Object getValue(Object jaxbElement)
  {
    if ((jaxbElement instanceof JAXBElement)) {
      return ((JAXBElement)jaxbElement).getValue();
    }
    return jaxbElement;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\JAXBIntrospector.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */