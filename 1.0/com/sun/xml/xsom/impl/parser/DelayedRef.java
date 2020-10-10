package com.sun.xml.xsom.impl.parser;

import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.impl.SchemaImpl;
import com.sun.xml.xsom.impl.UName;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public abstract class DelayedRef
  implements Patch
{
  protected final XSSchemaSet schema;
  private PatcherManager manager;
  private UName name;
  private Locator source;
  
  DelayedRef(PatcherManager _manager, Locator _source, SchemaImpl _schema, UName _name)
  {
    this.schema = _schema.getParent();
    this.manager = _manager;
    this.name = _name;
    this.source = _source;
    if (this.name == null) {
      throw new InternalError();
    }
    this.manager.addPatcher(this);
  }
  
  public void run()
    throws SAXException
  {
    if (this.ref == null) {
      resolve();
    }
    this.manager = null;
    this.name = null;
    this.source = null;
  }
  
  private Object ref = null;
  
  protected abstract Object resolveReference(UName paramUName);
  
  protected abstract String getErrorProperty();
  
  protected final Object _get()
  {
    if (this.ref == null) {
      throw new InternalError("unresolved reference");
    }
    return this.ref;
  }
  
  private void resolve()
    throws SAXException
  {
    this.ref = resolveReference(this.name);
    if (this.ref == null) {
      this.manager.reportError(Messages.format(getErrorProperty(), this.name.getQualifiedName()), this.source);
    }
  }
  
  public void redefine(XSDeclaration d)
  {
    if ((!d.getTargetNamespace().equals(this.name.getNamespaceURI())) || (!d.getName().equals(this.name.getName()))) {
      return;
    }
    this.ref = d;
    this.manager = null;
    this.name = null;
    this.source = null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\parser\DelayedRef.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */