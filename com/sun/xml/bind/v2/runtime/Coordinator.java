package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.v2.ClassFactory;
import java.util.HashMap;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.helpers.ValidationEventImpl;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class Coordinator
  implements ErrorHandler, ValidationEventHandler
{
  private final HashMap<Class<? extends XmlAdapter>, XmlAdapter> adapters = new HashMap();
  private Coordinator old;
  private Coordinator[] table;
  public Exception guyWhoSetTheTableToNull;
  private static final ThreadLocal<Coordinator[]> activeTable;
  public static boolean debugTableNPE;
  
  public final XmlAdapter putAdapter(Class<? extends XmlAdapter> c, XmlAdapter a)
  {
    if (a == null) {
      return (XmlAdapter)this.adapters.remove(c);
    }
    return (XmlAdapter)this.adapters.put(c, a);
  }
  
  public final <T extends XmlAdapter> T getAdapter(Class<T> key)
  {
    T v = (XmlAdapter)key.cast(this.adapters.get(key));
    if (v == null)
    {
      v = (XmlAdapter)ClassFactory.create(key);
      putAdapter(key, v);
    }
    return v;
  }
  
  public <T extends XmlAdapter> boolean containsAdapter(Class<T> type)
  {
    return this.adapters.containsKey(type);
  }
  
  protected final void setThreadAffinity()
  {
    this.table = ((Coordinator[])activeTable.get());
    assert (this.table != null);
  }
  
  protected final void resetThreadAffinity()
  {
    if (debugTableNPE) {
      this.guyWhoSetTheTableToNull = new Exception();
    }
    this.table = null;
  }
  
  protected final void pushCoordinator()
  {
    this.old = this.table[0];
    this.table[0] = this;
  }
  
  protected final void popCoordinator()
  {
    assert (this.table[0] == this);
    this.table[0] = this.old;
    this.old = null;
  }
  
  public static Coordinator _getInstance()
  {
    return ((Coordinator[])activeTable.get())[0];
  }
  
  protected abstract ValidationEventLocator getLocation();
  
  public final void error(SAXParseException exception)
    throws SAXException
  {
    propagateEvent(1, exception);
  }
  
  public final void warning(SAXParseException exception)
    throws SAXException
  {
    propagateEvent(0, exception);
  }
  
  public final void fatalError(SAXParseException exception)
    throws SAXException
  {
    propagateEvent(2, exception);
  }
  
  private void propagateEvent(int severity, SAXParseException saxException)
    throws SAXException
  {
    ValidationEventImpl ve = new ValidationEventImpl(severity, saxException.getMessage(), getLocation());
    
    Exception e = saxException.getException();
    if (e != null) {
      ve.setLinkedException(e);
    } else {
      ve.setLinkedException(saxException);
    }
    boolean result = handleEvent(ve);
    if (!result) {
      throw saxException;
    }
  }
  
  static
  {
    activeTable = new ThreadLocal()
    {
      public Coordinator[] initialValue()
      {
        return new Coordinator[1];
      }
    };
    try
    {
      debugTableNPE = Boolean.getBoolean(Coordinator.class.getName() + ".debugTableNPE");
    }
    catch (SecurityException t) {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\Coordinator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */