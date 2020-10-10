package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import javax.xml.bind.annotation.XmlElements;

public abstract interface XmlElementsWriter
  extends JAnnotationWriter<XmlElements>
{
  public abstract XmlElementWriter value();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlElementsWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */