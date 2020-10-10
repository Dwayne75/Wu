package com.sun.tools.xjc.reader.annotator;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.DataExp;
import com.sun.msv.grammar.DataOrValueExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.NameClassAndExpression;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassCandidateItem;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.xducer.BuiltinDatatypeTransducerFactory;
import com.sun.tools.xjc.grammar.xducer.EnumerationXducer;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.PackageTracker;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.relaxng.datatype.Datatype;

class PrimitiveTypeAnnotator
  extends ExpressionCloner
{
  private final AnnotatedGrammar grammar;
  private final AnnotatorController controller;
  private final CodeModelClassFactory classFactory;
  
  PrimitiveTypeAnnotator(AnnotatedGrammar _grammar, AnnotatorController _controller)
  {
    super(_grammar.getPool());
    this.grammar = _grammar;
    this.controller = _controller;
    this.classFactory = new CodeModelClassFactory(this.controller.getErrorReceiver());
    
    this.currentPackage = _grammar.codeModel._package("");
  }
  
  private final Set visitedExps = new HashSet();
  private final Map primitiveItems = new HashMap();
  private JPackage currentPackage;
  
  public Expression onRef(ReferenceExp exp)
  {
    JPackage oldPackage = this.currentPackage;
    if (this.controller.getPackageTracker().get(exp) != null) {
      this.currentPackage = this.controller.getPackageTracker().get(exp);
    }
    if (this.visitedExps.add(exp))
    {
      Expression e = processEnumeration(exp.name, exp.exp);
      if (e == null) {
        e = exp.exp.visit(this);
      }
      exp.exp = e;
    }
    this.currentPackage = oldPackage;
    return exp;
  }
  
  public Expression onOther(OtherExp exp)
  {
    if ((exp instanceof PrimitiveItem)) {
      return exp;
    }
    if ((exp instanceof IgnoreItem)) {
      return exp;
    }
    if (this.visitedExps.add(exp))
    {
      String name = null;
      if ((exp instanceof ClassItem)) {
        name = ((ClassItem)exp).name;
      }
      if ((exp instanceof ClassCandidateItem)) {
        name = ((ClassCandidateItem)exp).name;
      }
      Expression e = null;
      if (name != null) {
        e = processEnumeration(name, exp.exp);
      }
      if (e == null) {
        e = exp.exp.visit(this);
      }
      exp.exp = e;
    }
    return exp;
  }
  
  public Expression onElement(ElementExp exp)
  {
    if (this.visitedExps.add(exp))
    {
      Expression e = processEnumeration(exp);
      if (e == null) {
        e = exp.contentModel.visit(this);
      }
      exp.contentModel = e;
    }
    return exp;
  }
  
  public Expression onAttribute(AttributeExp exp)
  {
    if (this.visitedExps.contains(exp)) {
      return exp;
    }
    Expression e = processEnumeration(exp);
    if (e == null) {
      e = exp.exp.visit(this);
    }
    e = this.pool.createAttribute(exp.nameClass, e);
    this.visitedExps.add(e);
    return e;
  }
  
  public Expression processEnumeration(NameClassAndExpression exp)
  {
    NameClass nc = exp.getNameClass();
    if (!(nc instanceof SimpleNameClass)) {
      return null;
    }
    return processEnumeration(((SimpleNameClass)nc).localName + "Type", exp.getContentModel());
  }
  
  public Expression processEnumeration(String className, Expression exp)
  {
    if (className == null) {
      return null;
    }
    Expression e = exp.visit(new PrimitiveTypeAnnotator.1(this, this.pool));
    if (!(e instanceof ChoiceExp)) {
      return null;
    }
    ChoiceExp cexp = (ChoiceExp)e;
    Expression[] children = cexp.getChildren();
    for (int i = 0; i < children.length; i++) {
      if (!(children[i] instanceof ValueExp)) {
        return null;
      }
    }
    int cnt = 1;
    String decoratedClassName;
    do
    {
      decoratedClassName = this.controller.getNameConverter().toClassName(className) + (cnt++ == 1 ? "" : String.valueOf(cnt));
    } while (this.currentPackage._getClass(decoratedClassName) != null);
    PrimitiveItem p = this.grammar.createPrimitiveItem(new EnumerationXducer(this.controller.getNameConverter(), this.classFactory.createClass(this.currentPackage, decoratedClassName, null), cexp, new HashMap(), null), StringType.theInstance, cexp, null);
    
    this.primitiveItems.put(exp, p);
    return p;
  }
  
  public Expression onData(DataExp exp)
  {
    return onDataOrValue(exp);
  }
  
  public Expression onValue(ValueExp exp)
  {
    return onDataOrValue(exp);
  }
  
  private Expression onDataOrValue(DataOrValueExp exp)
  {
    if (this.primitiveItems.containsKey(exp)) {
      return (Expression)this.primitiveItems.get(exp);
    }
    Datatype dt = exp.getType();
    XSDatatype guard;
    Transducer xducer;
    XSDatatype guard;
    if ((dt instanceof XSDatatype))
    {
      Transducer xducer = BuiltinDatatypeTransducerFactory.get(this.grammar, (XSDatatype)dt);
      
      guard = (XSDatatype)dt;
    }
    else
    {
      xducer = new IdentityTransducer(this.grammar.codeModel);
      guard = StringType.theInstance;
    }
    PrimitiveItem p = this.grammar.createPrimitiveItem(xducer, guard, (Expression)exp, null);
    this.primitiveItems.put(exp, p);
    return p;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\annotator\PrimitiveTypeAnnotator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */