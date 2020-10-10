package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.msv.datatype.xsd.ByteType;
import com.sun.msv.datatype.xsd.DoubleType;
import com.sun.msv.datatype.xsd.FloatType;
import com.sun.msv.datatype.xsd.IntType;
import com.sun.msv.datatype.xsd.LongType;
import com.sun.msv.datatype.xsd.ShortType;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.id.SymbolSpace;
import com.sun.tools.xjc.grammar.xducer.BuiltinDatatypeTransducerFactory;
import com.sun.tools.xjc.grammar.xducer.CastTranducer;
import com.sun.tools.xjc.grammar.xducer.DelayedTransducer;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.grammar.xducer.TypeAdaptedTransducer;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;

public class MagicTransducer
  extends DelayedTransducer
{
  private final JType targetType;
  private BIConversion parent;
  protected static final String ERR_ATTRIBUTE_REQUIRED = "MagicTransducer.AttributeRequired";
  
  public MagicTransducer(JType _targetType)
  {
    this.targetType = _targetType;
  }
  
  public void setParent(BIConversion conv)
  {
    this.parent = conv;
  }
  
  protected Transducer create()
  {
    if (!this.targetType.isPrimitive())
    {
      JPrimitiveType unboxed = ((JClass)this.targetType).getPrimitiveType();
      if (unboxed == null) {
        return error();
      }
      return TypeAdaptedTransducer.adapt(new CastTranducer(unboxed, createCore()), this.targetType);
    }
    return new CastTranducer((JPrimitiveType)this.targetType, createCore());
  }
  
  public boolean isID()
  {
    return false;
  }
  
  public SymbolSpace getIDSymbolSpace()
  {
    return null;
  }
  
  protected Transducer createCore()
  {
    XSSimpleType owner = findOwner();
    
    AnnotatedGrammar grammar = this.parent.getBuilder().grammar;
    for (XSSimpleType st = owner; st != null; st = st.getSimpleBaseType()) {
      if ("http://www.w3.org/2001/XMLSchema".equals(st.getTargetNamespace()))
      {
        String name = st.getName().intern();
        if (name == "float") {
          return BuiltinDatatypeTransducerFactory.get(grammar, FloatType.theInstance);
        }
        if (name == "double") {
          return BuiltinDatatypeTransducerFactory.get(grammar, DoubleType.theInstance);
        }
        if (name == "byte") {
          return BuiltinDatatypeTransducerFactory.get(grammar, ByteType.theInstance);
        }
        if (name == "short") {
          return BuiltinDatatypeTransducerFactory.get(grammar, ShortType.theInstance);
        }
        if (name == "int") {
          return BuiltinDatatypeTransducerFactory.get(grammar, IntType.theInstance);
        }
        if (name == "long") {
          return BuiltinDatatypeTransducerFactory.get(grammar, LongType.theInstance);
        }
      }
    }
    return error();
  }
  
  private XSSimpleType findOwner()
  {
    XSComponent c = this.parent.getOwner();
    if ((c instanceof XSSimpleType)) {
      return (XSSimpleType)c;
    }
    if ((c instanceof XSComplexType)) {
      return ((XSComplexType)c).getContentType().asSimpleType();
    }
    if ((c instanceof XSElementDecl)) {
      return ((XSElementDecl)c).getType().asSimpleType();
    }
    if ((c instanceof XSAttributeDecl)) {
      return ((XSAttributeDecl)c).getType();
    }
    return null;
  }
  
  private Transducer error()
  {
    this.parent.getBuilder().errorReceiver.error(this.parent.getLocation(), Messages.format("MagicTransducer.AttributeRequired"));
    
    return new IdentityTransducer(this.parent.getBuilder().grammar.codeModel);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\MagicTransducer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */