package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import com.sun.codemodel.JType;
import javax.xml.bind.annotation.XmlType;

public abstract interface XmlTypeWriter
  extends JAnnotationWriter<XmlType>
{
  public abstract XmlTypeWriter name(String paramString);
  
  public abstract XmlTypeWriter namespace(String paramString);
  
  public abstract XmlTypeWriter propOrder(String paramString);
  
  public abstract XmlTypeWriter factoryClass(Class paramClass);
  
  public abstract XmlTypeWriter factoryClass(JType paramJType);
  
  public abstract XmlTypeWriter factoryMethod(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlTypeWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */