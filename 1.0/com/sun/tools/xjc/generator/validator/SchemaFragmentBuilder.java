package com.sun.tools.xjc.generator.validator;

import com.sun.codemodel.JType;
import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.NameClassAndExpression;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.JavaItem;
import com.sun.tools.xjc.grammar.JavaItemVisitor;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.xml.bind.JAXBAssertionError;

class SchemaFragmentBuilder
  extends ExpressionCloner
  implements JavaItemVisitor
{
  private boolean inAttribute = false;
  private boolean inSuperClass = false;
  
  public SchemaFragmentBuilder(ExpressionPool pool)
  {
    super(pool);
  }
  
  public Expression onRef(ReferenceExp exp)
  {
    return exp.exp.visit(this);
  }
  
  public Expression onOther(OtherExp exp)
  {
    if ((exp instanceof JavaItem)) {
      return (Expression)((JavaItem)exp).visitJI(this);
    }
    return exp.exp.visit(this);
  }
  
  public Expression onAttribute(AttributeExp exp)
  {
    if (this.inAttribute) {
      throw new JAXBAssertionError();
    }
    this.inAttribute = true;
    try
    {
      return new AttributeExp(exp.nameClass, exp.exp.visit(this));
    }
    finally
    {
      this.inAttribute = false;
    }
  }
  
  public Expression onElement(ElementExp exp)
  {
    return createElement(exp);
  }
  
  public ElementPattern createElement(NameClassAndExpression exp)
  {
    return new ElementPattern(exp.getNameClass(), exp.getContentModel().visit(this));
  }
  
  public Object onPrimitive(PrimitiveItem pi)
  {
    return pi.exp.visit(this);
  }
  
  public Object onField(FieldItem fi)
  {
    return fi.exp.visit(this);
  }
  
  public Object onIgnore(IgnoreItem ii)
  {
    return ii.exp.visit(this);
  }
  
  public Object onInterface(InterfaceItem ii)
  {
    return ii.exp.visit(this);
  }
  
  public Object onSuper(SuperClassItem si)
  {
    this.inSuperClass = true;
    try
    {
      return si.exp.visit(this);
    }
    finally
    {
      this.inSuperClass = false;
    }
  }
  
  public Object onExternal(ExternalItem ei)
  {
    return ei.createValidationFragment();
  }
  
  private Expression anyAttributes = this.pool.createZeroOrMore(this.pool.createAttribute(NameClass.ALL));
  
  public Object onClass(ClassItem ii)
  {
    if (this.inSuperClass)
    {
      this.inSuperClass = false;
      try
      {
        return ii.exp.visit(this);
      }
      finally
      {
        this.inSuperClass = true;
      }
    }
    if (this.inAttribute) {
      return this.pool.createValue(StringType.theInstance, ("\000" + ii.getType().fullName()).intern());
    }
    return new ElementPattern(new SimpleNameClass("http://java.sun.com/jaxb/xjc/dummy-elements", ii.getType().fullName().intern()), this.anyAttributes);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\validator\SchemaFragmentBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */