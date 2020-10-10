package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.codemodel.JDefinedClass;

public class BIXSuperClass
{
  private final JDefinedClass cls;
  
  public BIXSuperClass(JDefinedClass _cls)
  {
    this.cls = _cls;
    _cls.hide();
  }
  
  public JDefinedClass getRootClass()
  {
    return this.cls;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIXSuperClass.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */