package org.flywaydb.core.internal.dbsupport.oracle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

public class OracleSqlStatementBuilder
  extends SqlStatementBuilder
{
  private static final Pattern KEYWORDS_BEFORE_STRING_LITERAL_REGEX = Pattern.compile("^(N|IF|ELSIF|SELECT|IMMEDIATE|RETURN|IS)('.*)");
  private static final Pattern KEYWORDS_AFTER_STRING_LITERAL_REGEX = Pattern.compile("(.*')(USING|THEN|FROM|AND|OR)(?!.)");
  private static final Delimiter PLSQL_DELIMITER = new Delimiter("/", true);
  private String statementStart = "";
  
  protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter)
  {
    if ((line.matches("DECLARE|DECLARE\\s.*")) || (line.matches("BEGIN|BEGIN\\s.*"))) {
      return PLSQL_DELIMITER;
    }
    if (StringUtils.countOccurrencesOf(this.statementStart, " ") < 8)
    {
      this.statementStart += line;
      this.statementStart += " ";
      this.statementStart = this.statementStart.replaceAll("\\s+", " ");
    }
    if ((this.statementStart.matches("CREATE( OR REPLACE)? (FUNCTION|PROCEDURE|PACKAGE|TYPE|TRIGGER).*")) || 
      (this.statementStart.matches("CREATE( OR REPLACE)?( AND (RESOLVE|COMPILE))?( NOFORCE)? JAVA (SOURCE|RESOURCE|CLASS).*"))) {
      return PLSQL_DELIMITER;
    }
    return delimiter;
  }
  
  protected String cleanToken(String token)
  {
    if ((token.startsWith("'")) && (token.endsWith("'"))) {
      return token;
    }
    Matcher beforeMatcher = KEYWORDS_BEFORE_STRING_LITERAL_REGEX.matcher(token);
    if (beforeMatcher.find()) {
      token = beforeMatcher.group(2);
    }
    Matcher afterMatcher = KEYWORDS_AFTER_STRING_LITERAL_REGEX.matcher(token);
    if (afterMatcher.find()) {
      token = afterMatcher.group(1);
    }
    return token;
  }
  
  protected String simplifyLine(String line)
  {
    String simplifiedQQuotes = StringUtils.replaceAll(StringUtils.replaceAll(line, "q'(", "q'["), ")'", "]'");
    return super.simplifyLine(simplifiedQQuotes);
  }
  
  protected String extractAlternateOpenQuote(String token)
  {
    if ((token.startsWith("Q'")) && (token.length() >= 3)) {
      return token.substring(0, 3);
    }
    return null;
  }
  
  protected String computeAlternateCloseQuote(String openQuote)
  {
    char specialChar = openQuote.charAt(2);
    switch (specialChar)
    {
    case '[': 
      return "]'";
    case '(': 
      return ")'";
    case '{': 
      return "}'";
    case '<': 
      return ">'";
    }
    return specialChar + "'";
  }
  
  public boolean canDiscard()
  {
    return (super.canDiscard()) || (this.statementStart.startsWith("SET DEFINE OFF"));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\oracle\OracleSqlStatementBuilder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */