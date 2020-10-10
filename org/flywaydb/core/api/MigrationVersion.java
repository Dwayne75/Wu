package org.flywaydb.core.api;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class MigrationVersion
  implements Comparable<MigrationVersion>
{
  public static final MigrationVersion EMPTY = new MigrationVersion(null, "<< Empty Schema >>");
  public static final MigrationVersion LATEST = new MigrationVersion(BigInteger.valueOf(-1L), "<< Latest Version >>");
  public static final MigrationVersion CURRENT = new MigrationVersion(BigInteger.valueOf(-2L), "<< Current Version >>");
  private static Pattern splitPattern = Pattern.compile("\\.(?=\\d)");
  private final List<BigInteger> versionParts;
  private final String displayText;
  
  public static MigrationVersion fromVersion(String version)
  {
    if ("current".equalsIgnoreCase(version)) {
      return CURRENT;
    }
    if (LATEST.getVersion().equals(version)) {
      return LATEST;
    }
    if (version == null) {
      return EMPTY;
    }
    return new MigrationVersion(version);
  }
  
  private MigrationVersion(String version)
  {
    String normalizedVersion = version.replace('_', '.');
    this.versionParts = tokenize(normalizedVersion);
    this.displayText = normalizedVersion;
  }
  
  private MigrationVersion(BigInteger version, String displayText)
  {
    this.versionParts = new ArrayList();
    this.versionParts.add(version);
    this.displayText = displayText;
  }
  
  public String toString()
  {
    return this.displayText;
  }
  
  public String getVersion()
  {
    if (equals(EMPTY)) {
      return null;
    }
    if (equals(LATEST)) {
      return Long.toString(Long.MAX_VALUE);
    }
    return this.displayText;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    MigrationVersion version1 = (MigrationVersion)o;
    
    return compareTo(version1) == 0;
  }
  
  public int hashCode()
  {
    return this.versionParts == null ? 0 : this.versionParts.hashCode();
  }
  
  public int compareTo(MigrationVersion o)
  {
    if (o == null) {
      return 1;
    }
    if (this == EMPTY) {
      return o == EMPTY ? 0 : Integer.MIN_VALUE;
    }
    if (this == CURRENT) {
      return o == CURRENT ? 0 : Integer.MIN_VALUE;
    }
    if (this == LATEST) {
      return o == LATEST ? 0 : Integer.MAX_VALUE;
    }
    if (o == EMPTY) {
      return Integer.MAX_VALUE;
    }
    if (o == CURRENT) {
      return Integer.MAX_VALUE;
    }
    if (o == LATEST) {
      return Integer.MIN_VALUE;
    }
    List<BigInteger> elements1 = this.versionParts;
    List<BigInteger> elements2 = o.versionParts;
    int largestNumberOfElements = Math.max(elements1.size(), elements2.size());
    for (int i = 0; i < largestNumberOfElements; i++)
    {
      int compared = getOrZero(elements1, i).compareTo(getOrZero(elements2, i));
      if (compared != 0) {
        return compared;
      }
    }
    return 0;
  }
  
  private BigInteger getOrZero(List<BigInteger> elements, int i)
  {
    return i < elements.size() ? (BigInteger)elements.get(i) : BigInteger.ZERO;
  }
  
  private List<BigInteger> tokenize(String str)
  {
    List<BigInteger> numbers = new ArrayList();
    for (String number : splitPattern.split(str)) {
      try
      {
        numbers.add(new BigInteger(number));
      }
      catch (NumberFormatException e)
      {
        throw new FlywayException("Invalid version containing non-numeric characters. Only 0..9 and . are allowed. Invalid version: " + str);
      }
    }
    for (int i = numbers.size() - 1; i > 0; i--)
    {
      if (!((BigInteger)numbers.get(i)).equals(BigInteger.ZERO)) {
        break;
      }
      numbers.remove(i);
    }
    return numbers;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\api\MigrationVersion.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */