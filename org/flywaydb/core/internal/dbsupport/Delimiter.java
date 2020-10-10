package org.flywaydb.core.internal.dbsupport;

public class Delimiter
{
  private final String delimiter;
  private final boolean aloneOnLine;
  
  public Delimiter(String delimiter, boolean aloneOnLine)
  {
    this.delimiter = delimiter;
    this.aloneOnLine = aloneOnLine;
  }
  
  public String getDelimiter()
  {
    return this.delimiter;
  }
  
  public boolean isAloneOnLine()
  {
    return this.aloneOnLine;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    Delimiter delimiter1 = (Delimiter)o;
    
    return (this.aloneOnLine == delimiter1.aloneOnLine) && (this.delimiter.equals(delimiter1.delimiter));
  }
  
  public int hashCode()
  {
    int result = this.delimiter.hashCode();
    result = 31 * result + (this.aloneOnLine ? 1 : 0);
    return result;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\Delimiter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */