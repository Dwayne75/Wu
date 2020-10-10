package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import javax.xml.bind.annotation.XmlNs;

public abstract interface XmlNsWriter
  extends JAnnotationWriter<XmlNs>
{
  public abstract XmlNsWriter prefix(String paramString);
  
  public abstract XmlNsWriter namespaceURI(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlNsWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */