package com.sun.xml.bind.v2.model.annotation;

import java.lang.annotation.Annotation;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;

final class XmlElementRefsQuick
  extends Quick
  implements XmlElementRefs
{
  private final XmlElementRefs core;
  
  public XmlElementRefsQuick(Locatable upstream, XmlElementRefs core)
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
    return new XmlElementRefsQuick(upstream, (XmlElementRefs)core);
  }
  
  public Class<XmlElementRefs> annotationType()
  {
    return XmlElementRefs.class;
  }
  
  public XmlElementRef[] value()
  {
    return this.core.value();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\annotation\XmlElementRefsQuick.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */