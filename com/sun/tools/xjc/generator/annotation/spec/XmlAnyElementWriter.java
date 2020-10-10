package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import com.sun.codemodel.JType;
import javax.xml.bind.annotation.XmlAnyElement;

public abstract interface XmlAnyElementWriter
  extends JAnnotationWriter<XmlAnyElement>
{
  public abstract XmlAnyElementWriter value(Class paramClass);
  
  public abstract XmlAnyElementWriter value(JType paramJType);
  
  public abstract XmlAnyElementWriter lax(boolean paramBoolean);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlAnyElementWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */