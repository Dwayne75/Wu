package com.sun.tools.xjc.grammar;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ReferenceExp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.xml.sax.Locator;

public final class ClassItem
  extends TypeItem
{
  private final JDefinedClass type;
  private String userSpecifiedImplClass;
  public final AnnotatedGrammar owner;
  
  protected ClassItem(AnnotatedGrammar _owner, JDefinedClass _type, Expression exp, Locator loc)
  {
    super(_type.name(), loc);
    this.type = _type;
    this.owner = _owner;
    this.exp = exp;
  }
  
  public JType getType()
  {
    return this.type;
  }
  
  public JDefinedClass getTypeAsDefined()
  {
    return this.type;
  }
  
  private final Map fields = new HashMap();
  
  public final FieldUse getDeclaredField(String name)
  {
    return (FieldUse)this.fields.get(name);
  }
  
  public final FieldUse getField(String name)
  {
    FieldUse fu = getDeclaredField(name);
    if (fu != null) {
      return fu;
    }
    if (this.superClass != null) {
      return getSuperClass().getField(name);
    }
    return null;
  }
  
  public final FieldUse[] getDeclaredFieldUses()
  {
    return (FieldUse[])this.fields.values().toArray(new FieldUse[this.fields.size()]);
  }
  
  public FieldUse getOrCreateFieldUse(String name)
  {
    FieldUse r = (FieldUse)this.fields.get(name);
    if (r == null) {
      this.fields.put(name, r = new FieldUse(name, this));
    }
    return r;
  }
  
  public void removeDuplicateFieldUses()
  {
    ClassItem superClass = getSuperClass();
    if (this.superClass == null) {
      return;
    }
    FieldUse[] fu = getDeclaredFieldUses();
    for (int i = 0; i < fu.length; i++) {
      if (superClass.getField(fu[i].name) != null) {
        this.fields.remove(fu[i].name);
      }
    }
  }
  
  public final ReferenceExp agm = new ReferenceExp(null);
  private final Vector constructors = new Vector();
  
  public void addConstructor(String[] fieldNames)
  {
    this.constructors.add(new Constructor(fieldNames));
  }
  
  public Iterator iterateConstructors()
  {
    return this.constructors.iterator();
  }
  
  public boolean hasGetContentMethod = false;
  public SuperClassItem superClass;
  
  public ClassItem getSuperClass()
  {
    if (this.superClass == null) {
      return null;
    }
    return this.superClass.definition;
  }
  
  public Object visitJI(JavaItemVisitor visitor)
  {
    return visitor.onClass(this);
  }
  
  protected boolean calcEpsilonReducibility()
  {
    return false;
  }
  
  public String getUserSpecifiedImplClass()
  {
    return this.userSpecifiedImplClass;
  }
  
  public void setUserSpecifiedImplClass(String userSpecifiedImplClass)
  {
    this.userSpecifiedImplClass = userSpecifiedImplClass;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\ClassItem.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */