package com.google.common.net;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Beta
@Immutable
@GwtCompatible
public final class HostAndPort
  implements Serializable
{
  private static final int NO_PORT = -1;
  private final String host;
  private final int port;
  private final boolean hasBracketlessColons;
  private static final long serialVersionUID = 0L;
  
  private HostAndPort(String host, int port, boolean hasBracketlessColons)
  {
    this.host = host;
    this.port = port;
    this.hasBracketlessColons = hasBracketlessColons;
  }
  
  public String getHostText()
  {
    return this.host;
  }
  
  public boolean hasPort()
  {
    return this.port >= 0;
  }
  
  public int getPort()
  {
    Preconditions.checkState(hasPort());
    return this.port;
  }
  
  public int getPortOrDefault(int defaultPort)
  {
    return hasPort() ? this.port : defaultPort;
  }
  
  public static HostAndPort fromParts(String host, int port)
  {
    Preconditions.checkArgument(isValidPort(port), "Port out of range: %s", new Object[] { Integer.valueOf(port) });
    HostAndPort parsedHost = fromString(host);
    Preconditions.checkArgument(!parsedHost.hasPort(), "Host has a port: %s", new Object[] { host });
    return new HostAndPort(parsedHost.host, port, parsedHost.hasBracketlessColons);
  }
  
  public static HostAndPort fromHost(String host)
  {
    HostAndPort parsedHost = fromString(host);
    Preconditions.checkArgument(!parsedHost.hasPort(), "Host has a port: %s", new Object[] { host });
    return parsedHost;
  }
  
  /* Error */
  public static HostAndPort fromString(String hostPortString)
  {
    // Byte code:
    //   0: aload_0
    //   1: invokestatic 16	com/google/common/base/Preconditions:checkNotNull	(Ljava/lang/Object;)Ljava/lang/Object;
    //   4: pop
    //   5: aconst_null
    //   6: astore_2
    //   7: iconst_0
    //   8: istore_3
    //   9: aload_0
    //   10: ldc 17
    //   12: invokevirtual 18	java/lang/String:startsWith	(Ljava/lang/String;)Z
    //   15: ifeq +22 -> 37
    //   18: aload_0
    //   19: invokestatic 19	com/google/common/net/HostAndPort:getHostAndPortFromBracketedHost	(Ljava/lang/String;)[Ljava/lang/String;
    //   22: astore 4
    //   24: aload 4
    //   26: iconst_0
    //   27: aaload
    //   28: astore_1
    //   29: aload 4
    //   31: iconst_1
    //   32: aaload
    //   33: astore_2
    //   34: goto +63 -> 97
    //   37: aload_0
    //   38: bipush 58
    //   40: invokevirtual 20	java/lang/String:indexOf	(I)I
    //   43: istore 4
    //   45: iload 4
    //   47: iflt +37 -> 84
    //   50: aload_0
    //   51: bipush 58
    //   53: iload 4
    //   55: iconst_1
    //   56: iadd
    //   57: invokevirtual 21	java/lang/String:indexOf	(II)I
    //   60: iconst_m1
    //   61: if_icmpne +23 -> 84
    //   64: aload_0
    //   65: iconst_0
    //   66: iload 4
    //   68: invokevirtual 22	java/lang/String:substring	(II)Ljava/lang/String;
    //   71: astore_1
    //   72: aload_0
    //   73: iload 4
    //   75: iconst_1
    //   76: iadd
    //   77: invokevirtual 23	java/lang/String:substring	(I)Ljava/lang/String;
    //   80: astore_2
    //   81: goto +16 -> 97
    //   84: aload_0
    //   85: astore_1
    //   86: iload 4
    //   88: iflt +7 -> 95
    //   91: iconst_1
    //   92: goto +4 -> 96
    //   95: iconst_0
    //   96: istore_3
    //   97: iconst_m1
    //   98: istore 4
    //   100: aload_2
    //   101: invokestatic 24	com/google/common/base/Strings:isNullOrEmpty	(Ljava/lang/String;)Z
    //   104: ifne +95 -> 199
    //   107: aload_2
    //   108: ldc 25
    //   110: invokevirtual 18	java/lang/String:startsWith	(Ljava/lang/String;)Z
    //   113: ifne +7 -> 120
    //   116: iconst_1
    //   117: goto +4 -> 121
    //   120: iconst_0
    //   121: ldc 26
    //   123: iconst_1
    //   124: anewarray 9	java/lang/Object
    //   127: dup
    //   128: iconst_0
    //   129: aload_0
    //   130: aastore
    //   131: invokestatic 11	com/google/common/base/Preconditions:checkArgument	(ZLjava/lang/String;[Ljava/lang/Object;)V
    //   134: aload_2
    //   135: invokestatic 27	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   138: istore 4
    //   140: goto +41 -> 181
    //   143: astore 5
    //   145: new 29	java/lang/IllegalArgumentException
    //   148: dup
    //   149: ldc 30
    //   151: aload_0
    //   152: invokestatic 31	java/lang/String:valueOf	(Ljava/lang/Object;)Ljava/lang/String;
    //   155: dup
    //   156: invokevirtual 32	java/lang/String:length	()I
    //   159: ifeq +9 -> 168
    //   162: invokevirtual 33	java/lang/String:concat	(Ljava/lang/String;)Ljava/lang/String;
    //   165: goto +12 -> 177
    //   168: pop
    //   169: new 34	java/lang/String
    //   172: dup_x1
    //   173: swap
    //   174: invokespecial 35	java/lang/String:<init>	(Ljava/lang/String;)V
    //   177: invokespecial 36	java/lang/IllegalArgumentException:<init>	(Ljava/lang/String;)V
    //   180: athrow
    //   181: iload 4
    //   183: invokestatic 7	com/google/common/net/HostAndPort:isValidPort	(I)Z
    //   186: ldc 37
    //   188: iconst_1
    //   189: anewarray 9	java/lang/Object
    //   192: dup
    //   193: iconst_0
    //   194: aload_0
    //   195: aastore
    //   196: invokestatic 11	com/google/common/base/Preconditions:checkArgument	(ZLjava/lang/String;[Ljava/lang/Object;)V
    //   199: new 14	com/google/common/net/HostAndPort
    //   202: dup
    //   203: aload_1
    //   204: iload 4
    //   206: iload_3
    //   207: invokespecial 15	com/google/common/net/HostAndPort:<init>	(Ljava/lang/String;IZ)V
    //   210: areturn
    // Line number table:
    //   Java source line #168	-> byte code offset #0
    //   Java source line #170	-> byte code offset #5
    //   Java source line #171	-> byte code offset #7
    //   Java source line #173	-> byte code offset #9
    //   Java source line #174	-> byte code offset #18
    //   Java source line #175	-> byte code offset #24
    //   Java source line #176	-> byte code offset #29
    //   Java source line #177	-> byte code offset #34
    //   Java source line #178	-> byte code offset #37
    //   Java source line #179	-> byte code offset #45
    //   Java source line #181	-> byte code offset #64
    //   Java source line #182	-> byte code offset #72
    //   Java source line #185	-> byte code offset #84
    //   Java source line #186	-> byte code offset #86
    //   Java source line #190	-> byte code offset #97
    //   Java source line #191	-> byte code offset #100
    //   Java source line #194	-> byte code offset #107
    //   Java source line #196	-> byte code offset #134
    //   Java source line #199	-> byte code offset #140
    //   Java source line #197	-> byte code offset #143
    //   Java source line #198	-> byte code offset #145
    //   Java source line #200	-> byte code offset #181
    //   Java source line #203	-> byte code offset #199
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	211	0	hostPortString	String
    //   29	8	1	host	String
    //   72	12	1	host	String
    //   86	125	1	host	String
    //   7	204	2	portString	String
    //   9	202	3	hasBracketlessColons	boolean
    //   24	10	4	hostAndPort	String[]
    //   45	52	4	colonPos	int
    //   100	111	4	port	int
    //   145	36	5	e	NumberFormatException
    // Exception table:
    //   from	to	target	type
    //   134	140	143	java/lang/NumberFormatException
  }
  
  private static String[] getHostAndPortFromBracketedHost(String hostPortString)
  {
    int colonIndex = 0;
    int closeBracketIndex = 0;
    Preconditions.checkArgument(hostPortString.charAt(0) == '[', "Bracketed host-port string must start with a bracket: %s", new Object[] { hostPortString });
    
    colonIndex = hostPortString.indexOf(':');
    closeBracketIndex = hostPortString.lastIndexOf(']');
    Preconditions.checkArgument((colonIndex > -1) && (closeBracketIndex > colonIndex), "Invalid bracketed host/port: %s", new Object[] { hostPortString });
    
    String host = hostPortString.substring(1, closeBracketIndex);
    if (closeBracketIndex + 1 == hostPortString.length()) {
      return new String[] { host, "" };
    }
    Preconditions.checkArgument(hostPortString.charAt(closeBracketIndex + 1) == ':', "Only a colon may follow a close bracket: %s", new Object[] { hostPortString });
    for (int i = closeBracketIndex + 2; i < hostPortString.length(); i++) {
      Preconditions.checkArgument(Character.isDigit(hostPortString.charAt(i)), "Port must be numeric: %s", new Object[] { hostPortString });
    }
    return new String[] { host, hostPortString.substring(closeBracketIndex + 2) };
  }
  
  public HostAndPort withDefaultPort(int defaultPort)
  {
    Preconditions.checkArgument(isValidPort(defaultPort));
    if ((hasPort()) || (this.port == defaultPort)) {
      return this;
    }
    return new HostAndPort(this.host, defaultPort, this.hasBracketlessColons);
  }
  
  public HostAndPort requireBracketsForIPv6()
  {
    Preconditions.checkArgument(!this.hasBracketlessColons, "Possible bracketless IPv6 literal: %s", new Object[] { this.host });
    return this;
  }
  
  public boolean equals(@Nullable Object other)
  {
    if (this == other) {
      return true;
    }
    if ((other instanceof HostAndPort))
    {
      HostAndPort that = (HostAndPort)other;
      return (Objects.equal(this.host, that.host)) && (this.port == that.port) && (this.hasBracketlessColons == that.hasBracketlessColons);
    }
    return false;
  }
  
  public int hashCode()
  {
    return Objects.hashCode(new Object[] { this.host, Integer.valueOf(this.port), Boolean.valueOf(this.hasBracketlessColons) });
  }
  
  public String toString()
  {
    StringBuilder builder = new StringBuilder(this.host.length() + 8);
    if (this.host.indexOf(':') >= 0) {
      builder.append('[').append(this.host).append(']');
    } else {
      builder.append(this.host);
    }
    if (hasPort()) {
      builder.append(':').append(this.port);
    }
    return builder.toString();
  }
  
  private static boolean isValidPort(int port)
  {
    return (port >= 0) && (port <= 65535);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\net\HostAndPort.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */