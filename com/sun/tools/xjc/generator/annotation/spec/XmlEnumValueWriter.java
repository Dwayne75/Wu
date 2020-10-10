package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import javax.xml.bind.annotation.XmlEnumValue;

public abstract interface XmlEnumValueWriter
  extends JAnnotationWriter<XmlEnumValue>
{
  public abstract XmlEnumValueWriter value(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlEnumValueWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */