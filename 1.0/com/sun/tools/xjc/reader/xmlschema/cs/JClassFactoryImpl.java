package com.sun.tools.xjc.reader.xmlschema.cs;

import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.reader.xmlschema.JClassFactory;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import org.xml.sax.Locator;

class JClassFactoryImpl
  implements JClassFactory
{
  private final ClassSelector owner;
  private final JClassFactory parent;
  private final JClassContainer container;
  
  JClassFactoryImpl(ClassSelector owner, JClassContainer _cont)
  {
    this.parent = owner.getClassFactory();
    this.container = _cont;
    this.owner = owner;
  }
  
  public JDefinedClass create(String name, Locator sourceLocation)
  {
    return this.owner.codeModelClassFactory.createInterface(this.container, name, sourceLocation);
  }
  
  public JClassFactory getParentFactory()
  {
    return this.parent;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\cs\JClassFactoryImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */