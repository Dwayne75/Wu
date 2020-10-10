package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import javax.xml.bind.annotation.XmlAttribute;

public abstract interface XmlAttributeWriter
  extends JAnnotationWriter<XmlAttribute>
{
  public abstract XmlAttributeWriter name(String paramString);
  
  public abstract XmlAttributeWriter namespace(String paramString);
  
  public abstract XmlAttributeWriter required(boolean paramBoolean);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlAttributeWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */