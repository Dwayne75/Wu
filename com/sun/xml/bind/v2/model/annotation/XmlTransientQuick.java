package com.sun.xml.bind.v2.model.annotation;

import java.lang.annotation.Annotation;
import javax.xml.bind.annotation.XmlTransient;

final class XmlTransientQuick
  extends Quick
  implements XmlTransient
{
  private final XmlTransient core;
  
  public XmlTransientQuick(Locatable upstream, XmlTransient core)
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
    return new XmlTransientQuick(upstream, (XmlTransient)core);
  }
  
  public Class<XmlTransient> annotationType()
  {
    return XmlTransient.class;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\annotation\XmlTransientQuick.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */