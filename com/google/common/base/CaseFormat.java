package com.google.common.base;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import java.io.Serializable;
import javax.annotation.Nullable;

@GwtCompatible
public enum CaseFormat
{
  LOWER_HYPHEN(CharMatcher.is('-'), "-"),  LOWER_UNDERSCORE(CharMatcher.is('_'), "_"),  LOWER_CAMEL(CharMatcher.inRange('A', 'Z'), ""),  UPPER_CAMEL(CharMatcher.inRange('A', 'Z'), ""),  UPPER_UNDERSCORE(CharMatcher.is('_'), "_");
  
  private final CharMatcher wordBoundary;
  private final String wordSeparator;
  
  private CaseFormat(CharMatcher wordBoundary, String wordSeparator)
  {
    this.wordBoundary = wordBoundary;
    this.wordSeparator = wordSeparator;
  }
  
  public final String to(CaseFormat format, String str)
  {
    Preconditions.checkNotNull(format);
    Preconditions.checkNotNull(str);
    return format == this ? str : convert(format, str);
  }
  
  String convert(CaseFormat format, String s)
  {
    StringBuilder out = null;
    int i = 0;
    int j = -1;
    while ((j = this.wordBoundary.indexIn(s, ++j)) != -1)
    {
      if (i == 0)
      {
        out = new StringBuilder(s.length() + 4 * this.wordSeparator.length());
        out.append(format.normalizeFirstWord(s.substring(i, j)));
      }
      else
      {
        out.append(format.normalizeWord(s.substring(i, j)));
      }
      out.append(format.wordSeparator);
      i = j + this.wordSeparator.length();
    }
    return format.normalizeWord(s.substring(i));
  }
  
  @Beta
  public Converter<String, String> converterTo(CaseFormat targetFormat)
  {
    return new StringConverter(this, targetFormat);
  }
  
  abstract String normalizeWord(String paramString);
  
  private static final class StringConverter
    extends Converter<String, String>
    implements Serializable
  {
    private final CaseFormat sourceFormat;
    private final CaseFormat targetFormat;
    private static final long serialVersionUID = 0L;
    
    StringConverter(CaseFormat sourceFormat, CaseFormat targetFormat)
    {
      this.sourceFormat = ((CaseFormat)Preconditions.checkNotNull(sourceFormat));
      this.targetFormat = ((CaseFormat)Preconditions.checkNotNull(targetFormat));
    }
    
    protected String doForward(String s)
    {
      return s == null ? null : this.sourceFormat.to(this.targetFormat, s);
    }
    
    protected String doBackward(String s)
    {
      return s == null ? null : this.targetFormat.to(this.sourceFormat, s);
    }
    
    public boolean equals(@Nullable Object object)
    {
      if ((object instanceof StringConverter))
      {
        StringConverter that = (StringConverter)object;
        return (this.sourceFormat.equals(that.sourceFormat)) && (this.targetFormat.equals(that.targetFormat));
      }
      return false;
    }
    
    public int hashCode()
    {
      return this.sourceFormat.hashCode() ^ this.targetFormat.hashCode();
    }
    
    public String toString()
    {
      String str1 = String.valueOf(String.valueOf(this.sourceFormat));String str2 = String.valueOf(String.valueOf(this.targetFormat));return 14 + str1.length() + str2.length() + str1 + ".converterTo(" + str2 + ")";
    }
  }
  
  private String normalizeFirstWord(String word)
  {
    return this == LOWER_CAMEL ? Ascii.toLowerCase(word) : normalizeWord(word);
  }
  
  private static String firstCharOnlyToUpper(String word)
  {
    return word.length() + Ascii.toUpperCase(word.charAt(0)) + Ascii.toLowerCase(word.substring(1));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\base\CaseFormat.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */