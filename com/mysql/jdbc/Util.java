package com.mysql.jdbc;

import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

public class Util
{
  protected static Method systemNanoTimeMethod;
  private static Method CAST_METHOD;
  private static final TimeZone DEFAULT_TIMEZONE;
  private static Util enclosingInstance;
  private static boolean isJdbc4;
  private static boolean isColdFusion;
  
  public static boolean nanoTimeAvailable()
  {
    return systemNanoTimeMethod != null;
  }
  
  static final TimeZone getDefaultTimeZone()
  {
    return (TimeZone)DEFAULT_TIMEZONE.clone();
  }
  
  static
  {
    try
    {
      systemNanoTimeMethod = System.class.getMethod("nanoTime", null);
    }
    catch (SecurityException e)
    {
      systemNanoTimeMethod = null;
    }
    catch (NoSuchMethodException e)
    {
      systemNanoTimeMethod = null;
    }
    DEFAULT_TIMEZONE = TimeZone.getDefault();
    
    enclosingInstance = new Util();
    
    isJdbc4 = false;
    
    isColdFusion = false;
    try
    {
      CAST_METHOD = class$java$lang$Class.getMethod("cast", new Class[] { Object.class });
    }
    catch (Throwable t) {}
    try
    {
      Class.forName("java.sql.NClob");
      isJdbc4 = true;
    }
    catch (Throwable t)
    {
      isJdbc4 = false;
    }
    String loadedFrom = stackTraceToString(new Throwable());
    if (loadedFrom != null) {
      isColdFusion = loadedFrom.indexOf("coldfusion") != -1;
    } else {
      isColdFusion = false;
    }
  }
  
  public static boolean isJdbc4()
  {
    return isJdbc4;
  }
  
  public static boolean isColdFusion()
  {
    return isColdFusion;
  }
  
  static String newCrypt(String password, String seed)
  {
    if ((password == null) || (password.length() == 0)) {
      return password;
    }
    long[] pw = newHash(seed);
    long[] msg = newHash(password);
    long max = 1073741823L;
    long seed1 = (pw[0] ^ msg[0]) % max;
    long seed2 = (pw[1] ^ msg[1]) % max;
    char[] chars = new char[seed.length()];
    for (int i = 0; i < seed.length(); i++)
    {
      seed1 = (seed1 * 3L + seed2) % max;
      seed2 = (seed1 + seed2 + 33L) % max;
      double d = seed1 / max;
      byte b = (byte)(int)Math.floor(d * 31.0D + 64.0D);
      chars[i] = ((char)b);
    }
    seed1 = (seed1 * 3L + seed2) % max;
    seed2 = (seed1 + seed2 + 33L) % max;
    double d = seed1 / max;
    byte b = (byte)(int)Math.floor(d * 31.0D);
    for (int i = 0; i < seed.length(); tmp205_203++)
    {
      int tmp205_203 = i; char[] tmp205_201 = chars;tmp205_201[tmp205_203] = ((char)(tmp205_201[tmp205_203] ^ (char)b));
    }
    return new String(chars);
  }
  
  static long[] newHash(String password)
  {
    long nr = 1345345333L;
    long add = 7L;
    long nr2 = 305419889L;
    for (int i = 0; i < password.length(); i++) {
      if ((password.charAt(i) != ' ') && (password.charAt(i) != '\t'))
      {
        long tmp = 0xFF & password.charAt(i);
        nr ^= ((nr & 0x3F) + add) * tmp + (nr << 8);
        nr2 += (nr2 << 8 ^ nr);
        add += tmp;
      }
    }
    long[] result = new long[2];
    result[0] = (nr & 0x7FFFFFFF);
    result[1] = (nr2 & 0x7FFFFFFF);
    
    return result;
  }
  
  static String oldCrypt(String password, String seed)
  {
    long max = 33554431L;
    if ((password == null) || (password.length() == 0)) {
      return password;
    }
    long hp = oldHash(seed);
    long hm = oldHash(password);
    
    long nr = hp ^ hm;
    nr %= max;
    long s1 = nr;
    long s2 = nr / 2L;
    
    char[] chars = new char[seed.length()];
    for (int i = 0; i < seed.length(); i++)
    {
      s1 = (s1 * 3L + s2) % max;
      s2 = (s1 + s2 + 33L) % max;
      double d = s1 / max;
      byte b = (byte)(int)Math.floor(d * 31.0D + 64.0D);
      chars[i] = ((char)b);
    }
    return new String(chars);
  }
  
  static long oldHash(String password)
  {
    long nr = 1345345333L;
    long nr2 = 7L;
    for (int i = 0; i < password.length(); i++) {
      if ((password.charAt(i) != ' ') && (password.charAt(i) != '\t'))
      {
        long tmp = password.charAt(i);
        nr ^= ((nr & 0x3F) + nr2) * tmp + (nr << 8);
        nr2 += tmp;
      }
    }
    return nr & 0x7FFFFFFF;
  }
  
  private static RandStructcture randomInit(long seed1, long seed2)
  {
    Util tmp7_4 = enclosingInstance;tmp7_4.getClass();RandStructcture randStruct = new RandStructcture(tmp7_4);
    
    randStruct.maxValue = 1073741823L;
    randStruct.maxValueDbl = randStruct.maxValue;
    randStruct.seed1 = (seed1 % randStruct.maxValue);
    randStruct.seed2 = (seed2 % randStruct.maxValue);
    
    return randStruct;
  }
  
  public static Object readObject(ResultSet resultSet, int index)
    throws Exception
  {
    ObjectInputStream objIn = new ObjectInputStream(resultSet.getBinaryStream(index));
    
    Object obj = objIn.readObject();
    objIn.close();
    
    return obj;
  }
  
  private static double rnd(RandStructcture randStruct)
  {
    randStruct.seed1 = ((randStruct.seed1 * 3L + randStruct.seed2) % randStruct.maxValue);
    
    randStruct.seed2 = ((randStruct.seed1 + randStruct.seed2 + 33L) % randStruct.maxValue);
    
    return randStruct.seed1 / randStruct.maxValueDbl;
  }
  
  public static String scramble(String message, String password)
  {
    byte[] to = new byte[8];
    String val = "";
    
    message = message.substring(0, 8);
    if ((password != null) && (password.length() > 0))
    {
      long[] hashPass = newHash(password);
      long[] hashMessage = newHash(message);
      
      RandStructcture randStruct = randomInit(hashPass[0] ^ hashMessage[0], hashPass[1] ^ hashMessage[1]);
      
      int msgPos = 0;
      int msgLength = message.length();
      int toPos = 0;
      while (msgPos++ < msgLength) {
        to[(toPos++)] = ((byte)(int)(Math.floor(rnd(randStruct) * 31.0D) + 64.0D));
      }
      byte extra = (byte)(int)Math.floor(rnd(randStruct) * 31.0D);
      for (int i = 0; i < to.length; i++)
      {
        int tmp140_138 = i; byte[] tmp140_136 = to;tmp140_136[tmp140_138] = ((byte)(tmp140_136[tmp140_138] ^ extra));
      }
      val = new String(to);
    }
    return val;
  }
  
  public static String stackTraceToString(Throwable ex)
  {
    StringBuffer traceBuf = new StringBuffer();
    traceBuf.append(Messages.getString("Util.1"));
    if (ex != null)
    {
      traceBuf.append(ex.getClass().getName());
      
      String message = ex.getMessage();
      if (message != null)
      {
        traceBuf.append(Messages.getString("Util.2"));
        traceBuf.append(message);
      }
      StringWriter out = new StringWriter();
      
      PrintWriter printOut = new PrintWriter(out);
      
      ex.printStackTrace(printOut);
      
      traceBuf.append(Messages.getString("Util.3"));
      traceBuf.append(out.toString());
    }
    traceBuf.append(Messages.getString("Util.4"));
    
    return traceBuf.toString();
  }
  
  public static Object getInstance(String className, Class[] argTypes, Object[] args, ExceptionInterceptor exceptionInterceptor)
    throws SQLException
  {
    try
    {
      return handleNewInstance(Class.forName(className).getConstructor(argTypes), args, exceptionInterceptor);
    }
    catch (SecurityException e)
    {
      throw SQLError.createSQLException("Can't instantiate required class", "S1000", e, exceptionInterceptor);
    }
    catch (NoSuchMethodException e)
    {
      throw SQLError.createSQLException("Can't instantiate required class", "S1000", e, exceptionInterceptor);
    }
    catch (ClassNotFoundException e)
    {
      throw SQLError.createSQLException("Can't instantiate required class", "S1000", e, exceptionInterceptor);
    }
  }
  
  public static final Object handleNewInstance(Constructor ctor, Object[] args, ExceptionInterceptor exceptionInterceptor)
    throws SQLException
  {
    try
    {
      return ctor.newInstance(args);
    }
    catch (IllegalArgumentException e)
    {
      throw SQLError.createSQLException("Can't instantiate required class", "S1000", e, exceptionInterceptor);
    }
    catch (InstantiationException e)
    {
      throw SQLError.createSQLException("Can't instantiate required class", "S1000", e, exceptionInterceptor);
    }
    catch (IllegalAccessException e)
    {
      throw SQLError.createSQLException("Can't instantiate required class", "S1000", e, exceptionInterceptor);
    }
    catch (InvocationTargetException e)
    {
      Throwable target = e.getTargetException();
      if ((target instanceof SQLException)) {
        throw ((SQLException)target);
      }
      if ((target instanceof ExceptionInInitializerError)) {
        target = ((ExceptionInInitializerError)target).getException();
      }
      throw SQLError.createSQLException(target.toString(), "S1000", exceptionInterceptor);
    }
  }
  
  public static boolean interfaceExists(String hostname)
  {
    try
    {
      Class networkInterfaceClass = Class.forName("java.net.NetworkInterface");
      
      return networkInterfaceClass.getMethod("getByName", null).invoke(networkInterfaceClass, new Object[] { hostname }) != null;
    }
    catch (Throwable t) {}
    return false;
  }
  
  public static Object cast(Object invokeOn, Object toCast)
  {
    if (CAST_METHOD != null) {
      try
      {
        return CAST_METHOD.invoke(invokeOn, new Object[] { toCast });
      }
      catch (Throwable t)
      {
        return null;
      }
    }
    return null;
  }
  
  public static long getCurrentTimeNanosOrMillis()
  {
    if (systemNanoTimeMethod != null) {
      try
      {
        return ((Long)systemNanoTimeMethod.invoke(null, null)).longValue();
      }
      catch (IllegalArgumentException e) {}catch (IllegalAccessException e) {}catch (InvocationTargetException e) {}
    }
    return System.currentTimeMillis();
  }
  
  public static void resultSetToMap(Map mappedValues, ResultSet rs)
    throws SQLException
  {
    while (rs.next()) {
      mappedValues.put(rs.getObject(1), rs.getObject(2));
    }
  }
  
  public static Map calculateDifferences(Map map1, Map map2)
  {
    Map diffMap = new HashMap();
    
    Iterator map1Entries = map1.entrySet().iterator();
    while (map1Entries.hasNext())
    {
      Map.Entry entry = (Map.Entry)map1Entries.next();
      Object key = entry.getKey();
      
      Number value1 = null;
      Number value2 = null;
      if ((entry.getValue() instanceof Number))
      {
        value1 = (Number)entry.getValue();
        value2 = (Number)map2.get(key);
      }
      else
      {
        try
        {
          value1 = new Double(entry.getValue().toString());
          value2 = new Double(map2.get(key).toString());
        }
        catch (NumberFormatException nfe) {}
        continue;
      }
      if (!value1.equals(value2)) {
        if ((value1 instanceof Byte)) {
          diffMap.put(key, new Byte((byte)(((Byte)value2).byteValue() - ((Byte)value1).byteValue())));
        } else if ((value1 instanceof Short)) {
          diffMap.put(key, new Short((short)(((Short)value2).shortValue() - ((Short)value1).shortValue())));
        } else if ((value1 instanceof Integer)) {
          diffMap.put(key, new Integer(((Integer)value2).intValue() - ((Integer)value1).intValue()));
        } else if ((value1 instanceof Long)) {
          diffMap.put(key, new Long(((Long)value2).longValue() - ((Long)value1).longValue()));
        } else if ((value1 instanceof Float)) {
          diffMap.put(key, new Float(((Float)value2).floatValue() - ((Float)value1).floatValue()));
        } else if ((value1 instanceof Double)) {
          diffMap.put(key, new Double(((Double)value2).shortValue() - ((Double)value1).shortValue()));
        } else if ((value1 instanceof BigDecimal)) {
          diffMap.put(key, ((BigDecimal)value2).subtract((BigDecimal)value1));
        } else if ((value1 instanceof BigInteger)) {
          diffMap.put(key, ((BigInteger)value2).subtract((BigInteger)value1));
        }
      }
    }
    return diffMap;
  }
  
  public static List loadExtensions(Connection conn, Properties props, String extensionClassNames, String errorMessageKey, ExceptionInterceptor exceptionInterceptor)
    throws SQLException
  {
    List extensionList = new LinkedList();
    
    List interceptorsToCreate = StringUtils.split(extensionClassNames, ",", true);
    
    Iterator iter = interceptorsToCreate.iterator();
    
    String className = null;
    try
    {
      while (iter.hasNext())
      {
        className = iter.next().toString();
        Extension extensionInstance = (Extension)Class.forName(className).newInstance();
        
        extensionInstance.init(conn, props);
        
        extensionList.add(extensionInstance);
      }
    }
    catch (Throwable t)
    {
      SQLException sqlEx = SQLError.createSQLException(Messages.getString(errorMessageKey, new Object[] { className }), exceptionInterceptor);
      
      sqlEx.initCause(t);
      
      throw sqlEx;
    }
    return extensionList;
  }
  
  class RandStructcture
  {
    long maxValue;
    double maxValueDbl;
    long seed1;
    long seed2;
    
    RandStructcture() {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\Util.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */