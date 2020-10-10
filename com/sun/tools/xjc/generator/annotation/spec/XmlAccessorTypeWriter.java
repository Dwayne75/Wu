package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

public abstract interface XmlAccessorTypeWriter
  extends JAnnotationWriter<XmlAccessorType>
{
  public abstract XmlAccessorTypeWriter value(XmlAccessType paramXmlAccessType);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlAccessorTypeWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */