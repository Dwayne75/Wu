package com.sun.tools.xjc.reader.xmlschema.cs;

import com.sun.codemodel.JDefinedClass;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSSchema;

abstract class AbstractBinderImpl
  implements ClassBinder
{
  protected final BGMBuilder builder;
  protected final ClassSelector owner;
  
  protected AbstractBinderImpl(ClassSelector _owner)
  {
    this.owner = _owner;
    this.builder = this.owner.builder;
  }
  
  protected final ClassItem wrapByClassItem(XSComponent sc, JDefinedClass cls)
  {
    return this.owner.builder.grammar.createClassItem(cls, Expression.epsilon, sc.getLocator());
  }
  
  protected final String deriveName(XSDeclaration comp)
  {
    return deriveName(comp.getName(), comp);
  }
  
  protected final String deriveName(String name, XSComponent comp)
  {
    XSSchema owner = comp.getOwnerSchema();
    if (owner != null)
    {
      BISchemaBinding sb = (BISchemaBinding)this.builder.getBindInfo(owner).get(BISchemaBinding.NAME);
      if (sb != null) {
        name = sb.mangleClassName(name, comp);
      }
    }
    name = this.builder.getNameConverter().toClassName(name);
    
    return name;
  }
  
  protected static void _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\cs\AbstractBinderImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */