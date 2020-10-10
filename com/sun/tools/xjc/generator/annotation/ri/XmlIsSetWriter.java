package com.sun.tools.xjc.generator.annotation.ri;

import com.sun.codemodel.JAnnotationWriter;
import com.sun.xml.bind.annotation.XmlIsSet;

public abstract interface XmlIsSetWriter
  extends JAnnotationWriter<XmlIsSet>
{
  public abstract XmlIsSetWriter value(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\ri\XmlIsSetWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */