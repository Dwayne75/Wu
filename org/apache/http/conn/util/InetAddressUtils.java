package org.apache.http.conn.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.annotation.Immutable;

@Immutable
public class InetAddressUtils
{
  private static final Pattern IPV4_PATTERN = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
  private static final Pattern IPV6_STD_PATTERN = Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
  private static final Pattern IPV6_HEX_COMPRESSED_PATTERN = Pattern.compile("^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");
  private static final char COLON_CHAR = ':';
  private static final int MAX_COLON_COUNT = 7;
  
  public static boolean isIPv4Address(String input)
  {
    return IPV4_PATTERN.matcher(input).matches();
  }
  
  public static boolean isIPv6StdAddress(String input)
  {
    return IPV6_STD_PATTERN.matcher(input).matches();
  }
  
  public static boolean isIPv6HexCompressedAddress(String input)
  {
    int colonCount = 0;
    for (int i = 0; i < input.length(); i++) {
      if (input.charAt(i) == ':') {
        colonCount++;
      }
    }
    return (colonCount <= 7) && (IPV6_HEX_COMPRESSED_PATTERN.matcher(input).matches());
  }
  
  public static boolean isIPv6Address(String input)
  {
    return (isIPv6StdAddress(input)) || (isIPv6HexCompressedAddress(input));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\conn\util\InetAddressUtils.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */