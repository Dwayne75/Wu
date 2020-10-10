package org.fourthline.cling.model.types;

import java.net.URI;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract interface Datatype<V>
{
  public abstract boolean isHandlingJavaType(Class paramClass);
  
  public abstract Builtin getBuiltin();
  
  public abstract boolean isValid(V paramV);
  
  public abstract String getString(V paramV)
    throws InvalidValueException;
  
  public abstract V valueOf(String paramString)
    throws InvalidValueException;
  
  public abstract String getDisplayString();
  
  public static enum Default
  {
    BOOLEAN(Boolean.class, Datatype.Builtin.BOOLEAN),  BOOLEAN_PRIMITIVE(Boolean.TYPE, Datatype.Builtin.BOOLEAN),  SHORT(Short.class, Datatype.Builtin.I2_SHORT),  SHORT_PRIMITIVE(Short.TYPE, Datatype.Builtin.I2_SHORT),  INTEGER(Integer.class, Datatype.Builtin.I4),  INTEGER_PRIMITIVE(Integer.TYPE, Datatype.Builtin.I4),  UNSIGNED_INTEGER_ONE_BYTE(UnsignedIntegerOneByte.class, Datatype.Builtin.UI1),  UNSIGNED_INTEGER_TWO_BYTES(UnsignedIntegerTwoBytes.class, Datatype.Builtin.UI2),  UNSIGNED_INTEGER_FOUR_BYTES(UnsignedIntegerFourBytes.class, Datatype.Builtin.UI4),  FLOAT(Float.class, Datatype.Builtin.R4),  FLOAT_PRIMITIVE(Float.TYPE, Datatype.Builtin.R4),  DOUBLE(Double.class, Datatype.Builtin.FLOAT),  DOUBLE_PRIMTIIVE(Double.TYPE, Datatype.Builtin.FLOAT),  CHAR(Character.class, Datatype.Builtin.CHAR),  CHAR_PRIMITIVE(Character.TYPE, Datatype.Builtin.CHAR),  STRING(String.class, Datatype.Builtin.STRING),  CALENDAR(Calendar.class, Datatype.Builtin.DATETIME),  BYTES(byte[].class, Datatype.Builtin.BIN_BASE64),  URI(URI.class, Datatype.Builtin.URI);
    
    private Class javaType;
    private Datatype.Builtin builtinType;
    
    private Default(Class javaType, Datatype.Builtin builtinType)
    {
      this.javaType = javaType;
      this.builtinType = builtinType;
    }
    
    public Class getJavaType()
    {
      return this.javaType;
    }
    
    public Datatype.Builtin getBuiltinType()
    {
      return this.builtinType;
    }
    
    public static Default getByJavaType(Class javaType)
    {
      for (Default d : ) {
        if (d.getJavaType().equals(javaType)) {
          return d;
        }
      }
      return null;
    }
    
    public String toString()
    {
      return getJavaType() + " => " + getBuiltinType();
    }
  }
  
  public static enum Builtin
  {
    UI1("ui1", new UnsignedIntegerOneByteDatatype()),  UI2("ui2", new UnsignedIntegerTwoBytesDatatype()),  UI4("ui4", new UnsignedIntegerFourBytesDatatype()),  I1("i1", new IntegerDatatype(1)),  I2("i2", new IntegerDatatype(2)),  I2_SHORT("i2", new ShortDatatype()),  I4("i4", new IntegerDatatype(4)),  INT("int", new IntegerDatatype(4)),  R4("r4", new FloatDatatype()),  R8("r8", new DoubleDatatype()),  NUMBER("number", new DoubleDatatype()),  FIXED144("fixed.14.4", new DoubleDatatype()),  FLOAT("float", new DoubleDatatype()),  CHAR("char", new CharacterDatatype()),  STRING("string", new StringDatatype()),  DATE("date", new DateTimeDatatype(new String[] { "yyyy-MM-dd" }, "yyyy-MM-dd")),  DATETIME("dateTime", new DateTimeDatatype(new String[] { "yyyy-MM-dd", "yyyy-MM-dd'T'HH:mm:ss" }, "yyyy-MM-dd'T'HH:mm:ss")),  DATETIME_TZ("dateTime.tz", new DateTimeDatatype(new String[] { "yyyy-MM-dd", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ssZ" }, "yyyy-MM-dd'T'HH:mm:ssZ")),  TIME("time", new DateTimeDatatype(new String[] { "HH:mm:ss" }, "HH:mm:ss")),  TIME_TZ("time.tz", new DateTimeDatatype(new String[] { "HH:mm:ssZ", "HH:mm:ss" }, "HH:mm:ssZ")),  BOOLEAN("boolean", new BooleanDatatype()),  BIN_BASE64("bin.base64", new Base64Datatype()),  BIN_HEX("bin.hex", new BinHexDatatype()),  URI("uri", new URIDatatype()),  UUID("uuid", new StringDatatype());
    
    private static Map<String, Builtin> byName = new HashMap() {};
    private String descriptorName;
    private Datatype datatype;
    
    private <VT> Builtin(String descriptorName, AbstractDatatype<VT> datatype)
    {
      datatype.setBuiltin(this);
      this.descriptorName = descriptorName;
      this.datatype = datatype;
    }
    
    public String getDescriptorName()
    {
      return this.descriptorName;
    }
    
    public Datatype getDatatype()
    {
      return this.datatype;
    }
    
    public static Builtin getByDescriptorName(String descriptorName)
    {
      if (descriptorName == null) {
        return null;
      }
      return (Builtin)byName.get(descriptorName.toLowerCase(Locale.ROOT));
    }
    
    public static boolean isNumeric(Builtin builtin)
    {
      return (builtin != null) && ((builtin.equals(UI1)) || (builtin.equals(UI2)) || (builtin.equals(UI4)) || (builtin.equals(I1)) || (builtin.equals(I2)) || (builtin.equals(I4)) || (builtin.equals(INT)));
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\Datatype.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */