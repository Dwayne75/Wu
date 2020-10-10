package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import javax.xml.bind.annotation.XmlRootElement;

public abstract interface XmlRootElementWriter
  extends JAnnotationWriter<XmlRootElement>
{
  public abstract XmlRootElementWriter name(String paramString);
  
  public abstract XmlRootElementWriter namespace(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlRootElementWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */