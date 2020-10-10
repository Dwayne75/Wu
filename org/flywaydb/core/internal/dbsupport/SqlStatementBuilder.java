package org.flywaydb.core.internal.dbsupport;

import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.internal.util.StringUtils;

public class SqlStatementBuilder
{
  private StringBuilder statement = new StringBuilder();
  private int lineNumber;
  private boolean empty = true;
  private boolean terminated;
  private boolean insideQuoteStringLiteral = false;
  private boolean insideAlternateQuoteStringLiteral = false;
  private String alternateQuote;
  private boolean lineEndsWithSingleLineComment = false;
  private boolean insideMultiLineComment = false;
  private boolean nonCommentStatementPartSeen = false;
  protected Delimiter delimiter = getDefaultDelimiter();
  
  protected Delimiter getDefaultDelimiter()
  {
    return new Delimiter(";", false);
  }
  
  public void setLineNumber(int lineNumber)
  {
    this.lineNumber = lineNumber;
  }
  
  public void setDelimiter(Delimiter delimiter)
  {
    this.delimiter = delimiter;
  }
  
  public boolean isEmpty()
  {
    return this.empty;
  }
  
  public boolean isTerminated()
  {
    return this.terminated;
  }
  
  public SqlStatement getSqlStatement()
  {
    String sql = this.statement.toString();
    return new SqlStatement(this.lineNumber, sql, isPgCopyFromStdIn());
  }
  
  public Delimiter extractNewDelimiterFromLine(String line)
  {
    return null;
  }
  
  public boolean isPgCopyFromStdIn()
  {
    return false;
  }
  
  public boolean isCommentDirective(String line)
  {
    return false;
  }
  
  protected boolean isSingleLineComment(String line)
  {
    return line.startsWith("--");
  }
  
  public void addLine(String line)
  {
    if (isEmpty()) {
      this.empty = false;
    } else {
      this.statement.append("\n");
    }
    if (isCommentDirective(line.trim())) {
      this.nonCommentStatementPartSeen = true;
    }
    String lineSimplified = simplifyLine(line);
    
    applyStateChanges(lineSimplified);
    if ((endWithOpenMultilineStringLiteral()) || (this.insideMultiLineComment))
    {
      this.statement.append(line);
      return;
    }
    this.delimiter = changeDelimiterIfNecessary(lineSimplified, this.delimiter);
    
    this.statement.append(line);
    if ((!this.lineEndsWithSingleLineComment) && (lineTerminatesStatement(lineSimplified, this.delimiter)))
    {
      stripDelimiter(this.statement, this.delimiter);
      this.terminated = true;
    }
  }
  
  boolean endWithOpenMultilineStringLiteral()
  {
    return (this.insideQuoteStringLiteral) || (this.insideAlternateQuoteStringLiteral);
  }
  
  public boolean canDiscard()
  {
    return (!this.insideAlternateQuoteStringLiteral) && (!this.insideQuoteStringLiteral) && (!this.insideMultiLineComment) && (!this.nonCommentStatementPartSeen);
  }
  
  protected String simplifyLine(String line)
  {
    return removeEscapedQuotes(line).replace("--", " -- ").replace("/*", " /* ").replace("*/", " */ ").replaceAll("\\s+", " ").trim().toUpperCase();
  }
  
  protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter)
  {
    return delimiter;
  }
  
  private boolean lineTerminatesStatement(String line, Delimiter delimiter)
  {
    if (delimiter == null) {
      return false;
    }
    String upperCaseDelimiter = delimiter.getDelimiter().toUpperCase();
    if (delimiter.isAloneOnLine()) {
      return line.equals(upperCaseDelimiter);
    }
    return line.endsWith(upperCaseDelimiter);
  }
  
  static void stripDelimiter(StringBuilder sql, Delimiter delimiter)
  {
    for (int last = sql.length(); last > 0; last--) {
      if (!Character.isWhitespace(sql.charAt(last - 1))) {
        break;
      }
    }
    sql.delete(last - delimiter.getDelimiter().length(), sql.length());
  }
  
  protected String extractAlternateOpenQuote(String token)
  {
    return null;
  }
  
  protected String computeAlternateCloseQuote(String openQuote)
  {
    return openQuote;
  }
  
  protected void applyStateChanges(String line)
  {
    String[] tokens = StringUtils.tokenizeToStringArray(line, " @<>;:=|(),+{}");
    
    List<TokenType> delimitingTokens = extractStringLiteralDelimitingTokens(tokens);
    
    this.lineEndsWithSingleLineComment = false;
    for (TokenType delimitingToken : delimitingTokens)
    {
      if ((!this.insideQuoteStringLiteral) && (!this.insideAlternateQuoteStringLiteral) && 
        (TokenType.MULTI_LINE_COMMENT_OPEN.equals(delimitingToken))) {
        this.insideMultiLineComment = true;
      }
      if ((!this.insideQuoteStringLiteral) && (!this.insideAlternateQuoteStringLiteral) && 
        (TokenType.MULTI_LINE_COMMENT_CLOSE.equals(delimitingToken))) {
        this.insideMultiLineComment = false;
      }
      if ((!this.insideQuoteStringLiteral) && (!this.insideAlternateQuoteStringLiteral) && (!this.insideMultiLineComment) && 
        (TokenType.SINGLE_LINE_COMMENT.equals(delimitingToken)))
      {
        this.lineEndsWithSingleLineComment = true;
        return;
      }
      if ((!this.insideMultiLineComment) && (!this.insideQuoteStringLiteral) && 
        (TokenType.ALTERNATE_QUOTE.equals(delimitingToken))) {
        this.insideAlternateQuoteStringLiteral = (!this.insideAlternateQuoteStringLiteral);
      }
      if ((!this.insideMultiLineComment) && (!this.insideAlternateQuoteStringLiteral) && 
        (TokenType.QUOTE.equals(delimitingToken))) {
        this.insideQuoteStringLiteral = (!this.insideQuoteStringLiteral);
      }
      if ((!this.insideMultiLineComment) && (!this.insideQuoteStringLiteral) && (!this.insideAlternateQuoteStringLiteral) && 
        (TokenType.OTHER.equals(delimitingToken))) {
        this.nonCommentStatementPartSeen = true;
      }
    }
  }
  
  private List<TokenType> extractStringLiteralDelimitingTokens(String[] tokens)
  {
    List<TokenType> delimitingTokens = new ArrayList();
    for (String token : tokens)
    {
      String cleanToken = cleanToken(token);
      boolean handled = false;
      if (this.alternateQuote == null)
      {
        String alternateQuoteFromToken = extractAlternateOpenQuote(cleanToken);
        if (alternateQuoteFromToken != null)
        {
          String closeQuote = computeAlternateCloseQuote(alternateQuoteFromToken);
          if ((cleanToken.length() >= alternateQuoteFromToken.length() + closeQuote.length()) && 
            (cleanToken.startsWith(alternateQuoteFromToken)) && (cleanToken.endsWith(closeQuote))) {
            continue;
          }
          this.alternateQuote = closeQuote;
          delimitingTokens.add(TokenType.ALTERNATE_QUOTE);
          
          continue;
        }
      }
      if ((this.alternateQuote != null) && (cleanToken.endsWith(this.alternateQuote)))
      {
        this.alternateQuote = null;
        delimitingTokens.add(TokenType.ALTERNATE_QUOTE);
      }
      else if ((cleanToken.length() < 2) || (!cleanToken.startsWith("'")) || (!cleanToken.endsWith("'")))
      {
        if (cleanToken.length() >= 4)
        {
          int numberOfOpeningMultiLineComments = StringUtils.countOccurrencesOf(cleanToken, "/*");
          int numberOfClosingMultiLineComments = StringUtils.countOccurrencesOf(cleanToken, "*/");
          if ((numberOfOpeningMultiLineComments > 0) && (numberOfOpeningMultiLineComments == numberOfClosingMultiLineComments)) {}
        }
        else
        {
          if (isSingleLineComment(cleanToken))
          {
            delimitingTokens.add(TokenType.SINGLE_LINE_COMMENT);
            handled = true;
          }
          if (cleanToken.contains("/*"))
          {
            delimitingTokens.add(TokenType.MULTI_LINE_COMMENT_OPEN);
            handled = true;
          }
          else if (cleanToken.startsWith("'"))
          {
            delimitingTokens.add(TokenType.QUOTE);
            handled = true;
          }
          if ((!cleanToken.contains("/*")) && (cleanToken.contains("*/")))
          {
            delimitingTokens.add(TokenType.MULTI_LINE_COMMENT_CLOSE);
            handled = true;
          }
          else if ((!cleanToken.startsWith("'")) && (cleanToken.endsWith("'")))
          {
            delimitingTokens.add(TokenType.QUOTE);
            handled = true;
          }
          if (!handled) {
            delimitingTokens.add(TokenType.OTHER);
          }
        }
      }
    }
    return delimitingTokens;
  }
  
  protected String removeEscapedQuotes(String token)
  {
    return StringUtils.replaceAll(token, "''", "");
  }
  
  protected String cleanToken(String token)
  {
    return token;
  }
  
  private static enum TokenType
  {
    OTHER,  QUOTE,  ALTERNATE_QUOTE,  SINGLE_LINE_COMMENT,  MULTI_LINE_COMMENT_OPEN,  MULTI_LINE_COMMENT_CLOSE;
    
    private TokenType() {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\SqlStatementBuilder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */