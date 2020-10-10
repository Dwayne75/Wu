package com.sun.tools.xjc.reader;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import java.util.Stack;
import org.xml.sax.Attributes;

public class StackPackageManager
  implements PackageManager
{
  private final JCodeModel codeModel;
  
  public StackPackageManager(JPackage pkg)
  {
    this.codeModel = pkg.owner();
    this.stack.push(pkg);
  }
  
  public final JPackage getCurrentPackage()
  {
    return (JPackage)this.stack.peek();
  }
  
  private final Stack stack = new Stack();
  
  public final void startElement(Attributes atts)
  {
    if (atts.getIndex("http://java.sun.com/xml/ns/jaxb", "package") != -1)
    {
      String name = atts.getValue("http://java.sun.com/xml/ns/jaxb", "package");
      this.stack.push(this.codeModel._package(name));
    }
    else
    {
      this.stack.push(this.stack.peek());
    }
  }
  
  public final void endElement()
  {
    this.stack.pop();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\StackPackageManager.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */