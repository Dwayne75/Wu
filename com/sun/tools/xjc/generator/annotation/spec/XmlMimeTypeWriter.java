package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import javax.xml.bind.annotation.XmlMimeType;

public abstract interface XmlMimeTypeWriter
  extends JAnnotationWriter<XmlMimeType>
{
  public abstract XmlMimeTypeWriter value(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlMimeTypeWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */