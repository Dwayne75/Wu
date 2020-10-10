package com.sun.tools.xjc.reader.xmlschema;

import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.grammar.xducer.WhitespaceTransducer;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSSimpleType;
import java.util.Stack;

public class SimpleTypeBuilder
{
  protected final BGMBuilder builder;
  public final DatatypeBuilder datatypeBuilder;
  protected final ConversionFinder conversionFinder;
  private final ExpressionPool pool;
  
  SimpleTypeBuilder(BGMBuilder builder)
  {
    this.builder = builder;
    this.datatypeBuilder = new DatatypeBuilder(builder, builder.schemas);
    this.conversionFinder = new ConversionFinder(builder);
    this.pool = builder.pool;
  }
  
  public final Stack refererStack = new Stack();
  
  public Expression build(XSSimpleType type)
  {
    Expression e = checkRefererCustomization(type);
    if (e == null) {
      e = (Expression)type.apply(new SimpleTypeBuilder.Functor(this, type, null));
    }
    return e;
  }
  
  private BIConversion getRefererCustomization()
  {
    BindInfo info = this.builder.getBindInfo((XSComponent)this.refererStack.peek());
    BIProperty prop = (BIProperty)info.get(BIProperty.NAME);
    if (prop == null) {
      return null;
    }
    return prop.conv;
  }
  
  private Expression checkRefererCustomization(XSSimpleType type)
  {
    XSComponent top = (XSComponent)this.refererStack.peek();
    if ((top instanceof XSElementDecl))
    {
      XSElementDecl eref = (XSElementDecl)top;
      _assert(eref.getType() == type);
      detectJavaTypeCustomization();
    }
    else if ((top instanceof XSAttributeDecl))
    {
      XSAttributeDecl aref = (XSAttributeDecl)top;
      _assert(aref.getType() == type);
      detectJavaTypeCustomization();
    }
    else if ((top instanceof XSComplexType))
    {
      XSComplexType tref = (XSComplexType)top;
      _assert(tref.getBaseType() == type);
      detectJavaTypeCustomization();
    }
    else if (top != type)
    {
      _assert(false);
    }
    BIConversion conv = getRefererCustomization();
    if (conv != null)
    {
      conv.markAsAcknowledged();
      
      return buildPrimitiveType(type, conv.getTransducer());
    }
    return null;
  }
  
  private void detectJavaTypeCustomization()
  {
    BindInfo info = this.builder.getBindInfo((XSComponent)this.refererStack.peek());
    BIConversion conv = (BIConversion)info.get(BIConversion.NAME);
    if (conv != null)
    {
      conv.markAsAcknowledged();
      
      this.builder.errorReporter.error(conv.getLocation(), "SimpleTypeBuilder.UnnestedJavaTypeCustomization");
    }
  }
  
  private PrimitiveItem buildPrimitiveType(XSSimpleType type, Transducer xducer)
  {
    XSDatatype dt = this.datatypeBuilder.build(type);
    return this.builder.grammar.createPrimitiveItem(WhitespaceTransducer.create(xducer, this.builder.grammar.codeModel, type), dt, this.pool.createData(dt), type.getLocator());
  }
  
  private static final void _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\SimpleTypeBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */