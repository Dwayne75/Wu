package com.sun.tools.xjc.reader.xmlschema;

import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.xmlschema.cs.ClassSelector;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.xml.xsom.XSModelGroupDecl;
import org.xml.sax.Locator;

public class PrefixedJClassFactoryImpl
  implements JClassFactory
{
  private final JClassFactory parent;
  private final String prefix;
  private final JClassContainer pkg;
  private final BGMBuilder builder;
  
  public PrefixedJClassFactoryImpl(BGMBuilder builder, JDefinedClass parentClass)
  {
    this.builder = builder;
    this.parent = builder.selector.getClassFactory();
    this.prefix = parentClass.name();
    
    this.pkg = parentClass.parentContainer();
  }
  
  public PrefixedJClassFactoryImpl(BGMBuilder builder, XSModelGroupDecl decl)
  {
    if (decl.isLocal()) {
      throw new IllegalArgumentException();
    }
    this.builder = builder;
    this.parent = builder.selector.getClassFactory();
    this.prefix = builder.getNameConverter().toClassName(decl.getName());
    
    this.pkg = builder.selector.getPackage(decl.getTargetNamespace());
  }
  
  public JDefinedClass create(String name, Locator sourceLocation)
  {
    return this.builder.selector.codeModelClassFactory.createInterface(this.pkg, this.prefix + name, sourceLocation);
  }
  
  public JClassFactory getParentFactory()
  {
    return this.parent;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\PrefixedJClassFactoryImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */