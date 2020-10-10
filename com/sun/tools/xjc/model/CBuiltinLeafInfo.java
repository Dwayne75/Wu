package com.sun.tools.xjc.model;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.model.nav.NavigatorImpl;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.runtime.ZeroOneBooleanAdapter;
import com.sun.tools.xjc.util.NamespaceContextAdapter;
import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.impl.BuiltinLeafInfoImpl;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XmlString;
import java.awt.Image;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.MimeType;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import org.xml.sax.Locator;

public abstract class CBuiltinLeafInfo
  extends BuiltinLeafInfoImpl<NType, NClass>
  implements CNonElement
{
  private final ID id;
  
  private CBuiltinLeafInfo(NType typeToken, QName typeName, ID id)
  {
    super(typeToken, new QName[] { typeName });
    this.id = id;
  }
  
  public JType toType(Outline o, Aspect aspect)
  {
    return ((NType)getType()).toType(o, aspect);
  }
  
  @Deprecated
  public final boolean isCollection()
  {
    return false;
  }
  
  @Deprecated
  public CNonElement getInfo()
  {
    return this;
  }
  
  public ID idUse()
  {
    return this.id;
  }
  
  public MimeType getExpectedMimeType()
  {
    return null;
  }
  
  @Deprecated
  public final CAdapter getAdapterUse()
  {
    return null;
  }
  
  public Locator getLocator()
  {
    return Model.EMPTY_LOCATOR;
  }
  
  public final XSComponent getSchemaComponent()
  {
    throw new UnsupportedOperationException("TODO. If you hit this, let us know.");
  }
  
  public final TypeUse makeCollection()
  {
    return TypeUseFactory.makeCollection(this);
  }
  
  public final TypeUse makeAdapted(Class<? extends XmlAdapter> adapter, boolean copy)
  {
    return TypeUseFactory.adapt(this, adapter, copy);
  }
  
  public final TypeUse makeMimeTyped(MimeType mt)
  {
    return TypeUseFactory.makeMimeTyped(this, mt);
  }
  
  private static abstract class Builtin
    extends CBuiltinLeafInfo
  {
    protected Builtin(Class c, String typeName)
    {
      this(c, typeName, ID.NONE);
    }
    
    protected Builtin(Class c, String typeName, ID id)
    {
      super(new QName("http://www.w3.org/2001/XMLSchema", typeName), id, null);
      LEAVES.put(getType(), this);
    }
    
    public CCustomizations getCustomizations()
    {
      return CCustomizations.EMPTY;
    }
  }
  
  private static final class NoConstantBuiltin
    extends CBuiltinLeafInfo.Builtin
  {
    public NoConstantBuiltin(Class c, String typeName)
    {
      super(typeName);
    }
    
    public JExpression createConstant(Outline outline, XmlString lexical)
    {
      return null;
    }
  }
  
  public static final Map<NType, CBuiltinLeafInfo> LEAVES = new HashMap();
  public static final CBuiltinLeafInfo ANYTYPE = new NoConstantBuiltin(Object.class, "anyType");
  public static final CBuiltinLeafInfo STRING = new Builtin(String.class, "string")
  {
    public JExpression createConstant(Outline outline, XmlString lexical)
    {
      return JExpr.lit(lexical.value);
    }
  };
  public static final CBuiltinLeafInfo BOOLEAN = new Builtin(Boolean.class, "boolean")
  {
    public JExpression createConstant(Outline outline, XmlString lexical)
    {
      return JExpr.lit(DatatypeConverterImpl._parseBoolean(lexical.value));
    }
  };
  public static final CBuiltinLeafInfo INT = new Builtin(Integer.class, "int")
  {
    public JExpression createConstant(Outline outline, XmlString lexical)
    {
      return JExpr.lit(DatatypeConverterImpl._parseInt(lexical.value));
    }
  };
  public static final CBuiltinLeafInfo LONG = new Builtin(Long.class, "long")
  {
    public JExpression createConstant(Outline outline, XmlString lexical)
    {
      return JExpr.lit(DatatypeConverterImpl._parseLong(lexical.value));
    }
  };
  public static final CBuiltinLeafInfo BYTE = new Builtin(Byte.class, "byte")
  {
    public JExpression createConstant(Outline outline, XmlString lexical)
    {
      return JExpr.cast(outline.getCodeModel().BYTE, JExpr.lit(DatatypeConverterImpl._parseByte(lexical.value)));
    }
  };
  public static final CBuiltinLeafInfo SHORT = new Builtin(Short.class, "short")
  {
    public JExpression createConstant(Outline outline, XmlString lexical)
    {
      return JExpr.cast(outline.getCodeModel().SHORT, JExpr.lit(DatatypeConverterImpl._parseShort(lexical.value)));
    }
  };
  public static final CBuiltinLeafInfo FLOAT = new Builtin(Float.class, "float")
  {
    public JExpression createConstant(Outline outline, XmlString lexical)
    {
      return JExpr.lit(DatatypeConverterImpl._parseFloat(lexical.value));
    }
  };
  public static final CBuiltinLeafInfo DOUBLE = new Builtin(Double.class, "double")
  {
    public JExpression createConstant(Outline outline, XmlString lexical)
    {
      return JExpr.lit(DatatypeConverterImpl._parseDouble(lexical.value));
    }
  };
  public static final CBuiltinLeafInfo QNAME = new Builtin(QName.class, "QName")
  {
    public JExpression createConstant(Outline outline, XmlString lexical)
    {
      QName qn = DatatypeConverterImpl._parseQName(lexical.value, new NamespaceContextAdapter(lexical));
      return JExpr._new(outline.getCodeModel().ref(QName.class)).arg(qn.getNamespaceURI()).arg(qn.getLocalPart()).arg(qn.getPrefix());
    }
  };
  public static final CBuiltinLeafInfo CALENDAR = new NoConstantBuiltin(XMLGregorianCalendar.class, "\000");
  public static final CBuiltinLeafInfo DURATION = new NoConstantBuiltin(Duration.class, "duration");
  public static final CBuiltinLeafInfo BIG_INTEGER = new Builtin(BigInteger.class, "integer")
  {
    public JExpression createConstant(Outline outline, XmlString lexical)
    {
      return JExpr._new(outline.getCodeModel().ref(BigInteger.class)).arg(lexical.value.trim());
    }
  };
  public static final CBuiltinLeafInfo BIG_DECIMAL = new Builtin(BigDecimal.class, "decimal")
  {
    public JExpression createConstant(Outline outline, XmlString lexical)
    {
      return JExpr._new(outline.getCodeModel().ref(BigDecimal.class)).arg(lexical.value.trim());
    }
  };
  public static final CBuiltinLeafInfo BASE64_BYTE_ARRAY = new Builtin(byte[].class, "base64Binary")
  {
    public JExpression createConstant(Outline outline, XmlString lexical)
    {
      return outline.getCodeModel().ref(DatatypeConverter.class).staticInvoke("parseBase64Binary").arg(lexical.value);
    }
  };
  public static final CBuiltinLeafInfo DATA_HANDLER = new NoConstantBuiltin(DataHandler.class, "base64Binary");
  public static final CBuiltinLeafInfo IMAGE = new NoConstantBuiltin(Image.class, "base64Binary");
  public static final CBuiltinLeafInfo XML_SOURCE = new NoConstantBuiltin(Source.class, "base64Binary");
  public static final TypeUse HEXBIN_BYTE_ARRAY = STRING.makeAdapted(HexBinaryAdapter.class, false);
  public static final TypeUse TOKEN = STRING.makeAdapted(CollapsedStringAdapter.class, false);
  public static final TypeUse NORMALIZED_STRING = STRING.makeAdapted(NormalizedStringAdapter.class, false);
  public static final TypeUse ID = TypeUseFactory.makeID(TOKEN, ID.ID);
  public static final TypeUse BOOLEAN_ZERO_OR_ONE = STRING.makeAdapted(ZeroOneBooleanAdapter.class, true);
  public static final TypeUse IDREF = TypeUseFactory.makeID(ANYTYPE, ID.IDREF);
  public static final TypeUse STRING_LIST = STRING.makeCollection();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CBuiltinLeafInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */