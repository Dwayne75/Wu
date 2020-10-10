package com.sun.tools.jxc;

import com.sun.tools.jxc.gen.config.NGCCRuntime;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public final class NGCCRuntimeEx
  extends NGCCRuntime
{
  private final ErrorHandler errorHandler;
  
  public NGCCRuntimeEx(ErrorHandler errorHandler)
  {
    this.errorHandler = errorHandler;
  }
  
  public File getBaseDir(String baseDir)
    throws SAXException
  {
    File dir = new File(baseDir);
    if (dir.exists()) {
      return dir;
    }
    SAXParseException e = new SAXParseException(Messages.BASEDIR_DOESNT_EXIST.format(new Object[] { dir.getAbsolutePath() }), getLocator());
    
    this.errorHandler.error(e);
    throw e;
  }
  
  public List<Pattern> getIncludePatterns(List includeContent)
  {
    List<Pattern> includeRegexList = new ArrayList();
    for (int i = 0; i < includeContent.size(); i++)
    {
      String includes = (String)includeContent.get(i);
      String regex = convertToRegex(includes);
      Pattern pattern = Pattern.compile(regex);
      includeRegexList.add(pattern);
    }
    return includeRegexList;
  }
  
  public List getExcludePatterns(List excludeContent)
  {
    List excludeRegexList = new ArrayList();
    for (int i = 0; i < excludeContent.size(); i++)
    {
      String excludes = (String)excludeContent.get(i);
      String regex = convertToRegex(excludes);
      Pattern pattern = Pattern.compile(regex);
      excludeRegexList.add(pattern);
    }
    return excludeRegexList;
  }
  
  private String convertToRegex(String pattern)
  {
    StringBuilder regex = new StringBuilder();
    char nc = ' ';
    if (pattern.length() > 0) {
      for (int i = 0; i < pattern.length(); i++)
      {
        char c = pattern.charAt(i);
        int j = i;
        nc = ' ';
        if (j + 1 != pattern.length()) {
          nc = pattern.charAt(j + 1);
        }
        if ((c == '.') && (nc != '.'))
        {
          regex.append('\\');
          regex.append('.');
        }
        else if ((c != '.') || (nc != '.'))
        {
          if ((c == '*') && (nc == '*'))
          {
            regex.append(".*");
            break;
          }
          if (c == '*') {
            regex.append("[^\\.]+");
          } else if (c == '?') {
            regex.append("[^\\.]");
          } else {
            regex.append(c);
          }
        }
      }
    }
    return regex.toString();
  }
  
  protected void unexpectedX(String token)
    throws SAXException
  {
    this.errorHandler.error(new SAXParseException(Messages.UNEXPECTED_NGCC_TOKEN.format(new Object[] { token, Integer.valueOf(getLocator().getLineNumber()), Integer.valueOf(getLocator().getColumnNumber()) }), getLocator()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\jxc\NGCCRuntimeEx.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */