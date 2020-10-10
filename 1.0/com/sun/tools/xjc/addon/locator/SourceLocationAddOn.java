package com.sun.tools.xjc.addon.locator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.CodeAugmenter;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.runtime.UnmarshallingContext;
import com.sun.xml.bind.Locatable;
import java.io.IOException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

public class SourceLocationAddOn
  implements CodeAugmenter
{
  private static final String fieldName = "locator";
  
  public String getOptionName()
  {
    return "Xlocator";
  }
  
  public String getUsage()
  {
    return "  -Xlocator          :  enable source location support for generated code";
  }
  
  public int parseArgument(Options opt, String[] args, int i)
    throws BadCommandLineException, IOException
  {
    return 0;
  }
  
  public boolean run(AnnotatedGrammar grammar, GeneratorContext context, Options opt, ErrorHandler errorHandler)
  {
    JCodeModel codeModel = grammar.codeModel;
    
    ClassItem[] cis = grammar.getClasses();
    for (int i = 0; i < cis.length; i++)
    {
      JDefinedClass impl = context.getClassContext(cis[i]).implClass;
      if (cis[i].getSuperClass() == null)
      {
        JVar $loc = impl.field(2, Locator.class, "locator");
        impl._implements(Locatable.class);
        
        impl.method(1, Locator.class, "sourceLocation").body()._return($loc);
      }
      JClass[] inner = impl.listClasses();
      for (int j = 0; j < inner.length; j++) {
        if (inner[j].name().equals("Unmarshaller"))
        {
          JDefinedClass unm = (JDefinedClass)inner[j];
          
          JMethod cons = unm.getConstructor(new JType[] { context.getRuntime(UnmarshallingContext.class) });
          
          JFieldRef locatorField = JExpr.ref("locator");
          cons.body()._if(locatorField.eq(JExpr._null()))._then().assign(locatorField, JExpr._new(codeModel.ref(LocatorImpl.class)).arg(cons.listParams()[0].invoke("getLocator")));
        }
      }
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\addon\locator\SourceLocationAddOn.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */