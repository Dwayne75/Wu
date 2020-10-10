package com.sun.xml.bind.v2.model.annotation;

import java.lang.annotation.Annotation;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;

final class XmlSchemaQuick
  extends Quick
  implements XmlSchema
{
  private final XmlSchema core;
  
  public XmlSchemaQuick(Locatable upstream, XmlSchema core)
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
    return new XmlSchemaQuick(upstream, (XmlSchema)core);
  }
  
  public Class<XmlSchema> annotationType()
  {
    return XmlSchema.class;
  }
  
  public String location()
  {
    return this.core.location();
  }
  
  public String namespace()
  {
    return this.core.namespace();
  }
  
  public XmlNs[] xmlns()
  {
    return this.core.xmlns();
  }
  
  public XmlNsForm elementFormDefault()
  {
    return this.core.elementFormDefault();
  }
  
  public XmlNsForm attributeFormDefault()
  {
    return this.core.attributeFormDefault();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\annotation\XmlSchemaQuick.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */