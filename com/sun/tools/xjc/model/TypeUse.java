package com.sun.tools.xjc.model;

import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.xsom.XmlString;
import javax.activation.MimeType;

public abstract interface TypeUse
{
  public abstract boolean isCollection();
  
  public abstract CAdapter getAdapterUse();
  
  public abstract CNonElement getInfo();
  
  public abstract ID idUse();
  
  public abstract MimeType getExpectedMimeType();
  
  public abstract JExpression createConstant(Outline paramOutline, XmlString paramXmlString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\TypeUse.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */