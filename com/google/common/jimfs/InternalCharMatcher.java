package com.google.common.jimfs;

import java.util.Arrays;

final class InternalCharMatcher
{
  private final char[] chars;
  
  public static InternalCharMatcher anyOf(String chars)
  {
    return new InternalCharMatcher(chars);
  }
  
  private InternalCharMatcher(String chars)
  {
    this.chars = chars.toCharArray();
    Arrays.sort(this.chars);
  }
  
  public boolean matches(char c)
  {
    return Arrays.binarySearch(this.chars, c) >= 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\InternalCharMatcher.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */