package com.sun.tools.xjc.model;

import com.sun.istack.Nullable;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.TypeRef;
import com.sun.xml.bind.v2.runtime.RuntimeUtil.ToStringAdapter;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XmlString;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

public final class CTypeRef
  implements TypeRef<NType, NClass>
{
  @XmlJavaTypeAdapter(RuntimeUtil.ToStringAdapter.class)
  private final CNonElement type;
  private final QName elementName;
  @Nullable
  final QName typeName;
  private final boolean nillable;
  public final XmlString defaultValue;
  
  public CTypeRef(CNonElement type, XSElementDecl decl)
  {
    this(type, BGMBuilder.getName(decl), getSimpleTypeName(decl), decl.isNillable(), decl.getDefaultValue());
  }
  
  public static QName getSimpleTypeName(XSElementDecl decl)
  {
    if (decl == null) {
      return null;
    }
    QName typeName = null;
    if (decl.getType().isSimpleType()) {
      typeName = BGMBuilder.getName(decl.getType());
    }
    return typeName;
  }
  
  public CTypeRef(CNonElement type, QName elementName, QName typeName, boolean nillable, XmlString defaultValue)
  {
    assert (type != null);
    assert (elementName != null);
    
    this.type = type;
    this.elementName = elementName;
    this.typeName = typeName;
    this.nillable = nillable;
    this.defaultValue = defaultValue;
  }
  
  public CNonElement getTarget()
  {
    return this.type;
  }
  
  public QName getTagName()
  {
    return this.elementName;
  }
  
  public boolean isNillable()
  {
    return this.nillable;
  }
  
  public String getDefaultValue()
  {
    if (this.defaultValue != null) {
      return this.defaultValue.value;
    }
    return null;
  }
  
  public boolean isLeaf()
  {
    throw new UnsupportedOperationException();
  }
  
  public PropertyInfo<NType, NClass> getSource()
  {
    throw new UnsupportedOperationException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CTypeRef.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */