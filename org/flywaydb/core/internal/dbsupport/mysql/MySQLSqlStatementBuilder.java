package org.flywaydb.core.internal.dbsupport.mysql;

import java.util.regex.Pattern;
import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

public class MySQLSqlStatementBuilder
  extends SqlStatementBuilder
{
  private static final String DELIMITER_KEYWORD = "DELIMITER";
  private final String[] charSets = { "ARMSCII8", "ASCII", "BIG5", "BINARY", "CP1250", "CP1251", "CP1256", "CP1257", "CP850", "CP852", "CP866", "CP932", "DEC8", "EUCJPMS", "EUCKR", "GB2312", "GBK", "GEOSTD8", "GREEK", "HEBREW", "HP8", "KEYBCS2", "KOI8R", "KOI8U", "LATIN1", "LATIN2", "LATIN5", "LATIN7", "MACCE", "MACROMAN", "SJIS", "SWE7", "TIS620", "UCS2", "UJIS", "UTF8" };
  boolean isInMultiLineCommentDirective = false;
  
  public Delimiter extractNewDelimiterFromLine(String line)
  {
    if (line.toUpperCase().startsWith("DELIMITER")) {
      return new Delimiter(line.substring("DELIMITER".length()).trim(), false);
    }
    return null;
  }
  
  protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter)
  {
    if (line.toUpperCase().startsWith("DELIMITER")) {
      return new Delimiter(line.substring("DELIMITER".length()).trim(), false);
    }
    return delimiter;
  }
  
  public boolean isCommentDirective(String line)
  {
    if (line.matches("^" + Pattern.quote("/*!") + "\\d{5} .*" + Pattern.quote("*/") + "\\s*;?")) {
      return true;
    }
    if (line.matches("^" + Pattern.quote("/*!") + "\\d{5} .*"))
    {
      this.isInMultiLineCommentDirective = true;
      return true;
    }
    if ((this.isInMultiLineCommentDirective) && (line.matches(".*" + Pattern.quote("*/") + "\\s*;?")))
    {
      this.isInMultiLineCommentDirective = false;
      return true;
    }
    return this.isInMultiLineCommentDirective;
  }
  
  protected boolean isSingleLineComment(String token)
  {
    return (token.startsWith("--")) || ((token.startsWith("#")) && ((!"#".equals(this.delimiter.getDelimiter())) || (!"#".equals(token))));
  }
  
  protected String removeEscapedQuotes(String token)
  {
    String noEscapedBackslashes = StringUtils.replaceAll(token, "\\\\", "");
    String noBackslashEscapes = StringUtils.replaceAll(StringUtils.replaceAll(noEscapedBackslashes, "\\'", ""), "\\\"", "");
    return StringUtils.replaceAll(noBackslashEscapes, "''", "").replace("'", " ' ");
  }
  
  protected String cleanToken(String token)
  {
    if ((token.startsWith("B'")) || (token.startsWith("X'"))) {
      return token.substring(token.indexOf("'"));
    }
    if (token.startsWith("_")) {
      for (String charSet : this.charSets)
      {
        String cast = "_" + charSet;
        if (token.startsWith(cast)) {
          return token.substring(cast.length());
        }
      }
    }
    return token;
  }
  
  protected String extractAlternateOpenQuote(String token)
  {
    if (token.startsWith("\"")) {
      return "\"";
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\mysql\MySQLSqlStatementBuilder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */