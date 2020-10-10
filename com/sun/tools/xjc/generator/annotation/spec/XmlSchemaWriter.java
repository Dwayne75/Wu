package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;

public abstract interface XmlSchemaWriter
  extends JAnnotationWriter<XmlSchema>
{
  public abstract XmlSchemaWriter location(String paramString);
  
  public abstract XmlSchemaWriter namespace(String paramString);
  
  public abstract XmlNsWriter xmlns();
  
  public abstract XmlSchemaWriter elementFormDefault(XmlNsForm paramXmlNsForm);
  
  public abstract XmlSchemaWriter attributeFormDefault(XmlNsForm paramXmlNsForm);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlSchemaWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */