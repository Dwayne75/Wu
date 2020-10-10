package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import com.sun.codemodel.JType;
import javax.xml.bind.annotation.XmlEnum;

public abstract interface XmlEnumWriter
  extends JAnnotationWriter<XmlEnum>
{
  public abstract XmlEnumWriter value(Class paramClass);
  
  public abstract XmlEnumWriter value(JType paramJType);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlEnumWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */