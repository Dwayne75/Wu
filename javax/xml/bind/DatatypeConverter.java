package javax.xml.bind;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

public final class DatatypeConverter
{
  private static DatatypeConverterInterface theConverter = new DatatypeConverterImpl();
  
  public static void setDatatypeConverter(DatatypeConverterInterface converter)
  {
    if (converter == null) {
      throw new IllegalArgumentException(Messages.format("DatatypeConverter.ConverterMustNotBeNull"));
    }
    if (theConverter == null) {
      theConverter = converter;
    }
  }
  
  public static String parseString(String lexicalXSDString)
  {
    return theConverter.parseString(lexicalXSDString);
  }
  
  public static BigInteger parseInteger(String lexicalXSDInteger)
  {
    return theConverter.parseInteger(lexicalXSDInteger);
  }
  
  public static int parseInt(String lexicalXSDInt)
  {
    return theConverter.parseInt(lexicalXSDInt);
  }
  
  public static long parseLong(String lexicalXSDLong)
  {
    return theConverter.parseLong(lexicalXSDLong);
  }
  
  public static short parseShort(String lexicalXSDShort)
  {
    return theConverter.parseShort(lexicalXSDShort);
  }
  
  public static BigDecimal parseDecimal(String lexicalXSDDecimal)
  {
    return theConverter.parseDecimal(lexicalXSDDecimal);
  }
  
  public static float parseFloat(String lexicalXSDFloat)
  {
    return theConverter.parseFloat(lexicalXSDFloat);
  }
  
  public static double parseDouble(String lexicalXSDDouble)
  {
    return theConverter.parseDouble(lexicalXSDDouble);
  }
  
  public static boolean parseBoolean(String lexicalXSDBoolean)
  {
    return theConverter.parseBoolean(lexicalXSDBoolean);
  }
  
  public static byte parseByte(String lexicalXSDByte)
  {
    return theConverter.parseByte(lexicalXSDByte);
  }
  
  public static QName parseQName(String lexicalXSDQName, NamespaceContext nsc)
  {
    return theConverter.parseQName(lexicalXSDQName, nsc);
  }
  
  public static Calendar parseDateTime(String lexicalXSDDateTime)
  {
    return theConverter.parseDateTime(lexicalXSDDateTime);
  }
  
  public static byte[] parseBase64Binary(String lexicalXSDBase64Binary)
  {
    return theConverter.parseBase64Binary(lexicalXSDBase64Binary);
  }
  
  public static byte[] parseHexBinary(String lexicalXSDHexBinary)
  {
    return theConverter.parseHexBinary(lexicalXSDHexBinary);
  }
  
  public static long parseUnsignedInt(String lexicalXSDUnsignedInt)
  {
    return theConverter.parseUnsignedInt(lexicalXSDUnsignedInt);
  }
  
  public static int parseUnsignedShort(String lexicalXSDUnsignedShort)
  {
    return theConverter.parseUnsignedShort(lexicalXSDUnsignedShort);
  }
  
  public static Calendar parseTime(String lexicalXSDTime)
  {
    return theConverter.parseTime(lexicalXSDTime);
  }
  
  public static Calendar parseDate(String lexicalXSDDate)
  {
    return theConverter.parseDate(lexicalXSDDate);
  }
  
  public static String parseAnySimpleType(String lexicalXSDAnySimpleType)
  {
    return theConverter.parseAnySimpleType(lexicalXSDAnySimpleType);
  }
  
  public static String printString(String val)
  {
    return theConverter.printString(val);
  }
  
  public static String printInteger(BigInteger val)
  {
    return theConverter.printInteger(val);
  }
  
  public static String printInt(int val)
  {
    return theConverter.printInt(val);
  }
  
  public static String printLong(long val)
  {
    return theConverter.printLong(val);
  }
  
  public static String printShort(short val)
  {
    return theConverter.printShort(val);
  }
  
  public static String printDecimal(BigDecimal val)
  {
    return theConverter.printDecimal(val);
  }
  
  public static String printFloat(float val)
  {
    return theConverter.printFloat(val);
  }
  
  public static String printDouble(double val)
  {
    return theConverter.printDouble(val);
  }
  
  public static String printBoolean(boolean val)
  {
    return theConverter.printBoolean(val);
  }
  
  public static String printByte(byte val)
  {
    return theConverter.printByte(val);
  }
  
  public static String printQName(QName val, NamespaceContext nsc)
  {
    return theConverter.printQName(val, nsc);
  }
  
  public static String printDateTime(Calendar val)
  {
    return theConverter.printDateTime(val);
  }
  
  public static String printBase64Binary(byte[] val)
  {
    return theConverter.printBase64Binary(val);
  }
  
  public static String printHexBinary(byte[] val)
  {
    return theConverter.printHexBinary(val);
  }
  
  public static String printUnsignedInt(long val)
  {
    return theConverter.printUnsignedInt(val);
  }
  
  public static String printUnsignedShort(int val)
  {
    return theConverter.printUnsignedShort(val);
  }
  
  public static String printTime(Calendar val)
  {
    return theConverter.printTime(val);
  }
  
  public static String printDate(Calendar val)
  {
    return theConverter.printDate(val);
  }
  
  public static String printAnySimpleType(String val)
  {
    return theConverter.printAnySimpleType(val);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\DatatypeConverter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */