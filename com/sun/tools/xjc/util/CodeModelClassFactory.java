package com.sun.tools.xjc.util;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JJavaName;
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
    return createClass(parent, mod, name, source, ClassType.CLASS);
  }
  
  public JDefinedClass createInterface(JClassContainer parent, String name, Locator source)
  {
    return createInterface(parent, 1, name, source);
  }
  
  public JDefinedClass createInterface(JClassContainer parent, int mod, String name, Locator source)
  {
    return createClass(parent, mod, name, source, ClassType.INTERFACE);
  }
  
  public JDefinedClass createClass(JClassContainer parent, String name, Locator source, ClassType kind)
  {
    return createClass(parent, 1, name, source, kind);
  }
  
  public JDefinedClass createClass(JClassContainer parent, int mod, String name, Locator source, ClassType kind)
  {
    if (!JJavaName.isJavaIdentifier(name))
    {
      this.errorReceiver.error(new SAXParseException(Messages.format("ERR_INVALID_CLASSNAME", new Object[] { name }), source));
      
      return createDummyClass(parent);
    }
    try
    {
      if ((parent.isClass()) && (kind == ClassType.CLASS)) {
        mod |= 0x10;
      }
      JDefinedClass r = parent._class(mod, name, kind);
      
      r.metadata = source;
      
      return r;
    }
    catch (JClassAlreadyExistsException e)
    {
      JDefinedClass cls = e.getExistingClass();
      
      this.errorReceiver.error(new SAXParseException(Messages.format("CodeModelClassFactory.ClassNameCollision", new Object[] { cls.fullName() }), (Locator)cls.metadata));
      
      this.errorReceiver.error(new SAXParseException(Messages.format("CodeModelClassFactory.ClassNameCollision.Source", new Object[] { name }), source));
      if (!name.equals(cls.name())) {
        this.errorReceiver.error(new SAXParseException(Messages.format("CodeModelClassFactory.CaseSensitivityCollision", new Object[] { name, cls.name() }), null));
      }
      if (Util.equals((Locator)cls.metadata, source)) {
        this.errorReceiver.error(new SAXParseException(Messages.format("ERR_CHAMELEON_SCHEMA_GONE_WILD", new Object[0]), source));
      }
    }
    return createDummyClass(parent);
  }
  
  private JDefinedClass createDummyClass(JClassContainer parent)
  {
    try
    {
      return parent._class("$$$garbage$$$" + this.ticketMaster++);
    }
    catch (JClassAlreadyExistsException ee)
    {
      return ee.getExistingClass();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\util\CodeModelClassFactory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */