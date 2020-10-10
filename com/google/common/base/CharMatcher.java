package com.google.common.base;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import java.util.Arrays;
import java.util.BitSet;
import javax.annotation.CheckReturnValue;

@Beta
@GwtCompatible(emulated=true)
public abstract class CharMatcher
  implements Predicate<Character>
{
  public static final CharMatcher BREAKING_WHITESPACE = new CharMatcher()
  {
    public boolean matches(char c)
    {
      switch (c)
      {
      case '\t': 
      case '\n': 
      case '\013': 
      case '\f': 
      case '\r': 
      case ' ': 
      case '': 
      case ' ': 
      case ' ': 
      case ' ': 
      case ' ': 
      case '　': 
        return true;
      case ' ': 
        return false;
      }
      return (c >= ' ') && (c <= ' ');
    }
    
    public String toString()
    {
      return "CharMatcher.BREAKING_WHITESPACE";
    }
  };
  public static final CharMatcher ASCII = inRange('\000', '', "CharMatcher.ASCII");
  private static final String ZEROES = "0٠۰߀०০੦૦୦௦౦೦൦๐໐༠၀႐០᠐᥆᧐᭐᮰᱀᱐꘠꣐꤀꩐０";
  private static final String NINES;
  
  private static class RangesMatcher
    extends CharMatcher
  {
    private final char[] rangeStarts;
    private final char[] rangeEnds;
    
    RangesMatcher(String description, char[] rangeStarts, char[] rangeEnds)
    {
      super();
      this.rangeStarts = rangeStarts;
      this.rangeEnds = rangeEnds;
      Preconditions.checkArgument(rangeStarts.length == rangeEnds.length);
      for (int i = 0; i < rangeStarts.length; i++)
      {
        Preconditions.checkArgument(rangeStarts[i] <= rangeEnds[i]);
        if (i + 1 < rangeStarts.length) {
          Preconditions.checkArgument(rangeEnds[i] < rangeStarts[(i + 1)]);
        }
      }
    }
    
    public boolean matches(char c)
    {
      int index = Arrays.binarySearch(this.rangeStarts, c);
      if (index >= 0) {
        return true;
      }
      index = (index ^ 0xFFFFFFFF) - 1;
      return (index >= 0) && (c <= this.rangeEnds[index]);
    }
  }
  
  static
  {
    StringBuilder builder = new StringBuilder("0٠۰߀०০੦૦୦௦౦೦൦๐໐༠၀႐០᠐᥆᧐᭐᮰᱀᱐꘠꣐꤀꩐０".length());
    for (int i = 0; i < "0٠۰߀०০੦૦୦௦౦೦൦๐໐༠၀႐០᠐᥆᧐᭐᮰᱀᱐꘠꣐꤀꩐０".length(); i++) {
      builder.append((char)("0٠۰߀०০੦૦୦௦౦೦൦๐໐༠၀႐០᠐᥆᧐᭐᮰᱀᱐꘠꣐꤀꩐０".charAt(i) + '\t'));
    }
    NINES = builder.toString();
  }
  
  public static final CharMatcher DIGIT = new RangesMatcher("CharMatcher.DIGIT", "0٠۰߀०০੦૦୦௦౦೦൦๐໐༠၀႐០᠐᥆᧐᭐᮰᱀᱐꘠꣐꤀꩐０".toCharArray(), NINES.toCharArray());
  public static final CharMatcher JAVA_DIGIT = new CharMatcher("CharMatcher.JAVA_DIGIT")
  {
    public boolean matches(char c)
    {
      return Character.isDigit(c);
    }
  };
  public static final CharMatcher JAVA_LETTER = new CharMatcher("CharMatcher.JAVA_LETTER")
  {
    public boolean matches(char c)
    {
      return Character.isLetter(c);
    }
  };
  public static final CharMatcher JAVA_LETTER_OR_DIGIT = new CharMatcher("CharMatcher.JAVA_LETTER_OR_DIGIT")
  {
    public boolean matches(char c)
    {
      return Character.isLetterOrDigit(c);
    }
  };
  public static final CharMatcher JAVA_UPPER_CASE = new CharMatcher("CharMatcher.JAVA_UPPER_CASE")
  {
    public boolean matches(char c)
    {
      return Character.isUpperCase(c);
    }
  };
  public static final CharMatcher JAVA_LOWER_CASE = new CharMatcher("CharMatcher.JAVA_LOWER_CASE")
  {
    public boolean matches(char c)
    {
      return Character.isLowerCase(c);
    }
  };
  public static final CharMatcher JAVA_ISO_CONTROL = inRange('\000', '\037').or(inRange('', '')).withToString("CharMatcher.JAVA_ISO_CONTROL");
  public static final CharMatcher INVISIBLE = new RangesMatcher("CharMatcher.INVISIBLE", "\000­؀؜۝܏ ᠎   ⁦⁧⁨⁩⁪　?﻿￹￺".toCharArray(), "  ­؄؜۝܏ ᠎‏ ⁤⁦⁧⁨⁩⁯　﻿￹￻".toCharArray());
  
  private static String showCharacter(char c)
  {
    String hex = "0123456789ABCDEF";
    char[] tmp = { '\\', 'u', '\000', '\000', '\000', '\000' };
    for (int i = 0; i < 4; i++)
    {
      tmp[(5 - i)] = hex.charAt(c & 0xF);
      c = (char)(c >> '\004');
    }
    return String.copyValueOf(tmp);
  }
  
  public static final CharMatcher SINGLE_WIDTH = new RangesMatcher("CharMatcher.SINGLE_WIDTH", "\000־א׳؀ݐ฀Ḁ℀ﭐﹰ｡".toCharArray(), "ӹ־ת״ۿݿ๿₯℺﷿﻿ￜ".toCharArray());
  public static final CharMatcher ANY = new FastMatcher("CharMatcher.ANY")
  {
    public boolean matches(char c)
    {
      return true;
    }
    
    public int indexIn(CharSequence sequence)
    {
      return sequence.length() == 0 ? -1 : 0;
    }
    
    public int indexIn(CharSequence sequence, int start)
    {
      int length = sequence.length();
      Preconditions.checkPositionIndex(start, length);
      return start == length ? -1 : start;
    }
    
    public int lastIndexIn(CharSequence sequence)
    {
      return sequence.length() - 1;
    }
    
    public boolean matchesAllOf(CharSequence sequence)
    {
      Preconditions.checkNotNull(sequence);
      return true;
    }
    
    public boolean matchesNoneOf(CharSequence sequence)
    {
      return sequence.length() == 0;
    }
    
    public String removeFrom(CharSequence sequence)
    {
      Preconditions.checkNotNull(sequence);
      return "";
    }
    
    public String replaceFrom(CharSequence sequence, char replacement)
    {
      char[] array = new char[sequence.length()];
      Arrays.fill(array, replacement);
      return new String(array);
    }
    
    public String replaceFrom(CharSequence sequence, CharSequence replacement)
    {
      StringBuilder retval = new StringBuilder(sequence.length() * replacement.length());
      for (int i = 0; i < sequence.length(); i++) {
        retval.append(replacement);
      }
      return retval.toString();
    }
    
    public String collapseFrom(CharSequence sequence, char replacement)
    {
      return sequence.length() == 0 ? "" : String.valueOf(replacement);
    }
    
    public String trimFrom(CharSequence sequence)
    {
      Preconditions.checkNotNull(sequence);
      return "";
    }
    
    public int countIn(CharSequence sequence)
    {
      return sequence.length();
    }
    
    public CharMatcher and(CharMatcher other)
    {
      return (CharMatcher)Preconditions.checkNotNull(other);
    }
    
    public CharMatcher or(CharMatcher other)
    {
      Preconditions.checkNotNull(other);
      return this;
    }
    
    public CharMatcher negate()
    {
      return NONE;
    }
  };
  public static final CharMatcher NONE = new FastMatcher("CharMatcher.NONE")
  {
    public boolean matches(char c)
    {
      return false;
    }
    
    public int indexIn(CharSequence sequence)
    {
      Preconditions.checkNotNull(sequence);
      return -1;
    }
    
    public int indexIn(CharSequence sequence, int start)
    {
      int length = sequence.length();
      Preconditions.checkPositionIndex(start, length);
      return -1;
    }
    
    public int lastIndexIn(CharSequence sequence)
    {
      Preconditions.checkNotNull(sequence);
      return -1;
    }
    
    public boolean matchesAllOf(CharSequence sequence)
    {
      return sequence.length() == 0;
    }
    
    public boolean matchesNoneOf(CharSequence sequence)
    {
      Preconditions.checkNotNull(sequence);
      return true;
    }
    
    public String removeFrom(CharSequence sequence)
    {
      return sequence.toString();
    }
    
    public String replaceFrom(CharSequence sequence, char replacement)
    {
      return sequence.toString();
    }
    
    public String replaceFrom(CharSequence sequence, CharSequence replacement)
    {
      Preconditions.checkNotNull(replacement);
      return sequence.toString();
    }
    
    public String collapseFrom(CharSequence sequence, char replacement)
    {
      return sequence.toString();
    }
    
    public String trimFrom(CharSequence sequence)
    {
      return sequence.toString();
    }
    
    public String trimLeadingFrom(CharSequence sequence)
    {
      return sequence.toString();
    }
    
    public String trimTrailingFrom(CharSequence sequence)
    {
      return sequence.toString();
    }
    
    public int countIn(CharSequence sequence)
    {
      Preconditions.checkNotNull(sequence);
      return 0;
    }
    
    public CharMatcher and(CharMatcher other)
    {
      Preconditions.checkNotNull(other);
      return this;
    }
    
    public CharMatcher or(CharMatcher other)
    {
      return (CharMatcher)Preconditions.checkNotNull(other);
    }
    
    public CharMatcher negate()
    {
      return ANY;
    }
  };
  final String description;
  private static final int DISTINCT_CHARS = 65536;
  static final String WHITESPACE_TABLE = " 　\r   　 \013　   　 \t     \f 　 　　 \n 　";
  static final int WHITESPACE_MULTIPLIER = 1682554634;
  
  public static CharMatcher is(final char match)
  {
    String str1 = String.valueOf(String.valueOf(showCharacter(match)));String description = 18 + str1.length() + "CharMatcher.is('" + str1 + "')";
    new FastMatcher(description)
    {
      public boolean matches(char c)
      {
        return c == match;
      }
      
      public String replaceFrom(CharSequence sequence, char replacement)
      {
        return sequence.toString().replace(match, replacement);
      }
      
      public CharMatcher and(CharMatcher other)
      {
        return other.matches(match) ? this : NONE;
      }
      
      public CharMatcher or(CharMatcher other)
      {
        return other.matches(match) ? other : super.or(other);
      }
      
      public CharMatcher negate()
      {
        return isNot(match);
      }
      
      @GwtIncompatible("java.util.BitSet")
      void setBits(BitSet table)
      {
        table.set(match);
      }
    };
  }
  
  public static CharMatcher isNot(final char match)
  {
    String str1 = String.valueOf(String.valueOf(showCharacter(match)));String description = 21 + str1.length() + "CharMatcher.isNot('" + str1 + "')";
    new FastMatcher(description)
    {
      public boolean matches(char c)
      {
        return c != match;
      }
      
      public CharMatcher and(CharMatcher other)
      {
        return other.matches(match) ? super.and(other) : other;
      }
      
      public CharMatcher or(CharMatcher other)
      {
        return other.matches(match) ? ANY : this;
      }
      
      @GwtIncompatible("java.util.BitSet")
      void setBits(BitSet table)
      {
        table.set(0, match);
        table.set(match + '\001', 65536);
      }
      
      public CharMatcher negate()
      {
        return is(match);
      }
    };
  }
  
  public static CharMatcher anyOf(CharSequence sequence)
  {
    switch (sequence.length())
    {
    case 0: 
      return NONE;
    case 1: 
      return is(sequence.charAt(0));
    case 2: 
      return isEither(sequence.charAt(0), sequence.charAt(1));
    }
    final char[] chars = sequence.toString().toCharArray();
    Arrays.sort(chars);
    StringBuilder description = new StringBuilder("CharMatcher.anyOf(\"");
    for (char c : chars) {
      description.append(showCharacter(c));
    }
    description.append("\")");
    new CharMatcher(description.toString())
    {
      public boolean matches(char c)
      {
        return Arrays.binarySearch(chars, c) >= 0;
      }
      
      @GwtIncompatible("java.util.BitSet")
      void setBits(BitSet table)
      {
        for (char c : chars) {
          table.set(c);
        }
      }
    };
  }
  
  private static CharMatcher isEither(final char match1, final char match2)
  {
    String str1 = String.valueOf(String.valueOf(showCharacter(match1)));String str2 = String.valueOf(String.valueOf(showCharacter(match2)));String description = 21 + str1.length() + str2.length() + "CharMatcher.anyOf(\"" + str1 + str2 + "\")";
    
    new FastMatcher(description)
    {
      public boolean matches(char c)
      {
        return (c == match1) || (c == match2);
      }
      
      @GwtIncompatible("java.util.BitSet")
      void setBits(BitSet table)
      {
        table.set(match1);
        table.set(match2);
      }
    };
  }
  
  public static CharMatcher noneOf(CharSequence sequence)
  {
    return anyOf(sequence).negate();
  }
  
  public static CharMatcher inRange(char startInclusive, char endInclusive)
  {
    Preconditions.checkArgument(endInclusive >= startInclusive);
    String str1 = String.valueOf(String.valueOf(showCharacter(startInclusive)));String str2 = String.valueOf(String.valueOf(showCharacter(endInclusive)));String description = 27 + str1.length() + str2.length() + "CharMatcher.inRange('" + str1 + "', '" + str2 + "')";
    
    return inRange(startInclusive, endInclusive, description);
  }
  
  static CharMatcher inRange(final char startInclusive, final char endInclusive, String description)
  {
    new FastMatcher(description)
    {
      public boolean matches(char c)
      {
        return (startInclusive <= c) && (c <= endInclusive);
      }
      
      @GwtIncompatible("java.util.BitSet")
      void setBits(BitSet table)
      {
        table.set(startInclusive, endInclusive + '\001');
      }
    };
  }
  
  public static CharMatcher forPredicate(final Predicate<? super Character> predicate)
  {
    Preconditions.checkNotNull(predicate);
    if ((predicate instanceof CharMatcher)) {
      return (CharMatcher)predicate;
    }
    String str1 = String.valueOf(String.valueOf(predicate));String description = 26 + str1.length() + "CharMatcher.forPredicate(" + str1 + ")";
    new CharMatcher(description)
    {
      public boolean matches(char c)
      {
        return predicate.apply(Character.valueOf(c));
      }
      
      public boolean apply(Character character)
      {
        return predicate.apply(Preconditions.checkNotNull(character));
      }
    };
  }
  
  CharMatcher(String description)
  {
    this.description = description;
  }
  
  protected CharMatcher()
  {
    this.description = super.toString();
  }
  
  public CharMatcher negate()
  {
    return new NegatedMatcher(this);
  }
  
  private static class NegatedMatcher
    extends CharMatcher
  {
    final CharMatcher original;
    
    NegatedMatcher(String toString, CharMatcher original)
    {
      super();
      this.original = original;
    }
    
    NegatedMatcher(CharMatcher original)
    {
      this(9 + str.length() + str + ".negate()", original);
    }
    
    public boolean matches(char c)
    {
      return !this.original.matches(c);
    }
    
    public boolean matchesAllOf(CharSequence sequence)
    {
      return this.original.matchesNoneOf(sequence);
    }
    
    public boolean matchesNoneOf(CharSequence sequence)
    {
      return this.original.matchesAllOf(sequence);
    }
    
    public int countIn(CharSequence sequence)
    {
      return sequence.length() - this.original.countIn(sequence);
    }
    
    @GwtIncompatible("java.util.BitSet")
    void setBits(BitSet table)
    {
      BitSet tmp = new BitSet();
      this.original.setBits(tmp);
      tmp.flip(0, 65536);
      table.or(tmp);
    }
    
    public CharMatcher negate()
    {
      return this.original;
    }
    
    CharMatcher withToString(String description)
    {
      return new NegatedMatcher(description, this.original);
    }
  }
  
  public CharMatcher and(CharMatcher other)
  {
    return new And(this, (CharMatcher)Preconditions.checkNotNull(other));
  }
  
  private static class And
    extends CharMatcher
  {
    final CharMatcher first;
    final CharMatcher second;
    
    And(CharMatcher a, CharMatcher b)
    {
      this(a, b, 19 + str1.length() + str2.length() + "CharMatcher.and(" + str1 + ", " + str2 + ")");
    }
    
    And(CharMatcher a, CharMatcher b, String description)
    {
      super();
      this.first = ((CharMatcher)Preconditions.checkNotNull(a));
      this.second = ((CharMatcher)Preconditions.checkNotNull(b));
    }
    
    public boolean matches(char c)
    {
      return (this.first.matches(c)) && (this.second.matches(c));
    }
    
    @GwtIncompatible("java.util.BitSet")
    void setBits(BitSet table)
    {
      BitSet tmp1 = new BitSet();
      this.first.setBits(tmp1);
      BitSet tmp2 = new BitSet();
      this.second.setBits(tmp2);
      tmp1.and(tmp2);
      table.or(tmp1);
    }
    
    CharMatcher withToString(String description)
    {
      return new And(this.first, this.second, description);
    }
  }
  
  public CharMatcher or(CharMatcher other)
  {
    return new Or(this, (CharMatcher)Preconditions.checkNotNull(other));
  }
  
  private static class Or
    extends CharMatcher
  {
    final CharMatcher first;
    final CharMatcher second;
    
    Or(CharMatcher a, CharMatcher b, String description)
    {
      super();
      this.first = ((CharMatcher)Preconditions.checkNotNull(a));
      this.second = ((CharMatcher)Preconditions.checkNotNull(b));
    }
    
    Or(CharMatcher a, CharMatcher b)
    {
      this(a, b, 18 + str1.length() + str2.length() + "CharMatcher.or(" + str1 + ", " + str2 + ")");
    }
    
    @GwtIncompatible("java.util.BitSet")
    void setBits(BitSet table)
    {
      this.first.setBits(table);
      this.second.setBits(table);
    }
    
    public boolean matches(char c)
    {
      return (this.first.matches(c)) || (this.second.matches(c));
    }
    
    CharMatcher withToString(String description)
    {
      return new Or(this.first, this.second, description);
    }
  }
  
  public CharMatcher precomputed()
  {
    return Platform.precomputeCharMatcher(this);
  }
  
  CharMatcher withToString(String description)
  {
    throw new UnsupportedOperationException();
  }
  
  static abstract class FastMatcher
    extends CharMatcher
  {
    FastMatcher() {}
    
    FastMatcher(String description)
    {
      super();
    }
    
    public final CharMatcher precomputed()
    {
      return this;
    }
    
    public CharMatcher negate()
    {
      return new CharMatcher.NegatedFastMatcher(this);
    }
  }
  
  static final class NegatedFastMatcher
    extends CharMatcher.NegatedMatcher
  {
    NegatedFastMatcher(CharMatcher original)
    {
      super();
    }
    
    NegatedFastMatcher(String toString, CharMatcher original)
    {
      super(original);
    }
    
    public final CharMatcher precomputed()
    {
      return this;
    }
    
    CharMatcher withToString(String description)
    {
      return new NegatedFastMatcher(description, this.original);
    }
  }
  
  @GwtIncompatible("java.util.BitSet")
  private static CharMatcher precomputedPositive(int totalCharacters, BitSet table, String description)
  {
    switch (totalCharacters)
    {
    case 0: 
      return NONE;
    case 1: 
      return is((char)table.nextSetBit(0));
    case 2: 
      char c1 = (char)table.nextSetBit(0);
      char c2 = (char)table.nextSetBit(c1 + '\001');
      return isEither(c1, c2);
    }
    return isSmall(totalCharacters, table.length()) ? SmallCharMatcher.from(table, description) : new BitSetMatcher(table, description, null);
  }
  
  @GwtIncompatible("SmallCharMatcher")
  private static boolean isSmall(int totalCharacters, int tableLength)
  {
    return (totalCharacters <= 1023) && (tableLength > totalCharacters * 4 * 16);
  }
  
  @GwtIncompatible("java.util.BitSet")
  private static class BitSetMatcher
    extends CharMatcher.FastMatcher
  {
    private final BitSet table;
    
    private BitSetMatcher(BitSet table, String description)
    {
      super();
      if (table.length() + 64 < table.size()) {
        table = (BitSet)table.clone();
      }
      this.table = table;
    }
    
    public boolean matches(char c)
    {
      return this.table.get(c);
    }
    
    void setBits(BitSet bitSet)
    {
      bitSet.or(this.table);
    }
  }
  
  @GwtIncompatible("java.util.BitSet")
  void setBits(BitSet table)
  {
    for (int c = 65535; c >= 0; c--) {
      if (matches((char)c)) {
        table.set(c);
      }
    }
  }
  
  public boolean matchesAnyOf(CharSequence sequence)
  {
    return !matchesNoneOf(sequence);
  }
  
  public boolean matchesAllOf(CharSequence sequence)
  {
    for (int i = sequence.length() - 1; i >= 0; i--) {
      if (!matches(sequence.charAt(i))) {
        return false;
      }
    }
    return true;
  }
  
  public boolean matchesNoneOf(CharSequence sequence)
  {
    return indexIn(sequence) == -1;
  }
  
  public int indexIn(CharSequence sequence)
  {
    int length = sequence.length();
    for (int i = 0; i < length; i++) {
      if (matches(sequence.charAt(i))) {
        return i;
      }
    }
    return -1;
  }
  
  public int indexIn(CharSequence sequence, int start)
  {
    int length = sequence.length();
    Preconditions.checkPositionIndex(start, length);
    for (int i = start; i < length; i++) {
      if (matches(sequence.charAt(i))) {
        return i;
      }
    }
    return -1;
  }
  
  public int lastIndexIn(CharSequence sequence)
  {
    for (int i = sequence.length() - 1; i >= 0; i--) {
      if (matches(sequence.charAt(i))) {
        return i;
      }
    }
    return -1;
  }
  
  public int countIn(CharSequence sequence)
  {
    int count = 0;
    for (int i = 0; i < sequence.length(); i++) {
      if (matches(sequence.charAt(i))) {
        count++;
      }
    }
    return count;
  }
  
  @CheckReturnValue
  public String removeFrom(CharSequence sequence)
  {
    String string = sequence.toString();
    int pos = indexIn(string);
    if (pos == -1) {
      return string;
    }
    char[] chars = string.toCharArray();
    int spread = 1;
    for (;;)
    {
      pos++;
      for (;;)
      {
        if (pos == chars.length) {
          break label79;
        }
        if (matches(chars[pos])) {
          break;
        }
        chars[(pos - spread)] = chars[pos];
        pos++;
      }
      spread++;
    }
    label79:
    return new String(chars, 0, pos - spread);
  }
  
  @CheckReturnValue
  public String retainFrom(CharSequence sequence)
  {
    return negate().removeFrom(sequence);
  }
  
  @CheckReturnValue
  public String replaceFrom(CharSequence sequence, char replacement)
  {
    String string = sequence.toString();
    int pos = indexIn(string);
    if (pos == -1) {
      return string;
    }
    char[] chars = string.toCharArray();
    chars[pos] = replacement;
    for (int i = pos + 1; i < chars.length; i++) {
      if (matches(chars[i])) {
        chars[i] = replacement;
      }
    }
    return new String(chars);
  }
  
  @CheckReturnValue
  public String replaceFrom(CharSequence sequence, CharSequence replacement)
  {
    int replacementLen = replacement.length();
    if (replacementLen == 0) {
      return removeFrom(sequence);
    }
    if (replacementLen == 1) {
      return replaceFrom(sequence, replacement.charAt(0));
    }
    String string = sequence.toString();
    int pos = indexIn(string);
    if (pos == -1) {
      return string;
    }
    int len = string.length();
    StringBuilder buf = new StringBuilder(len * 3 / 2 + 16);
    
    int oldpos = 0;
    do
    {
      buf.append(string, oldpos, pos);
      buf.append(replacement);
      oldpos = pos + 1;
      pos = indexIn(string, oldpos);
    } while (pos != -1);
    buf.append(string, oldpos, len);
    return buf.toString();
  }
  
  @CheckReturnValue
  public String trimFrom(CharSequence sequence)
  {
    int len = sequence.length();
    for (int first = 0; first < len; first++) {
      if (!matches(sequence.charAt(first))) {
        break;
      }
    }
    for (int last = len - 1; last > first; last--) {
      if (!matches(sequence.charAt(last))) {
        break;
      }
    }
    return sequence.subSequence(first, last + 1).toString();
  }
  
  @CheckReturnValue
  public String trimLeadingFrom(CharSequence sequence)
  {
    int len = sequence.length();
    for (int first = 0; first < len; first++) {
      if (!matches(sequence.charAt(first))) {
        return sequence.subSequence(first, len).toString();
      }
    }
    return "";
  }
  
  @CheckReturnValue
  public String trimTrailingFrom(CharSequence sequence)
  {
    int len = sequence.length();
    for (int last = len - 1; last >= 0; last--) {
      if (!matches(sequence.charAt(last))) {
        return sequence.subSequence(0, last + 1).toString();
      }
    }
    return "";
  }
  
  @CheckReturnValue
  public String collapseFrom(CharSequence sequence, char replacement)
  {
    int len = sequence.length();
    for (int i = 0; i < len; i++)
    {
      char c = sequence.charAt(i);
      if (matches(c)) {
        if ((c == replacement) && ((i == len - 1) || (!matches(sequence.charAt(i + 1)))))
        {
          i++;
        }
        else
        {
          StringBuilder builder = new StringBuilder(len).append(sequence.subSequence(0, i)).append(replacement);
          
          return finishCollapseFrom(sequence, i + 1, len, replacement, builder, true);
        }
      }
    }
    return sequence.toString();
  }
  
  @CheckReturnValue
  public String trimAndCollapseFrom(CharSequence sequence, char replacement)
  {
    int len = sequence.length();
    for (int first = 0; (first < len) && (matches(sequence.charAt(first))); first++) {}
    for (int last = len - 1; (last > first) && (matches(sequence.charAt(last))); last--) {}
    return (first == 0) && (last == len - 1) ? collapseFrom(sequence, replacement) : finishCollapseFrom(sequence, first, last + 1, replacement, new StringBuilder(last + 1 - first), false);
  }
  
  private String finishCollapseFrom(CharSequence sequence, int start, int end, char replacement, StringBuilder builder, boolean inMatchingGroup)
  {
    for (int i = start; i < end; i++)
    {
      char c = sequence.charAt(i);
      if (matches(c))
      {
        if (!inMatchingGroup)
        {
          builder.append(replacement);
          inMatchingGroup = true;
        }
      }
      else
      {
        builder.append(c);
        inMatchingGroup = false;
      }
    }
    return builder.toString();
  }
  
  @Deprecated
  public boolean apply(Character character)
  {
    return matches(character.charValue());
  }
  
  public String toString()
  {
    return this.description;
  }
  
  static final int WHITESPACE_SHIFT = Integer.numberOfLeadingZeros(" 　\r   　 \013　   　 \t     \f 　 　　 \n 　".length() - 1);
  public static final CharMatcher WHITESPACE = new FastMatcher("WHITESPACE")
  {
    public boolean matches(char c)
    {
      return " 　\r   　 \013　   　 \t     \f 　 　　 \n 　".charAt(1682554634 * c >>> WHITESPACE_SHIFT) == c;
    }
    
    @GwtIncompatible("java.util.BitSet")
    void setBits(BitSet table)
    {
      for (int i = 0; i < " 　\r   　 \013　   　 \t     \f 　 　　 \n 　".length(); i++) {
        table.set(" 　\r   　 \013　   　 \t     \f 　 　　 \n 　".charAt(i));
      }
    }
  };
  
  public abstract boolean matches(char paramChar);
  
  /* Error */
  @GwtIncompatible("java.util.BitSet")
  CharMatcher precomputedInternal()
  {
    // Byte code:
    //   0: new 63	java/util/BitSet
    //   3: dup
    //   4: invokespecial 64	java/util/BitSet:<init>	()V
    //   7: astore_1
    //   8: aload_0
    //   9: aload_1
    //   10: invokevirtual 65	com/google/common/base/CharMatcher:setBits	(Ljava/util/BitSet;)V
    //   13: aload_1
    //   14: invokevirtual 66	java/util/BitSet:cardinality	()I
    //   17: istore_2
    //   18: iload_2
    //   19: iconst_2
    //   20: imul
    //   21: ldc 67
    //   23: if_icmpgt +13 -> 36
    //   26: iload_2
    //   27: aload_1
    //   28: aload_0
    //   29: getfield 52	com/google/common/base/CharMatcher:description	Ljava/lang/String;
    //   32: invokestatic 68	com/google/common/base/CharMatcher:precomputedPositive	(ILjava/util/BitSet;Ljava/lang/String;)Lcom/google/common/base/CharMatcher;
    //   35: areturn
    //   36: aload_1
    //   37: iconst_0
    //   38: ldc 67
    //   40: invokevirtual 69	java/util/BitSet:flip	(II)V
    //   43: ldc 67
    //   45: iload_2
    //   46: isub
    //   47: istore_3
    //   48: ldc 70
    //   50: astore 4
    //   52: aload_0
    //   53: getfield 52	com/google/common/base/CharMatcher:description	Ljava/lang/String;
    //   56: aload 4
    //   58: invokevirtual 71	java/lang/String:endsWith	(Ljava/lang/String;)Z
    //   61: ifeq +27 -> 88
    //   64: aload_0
    //   65: getfield 52	com/google/common/base/CharMatcher:description	Ljava/lang/String;
    //   68: iconst_0
    //   69: aload_0
    //   70: getfield 52	com/google/common/base/CharMatcher:description	Ljava/lang/String;
    //   73: invokevirtual 11	java/lang/String:length	()I
    //   76: aload 4
    //   78: invokevirtual 11	java/lang/String:length	()I
    //   81: isub
    //   82: invokevirtual 72	java/lang/String:substring	(II)Ljava/lang/String;
    //   85: goto +37 -> 122
    //   88: aload_0
    //   89: getfield 52	com/google/common/base/CharMatcher:description	Ljava/lang/String;
    //   92: invokestatic 9	java/lang/String:valueOf	(Ljava/lang/Object;)Ljava/lang/String;
    //   95: aload 4
    //   97: invokestatic 9	java/lang/String:valueOf	(Ljava/lang/Object;)Ljava/lang/String;
    //   100: dup
    //   101: invokevirtual 11	java/lang/String:length	()I
    //   104: ifeq +9 -> 113
    //   107: invokevirtual 73	java/lang/String:concat	(Ljava/lang/String;)Ljava/lang/String;
    //   110: goto +12 -> 122
    //   113: pop
    //   114: new 74	java/lang/String
    //   117: dup_x1
    //   118: swap
    //   119: invokespecial 75	java/lang/String:<init>	(Ljava/lang/String;)V
    //   122: astore 5
    //   124: new 76	com/google/common/base/CharMatcher$NegatedFastMatcher
    //   127: dup
    //   128: aload_0
    //   129: invokevirtual 77	com/google/common/base/CharMatcher:toString	()Ljava/lang/String;
    //   132: iload_3
    //   133: aload_1
    //   134: aload 5
    //   136: invokestatic 68	com/google/common/base/CharMatcher:precomputedPositive	(ILjava/util/BitSet;Ljava/lang/String;)Lcom/google/common/base/CharMatcher;
    //   139: invokespecial 78	com/google/common/base/CharMatcher$NegatedFastMatcher:<init>	(Ljava/lang/String;Lcom/google/common/base/CharMatcher;)V
    //   142: areturn
    // Line number table:
    //   Java source line #792	-> byte code offset #0
    //   Java source line #793	-> byte code offset #8
    //   Java source line #794	-> byte code offset #13
    //   Java source line #795	-> byte code offset #18
    //   Java source line #796	-> byte code offset #26
    //   Java source line #799	-> byte code offset #36
    //   Java source line #800	-> byte code offset #43
    //   Java source line #801	-> byte code offset #48
    //   Java source line #802	-> byte code offset #52
    //   Java source line #805	-> byte code offset #124
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	143	0	this	CharMatcher
    //   8	135	1	table	BitSet
    //   18	125	2	totalCharacters	int
    //   48	95	3	negatedCharacters	int
    //   52	91	4	suffix	String
    //   124	19	5	negatedDescription	String
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\base\CharMatcher.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */