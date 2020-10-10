package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import javax.xml.bind.annotation.XmlElementWrapper;

public abstract interface XmlElementWrapperWriter
  extends JAnnotationWriter<XmlElementWrapper>
{
  public abstract XmlElementWrapperWriter name(String paramString);
  
  public abstract XmlElementWrapperWriter namespace(String paramString);
  
  public abstract XmlElementWrapperWriter required(boolean paramBoolean);
  
  public abstract XmlElementWrapperWriter nillable(boolean paramBoolean);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlElementWrapperWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */