package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import javax.xml.bind.annotation.XmlSchemaTypes;

public abstract interface XmlSchemaTypesWriter
  extends JAnnotationWriter<XmlSchemaTypes>
{
  public abstract XmlSchemaTypeWriter value();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlSchemaTypesWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */