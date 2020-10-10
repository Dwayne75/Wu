package org.apache.commons.codec.language.bm;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lang
{
  private static final class LangRule
  {
    private final boolean acceptOnMatch;
    private final Set<String> languages;
    private final Pattern pattern;
    
    private LangRule(Pattern pattern, Set<String> languages, boolean acceptOnMatch)
    {
      this.pattern = pattern;
      this.languages = languages;
      this.acceptOnMatch = acceptOnMatch;
    }
    
    public boolean matches(String txt)
    {
      return this.pattern.matcher(txt).find();
    }
  }
  
  private static final Map<NameType, Lang> Langs = new EnumMap(NameType.class);
  private static final String LANGUAGE_RULES_RN = "org/apache/commons/codec/language/bm/lang.txt";
  private final Languages languages;
  private final List<LangRule> rules;
  
  static
  {
    for (NameType s : NameType.values()) {
      Langs.put(s, loadFromResource("org/apache/commons/codec/language/bm/lang.txt", Languages.getInstance(s)));
    }
  }
  
  public static Lang instance(NameType nameType)
  {
    return (Lang)Langs.get(nameType);
  }
  
  public static Lang loadFromResource(String languageRulesResourceName, Languages languages)
  {
    List<LangRule> rules = new ArrayList();
    InputStream lRulesIS = Lang.class.getClassLoader().getResourceAsStream(languageRulesResourceName);
    if (lRulesIS == null) {
      throw new IllegalStateException("Unable to resolve required resource:org/apache/commons/codec/language/bm/lang.txt");
    }
    Scanner scanner = new Scanner(lRulesIS, "UTF-8");
    boolean inExtendedComment = false;
    while (scanner.hasNextLine())
    {
      String rawLine = scanner.nextLine();
      String line = rawLine;
      if (inExtendedComment)
      {
        if (line.endsWith("*/")) {
          inExtendedComment = false;
        }
      }
      else if (line.startsWith("/*"))
      {
        inExtendedComment = true;
      }
      else
      {
        int cmtI = line.indexOf("//");
        if (cmtI >= 0) {
          line = line.substring(0, cmtI);
        }
        line = line.trim();
        if (line.length() != 0)
        {
          String[] parts = line.split("\\s+");
          if (parts.length != 3)
          {
            System.err.println("Warning: malformed line '" + rawLine + "'");
          }
          else
          {
            Pattern pattern = Pattern.compile(parts[0]);
            String[] langs = parts[1].split("\\+");
            boolean accept = parts[2].equals("true");
            
            rules.add(new LangRule(pattern, new HashSet(Arrays.asList(langs)), accept, null));
          }
        }
      }
    }
    return new Lang(rules, languages);
  }
  
  private Lang(List<LangRule> rules, Languages languages)
  {
    this.rules = Collections.unmodifiableList(rules);
    this.languages = languages;
  }
  
  public String guessLanguage(String text)
  {
    Languages.LanguageSet ls = guessLanguages(text);
    return ls.isSingleton() ? ls.getAny() : "any";
  }
  
  public Languages.LanguageSet guessLanguages(String input)
  {
    String text = input.toLowerCase(Locale.ENGLISH);
    
    Set<String> langs = new HashSet(this.languages.getLanguages());
    for (LangRule rule : this.rules) {
      if (rule.matches(text)) {
        if (rule.acceptOnMatch) {
          langs.retainAll(rule.languages);
        } else {
          langs.removeAll(rule.languages);
        }
      }
    }
    Languages.LanguageSet ls = Languages.LanguageSet.from(langs);
    return ls.equals(Languages.NO_LANGUAGES) ? Languages.ANY_LANGUAGE : ls;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\commons\codec\language\bm\Lang.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */