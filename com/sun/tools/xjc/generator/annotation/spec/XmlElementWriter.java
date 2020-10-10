package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import com.sun.codemodel.JType;
import javax.xml.bind.annotation.XmlElement;

public abstract interface XmlElementWriter
  extends JAnnotationWriter<XmlElement>
{
  public abstract XmlElementWriter name(String paramString);
  
  public abstract XmlElementWriter type(Class paramClass);
  
  public abstract XmlElementWriter type(JType paramJType);
  
  public abstract XmlElementWriter namespace(String paramString);
  
  public abstract XmlElementWriter defaultValue(String paramString);
  
  public abstract XmlElementWriter required(boolean paramBoolean);
  
  public abstract XmlElementWriter nillable(boolean paramBoolean);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlElementWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */