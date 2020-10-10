package com.sun.tools.xjc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;
import org.xml.sax.SAXException;

public final class SchemaCache
{
  private Schema schema;
  private final URL source;
  
  public SchemaCache(URL source)
  {
    this.source = source;
  }
  
  public ValidatorHandler newValidator()
  {
    synchronized (this)
    {
      if (this.schema == null) {
        try
        {
          this.schema = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema").newSchema(this.source);
        }
        catch (SAXException e)
        {
          throw new AssertionError(e);
        }
      }
    }
    ValidatorHandler handler = this.schema.newValidatorHandler();
    fixValidatorBug6246922(handler);
    
    return handler;
  }
  
  private void fixValidatorBug6246922(ValidatorHandler handler)
  {
    try
    {
      Field f = handler.getClass().getDeclaredField("errorReporter");
      f.setAccessible(true);
      Object errorReporter = f.get(handler);
      
      Method get = errorReporter.getClass().getDeclaredMethod("getMessageFormatter", new Class[] { String.class });
      Object currentFormatter = get.invoke(errorReporter, new Object[] { "http://www.w3.org/TR/xml-schema-1" });
      if (currentFormatter != null) {
        return;
      }
      Method put = null;
      for (Method m : errorReporter.getClass().getDeclaredMethods()) {
        if (m.getName().equals("putMessageFormatter"))
        {
          put = m;
          break;
        }
      }
      if (put == null) {
        return;
      }
      ClassLoader cl = errorReporter.getClass().getClassLoader();
      String className = "com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter";
      Class xsformatter;
      Class xsformatter;
      if (cl == null) {
        xsformatter = Class.forName(className);
      } else {
        xsformatter = cl.loadClass(className);
      }
      put.invoke(errorReporter, new Object[] { "http://www.w3.org/TR/xml-schema-1", xsformatter.newInstance() });
    }
    catch (Throwable t) {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\SchemaCache.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */