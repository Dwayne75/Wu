package com.sun.xml.bind.v2.model.annotation;

import java.lang.annotation.Annotation;
import javax.xml.bind.annotation.XmlSchemaType;

final class XmlSchemaTypeQuick
  extends Quick
  implements XmlSchemaType
{
  private final XmlSchemaType core;
  
  public XmlSchemaTypeQuick(Locatable upstream, XmlSchemaType core)
  {
    super(upstream);
    this.core = core;
  }
  
  protected Annotation getAnnotation()
  {
    return this.core;
  }
  
  protected Quick newInstance(Locatable upstream, Annotation core)
  {
    return new XmlSchemaTypeQuick(upstream, (XmlSchemaType)core);
  }
  
  public Class<XmlSchemaType> annotationType()
  {
    return XmlSchemaType.class;
  }
  
  public String name()
  {
    return this.core.name();
  }
  
  public Class type()
  {
    return this.core.type();
  }
  
  public String namespace()
  {
    return this.core.namespace();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\annotation\XmlSchemaTypeQuick.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */