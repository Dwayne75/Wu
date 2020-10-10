package com.google.common.base;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;

@Beta
@GwtCompatible
public final class Utf8
{
  public static int encodedLength(CharSequence sequence)
  {
    int utf16Length = sequence.length();
    int utf8Length = utf16Length;
    int i = 0;
    while ((i < utf16Length) && (sequence.charAt(i) < '')) {
      i++;
    }
    char c;
    for (; i < utf16Length; i++)
    {
      c = sequence.charAt(i);
      if (c < 'ࠀ')
      {
        utf8Length += ('' - c >>> 31);
      }
      else
      {
        utf8Length += encodedLengthGeneral(sequence, i);
        break;
      }
    }
    if (utf8Length < utf16Length)
    {
      c = utf8Length + 4294967296L;throw new IllegalArgumentException(54 + "UTF-8 length does not fit in int: " + c);
    }
    return utf8Length;
  }
  
  private static int encodedLengthGeneral(CharSequence sequence, int start)
  {
    int utf16Length = sequence.length();
    int utf8Length = 0;
    for (int i = start; i < utf16Length; i++)
    {
      char c = sequence.charAt(i);
      if (c < 'ࠀ')
      {
        utf8Length += ('' - c >>> 31);
      }
      else
      {
        utf8Length += 2;
        if ((55296 <= c) && (c <= 57343))
        {
          int cp = Character.codePointAt(sequence, i);
          if (cp < 65536)
          {
            int i = i;throw new IllegalArgumentException(39 + "Unpaired surrogate at index " + i);
          }
          i++;
        }
      }
    }
    return utf8Length;
  }
  
  public static boolean isWellFormed(byte[] bytes)
  {
    return isWellFormed(bytes, 0, bytes.length);
  }
  
  public static boolean isWellFormed(byte[] bytes, int off, int len)
  {
    int end = off + len;
    Preconditions.checkPositionIndexes(off, end, bytes.length);
    for (int i = off; i < end; i++) {
      if (bytes[i] < 0) {
        return isWellFormedSlowPath(bytes, i, end);
      }
    }
    return true;
  }
  
  private static boolean isWellFormedSlowPath(byte[] bytes, int off, int end)
  {
    int index = off;
    for (;;)
    {
      if (index >= end) {
        return true;
      }
      int byte1;
      if ((byte1 = bytes[(index++)]) < 0) {
        if (byte1 < -32)
        {
          if (index == end) {
            return false;
          }
          if ((byte1 < -62) || (bytes[(index++)] > -65)) {
            return false;
          }
        }
        else if (byte1 < -16)
        {
          if (index + 1 >= end) {
            return false;
          }
          int byte2 = bytes[(index++)];
          if ((byte2 > -65) || ((byte1 == -32) && (byte2 < -96)) || ((byte1 == -19) && (-96 <= byte2)) || (bytes[(index++)] > -65)) {
            return false;
          }
        }
        else
        {
          if (index + 2 >= end) {
            return false;
          }
          int byte2 = bytes[(index++)];
          if ((byte2 > -65) || ((byte1 << 28) + (byte2 - -112) >> 30 != 0) || (bytes[(index++)] > -65) || (bytes[(index++)] > -65)) {
            return false;
          }
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\base\Utf8.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */