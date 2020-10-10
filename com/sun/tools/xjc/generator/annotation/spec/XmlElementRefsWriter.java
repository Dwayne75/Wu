package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import javax.xml.bind.annotation.XmlElementRefs;

public abstract interface XmlElementRefsWriter
  extends JAnnotationWriter<XmlElementRefs>
{
  public abstract XmlElementRefWriter value();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlElementRefsWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */