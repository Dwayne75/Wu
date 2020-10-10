package com.sun.tools.xjc.model;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;

public class SymbolSpace
{
  private JType type;
  private final JCodeModel codeModel;
  
  public SymbolSpace(JCodeModel _codeModel)
  {
    this.codeModel = _codeModel;
  }
  
  public JType getType()
  {
    if (this.type == null) {
      return this.codeModel.ref(Object.class);
    }
    return this.type;
  }
  
  public void setType(JType _type)
  {
    if (this.type == null) {
      this.type = _type;
    }
  }
  
  public String toString()
  {
    if (this.type == null) {
      return "undetermined";
    }
    return this.type.name();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\SymbolSpace.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */