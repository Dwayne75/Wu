package org.fourthline.cling.model;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;

public class ModelUtil
{
  public static final boolean ANDROID_RUNTIME;
  public static final boolean ANDROID_EMULATOR;
  
  static
  {
    boolean foundAndroid = false;
    try
    {
      Class androidBuild = Thread.currentThread().getContextClassLoader().loadClass("android.os.Build");
      foundAndroid = androidBuild.getField("ID").get(null) != null;
    }
    catch (Exception localException) {}
    ANDROID_RUNTIME = foundAndroid;
    
    boolean foundEmulator = false;
    try
    {
      Class androidBuild = Thread.currentThread().getContextClassLoader().loadClass("android.os.Build");
      String product = (String)androidBuild.getField("PRODUCT").get(null);
      if (("google_sdk".equals(product)) || ("sdk".equals(product))) {
        foundEmulator = true;
      }
    }
    catch (Exception localException1) {}
    ANDROID_EMULATOR = foundEmulator;
  }
  
  public static boolean isStringConvertibleType(Set<Class> stringConvertibleTypes, Class clazz)
  {
    if (clazz.isEnum()) {
      return true;
    }
    for (Class toStringOutputType : stringConvertibleTypes) {
      if (toStringOutputType.isAssignableFrom(clazz)) {
        return true;
      }
    }
    return false;
  }
  
  public static boolean isValidUDAName(String name)
  {
    if (ANDROID_RUNTIME) {
      return (name != null) && (name.length() != 0);
    }
    return (name != null) && (name.length() != 0) && (!name.toLowerCase(Locale.ROOT).startsWith("xml")) && (name.matches("[a-zA-Z0-9^-_\\p{L}\\p{N}]{1}[a-zA-Z0-9^-_\\.\\\\p{L}\\\\p{N}\\p{Mc}\\p{Sk}]*"));
  }
  
  public static InetAddress getInetAddressByName(String name)
  {
    try
    {
      return InetAddress.getByName(name);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public static String toCommaSeparatedList(Object[] o)
  {
    return toCommaSeparatedList(o, true, false);
  }
  
  public static String toCommaSeparatedList(Object[] o, boolean escapeCommas, boolean escapeDoubleQuotes)
  {
    if (o == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (Object obj : o)
    {
      String objString = obj.toString();
      objString = objString.replaceAll("\\\\", "\\\\\\\\");
      if (escapeCommas) {
        objString = objString.replaceAll(",", "\\\\,");
      }
      if (escapeDoubleQuotes) {
        objString = objString.replaceAll("\"", "\\\"");
      }
      sb.append(objString).append(",");
    }
    if (sb.length() > 1) {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }
  
  public static String[] fromCommaSeparatedList(String s)
  {
    return fromCommaSeparatedList(s, true);
  }
  
  public static String[] fromCommaSeparatedList(String s, boolean unescapeCommas)
  {
    if ((s == null) || (s.length() == 0)) {
      return null;
    }
    String QUOTED_COMMA_PLACEHOLDER = "XXX1122334455XXX";
    if (unescapeCommas) {
      s = s.replaceAll("\\\\,", "XXX1122334455XXX");
    }
    String[] split = s.split(",");
    for (int i = 0; i < split.length; i++)
    {
      split[i] = split[i].replaceAll("XXX1122334455XXX", ",");
      split[i] = split[i].replaceAll("\\\\\\\\", "\\\\");
    }
    return split;
  }
  
  public static String toTimeString(long seconds)
  {
    long hours = seconds / 3600L;
    long remainder = seconds % 3600L;
    long minutes = remainder / 60L;
    long secs = remainder % 60L;
    
    return (hours < 10L ? "0" : "") + hours + ":" + (minutes < 10L ? "0" : "") + minutes + ":" + (secs < 10L ? "0" : "") + secs;
  }
  
  public static long fromTimeString(String s)
  {
    if (s.lastIndexOf(".") != -1) {
      s = s.substring(0, s.lastIndexOf("."));
    }
    String[] split = s.split(":");
    if (split.length != 3) {
      throw new IllegalArgumentException("Can't parse time string: " + s);
    }
    return Long.parseLong(split[0]) * 3600L + Long.parseLong(split[1]) * 60L + Long.parseLong(split[2]);
  }
  
  public static String commaToNewline(String s)
  {
    StringBuilder sb = new StringBuilder();
    String[] split = s.split(",");
    for (String splitString : split) {
      sb.append(splitString).append(",").append("\n");
    }
    if (sb.length() > 2) {
      sb.deleteCharAt(sb.length() - 2);
    }
    return sb.toString();
  }
  
  public static String getLocalHostName(boolean includeDomain)
  {
    try
    {
      String hostname = InetAddress.getLocalHost().getHostName();
      
      return hostname.indexOf(".") != -1 ? hostname.substring(0, hostname.indexOf(".")) : includeDomain ? hostname : hostname;
    }
    catch (Exception ex) {}
    return "UNKNOWN HOST";
  }
  
  public static byte[] getFirstNetworkInterfaceHardwareAddress()
  {
    try
    {
      Enumeration<NetworkInterface> interfaceEnumeration = NetworkInterface.getNetworkInterfaces();
      for (NetworkInterface iface : Collections.list(interfaceEnumeration)) {
        if ((!iface.isLoopback()) && (iface.isUp()) && (iface.getHardwareAddress() != null)) {
          return iface.getHardwareAddress();
        }
      }
    }
    catch (Exception ex)
    {
      throw new RuntimeException("Could not discover first network interface hardware address");
    }
    throw new RuntimeException("Could not discover first network interface hardware address");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\ModelUtil.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */