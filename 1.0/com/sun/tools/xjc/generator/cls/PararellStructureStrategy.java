package com.sun.tools.xjc.generator.cls;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.MethodWriter;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import java.util.HashMap;
import java.util.Map;
import org.xml.sax.Locator;

public final class PararellStructureStrategy
  implements ImplStructureStrategy
{
  private final Map intf2impl = new HashMap();
  private final CodeModelClassFactory codeModelClassFactory;
  
  public PararellStructureStrategy(CodeModelClassFactory _codeModelClassFactory)
  {
    this.codeModelClassFactory = _codeModelClassFactory;
  }
  
  private JDefinedClass determineImplClass(JDefinedClass intf)
  {
    JDefinedClass d = (JDefinedClass)this.intf2impl.get(intf);
    if (d != null) {
      return d;
    }
    JClassContainer parent = intf.parentContainer();
    int mod = 1;
    if ((parent instanceof JPackage))
    {
      parent = ((JPackage)parent).subPackage("impl");
    }
    else
    {
      parent = determineImplClass((JDefinedClass)parent);
      mod |= 0x10;
    }
    d = this.codeModelClassFactory.createClass(parent, mod, intf.name() + "Impl", (Locator)intf.metadata);
    
    this.intf2impl.put(intf, d);
    return d;
  }
  
  public JDefinedClass createImplClass(ClassItem ci)
  {
    JDefinedClass impl = determineImplClass(ci.getTypeAsDefined());
    
    impl._implements(ci.getTypeAsDefined());
    
    impl.method(28, Class.class, "PRIMARY_INTERFACE_CLASS").body()._return(ci.getTypeAsDefined().dotclass());
    
    return impl;
  }
  
  public MethodWriter createMethodWriter(ClassContext target)
  {
    return new PararellStructureStrategy.1(this, target, target);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\cls\PararellStructureStrategy.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */