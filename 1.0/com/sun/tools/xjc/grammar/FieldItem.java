package com.sun.tools.xjc.grammar;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.generator.field.FieldRendererFactory;
import com.sun.tools.xjc.grammar.util.Multiplicity;
import com.sun.tools.xjc.reader.TypeUtil;
import java.util.HashSet;
import java.util.Set;
import org.xml.sax.Locator;

public final class FieldItem
  extends JavaItem
{
  public FieldRendererFactory realization;
  
  public FieldItem(String name, Locator loc)
  {
    this(name, null, loc);
  }
  
  public FieldItem(String name, Expression exp, Locator loc)
  {
    this(name, exp, null, loc);
  }
  
  public FieldItem(String name, Expression _exp, JType _userDefinedType, Locator loc)
  {
    super(name, loc);
    this.exp = _exp;
    this.userSpecifiedType = _userDefinedType;
  }
  
  public DefaultValue[] defaultValues = null;
  public Multiplicity multiplicity;
  public boolean collisionExpected = false;
  public String javadoc = null;
  private boolean delegation = false;
  
  public void setDelegation(boolean f)
  {
    this.delegation = f;
  }
  
  protected boolean isDelegated()
  {
    return this.delegation;
  }
  
  private final Set types = new HashSet();
  public final JType userSpecifiedType;
  
  public final void addType(TypeItem ti)
    throws FieldItem.BadTypeException
  {
    if (this.userSpecifiedType != null) {
      throw new FieldItem.BadTypeException(this.userSpecifiedType, null);
    }
    this.types.add(ti);
  }
  
  public final TypeItem[] listTypes()
  {
    return (TypeItem[])this.types.toArray(new TypeItem[this.types.size()]);
  }
  
  public final boolean hasTypes()
  {
    return !this.types.isEmpty();
  }
  
  public JType getType(JCodeModel codeModel)
  {
    if (this.userSpecifiedType != null) {
      return this.userSpecifiedType;
    }
    JType[] classes = new JType[this.types.size()];
    TypeItem[] types = listTypes();
    for (int i = 0; i < types.length; i++) {
      classes[i] = types[i].getType();
    }
    return TypeUtil.getCommonBaseType(codeModel, classes);
  }
  
  public boolean isUnboxable(JCodeModel codeModel)
  {
    TypeItem[] types = listTypes();
    if (!getType(codeModel).isPrimitive()) {
      return false;
    }
    for (int i = 0; i < types.length; i++)
    {
      JType t = types[i].getType();
      if (!(t instanceof JPrimitiveType)) {
        if (((JClass)t).getPrimitiveType() == null) {
          return false;
        }
      }
    }
    return true;
  }
  
  public Object visitJI(JavaItemVisitor visitor)
  {
    return visitor.onField(this);
  }
  
  public String toString()
  {
    return super.toString() + '[' + this.name + ']';
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\FieldItem.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */