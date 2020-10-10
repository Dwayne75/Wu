package com.sun.tools.xjc.model;

import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.xsom.XmlString;

public abstract class CDefaultValue
{
  public abstract JExpression compute(Outline paramOutline);
  
  public static CDefaultValue create(TypeUse typeUse, final XmlString defaultValue)
  {
    new CDefaultValue()
    {
      public JExpression compute(Outline outline)
      {
        return this.val$typeUse.createConstant(outline, defaultValue);
      }
    };
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CDefaultValue.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */