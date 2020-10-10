package org.flywaydb.core.internal.dbsupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.Resource;

public class SqlScript
{
  private static final Log LOG = LogFactory.getLog(SqlScript.class);
  private final DbSupport dbSupport;
  private final List<SqlStatement> sqlStatements;
  private final Resource resource;
  
  public SqlScript(String sqlScriptSource, DbSupport dbSupport)
  {
    this.dbSupport = dbSupport;
    this.sqlStatements = parse(sqlScriptSource);
    this.resource = null;
  }
  
  public SqlScript(DbSupport dbSupport, Resource sqlScriptResource, PlaceholderReplacer placeholderReplacer, String encoding)
  {
    this.dbSupport = dbSupport;
    
    String sqlScriptSource = sqlScriptResource.loadAsString(encoding);
    this.sqlStatements = parse(placeholderReplacer.replacePlaceholders(sqlScriptSource));
    
    this.resource = sqlScriptResource;
  }
  
  public List<SqlStatement> getSqlStatements()
  {
    return this.sqlStatements;
  }
  
  public Resource getResource()
  {
    return this.resource;
  }
  
  public void execute(JdbcTemplate jdbcTemplate)
  {
    for (SqlStatement sqlStatement : this.sqlStatements)
    {
      String sql = sqlStatement.getSql();
      LOG.debug("Executing SQL: " + sql);
      try
      {
        if (sqlStatement.isPgCopy()) {
          this.dbSupport.executePgCopy(jdbcTemplate.getConnection(), sql);
        } else {
          jdbcTemplate.executeStatement(sql);
        }
      }
      catch (SQLException e)
      {
        throw new FlywaySqlScriptException(this.resource, sqlStatement, e);
      }
    }
  }
  
  List<SqlStatement> parse(String sqlScriptSource)
  {
    return linesToStatements(readLines(new StringReader(sqlScriptSource)));
  }
  
  List<SqlStatement> linesToStatements(List<String> lines)
  {
    List<SqlStatement> statements = new ArrayList();
    
    Delimiter nonStandardDelimiter = null;
    SqlStatementBuilder sqlStatementBuilder = this.dbSupport.createSqlStatementBuilder();
    for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++)
    {
      String line = (String)lines.get(lineNumber - 1);
      if (sqlStatementBuilder.isEmpty())
      {
        if (!StringUtils.hasText(line)) {
          continue;
        }
        Delimiter newDelimiter = sqlStatementBuilder.extractNewDelimiterFromLine(line);
        if (newDelimiter != null)
        {
          nonStandardDelimiter = newDelimiter;
          
          continue;
        }
        sqlStatementBuilder.setLineNumber(lineNumber);
        if (nonStandardDelimiter != null) {
          sqlStatementBuilder.setDelimiter(nonStandardDelimiter);
        }
      }
      sqlStatementBuilder.addLine(line);
      if (sqlStatementBuilder.canDiscard())
      {
        sqlStatementBuilder = this.dbSupport.createSqlStatementBuilder();
      }
      else if (sqlStatementBuilder.isTerminated())
      {
        SqlStatement sqlStatement = sqlStatementBuilder.getSqlStatement();
        statements.add(sqlStatement);
        LOG.debug("Found statement at line " + sqlStatement.getLineNumber() + ": " + sqlStatement.getSql());
        
        sqlStatementBuilder = this.dbSupport.createSqlStatementBuilder();
      }
    }
    if (!sqlStatementBuilder.isEmpty()) {
      statements.add(sqlStatementBuilder.getSqlStatement());
    }
    return statements;
  }
  
  private List<String> readLines(Reader reader)
  {
    List<String> lines = new ArrayList();
    
    BufferedReader bufferedReader = new BufferedReader(reader);
    try
    {
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        lines.add(line);
      }
    }
    catch (IOException e)
    {
      String message = "Unable to parse " + this.resource.getLocation() + " (" + this.resource.getLocationOnDisk() + ")";
      throw new FlywayException(message, e);
    }
    String line;
    return lines;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\SqlScript.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */