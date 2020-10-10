package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import com.sun.codemodel.JType;
import javax.xml.bind.annotation.XmlSchemaType;

public abstract interface XmlSchemaTypeWriter
  extends JAnnotationWriter<XmlSchemaType>
{
  public abstract XmlSchemaTypeWriter name(String paramString);
  
  public abstract XmlSchemaTypeWriter type(Class paramClass);
  
  public abstract XmlSchemaTypeWriter type(JType paramJType);
  
  public abstract XmlSchemaTypeWriter namespace(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlSchemaTypeWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */