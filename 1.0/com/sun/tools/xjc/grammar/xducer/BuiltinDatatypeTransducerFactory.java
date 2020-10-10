package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import com.sun.msv.datatype.xsd.Base64BinaryType;
import com.sun.msv.datatype.xsd.BooleanType;
import com.sun.msv.datatype.xsd.ByteType;
import com.sun.msv.datatype.xsd.DateTimeType;
import com.sun.msv.datatype.xsd.DateType;
import com.sun.msv.datatype.xsd.DoubleType;
import com.sun.msv.datatype.xsd.FloatType;
import com.sun.msv.datatype.xsd.HexBinaryType;
import com.sun.msv.datatype.xsd.IDREFType;
import com.sun.msv.datatype.xsd.IDType;
import com.sun.msv.datatype.xsd.IntType;
import com.sun.msv.datatype.xsd.IntegerType;
import com.sun.msv.datatype.xsd.LongType;
import com.sun.msv.datatype.xsd.NormalizedStringType;
import com.sun.msv.datatype.xsd.NumberType;
import com.sun.msv.datatype.xsd.QnameType;
import com.sun.msv.datatype.xsd.ShortType;
import com.sun.msv.datatype.xsd.SimpleURType;
import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.datatype.xsd.TimeType;
import com.sun.msv.datatype.xsd.TokenType;
import com.sun.msv.datatype.xsd.UnsignedByteType;
import com.sun.msv.datatype.xsd.UnsignedIntType;
import com.sun.msv.datatype.xsd.UnsignedShortType;
import com.sun.msv.datatype.xsd.WhiteSpaceFacet;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.msv.datatype.xsd.XSDatatypeImpl;
import com.sun.tools.xjc.generator.util.WhitespaceNormalizer;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.id.IDREFTransducer;
import com.sun.tools.xjc.grammar.id.IDTransducer;
import com.sun.xml.bind.JAXBAssertionError;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

public class BuiltinDatatypeTransducerFactory
{
  private static Transducer create(JCodeModel model, Class type)
  {
    try
    {
      Method m = type.getMethod("load", new Class[] { String.class });
      String className = type.getName();
      if (!Modifier.isStatic(m.getModifiers())) {
        throw new JAXBAssertionError();
      }
      return new UserTransducer(model.ref(m.getReturnType()), className + ".load", className + ".save");
    }
    catch (NoSuchMethodException e)
    {
      throw new NoSuchMethodError("cannot find the load method for " + type.getName());
    }
  }
  
  private static Transducer create(JType returnType, String stem)
  {
    return new UserTransducer(returnType, "javax.xml.bind.DatatypeConverter.parse" + stem, "javax.xml.bind.DatatypeConverter.print" + stem);
  }
  
  public static Transducer get(AnnotatedGrammar grammar, XSDatatype dt)
  {
    Transducer base = getWithoutWhitespaceNormalization(grammar, dt);
    if ((dt instanceof XSDatatypeImpl)) {
      return WhitespaceTransducer.create(base, grammar.codeModel, ((XSDatatypeImpl)dt).whiteSpace);
    }
    WhiteSpaceFacet wsf = (WhiteSpaceFacet)dt.getFacetObject("whiteSpace");
    if (wsf != null) {
      return WhitespaceTransducer.create(base, grammar.codeModel, wsf.whiteSpace);
    }
    return WhitespaceTransducer.create(base, grammar.codeModel, WhitespaceNormalizer.COLLAPSE);
  }
  
  public static Transducer getWithoutWhitespaceNormalization(AnnotatedGrammar grammar, XSDatatype dt)
  {
    return new BuiltinDatatypeTransducerFactory.1(_getWithoutWhitespaceNormalization(grammar, dt));
  }
  
  private static Transducer _getWithoutWhitespaceNormalization(AnnotatedGrammar grammar, XSDatatype dt)
  {
    JCodeModel codeModel = grammar.codeModel;
    if (dt.getVariety() != 1) {
      throw new JAXBAssertionError();
    }
    if (dt == SimpleURType.theInstance) {
      return new IdentityTransducer(codeModel);
    }
    if ((dt == StringType.theInstance) || (dt == NormalizedStringType.theInstance) || (dt == TokenType.theInstance)) {
      return new IdentityTransducer(codeModel);
    }
    if (dt == IDType.theInstance) {
      return new IDTransducer(codeModel, grammar.defaultSymbolSpace);
    }
    if (dt == IDREFType.theInstance) {
      return new IDREFTransducer(codeModel, grammar.defaultSymbolSpace, false);
    }
    if (dt == BooleanType.theInstance) {
      return create(codeModel.BOOLEAN, "Boolean");
    }
    if (dt == Base64BinaryType.theInstance) {
      return create(codeModel, Base64BinaryType.class);
    }
    if (dt == HexBinaryType.theInstance) {
      return create(codeModel, HexBinaryType.class);
    }
    if (dt == FloatType.theInstance) {
      return create(codeModel.FLOAT, "Float");
    }
    if (dt == DoubleType.theInstance) {
      return create(codeModel.DOUBLE, "Double");
    }
    if (dt == NumberType.theInstance) {
      return create(codeModel.ref(BigDecimal.class), "Decimal");
    }
    if (dt == IntegerType.theInstance) {
      return create(codeModel.ref(BigInteger.class), "Integer");
    }
    if ((dt == LongType.theInstance) || (dt == UnsignedIntType.theInstance)) {
      return create(codeModel.LONG, "Long");
    }
    if ((dt == IntType.theInstance) || (dt == UnsignedShortType.theInstance)) {
      return create(codeModel.INT, "Int");
    }
    if ((dt == ShortType.theInstance) || (dt == UnsignedByteType.theInstance)) {
      return create(codeModel.SHORT, "Short");
    }
    if (dt == ByteType.theInstance) {
      return create(codeModel.BYTE, "Byte");
    }
    if (dt == QnameType.theInstance) {
      return new QNameTransducer(codeModel);
    }
    if (dt == DateType.theInstance) {
      return create(codeModel.ref(Calendar.class), "Date");
    }
    if (dt == TimeType.theInstance) {
      return new DateTransducer(codeModel, codeModel.ref(TimeType.class));
    }
    if (dt == DateTimeType.theInstance) {
      return new DateTransducer(codeModel, codeModel.ref(DateTimeType.class));
    }
    return _getWithoutWhitespaceNormalization(grammar, dt.getBaseType());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\xducer\BuiltinDatatypeTransducerFactory.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */