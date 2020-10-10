package com.sun.tools.xjc.util;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.ErrorReceiver;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

public final class CodeModelClassFactory
{
  private ErrorReceiver errorReceiver;
  private int ticketMaster = 0;
  
  public CodeModelClassFactory(ErrorReceiver _errorReceiver)
  {
    this.errorReceiver = _errorReceiver;
  }
  
  public JDefinedClass createClass(JClassContainer parent, String name, Locator source)
  {
    return createClass(parent, 1, name, source);
  }
  
  public JDefinedClass createClass(JClassContainer parent, int mod, String name, Locator source)
  {
    return createClass(parent, mod, name, source, false);
  }
  
  public JDefinedClass createInterface(JClassContainer parent, String name, Locator source)
  {
    return createInterface(parent, 1, name, source);
  }
  
  public JDefinedClass createInterface(JClassContainer parent, int mod, String name, Locator source)
  {
    return createClass(parent, mod, name, source, true);
  }
  
  private JDefinedClass createClass(JClassContainer parent, int mod, String name, Locator source, boolean isInterface)
  {
    try
    {
      JDefinedClass r = parent._class(mod, name, isInterface);
      
      r.metadata = source;
      
      return r;
    }
    catch (JClassAlreadyExistsException e)
    {
      JDefinedClass cls = e.getExistingClass();
      
      this.errorReceiver.error(new SAXParseException(Messages.format("CodeModelClassFactory.ClassNameCollision", cls.fullName()), (Locator)cls.metadata));
      
      this.errorReceiver.error(new SAXParseException(Messages.format("CodeModelClassFactory.ClassNameCollision.Source", name), source));
      if (!name.equals(cls.name())) {
        this.errorReceiver.error(new SAXParseException(Messages.format("CodeModelClassFactory.CaseSensitivityCollision", name, cls.name()), null));
      }
      try
      {
        return parent.owner()._class("$$$garbage$$$" + this.ticketMaster++);
      }
      catch (JClassAlreadyExistsException ee)
      {
        return ee.getExistingClass();
      }
    }
  }
  
  public void setErrorHandler(ErrorReceiver errorReceiver)
  {
    this.errorReceiver = errorReceiver;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\util\CodeModelClassFactory.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */