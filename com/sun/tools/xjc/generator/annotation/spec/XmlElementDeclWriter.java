package com.sun.tools.xjc.generator.annotation.spec;

import com.sun.codemodel.JAnnotationWriter;
import com.sun.codemodel.JType;
import javax.xml.bind.annotation.XmlElementDecl;

public abstract interface XmlElementDeclWriter
  extends JAnnotationWriter<XmlElementDecl>
{
  public abstract XmlElementDeclWriter name(String paramString);
  
  public abstract XmlElementDeclWriter namespace(String paramString);
  
  public abstract XmlElementDeclWriter defaultValue(String paramString);
  
  public abstract XmlElementDeclWriter scope(Class paramClass);
  
  public abstract XmlElementDeclWriter scope(JType paramJType);
  
  public abstract XmlElementDeclWriter substitutionHeadNamespace(String paramString);
  
  public abstract XmlElementDeclWriter substitutionHeadName(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\annotation\spec\XmlElementDeclWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */