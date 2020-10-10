package com.wurmonline.server.steam;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SteamId
{
  private long steamID64;
  private int accountNumber;
  private int accountInstance;
  private byte accountType;
  private byte accountUniverse;
  static Pattern idPattern = Pattern.compile("^STEAM_(?<x>\\d):(?<y>[01]):(?<z>\\d+)$");
  static Pattern id3Pattern = Pattern.compile("^\\[U:1:(?<w>\\d+)]$");
  static long uIdentifier = 76561197960265728L;
  static long gIdentifier = 103582791429521408L;
  
  public static SteamId fromSteamID64(long steamID64)
  {
    SteamId id = new SteamId();
    id.accountNumber = ((int)(0xFFFFFFFF & steamID64));
    id.accountInstance = ((int)((0xFFFFF00000000 & steamID64) >> 32));
    id.accountType = ((byte)(int)((0xF0000000000000 & steamID64) >> 52));
    id.accountUniverse = ((byte)(int)((0xFF00000000000000 & steamID64) >> 56));
    id.steamID64 = steamID64;
    return id;
  }
  
  public static SteamId fromSteamIDString(String steamIDString)
  {
    return fromSteamIDString(steamIDString, true);
  }
  
  public static SteamId fromSteamIDString(String steamIDString, boolean individual)
  {
    Matcher m = idPattern.matcher(steamIDString);
    if ((!m.matches()) || (m.groupCount() < 3)) {
      return null;
    }
    int y = Integer.valueOf(m.group("y")).intValue();
    int z = Integer.valueOf(m.group("z")).intValue();
    return fromSteamID64(z * 2 + (individual ? uIdentifier : gIdentifier) + y);
  }
  
  public static SteamId fromSteamID3String(String steamID3String)
  {
    Matcher m = id3Pattern.matcher(steamID3String);
    if (!m.matches()) {
      return null;
    }
    int w = Integer.valueOf(m.group("w")).intValue();
    return fromSteamID64(w + uIdentifier);
  }
  
  public String steamIDString()
  {
    return String.format("STEAM_%d:%d:%d", new Object[] { Byte.valueOf(this.accountUniverse), Integer.valueOf(this.accountNumber & 0x1), Integer.valueOf(this.accountNumber >> 1) });
  }
  
  public String steamID3String()
  {
    return String.format("[U:1:%d]", new Object[] { Integer.valueOf((this.accountNumber >> 1) * 2 + (this.accountNumber & 0x1)) });
  }
  
  public long getSteamID64()
  {
    return this.steamID64;
  }
  
  public String toString()
  {
    return String.format("%d", new Object[] { Long.valueOf(this.steamID64) });
  }
  
  public boolean equals(Object obj)
  {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if ((obj instanceof SteamId))
    {
      SteamId id = (SteamId)obj;
      return id.getSteamID64() == getSteamID64();
    }
    if ((obj instanceof String))
    {
      String s = (String)obj;
      return (steamID3String().equals(s)) || (steamIDString().equals(s)) || (toString().equals(s));
    }
    if ((obj instanceof Long)) {
      return ((Long)obj).longValue() == getSteamID64();
    }
    return false;
  }
  
  public static SteamId fromAnyString(String input)
  {
    long id64 = Long.valueOf(input).longValue();
    if (id64 != 0L) {
      return fromSteamID64(id64);
    }
    SteamId id = fromSteamIDString(input);
    if (id != null) {
      return id;
    }
    return fromSteamID3String(input);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\steam\SteamId.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */