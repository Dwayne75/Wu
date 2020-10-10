package com.sun.tools.xjc.model;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.xsom.XSComponent;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.activation.MimeType;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public final class CElementPropertyInfo
  extends CPropertyInfo
  implements ElementPropertyInfo<NType, NClass>
{
  private final boolean required;
  private final MimeType expectedMimeType;
  private CAdapter adapter;
  private final boolean isValueList;
  private ID id;
  private final List<CTypeRef> types = new ArrayList();
  private final List<CNonElement> ref = new AbstractList()
  {
    public CNonElement get(int index)
    {
      return ((CTypeRef)CElementPropertyInfo.this.getTypes().get(index)).getTarget();
    }
    
    public int size()
    {
      return CElementPropertyInfo.this.getTypes().size();
    }
  };
  
  public CElementPropertyInfo(String name, CollectionMode collection, ID id, MimeType expectedMimeType, XSComponent source, CCustomizations customizations, Locator locator, boolean required)
  {
    super(name, collection.col, source, customizations, locator);
    this.required = required;
    this.id = id;
    this.expectedMimeType = expectedMimeType;
    this.isValueList = collection.val;
  }
  
  public ID id()
  {
    return this.id;
  }
  
  public List<CTypeRef> getTypes()
  {
    return this.types;
  }
  
  public List<CNonElement> ref()
  {
    return this.ref;
  }
  
  public QName getSchemaType()
  {
    if (this.types.size() != 1) {
      return null;
    }
    CTypeRef t = (CTypeRef)this.types.get(0);
    if (needsExplicitTypeName(t.getTarget(), t.typeName)) {
      return t.typeName;
    }
    return null;
  }
  
  @Deprecated
  public QName getXmlName()
  {
    return null;
  }
  
  public boolean isCollectionRequired()
  {
    return false;
  }
  
  public boolean isCollectionNillable()
  {
    return false;
  }
  
  public boolean isRequired()
  {
    return this.required;
  }
  
  public boolean isValueList()
  {
    return this.isValueList;
  }
  
  public boolean isUnboxable()
  {
    if ((!isCollection()) && (!this.required)) {
      return false;
    }
    for (CTypeRef t : getTypes()) {
      if (t.isNillable()) {
        return false;
      }
    }
    return super.isUnboxable();
  }
  
  public boolean isOptionalPrimitive()
  {
    for (CTypeRef t : getTypes()) {
      if (t.isNillable()) {
        return false;
      }
    }
    return (!isCollection()) && (!this.required) && (super.isUnboxable());
  }
  
  public <V> V accept(CPropertyVisitor<V> visitor)
  {
    return (V)visitor.onElement(this);
  }
  
  public CAdapter getAdapter()
  {
    return this.adapter;
  }
  
  public void setAdapter(CAdapter a)
  {
    assert (this.adapter == null);
    this.adapter = a;
  }
  
  public final PropertyKind kind()
  {
    return PropertyKind.ELEMENT;
  }
  
  public MimeType getExpectedMimeType()
  {
    return this.expectedMimeType;
  }
  
  public static enum CollectionMode
  {
    NOT_REPEATED(false, false),  REPEATED_ELEMENT(true, false),  REPEATED_VALUE(true, true);
    
    private final boolean col;
    private final boolean val;
    
    private CollectionMode(boolean col, boolean val)
    {
      this.col = col;
      this.val = val;
    }
    
    public boolean isRepeated()
    {
      return this.col;
    }
  }
  
  public QName collectElementNames(Map<QName, CPropertyInfo> table)
  {
    for (CTypeRef t : this.types)
    {
      QName n = t.getTagName();
      if (table.containsKey(n)) {
        return n;
      }
      table.put(n, this);
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CElementPropertyInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */