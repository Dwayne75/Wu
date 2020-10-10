package com.google.common.jimfs;

import com.google.common.base.Ascii;
import com.google.common.base.Function;
import com.ibm.icu.lang.UCharacter;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Pattern;

public enum PathNormalization
  implements Function<String, String>
{
  NONE(0),  NFC(128),  NFD(128),  CASE_FOLD_UNICODE(66),  CASE_FOLD_ASCII(2);
  
  private final int patternFlags;
  
  private PathNormalization(int patternFlags)
  {
    this.patternFlags = patternFlags;
  }
  
  public abstract String apply(String paramString);
  
  public int patternFlags()
  {
    return this.patternFlags;
  }
  
  public static String normalize(String string, Iterable<PathNormalization> normalizations)
  {
    String result = string;
    for (PathNormalization normalization : normalizations) {
      result = normalization.apply(result);
    }
    return result;
  }
  
  public static Pattern compilePattern(String regex, Iterable<PathNormalization> normalizations)
  {
    int flags = 0;
    for (PathNormalization normalization : normalizations) {
      flags |= normalization.patternFlags();
    }
    return Pattern.compile(regex, flags);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\PathNormalization.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */