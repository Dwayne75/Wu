package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import com.sun.codemodel.JType;
import javax.xml.bind.annotation.XmlElementRef;

public abstract interface XmlElementRefWriter
  extends JAnnotationWriter<XmlElementRef>
{
  public abstract XmlElementRefWriter name(String paramString);
  
  public abstract XmlElementRefWriter type(Class paramClass);
  
  public abstract XmlElementRefWriter type(JType paramJType);
  
  public abstract XmlElementRefWriter namespace(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlElementRefWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */