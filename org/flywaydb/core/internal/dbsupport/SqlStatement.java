package org.flywaydb.core.internal.dbsupport;

public class SqlStatement
{
  private int lineNumber;
  private String sql;
  private boolean pgCopy;
  
  public SqlStatement(int lineNumber, String sql, boolean pgCopy)
  {
    this.lineNumber = lineNumber;
    this.sql = sql;
    this.pgCopy = pgCopy;
  }
  
  public int getLineNumber()
  {
    return this.lineNumber;
  }
  
  public String getSql()
  {
    return this.sql;
  }
  
  public boolean isPgCopy()
  {
    return this.pgCopy;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\SqlStatement.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */