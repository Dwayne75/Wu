package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import com.sun.codemodel.JType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public abstract interface XmlJavaTypeAdapterWriter
  extends JAnnotationWriter<XmlJavaTypeAdapter>
{
  public abstract XmlJavaTypeAdapterWriter value(Class paramClass);
  
  public abstract XmlJavaTypeAdapterWriter value(JType paramJType);
  
  public abstract XmlJavaTypeAdapterWriter type(Class paramClass);
  
  public abstract XmlJavaTypeAdapterWriter type(JType paramJType);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlJavaTypeAdapterWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */