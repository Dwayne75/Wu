package com.sun.xml.xsom.impl.scd;

import java.io.IOException;
import java.io.PrintStream;

public class SCDParserTokenManager
  implements SCDParserConstants
{
  public PrintStream debugStream = System.out;
  
  public void setDebugStream(PrintStream ds)
  {
    this.debugStream = ds;
  }
  
  private final int jjStopStringLiteralDfa_0(int pos, long active0)
  {
    switch (pos)
    {
    case 0: 
      if ((active0 & 0x3C08000000) != 0L)
      {
        this.jjmatchedKind = 12;
        return 103;
      }
      if ((active0 & 0x400000) != 0L)
      {
        this.jjmatchedKind = 12;
        return 55;
      }
      if ((active0 & 0x30000000000) != 0L)
      {
        this.jjmatchedKind = 12;
        return 68;
      }
      if ((active0 & 0x2000000) != 0L)
      {
        this.jjmatchedKind = 12;
        return 81;
      }
      if ((active0 & 0x200000000) != 0L)
      {
        this.jjmatchedKind = 12;
        return 23;
      }
      if ((active0 & 0x40000000000) != 0L)
      {
        this.jjmatchedKind = 12;
        return 34;
      }
      if ((active0 & 0x100000) != 0L)
      {
        this.jjmatchedKind = 12;
        return 91;
      }
      if ((active0 & 0x18C1F4240000) != 0L)
      {
        this.jjmatchedKind = 12;
        return 1;
      }
      if ((active0 & 0x1000000) != 0L)
      {
        this.jjmatchedKind = 12;
        return 16;
      }
      return -1;
    case 1: 
      if ((active0 & 0x1FFFFF740000) != 0L)
      {
        this.jjmatchedKind = 12;
        this.jjmatchedPos = 1;
        return 1;
      }
      return -1;
    case 2: 
      if ((active0 & 0x1FFFFF740000) != 0L)
      {
        this.jjmatchedKind = 12;
        this.jjmatchedPos = 2;
        return 1;
      }
      return -1;
    case 3: 
      if ((active0 & 0x4100000000) != 0L)
      {
        if (this.jjmatchedPos < 2)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 2;
        }
        return -1;
      }
      if ((active0 & 0x1FBEFF740000) != 0L)
      {
        this.jjmatchedKind = 12;
        this.jjmatchedPos = 3;
        return 1;
      }
      return -1;
    case 4: 
      if ((active0 & 0x4100000000) != 0L)
      {
        if (this.jjmatchedPos < 2)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 2;
        }
        return -1;
      }
      if ((active0 & 0x400000) != 0L)
      {
        if (this.jjmatchedPos < 3)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 3;
        }
        return -1;
      }
      if ((active0 & 0x1FBEFF340000) != 0L)
      {
        this.jjmatchedKind = 12;
        this.jjmatchedPos = 4;
        return 1;
      }
      return -1;
    case 5: 
      if ((active0 & 0x4000000000) != 0L)
      {
        if (this.jjmatchedPos < 2)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 2;
        }
        return -1;
      }
      if ((active0 & 0x33C50000000) != 0L)
      {
        if (this.jjmatchedPos < 4)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 4;
        }
        return -1;
      }
      if ((active0 & 0x400000) != 0L)
      {
        if (this.jjmatchedPos < 3)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 3;
        }
        return -1;
      }
      if ((active0 & 0x1C82AF340000) != 0L)
      {
        this.jjmatchedKind = 12;
        this.jjmatchedPos = 5;
        return 1;
      }
      return -1;
    case 6: 
      if ((active0 & 0x33C50000000) != 0L)
      {
        if (this.jjmatchedPos < 4)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 4;
        }
        return -1;
      }
      if ((active0 & 0x1C82AF340000) != 0L)
      {
        if (this.jjmatchedPos != 6)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 6;
        }
        return 1;
      }
      return -1;
    case 7: 
      if ((active0 & 0x100000) != 0L)
      {
        if (this.jjmatchedPos < 6)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 6;
        }
        return -1;
      }
      if ((active0 & 0x13C00000000) != 0L)
      {
        if (this.jjmatchedPos < 4)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 4;
        }
        return -1;
      }
      if ((active0 & 0x1C82AF240000) != 0L)
      {
        this.jjmatchedKind = 12;
        this.jjmatchedPos = 7;
        return 1;
      }
      return -1;
    case 8: 
      if ((active0 & 0x480AA240000) != 0L)
      {
        this.jjmatchedKind = 12;
        this.jjmatchedPos = 8;
        return 1;
      }
      if ((active0 & 0x180205000000) != 0L)
      {
        if (this.jjmatchedPos < 7)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 7;
        }
        return -1;
      }
      if ((active0 & 0x100000) != 0L)
      {
        if (this.jjmatchedPos < 6)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 6;
        }
        return -1;
      }
      if ((active0 & 0x1C00000000) != 0L)
      {
        if (this.jjmatchedPos < 4)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 4;
        }
        return -1;
      }
      return -1;
    case 9: 
      if ((active0 & 0x80AA200000) != 0L)
      {
        if (this.jjmatchedPos != 9)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 9;
        }
        return 1;
      }
      if ((active0 & 0x180205000000) != 0L)
      {
        if (this.jjmatchedPos < 7)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 7;
        }
        return -1;
      }
      if ((active0 & 0x40000040000) != 0L)
      {
        if (this.jjmatchedPos < 8)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 8;
        }
        return -1;
      }
      if ((active0 & 0x1C00000000) != 0L)
      {
        if (this.jjmatchedPos < 4)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 4;
        }
        return -1;
      }
      return -1;
    case 10: 
      if ((active0 & 0x100000000000) != 0L)
      {
        if (this.jjmatchedPos < 7)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 7;
        }
        return -1;
      }
      if ((active0 & 0x8000000) != 0L)
      {
        if (this.jjmatchedPos < 9)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 9;
        }
        return -1;
      }
      if ((active0 & 0x40000040000) != 0L)
      {
        if (this.jjmatchedPos < 8)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 8;
        }
        return -1;
      }
      if ((active0 & 0x80A2200000) != 0L)
      {
        this.jjmatchedKind = 12;
        this.jjmatchedPos = 10;
        return 1;
      }
      if ((active0 & 0xC00000000) != 0L)
      {
        if (this.jjmatchedPos < 4)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 4;
        }
        return -1;
      }
      return -1;
    case 11: 
      if ((active0 & 0x40000000000) != 0L)
      {
        if (this.jjmatchedPos < 8)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 8;
        }
        return -1;
      }
      if ((active0 & 0x8000000) != 0L)
      {
        if (this.jjmatchedPos < 9)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 9;
        }
        return -1;
      }
      if ((active0 & 0xC00000000) != 0L)
      {
        if (this.jjmatchedPos < 4)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 4;
        }
        return -1;
      }
      if ((active0 & 0x80A2200000) != 0L)
      {
        this.jjmatchedKind = 12;
        this.jjmatchedPos = 11;
        return 1;
      }
      return -1;
    case 12: 
      if ((active0 & 0x8000000000) != 0L)
      {
        if (this.jjmatchedPos < 11)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 11;
        }
        return -1;
      }
      if ((active0 & 0xC00000000) != 0L)
      {
        if (this.jjmatchedPos < 4)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 4;
        }
        return -1;
      }
      if ((active0 & 0xA2200000) != 0L)
      {
        this.jjmatchedKind = 12;
        this.jjmatchedPos = 12;
        return 1;
      }
      return -1;
    case 13: 
      if ((active0 & 0x8000000000) != 0L)
      {
        if (this.jjmatchedPos < 11)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 11;
        }
        return -1;
      }
      if ((active0 & 0x2000000) != 0L)
      {
        if (this.jjmatchedPos < 12)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 12;
        }
        return -1;
      }
      if ((active0 & 0x400000000) != 0L)
      {
        if (this.jjmatchedPos < 4)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 4;
        }
        return -1;
      }
      if ((active0 & 0xA0200000) != 0L)
      {
        this.jjmatchedKind = 12;
        this.jjmatchedPos = 13;
        return 1;
      }
      return -1;
    case 14: 
      if ((active0 & 0x8000000000) != 0L)
      {
        if (this.jjmatchedPos < 11)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 11;
        }
        return -1;
      }
      if ((active0 & 0x20000000) != 0L)
      {
        if (this.jjmatchedPos < 13)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 13;
        }
        return -1;
      }
      if ((active0 & 0x2000000) != 0L)
      {
        if (this.jjmatchedPos < 12)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 12;
        }
        return -1;
      }
      if ((active0 & 0x400000000) != 0L)
      {
        if (this.jjmatchedPos < 4)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 4;
        }
        return -1;
      }
      if ((active0 & 0x80200000) != 0L)
      {
        this.jjmatchedKind = 12;
        this.jjmatchedPos = 14;
        return 1;
      }
      return -1;
    case 15: 
      if ((active0 & 0x20000000) != 0L)
      {
        if (this.jjmatchedPos < 13)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 13;
        }
        return -1;
      }
      if ((active0 & 0x80200000) != 0L)
      {
        this.jjmatchedKind = 12;
        this.jjmatchedPos = 15;
        return 1;
      }
      return -1;
    case 16: 
      if ((active0 & 0x80200000) != 0L)
      {
        this.jjmatchedKind = 12;
        this.jjmatchedPos = 16;
        return 1;
      }
      return -1;
    case 17: 
      if ((active0 & 0x80200000) != 0L)
      {
        if (this.jjmatchedPos < 16)
        {
          this.jjmatchedKind = 12;
          this.jjmatchedPos = 16;
        }
        return -1;
      }
      return -1;
    }
    return -1;
  }
  
  private final int jjStartNfa_0(int pos, long active0)
  {
    return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
  }
  
  private final int jjStopAtPos(int pos, int kind)
  {
    this.jjmatchedKind = kind;
    this.jjmatchedPos = pos;
    return pos + 1;
  }
  
  private final int jjStartNfaWithStates_0(int pos, int kind, int state)
  {
    this.jjmatchedKind = kind;
    this.jjmatchedPos = pos;
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      return pos + 1;
    }
    return jjMoveNfa_0(state, pos + 1);
  }
  
  private final int jjMoveStringLiteralDfa0_0()
  {
    switch (this.curChar)
    {
    case '*': 
      return jjStopAtPos(0, 45);
    case '/': 
      this.jjmatchedKind = 16;
      return jjMoveStringLiteralDfa1_0(131072L);
    case '0': 
      return jjStopAtPos(0, 46);
    case ':': 
      return jjStopAtPos(0, 15);
    case '@': 
      return jjStopAtPos(0, 19);
    case 'a': 
      return jjMoveStringLiteralDfa1_0(825170853888L);
    case 'b': 
      return jjMoveStringLiteralDfa1_0(16777216L);
    case 'c': 
      return jjMoveStringLiteralDfa1_0(4398046511104L);
    case 'e': 
      return jjMoveStringLiteralDfa1_0(1048576L);
    case 'f': 
      return jjMoveStringLiteralDfa1_0(3298534883328L);
    case 'g': 
      return jjMoveStringLiteralDfa1_0(1073741824L);
    case 'i': 
      return jjMoveStringLiteralDfa1_0(2214592512L);
    case 'k': 
      return jjMoveStringLiteralDfa1_0(4294967296L);
    case 'm': 
      return jjMoveStringLiteralDfa1_0(257832255488L);
    case 'n': 
      return jjMoveStringLiteralDfa1_0(8589934592L);
    case 'p': 
      return jjMoveStringLiteralDfa1_0(33554432L);
    case 's': 
      return jjMoveStringLiteralDfa1_0(270532608L);
    case 't': 
      return jjMoveStringLiteralDfa1_0(4194304L);
    case 'x': 
      return jjMoveStringLiteralDfa1_0(26388279066624L);
    case '~': 
      return jjStopAtPos(0, 23);
    }
    return jjMoveNfa_0(0, 0);
  }
  
  private final int jjMoveStringLiteralDfa1_0(long active0)
  {
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      jjStopStringLiteralDfa_0(0, active0);
      return 1;
    }
    switch (this.curChar)
    {
    case '-': 
      return jjMoveStringLiteralDfa2_0(active0, 26388279066624L);
    case '/': 
      if ((active0 & 0x20000) != 0L) {
        return jjStopAtPos(1, 17);
      }
      break;
    case 'a': 
      return jjMoveStringLiteralDfa2_0(active0, 3298551660544L);
    case 'c': 
      return jjMoveStringLiteralDfa2_0(active0, 268435456L);
    case 'd': 
      return jjMoveStringLiteralDfa2_0(active0, 2147483648L);
    case 'e': 
      return jjMoveStringLiteralDfa2_0(active0, 4429185024L);
    case 'l': 
      return jjMoveStringLiteralDfa2_0(active0, 1048576L);
    case 'n': 
      return jjMoveStringLiteralDfa2_0(active0, 824633720832L);
    case 'o': 
      return jjMoveStringLiteralDfa2_0(active0, 4664334483456L);
    case 'r': 
      return jjMoveStringLiteralDfa2_0(active0, 1107296256L);
    case 't': 
      return jjMoveStringLiteralDfa2_0(active0, 604241920L);
    case 'u': 
      return jjMoveStringLiteralDfa2_0(active0, 2097152L);
    case 'y': 
      return jjMoveStringLiteralDfa2_0(active0, 4194304L);
    }
    return jjStartNfa_0(0, active0);
  }
  
  private final int jjMoveStringLiteralDfa2_0(long old0, long active0)
  {
    if ((active0 &= old0) == 0L) {
      return jjStartNfa_0(0, old0);
    }
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      jjStopStringLiteralDfa_0(1, active0);
      return 2;
    }
    switch (this.curChar)
    {
    case 'b': 
      return jjMoveStringLiteralDfa3_0(active0, 2097152L);
    case 'c': 
      return jjMoveStringLiteralDfa3_0(active0, 3298534883328L);
    case 'd': 
      return jjMoveStringLiteralDfa3_0(active0, 257698037760L);
    case 'e': 
      return jjMoveStringLiteralDfa3_0(active0, 2215641088L);
    case 'i': 
      return jjMoveStringLiteralDfa3_0(active0, 33554432L);
    case 'm': 
      return jjMoveStringLiteralDfa3_0(active0, 4398180728832L);
    case 'o': 
      return jjMoveStringLiteralDfa3_0(active0, 1342177280L);
    case 'p': 
      return jjMoveStringLiteralDfa3_0(active0, 4194304L);
    case 's': 
      return jjMoveStringLiteralDfa3_0(active0, 26388295843840L);
    case 't': 
      return jjMoveStringLiteralDfa3_0(active0, 9127067648L);
    case 'y': 
      return jjMoveStringLiteralDfa3_0(active0, 828928688128L);
    }
    return jjStartNfa_0(1, active0);
  }
  
  private final int jjMoveStringLiteralDfa3_0(long old0, long active0)
  {
    if ((active0 &= old0) == 0L) {
      return jjStartNfa_0(1, old0);
    }
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      jjStopStringLiteralDfa_0(2, active0);
      return 3;
    }
    switch (this.curChar)
    {
    case ':': 
      return jjMoveStringLiteralDfa4_0(active0, 279172874240L);
    case 'A': 
      return jjMoveStringLiteralDfa4_0(active0, 549755813888L);
    case 'a': 
      return jjMoveStringLiteralDfa4_0(active0, 8589934592L);
    case 'b': 
      return jjMoveStringLiteralDfa4_0(active0, 134217728L);
    case 'c': 
      return jjMoveStringLiteralDfa4_0(active0, 26388279066624L);
    case 'e': 
      return jjMoveStringLiteralDfa4_0(active0, 3556253892608L);
    case 'm': 
      return jjMoveStringLiteralDfa4_0(active0, 101711872L);
    case 'n': 
      return jjMoveStringLiteralDfa4_0(active0, 2147483648L);
    case 'p': 
      return jjMoveStringLiteralDfa4_0(active0, 4398314946560L);
    case 'r': 
      return jjMoveStringLiteralDfa4_0(active0, 537133056L);
    case 's': 
      return jjMoveStringLiteralDfa4_0(active0, 2097152L);
    case 'u': 
      return jjMoveStringLiteralDfa4_0(active0, 1073741824L);
    }
    return jjStartNfa_0(2, active0);
  }
  
  private final int jjMoveStringLiteralDfa4_0(long old0, long active0)
  {
    if ((active0 &= old0) == 0L) {
      return jjStartNfa_0(2, old0);
    }
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      jjStopStringLiteralDfa_0(3, active0);
      return 4;
    }
    switch (this.curChar)
    {
    case ':': 
      if ((active0 & 0x100000000) != 0L) {
        return jjStopAtPos(4, 32);
      }
      return jjMoveStringLiteralDfa5_0(active0, 274882101248L);
    case 'T': 
      return jjMoveStringLiteralDfa5_0(active0, 83886080L);
    case 'e': 
      return jjMoveStringLiteralDfa5_0(active0, 403701760L);
    case 'h': 
      return jjMoveStringLiteralDfa5_0(active0, 26388279066624L);
    case 'i': 
      return jjMoveStringLiteralDfa5_0(active0, 570687488L);
    case 'l': 
      return jjMoveStringLiteralDfa5_0(active0, 257698037760L);
    case 'o': 
      return jjMoveStringLiteralDfa5_0(active0, 4398046511104L);
    case 'p': 
      return jjMoveStringLiteralDfa5_0(active0, 1073741824L);
    case 't': 
      return jjMoveStringLiteralDfa5_0(active0, 3859030212608L);
    }
    return jjStartNfa_0(3, active0);
  }
  
  private final int jjMoveStringLiteralDfa5_0(long old0, long active0)
  {
    if ((active0 &= old0) == 0L) {
      return jjStartNfa_0(3, old0);
    }
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      jjStopStringLiteralDfa_0(4, active0);
      return 5;
    }
    switch (this.curChar)
    {
    case '*': 
      if ((active0 & 0x4000000000) != 0L) {
        return jjStopAtPos(5, 38);
      }
      break;
    case ':': 
      if ((active0 & 0x400000) != 0L) {
        return jjStopAtPos(5, 22);
      }
      return jjMoveStringLiteralDfa6_0(active0, 3557575098368L);
    case 'b': 
      return jjMoveStringLiteralDfa6_0(active0, 537133056L);
    case 'e': 
      return jjMoveStringLiteralDfa6_0(active0, 26388279066624L);
    case 'i': 
      return jjMoveStringLiteralDfa6_0(active0, 10739515392L);
    case 'n': 
      return jjMoveStringLiteralDfa6_0(active0, 4398047559680L);
    case 'r': 
      return jjMoveStringLiteralDfa6_0(active0, 134217728L);
    case 't': 
      return jjMoveStringLiteralDfa6_0(active0, 549789368320L);
    case 'y': 
      return jjMoveStringLiteralDfa6_0(active0, 83886080L);
    }
    return jjStartNfa_0(4, active0);
  }
  
  private final int jjMoveStringLiteralDfa6_0(long old0, long active0)
  {
    if ((active0 &= old0) == 0L) {
      return jjStartNfa_0(4, old0);
    }
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      jjStopStringLiteralDfa_0(5, active0);
      return 6;
    }
    switch (this.curChar)
    {
    case ':': 
      if ((active0 & 0x10000000) != 0L) {
        return jjStopAtPos(6, 28);
      }
      if ((active0 & 0x40000000) != 0L) {
        return jjStopAtPos(6, 30);
      }
      if ((active0 & 0x20000000000) != 0L)
      {
        this.jjmatchedKind = 41;
        this.jjmatchedPos = 6;
      }
      return jjMoveStringLiteralDfa7_0(active0, 1357209665536L);
    case 'T': 
      return jjMoveStringLiteralDfa7_0(active0, 134217728L);
    case 'e': 
      return jjMoveStringLiteralDfa7_0(active0, 4398046511104L);
    case 'i': 
      return jjMoveStringLiteralDfa7_0(active0, 33554432L);
    case 'm': 
      return jjMoveStringLiteralDfa7_0(active0, 26388279066624L);
    case 'o': 
      return jjMoveStringLiteralDfa7_0(active0, 8589934592L);
    case 'p': 
      return jjMoveStringLiteralDfa7_0(active0, 83886080L);
    case 'r': 
      return jjMoveStringLiteralDfa7_0(active0, 549755813888L);
    case 't': 
      return jjMoveStringLiteralDfa7_0(active0, 2150629376L);
    case 'u': 
      return jjMoveStringLiteralDfa7_0(active0, 537133056L);
    }
    return jjStartNfa_0(5, active0);
  }
  
  private final int jjMoveStringLiteralDfa7_0(long old0, long active0)
  {
    if ((active0 &= old0) == 0L) {
      return jjStartNfa_0(5, old0);
    }
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      jjStopStringLiteralDfa_0(6, active0);
      return 7;
    }
    switch (this.curChar)
    {
    case '*': 
      if ((active0 & 0x2000000000) != 0L) {
        return jjStopAtPos(7, 37);
      }
      if ((active0 & 0x10000000000) != 0L) {
        return jjStopAtPos(7, 40);
      }
      break;
    case ':': 
      return jjMoveStringLiteralDfa8_0(active0, 1048576L);
    case 'a': 
      return jjMoveStringLiteralDfa8_0(active0, 26456998543360L);
    case 'c': 
      return jjMoveStringLiteralDfa8_0(active0, 34359738368L);
    case 'e': 
      return jjMoveStringLiteralDfa8_0(active0, 83886080L);
    case 'i': 
      return jjMoveStringLiteralDfa8_0(active0, 549755813888L);
    case 'n': 
      return jjMoveStringLiteralDfa8_0(active0, 4406636445696L);
    case 's': 
      return jjMoveStringLiteralDfa8_0(active0, 17179869184L);
    case 't': 
      return jjMoveStringLiteralDfa8_0(active0, 537133056L);
    case 'u': 
      return jjMoveStringLiteralDfa8_0(active0, 2097152L);
    case 'v': 
      return jjMoveStringLiteralDfa8_0(active0, 33554432L);
    case 'y': 
      return jjMoveStringLiteralDfa8_0(active0, 2281701376L);
    }
    return jjStartNfa_0(6, active0);
  }
  
  private final int jjMoveStringLiteralDfa8_0(long old0, long active0)
  {
    if ((active0 &= old0) == 0L) {
      return jjStartNfa_0(6, old0);
    }
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      jjStopStringLiteralDfa_0(7, active0);
      return 8;
    }
    switch (this.curChar)
    {
    case ':': 
      if ((active0 & 0x100000) != 0L) {
        return jjStopAtPos(8, 20);
      }
      return jjMoveStringLiteralDfa9_0(active0, 26396952887296L);
    case 'C': 
      return jjMoveStringLiteralDfa9_0(active0, 2147483648L);
    case 'b': 
      return jjMoveStringLiteralDfa9_0(active0, 549755813888L);
    case 'e': 
      return jjMoveStringLiteralDfa9_0(active0, 17750556672L);
    case 'h': 
      return jjMoveStringLiteralDfa9_0(active0, 34359738368L);
    case 'l': 
      return jjMoveStringLiteralDfa9_0(active0, 68719476736L);
    case 'p': 
      return jjMoveStringLiteralDfa9_0(active0, 134217728L);
    case 't': 
      return jjMoveStringLiteralDfa9_0(active0, 4398048608256L);
    }
    return jjStartNfa_0(7, active0);
  }
  
  private final int jjMoveStringLiteralDfa9_0(long old0, long active0)
  {
    if ((active0 &= old0) == 0L) {
      return jjStartNfa_0(7, old0);
    }
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      jjStopStringLiteralDfa_0(8, active0);
      return 9;
    }
    switch (this.curChar)
    {
    case ':': 
      if ((active0 & 0x1000000) != 0L) {
        return jjStopAtPos(9, 24);
      }
      if ((active0 & 0x4000000) != 0L) {
        return jjStopAtPos(9, 26);
      }
      if ((active0 & 0x200000000) != 0L) {
        return jjStopAtPos(9, 33);
      }
      if ((active0 & 0x80000000000) != 0L)
      {
        this.jjmatchedKind = 43;
        this.jjmatchedPos = 9;
      }
      return jjMoveStringLiteralDfa10_0(active0, 21990232817664L);
    case 'G': 
      return jjMoveStringLiteralDfa10_0(active0, 536870912L);
    case 'T': 
      return jjMoveStringLiteralDfa10_0(active0, 33554432L);
    case 'e': 
      return jjMoveStringLiteralDfa10_0(active0, 134217728L);
    case 'i': 
      return jjMoveStringLiteralDfa10_0(active0, 2097152L);
    case 'l': 
      if ((active0 & 0x1000000000) != 0L) {
        return jjStopAtPos(9, 36);
      }
      break;
    case 'o': 
      return jjMoveStringLiteralDfa10_0(active0, 36507222016L);
    case 'q': 
      return jjMoveStringLiteralDfa10_0(active0, 17179869184L);
    case 'u': 
      return jjMoveStringLiteralDfa10_0(active0, 549755813888L);
    }
    return jjStartNfa_0(8, active0);
  }
  
  private final int jjMoveStringLiteralDfa10_0(long old0, long active0)
  {
    if ((active0 &= old0) == 0L) {
      return jjStartNfa_0(8, old0);
    }
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      jjStopStringLiteralDfa_0(9, active0);
      return 10;
    }
    switch (this.curChar)
    {
    case '*': 
      if ((active0 & 0x100000000000) != 0L) {
        return jjStopAtPos(10, 44);
      }
      break;
    case ':': 
      if ((active0 & 0x40000) != 0L) {
        return jjStopAtPos(10, 18);
      }
      return jjMoveStringLiteralDfa11_0(active0, 4398180728832L);
    case 'i': 
      return jjMoveStringLiteralDfa11_0(active0, 34359738368L);
    case 'n': 
      return jjMoveStringLiteralDfa11_0(active0, 2147483648L);
    case 'o': 
      return jjMoveStringLiteralDfa11_0(active0, 2097152L);
    case 'r': 
      return jjMoveStringLiteralDfa11_0(active0, 536870912L);
    case 't': 
      return jjMoveStringLiteralDfa11_0(active0, 549755813888L);
    case 'u': 
      return jjMoveStringLiteralDfa11_0(active0, 17179869184L);
    case 'y': 
      return jjMoveStringLiteralDfa11_0(active0, 33554432L);
    }
    return jjStartNfa_0(9, active0);
  }
  
  private final int jjMoveStringLiteralDfa11_0(long old0, long active0)
  {
    if ((active0 &= old0) == 0L) {
      return jjStartNfa_0(9, old0);
    }
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      jjStopStringLiteralDfa_0(10, active0);
      return 11;
    }
    switch (this.curChar)
    {
    case '*': 
      if ((active0 & 0x40000000000) != 0L) {
        return jjStopAtPos(11, 42);
      }
      break;
    case ':': 
      if ((active0 & 0x8000000) != 0L) {
        return jjStopAtPos(11, 27);
      }
      break;
    case 'c': 
      return jjMoveStringLiteralDfa12_0(active0, 34359738368L);
    case 'e': 
      return jjMoveStringLiteralDfa12_0(active0, 566935683072L);
    case 'n': 
      return jjMoveStringLiteralDfa12_0(active0, 2097152L);
    case 'o': 
      return jjMoveStringLiteralDfa12_0(active0, 536870912L);
    case 'p': 
      return jjMoveStringLiteralDfa12_0(active0, 33554432L);
    case 't': 
      return jjMoveStringLiteralDfa12_0(active0, 2147483648L);
    }
    return jjStartNfa_0(10, active0);
  }
  
  private final int jjMoveStringLiteralDfa12_0(long old0, long active0)
  {
    if ((active0 &= old0) == 0L) {
      return jjStartNfa_0(10, old0);
    }
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      jjStopStringLiteralDfa_0(11, active0);
      return 12;
    }
    switch (this.curChar)
    {
    case ':': 
      return jjMoveStringLiteralDfa13_0(active0, 549755813888L);
    case 'G': 
      return jjMoveStringLiteralDfa13_0(active0, 2097152L);
    case 'e': 
      if ((active0 & 0x800000000) != 0L) {
        return jjStopAtPos(12, 35);
      }
      return jjMoveStringLiteralDfa13_0(active0, 33554432L);
    case 'n': 
      return jjMoveStringLiteralDfa13_0(active0, 17179869184L);
    case 'r': 
      return jjMoveStringLiteralDfa13_0(active0, 2147483648L);
    case 'u': 
      return jjMoveStringLiteralDfa13_0(active0, 536870912L);
    }
    return jjStartNfa_0(11, active0);
  }
  
  private final int jjMoveStringLiteralDfa13_0(long old0, long active0)
  {
    if ((active0 &= old0) == 0L) {
      return jjStartNfa_0(11, old0);
    }
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      jjStopStringLiteralDfa_0(12, active0);
      return 13;
    }
    switch (this.curChar)
    {
    case ':': 
      return jjMoveStringLiteralDfa14_0(active0, 549789368320L);
    case 'a': 
      return jjMoveStringLiteralDfa14_0(active0, 2147483648L);
    case 'c': 
      return jjMoveStringLiteralDfa14_0(active0, 17179869184L);
    case 'p': 
      return jjMoveStringLiteralDfa14_0(active0, 536870912L);
    case 'r': 
      return jjMoveStringLiteralDfa14_0(active0, 2097152L);
    }
    return jjStartNfa_0(12, active0);
  }
  
  private final int jjMoveStringLiteralDfa14_0(long old0, long active0)
  {
    if ((active0 &= old0) == 0L) {
      return jjStartNfa_0(12, old0);
    }
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      jjStopStringLiteralDfa_0(13, active0);
      return 14;
    }
    switch (this.curChar)
    {
    case '*': 
      if ((active0 & 0x8000000000) != 0L) {
        return jjStopAtPos(14, 39);
      }
      break;
    case ':': 
      if ((active0 & 0x2000000) != 0L) {
        return jjStopAtPos(14, 25);
      }
      return jjMoveStringLiteralDfa15_0(active0, 536870912L);
    case 'e': 
      if ((active0 & 0x400000000) != 0L) {
        return jjStopAtPos(14, 34);
      }
      break;
    case 'i': 
      return jjMoveStringLiteralDfa15_0(active0, 2147483648L);
    case 'o': 
      return jjMoveStringLiteralDfa15_0(active0, 2097152L);
    }
    return jjStartNfa_0(13, active0);
  }
  
  private final int jjMoveStringLiteralDfa15_0(long old0, long active0)
  {
    if ((active0 &= old0) == 0L) {
      return jjStartNfa_0(13, old0);
    }
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      jjStopStringLiteralDfa_0(14, active0);
      return 15;
    }
    switch (this.curChar)
    {
    case ':': 
      if ((active0 & 0x20000000) != 0L) {
        return jjStopAtPos(15, 29);
      }
      break;
    case 'n': 
      return jjMoveStringLiteralDfa16_0(active0, 2147483648L);
    case 'u': 
      return jjMoveStringLiteralDfa16_0(active0, 2097152L);
    }
    return jjStartNfa_0(14, active0);
  }
  
  private final int jjMoveStringLiteralDfa16_0(long old0, long active0)
  {
    if ((active0 &= old0) == 0L) {
      return jjStartNfa_0(14, old0);
    }
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      jjStopStringLiteralDfa_0(15, active0);
      return 16;
    }
    switch (this.curChar)
    {
    case 'p': 
      return jjMoveStringLiteralDfa17_0(active0, 2097152L);
    case 't': 
      return jjMoveStringLiteralDfa17_0(active0, 2147483648L);
    }
    return jjStartNfa_0(15, active0);
  }
  
  private final int jjMoveStringLiteralDfa17_0(long old0, long active0)
  {
    if ((active0 &= old0) == 0L) {
      return jjStartNfa_0(15, old0);
    }
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      jjStopStringLiteralDfa_0(16, active0);
      return 17;
    }
    switch (this.curChar)
    {
    case ':': 
      return jjMoveStringLiteralDfa18_0(active0, 2149580800L);
    }
    return jjStartNfa_0(16, active0);
  }
  
  private final int jjMoveStringLiteralDfa18_0(long old0, long active0)
  {
    if ((active0 &= old0) == 0L) {
      return jjStartNfa_0(16, old0);
    }
    try
    {
      this.curChar = this.input_stream.readChar();
    }
    catch (IOException e)
    {
      jjStopStringLiteralDfa_0(17, active0);
      return 18;
    }
    switch (this.curChar)
    {
    case ':': 
      if ((active0 & 0x200000) != 0L) {
        return jjStopAtPos(18, 21);
      }
      if ((active0 & 0x80000000) != 0L) {
        return jjStopAtPos(18, 31);
      }
      break;
    }
    return jjStartNfa_0(17, active0);
  }
  
  private final void jjCheckNAdd(int state)
  {
    if (this.jjrounds[state] != this.jjround)
    {
      this.jjstateSet[(this.jjnewStateCnt++)] = state;
      this.jjrounds[state] = this.jjround;
    }
  }
  
  private final void jjAddStates(int start, int end)
  {
    do
    {
      this.jjstateSet[(this.jjnewStateCnt++)] = jjnextStates[start];
    } while (start++ != end);
  }
  
  private final void jjCheckNAddTwoStates(int state1, int state2)
  {
    jjCheckNAdd(state1);
    jjCheckNAdd(state2);
  }
  
  private final void jjCheckNAddStates(int start, int end)
  {
    do
    {
      jjCheckNAdd(jjnextStates[start]);
    } while (start++ != end);
  }
  
  private final void jjCheckNAddStates(int start)
  {
    jjCheckNAdd(jjnextStates[start]);
    jjCheckNAdd(jjnextStates[(start + 1)]);
  }
  
  static final long[] jjbitVec0 = { 0L, -16384L, -17590038560769L, 8388607L };
  static final long[] jjbitVec2 = { 0L, 0L, 0L, -36028797027352577L };
  static final long[] jjbitVec3 = { 9219994337134247935L, 9223372036854775294L, -1L, -274156627316187121L };
  static final long[] jjbitVec4 = { 16777215L, -65536L, -576458553280167937L, 3L };
  static final long[] jjbitVec5 = { 0L, 0L, -17179879616L, 4503588160110591L };
  static final long[] jjbitVec6 = { -8194L, -536936449L, -65533L, 234134404065073567L };
  static final long[] jjbitVec7 = { -562949953421312L, -8547991553L, 127L, 1979120929931264L };
  static final long[] jjbitVec8 = { 576460743713488896L, -562949953419266L, 9007199254740991999L, 412319973375L };
  static final long[] jjbitVec9 = { 2594073385365405664L, 17163091968L, 271902628478820320L, 844440767823872L };
  static final long[] jjbitVec10 = { 247132830528276448L, 7881300924956672L, 2589004636761075680L, 4294967296L };
  static final long[] jjbitVec11 = { 2579997437506199520L, 15837691904L, 270153412153034720L, 0L };
  static final long[] jjbitVec12 = { 283724577500946400L, 12884901888L, 283724577500946400L, 13958643712L };
  static final long[] jjbitVec13 = { 288228177128316896L, 12884901888L, 0L, 0L };
  static final long[] jjbitVec14 = { 3799912185593854L, 63L, 2309621682768192918L, 31L };
  static final long[] jjbitVec15 = { 0L, 4398046510847L, 0L, 0L };
  static final long[] jjbitVec16 = { 0L, 0L, -4294967296L, 36028797018898495L };
  static final long[] jjbitVec17 = { 5764607523034749677L, 12493387738468353L, -756383734487318528L, 144405459145588743L };
  static final long[] jjbitVec18 = { -1L, -1L, -4026531841L, 288230376151711743L };
  static final long[] jjbitVec19 = { -3233808385L, 4611686017001275199L, 6908521828386340863L, 2295745090394464220L };
  static final long[] jjbitVec20 = { 83837761617920L, 0L, 7L, 0L };
  static final long[] jjbitVec21 = { 4389456576640L, -2L, -8587837441L, 576460752303423487L };
  static final long[] jjbitVec22 = { 35184372088800L, 0L, 0L, 0L };
  static final long[] jjbitVec23 = { -1L, -1L, 274877906943L, 0L };
  static final long[] jjbitVec24 = { -1L, -1L, 68719476735L, 0L };
  static final long[] jjbitVec25 = { 0L, 0L, 36028797018963968L, -36028797027352577L };
  static final long[] jjbitVec26 = { 16777215L, -65536L, -576458553280167937L, 196611L };
  static final long[] jjbitVec27 = { -1L, 12884901951L, -17179879488L, 4503588160110591L };
  static final long[] jjbitVec28 = { -8194L, -536936449L, -65413L, 234134404065073567L };
  static final long[] jjbitVec29 = { -562949953421312L, -8547991553L, -4899916411759099777L, 1979120929931286L };
  static final long[] jjbitVec30 = { 576460743713488896L, -277081224642561L, 9007199254740991999L, 288017070894841855L };
  static final long[] jjbitVec31 = { -864691128455135250L, 281268803485695L, -3186861885341720594L, 1125692414638495L };
  static final long[] jjbitVec32 = { -3211631683292264476L, 9006925953907079L, -869759877059465234L, 281204393786303L };
  static final long[] jjbitVec33 = { -878767076314341394L, 281215949093263L, -4341532606274353172L, 280925229301191L };
  static final long[] jjbitVec34 = { -4327961440926441490L, 281212990012895L, -4327961440926441492L, 281214063754719L };
  static final long[] jjbitVec35 = { -4323457841299070996L, 281212992110031L, 0L, 0L };
  static final long[] jjbitVec36 = { 576320014815068158L, 67076095L, 4323293666156225942L, 67059551L };
  static final long[] jjbitVec37 = { -4422530440275951616L, -558551906910465L, 215680200883507167L, 0L };
  static final long[] jjbitVec38 = { 0L, 0L, 0L, 9126739968L };
  static final long[] jjbitVec39 = { 17732914942836896L, -2L, -6876561409L, 8646911284551352319L };
  
  private final int jjMoveNfa_0(int startState, int curPos)
  {
    int startsAt = 0;
    this.jjnewStateCnt = 148;
    int i = 1;
    this.jjstateSet[0] = startState;
    int kind = Integer.MAX_VALUE;
    for (;;)
    {
      if (++this.jjround == Integer.MAX_VALUE) {
        ReInitRounds();
      }
      if (this.curChar < '@')
      {
        long l = 1L << this.curChar;
        do
        {
          switch (this.jjstateSet[(--i)])
          {
          case 1: 
          case 34: 
            if ((0x3FF600000000000 & l) != 0L)
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            break;
          case 91: 
            if ((0x3FF600000000000 & l) != 0L)
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            break;
          case 16: 
            if ((0x3FF600000000000 & l) != 0L)
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            break;
          case 55: 
            if ((0x3FF600000000000 & l) != 0L)
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            break;
          case 68: 
            if ((0x3FF600000000000 & l) != 0L)
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            break;
          case 103: 
            if ((0x3FF600000000000 & l) != 0L)
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            break;
          case 81: 
            if ((0x3FF600000000000 & l) != 0L)
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            break;
          case 23: 
            if ((0x3FF600000000000 & l) != 0L)
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            break;
          case 3: 
            if ((0x3FF000000000000 & l) != 0L) {
              jjAddStates(0, 1);
            }
            break;
          }
        } while (i != startsAt);
      }
      else if (this.curChar < '')
      {
        long l = 1L << (this.curChar & 0x3F);
        do
        {
          switch (this.jjstateSet[(--i)])
          {
          case 34: 
            if ((0x7FFFFFE87FFFFFE & l) != 0L)
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            if (this.curChar == 'a') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 33;
            }
            break;
          case 91: 
            if ((0x7FFFFFE87FFFFFE & l) != 0L)
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            if (this.curChar == 'n') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 90;
            }
            break;
          case 16: 
            if ((0x7FFFFFE87FFFFFE & l) != 0L)
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            if (this.curChar == 'o') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 15;
            }
            break;
          case 55: 
            if ((0x7FFFFFE87FFFFFE & l) != 0L)
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            if (this.curChar == 'o') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 54;
            }
            break;
          case 68: 
            if ((0x7FFFFFE87FFFFFE & l) != 0L)
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            if (this.curChar == 'r') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 67;
            }
            break;
          case 103: 
            if ((0x7FFFFFE87FFFFFE & l) != 0L)
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            if (this.curChar == 'a') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 146;
            } else if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 139;
            }
            if (this.curChar == 'a') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 132;
            } else if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 122;
            }
            if (this.curChar == 'a') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 112;
            } else if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 102;
            }
            break;
          case 0: 
            if ((0x7FFFFFE87FFFFFE & l) != 0L)
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            else if (this.curChar == '[')
            {
              this.jjstateSet[(this.jjnewStateCnt++)] = 3;
            }
            if (this.curChar == 'm') {
              jjAddStates(2, 7);
            } else if (this.curChar == 'e') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 91;
            } else if (this.curChar == 'p') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 81;
            } else if (this.curChar == 'l') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 74;
            } else if (this.curChar == 'f') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 68;
            } else if (this.curChar == 't') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 55;
            } else if (this.curChar == 'w') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 44;
            } else if (this.curChar == 'c') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 34;
            } else if (this.curChar == 'n') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 23;
            } else if (this.curChar == 'b') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 16;
            } else if (this.curChar == 'o') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 10;
            }
            break;
          case 81: 
            if ((0x7FFFFFE87FFFFFE & l) != 0L)
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            if (this.curChar == 'a') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 80;
            }
            break;
          case 23: 
            if ((0x7FFFFFE87FFFFFE & l) != 0L)
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            if (this.curChar == 'u') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 22;
            }
            break;
          case 1: 
            if ((0x7FFFFFE87FFFFFE & l) != 0L)
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            break;
          case 2: 
            if (this.curChar == '[') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 3;
            }
            break;
          case 4: 
            if (this.curChar == ']') {
              kind = 13;
            }
            break;
          case 5: 
            if ((this.curChar == 'd') && (kind > 14)) {
              kind = 14;
            }
            break;
          case 6: 
          case 12: 
            if (this.curChar == 'e') {
              jjCheckNAdd(5);
            }
            break;
          case 7: 
            if (this.curChar == 'r') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 6;
            }
            break;
          case 8: 
            if (this.curChar == 'e') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 7;
            }
            break;
          case 9: 
            if (this.curChar == 'd') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 8;
            }
            break;
          case 10: 
            if (this.curChar == 'r') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 9;
            }
            break;
          case 11: 
            if (this.curChar == 'o') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 10;
            }
            break;
          case 13: 
            if (this.curChar == 'd') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 12;
            }
            break;
          case 14: 
            if (this.curChar == 'n') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 13;
            }
            break;
          case 15: 
            if (this.curChar == 'u') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 14;
            }
            break;
          case 17: 
            if (this.curChar == 'b') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 16;
            }
            break;
          case 18: 
            if ((this.curChar == 'c') && (kind > 14)) {
              kind = 14;
            }
            break;
          case 19: 
            if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 18;
            }
            break;
          case 20: 
            if (this.curChar == 'r') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 19;
            }
            break;
          case 21: 
            if (this.curChar == 'e') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 20;
            }
            break;
          case 22: 
            if (this.curChar == 'm') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 21;
            }
            break;
          case 24: 
            if (this.curChar == 'n') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 23;
            }
            break;
          case 25: 
            if ((this.curChar == 'y') && (kind > 14)) {
              kind = 14;
            }
            break;
          case 26: 
            if (this.curChar == 't') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 25;
            }
            break;
          case 27: 
            if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 26;
            }
            break;
          case 28: 
            if (this.curChar == 'l') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 27;
            }
            break;
          case 29: 
            if (this.curChar == 'a') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 28;
            }
            break;
          case 30: 
            if (this.curChar == 'n') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 29;
            }
            break;
          case 31: 
            if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 30;
            }
            break;
          case 32: 
            if (this.curChar == 'd') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 31;
            }
            break;
          case 33: 
            if (this.curChar == 'r') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 32;
            }
            break;
          case 35: 
            if (this.curChar == 'c') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 34;
            }
            break;
          case 36: 
            if ((this.curChar == 'e') && (kind > 14)) {
              kind = 14;
            }
            break;
          case 37: 
            if (this.curChar == 'c') {
              jjCheckNAdd(36);
            }
            break;
          case 38: 
            if (this.curChar == 'a') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 37;
            }
            break;
          case 39: 
            if (this.curChar == 'p') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 38;
            }
            break;
          case 40: 
            if (this.curChar == 'S') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 39;
            }
            break;
          case 41: 
            if (this.curChar == 'e') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 40;
            }
            break;
          case 42: 
            if (this.curChar == 't') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 41;
            }
            break;
          case 43: 
            if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 42;
            }
            break;
          case 44: 
            if (this.curChar == 'h') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 43;
            }
            break;
          case 45: 
            if (this.curChar == 'w') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 44;
            }
            break;
          case 46: 
            if ((this.curChar == 's') && (kind > 14)) {
              kind = 14;
            }
            break;
          case 47: 
          case 57: 
            if (this.curChar == 't') {
              jjCheckNAdd(46);
            }
            break;
          case 48: 
            if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 47;
            }
            break;
          case 49: 
            if (this.curChar == 'g') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 48;
            }
            break;
          case 50: 
            if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 49;
            }
            break;
          case 51: 
            if (this.curChar == 'D') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 50;
            }
            break;
          case 52: 
            if (this.curChar == 'l') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 51;
            }
            break;
          case 53: 
            if (this.curChar == 'a') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 52;
            }
            break;
          case 54: 
            if (this.curChar == 't') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 53;
            }
            break;
          case 56: 
            if (this.curChar == 't') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 55;
            }
            break;
          case 58: 
            if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 57;
            }
            break;
          case 59: 
            if (this.curChar == 'g') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 58;
            }
            break;
          case 60: 
            if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 59;
            }
            break;
          case 61: 
            if (this.curChar == 'D') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 60;
            }
            break;
          case 62: 
            if (this.curChar == 'n') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 61;
            }
            break;
          case 63: 
            if (this.curChar == 'o') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 62;
            }
            break;
          case 64: 
            if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 63;
            }
            break;
          case 65: 
            if (this.curChar == 't') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 64;
            }
            break;
          case 66: 
            if (this.curChar == 'c') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 65;
            }
            break;
          case 67: 
            if (this.curChar == 'a') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 66;
            }
            break;
          case 69: 
            if (this.curChar == 'f') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 68;
            }
            break;
          case 70: 
            if ((this.curChar == 'h') && (kind > 14)) {
              kind = 14;
            }
            break;
          case 71: 
          case 134: 
          case 141: 
            if (this.curChar == 't') {
              jjCheckNAdd(70);
            }
            break;
          case 72: 
            if (this.curChar == 'g') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 71;
            }
            break;
          case 73: 
            if (this.curChar == 'n') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 72;
            }
            break;
          case 74: 
            if (this.curChar == 'e') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 73;
            }
            break;
          case 75: 
            if (this.curChar == 'l') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 74;
            }
            break;
          case 76: 
            if ((this.curChar == 'n') && (kind > 14)) {
              kind = 14;
            }
            break;
          case 77: 
            if (this.curChar == 'r') {
              jjCheckNAdd(76);
            }
            break;
          case 78: 
            if (this.curChar == 'e') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 77;
            }
            break;
          case 79: 
            if (this.curChar == 't') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 78;
            }
            break;
          case 80: 
            if (this.curChar == 't') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 79;
            }
            break;
          case 82: 
            if (this.curChar == 'p') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 81;
            }
            break;
          case 83: 
            if (this.curChar == 'o') {
              jjCheckNAdd(76);
            }
            break;
          case 84: 
            if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 83;
            }
            break;
          case 85: 
            if (this.curChar == 't') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 84;
            }
            break;
          case 86: 
            if (this.curChar == 'a') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 85;
            }
            break;
          case 87: 
            if (this.curChar == 'r') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 86;
            }
            break;
          case 88: 
            if (this.curChar == 'e') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 87;
            }
            break;
          case 89: 
            if (this.curChar == 'm') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 88;
            }
            break;
          case 90: 
            if (this.curChar == 'u') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 89;
            }
            break;
          case 92: 
            if (this.curChar == 'e') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 91;
            }
            break;
          case 93: 
            if (this.curChar == 'm') {
              jjAddStates(2, 7);
            }
            break;
          case 94: 
          case 104: 
          case 114: 
          case 124: 
            if (this.curChar == 'v') {
              jjCheckNAdd(36);
            }
            break;
          case 95: 
            if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 94;
            }
            break;
          case 96: 
            if (this.curChar == 's') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 95;
            }
            break;
          case 97: 
            if (this.curChar == 'u') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 96;
            }
            break;
          case 98: 
            if (this.curChar == 'l') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 97;
            }
            break;
          case 99: 
            if (this.curChar == 'c') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 98;
            }
            break;
          case 100: 
            if (this.curChar == 'n') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 99;
            }
            break;
          case 101: 
            if (this.curChar == 'I') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 100;
            }
            break;
          case 102: 
            if (this.curChar == 'n') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 101;
            }
            break;
          case 105: 
            if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 104;
            }
            break;
          case 106: 
            if (this.curChar == 's') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 105;
            }
            break;
          case 107: 
            if (this.curChar == 'u') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 106;
            }
            break;
          case 108: 
            if (this.curChar == 'l') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 107;
            }
            break;
          case 109: 
            if (this.curChar == 'c') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 108;
            }
            break;
          case 110: 
            if (this.curChar == 'n') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 109;
            }
            break;
          case 111: 
            if (this.curChar == 'I') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 110;
            }
            break;
          case 112: 
            if (this.curChar == 'x') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 111;
            }
            break;
          case 113: 
            if (this.curChar == 'a') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 112;
            }
            break;
          case 115: 
            if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 114;
            }
            break;
          case 116: 
            if (this.curChar == 's') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 115;
            }
            break;
          case 117: 
            if (this.curChar == 'u') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 116;
            }
            break;
          case 118: 
            if (this.curChar == 'l') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 117;
            }
            break;
          case 119: 
            if (this.curChar == 'c') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 118;
            }
            break;
          case 120: 
            if (this.curChar == 'x') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 119;
            }
            break;
          case 121: 
            if (this.curChar == 'E') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 120;
            }
            break;
          case 122: 
            if (this.curChar == 'n') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 121;
            }
            break;
          case 123: 
            if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 122;
            }
            break;
          case 125: 
            if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 124;
            }
            break;
          case 126: 
            if (this.curChar == 's') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 125;
            }
            break;
          case 127: 
            if (this.curChar == 'u') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 126;
            }
            break;
          case 128: 
            if (this.curChar == 'l') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 127;
            }
            break;
          case 129: 
            if (this.curChar == 'c') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 128;
            }
            break;
          case 130: 
            if (this.curChar == 'x') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 129;
            }
            break;
          case 131: 
            if (this.curChar == 'E') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 130;
            }
            break;
          case 132: 
            if (this.curChar == 'x') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 131;
            }
            break;
          case 133: 
            if (this.curChar == 'a') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 132;
            }
            break;
          case 135: 
            if (this.curChar == 'g') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 134;
            }
            break;
          case 136: 
            if (this.curChar == 'n') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 135;
            }
            break;
          case 137: 
            if (this.curChar == 'e') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 136;
            }
            break;
          case 138: 
            if (this.curChar == 'L') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 137;
            }
            break;
          case 139: 
            if (this.curChar == 'n') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 138;
            }
            break;
          case 140: 
            if (this.curChar == 'i') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 139;
            }
            break;
          case 142: 
            if (this.curChar == 'g') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 141;
            }
            break;
          case 143: 
            if (this.curChar == 'n') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 142;
            }
            break;
          case 144: 
            if (this.curChar == 'e') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 143;
            }
            break;
          case 145: 
            if (this.curChar == 'L') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 144;
            }
            break;
          case 146: 
            if (this.curChar == 'x') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 145;
            }
            break;
          case 147: 
            if (this.curChar == 'a') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 146;
            }
            break;
          }
        } while (i != startsAt);
      }
      else
      {
        int hiByte = this.curChar >> '\b';
        int i1 = hiByte >> 6;
        long l1 = 1L << (hiByte & 0x3F);
        int i2 = (this.curChar & 0xFF) >> '\006';
        long l2 = 1L << (this.curChar & 0x3F);
        do
        {
          switch (this.jjstateSet[(--i)])
          {
          case 1: 
          case 34: 
            if (jjCanMove_1(hiByte, i1, i2, l1, l2))
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            break;
          case 91: 
            if (jjCanMove_1(hiByte, i1, i2, l1, l2))
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            break;
          case 16: 
            if (jjCanMove_1(hiByte, i1, i2, l1, l2))
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            break;
          case 55: 
            if (jjCanMove_1(hiByte, i1, i2, l1, l2))
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            break;
          case 68: 
            if (jjCanMove_1(hiByte, i1, i2, l1, l2))
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            break;
          case 103: 
            if (jjCanMove_1(hiByte, i1, i2, l1, l2))
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            break;
          case 0: 
            if (jjCanMove_0(hiByte, i1, i2, l1, l2))
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            break;
          case 81: 
            if (jjCanMove_1(hiByte, i1, i2, l1, l2))
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            break;
          case 23: 
            if (jjCanMove_1(hiByte, i1, i2, l1, l2))
            {
              if (kind > 12) {
                kind = 12;
              }
              jjCheckNAdd(1);
            }
            break;
          }
        } while (i != startsAt);
      }
      if (kind != Integer.MAX_VALUE)
      {
        this.jjmatchedKind = kind;
        this.jjmatchedPos = curPos;
        kind = Integer.MAX_VALUE;
      }
      curPos++;
      if ((i = this.jjnewStateCnt) == (startsAt = 148 - (this.jjnewStateCnt = startsAt))) {
        return curPos;
      }
      try
      {
        this.curChar = this.input_stream.readChar();
      }
      catch (IOException e) {}
    }
    return curPos;
  }
  
  static final int[] jjnextStates = { 3, 4, 103, 113, 123, 133, 140, 147 };
  
  private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
  {
    switch (hiByte)
    {
    case 0: 
      return (jjbitVec2[i2] & l2) != 0L;
    case 1: 
      return (jjbitVec3[i2] & l2) != 0L;
    case 2: 
      return (jjbitVec4[i2] & l2) != 0L;
    case 3: 
      return (jjbitVec5[i2] & l2) != 0L;
    case 4: 
      return (jjbitVec6[i2] & l2) != 0L;
    case 5: 
      return (jjbitVec7[i2] & l2) != 0L;
    case 6: 
      return (jjbitVec8[i2] & l2) != 0L;
    case 9: 
      return (jjbitVec9[i2] & l2) != 0L;
    case 10: 
      return (jjbitVec10[i2] & l2) != 0L;
    case 11: 
      return (jjbitVec11[i2] & l2) != 0L;
    case 12: 
      return (jjbitVec12[i2] & l2) != 0L;
    case 13: 
      return (jjbitVec13[i2] & l2) != 0L;
    case 14: 
      return (jjbitVec14[i2] & l2) != 0L;
    case 15: 
      return (jjbitVec15[i2] & l2) != 0L;
    case 16: 
      return (jjbitVec16[i2] & l2) != 0L;
    case 17: 
      return (jjbitVec17[i2] & l2) != 0L;
    case 30: 
      return (jjbitVec18[i2] & l2) != 0L;
    case 31: 
      return (jjbitVec19[i2] & l2) != 0L;
    case 33: 
      return (jjbitVec20[i2] & l2) != 0L;
    case 48: 
      return (jjbitVec21[i2] & l2) != 0L;
    case 49: 
      return (jjbitVec22[i2] & l2) != 0L;
    case 159: 
      return (jjbitVec23[i2] & l2) != 0L;
    case 215: 
      return (jjbitVec24[i2] & l2) != 0L;
    }
    if ((jjbitVec0[i1] & l1) != 0L) {
      return true;
    }
    return false;
  }
  
  private static final boolean jjCanMove_1(int hiByte, int i1, int i2, long l1, long l2)
  {
    switch (hiByte)
    {
    case 0: 
      return (jjbitVec25[i2] & l2) != 0L;
    case 1: 
      return (jjbitVec3[i2] & l2) != 0L;
    case 2: 
      return (jjbitVec26[i2] & l2) != 0L;
    case 3: 
      return (jjbitVec27[i2] & l2) != 0L;
    case 4: 
      return (jjbitVec28[i2] & l2) != 0L;
    case 5: 
      return (jjbitVec29[i2] & l2) != 0L;
    case 6: 
      return (jjbitVec30[i2] & l2) != 0L;
    case 9: 
      return (jjbitVec31[i2] & l2) != 0L;
    case 10: 
      return (jjbitVec32[i2] & l2) != 0L;
    case 11: 
      return (jjbitVec33[i2] & l2) != 0L;
    case 12: 
      return (jjbitVec34[i2] & l2) != 0L;
    case 13: 
      return (jjbitVec35[i2] & l2) != 0L;
    case 14: 
      return (jjbitVec36[i2] & l2) != 0L;
    case 15: 
      return (jjbitVec37[i2] & l2) != 0L;
    case 16: 
      return (jjbitVec16[i2] & l2) != 0L;
    case 17: 
      return (jjbitVec17[i2] & l2) != 0L;
    case 30: 
      return (jjbitVec18[i2] & l2) != 0L;
    case 31: 
      return (jjbitVec19[i2] & l2) != 0L;
    case 32: 
      return (jjbitVec38[i2] & l2) != 0L;
    case 33: 
      return (jjbitVec20[i2] & l2) != 0L;
    case 48: 
      return (jjbitVec39[i2] & l2) != 0L;
    case 49: 
      return (jjbitVec22[i2] & l2) != 0L;
    case 159: 
      return (jjbitVec23[i2] & l2) != 0L;
    case 215: 
      return (jjbitVec24[i2] & l2) != 0L;
    }
    if ((jjbitVec0[i1] & l1) != 0L) {
      return true;
    }
    return false;
  }
  
  public static final String[] jjstrLiteralImages = { "", null, null, null, null, null, null, null, null, null, null, null, null, null, null, ":", "/", "//", "attribute::", "@", "element::", "substitutionGroup::", "type::", "~", "baseType::", "primitiveType::", "itemType::", "memberType::", "scope::", "attributeGroup::", "group::", "identityContraint::", "key::", "notation::", "model::sequence", "model::choice", "model::all", "model::*", "any::*", "anyAttribute::*", "facet::*", "facet::", "component::*", "x-schema::", "x-schema::*", "*", "0" };
  public static final String[] lexStateNames = { "DEFAULT" };
  static final long[] jjtoToken = { 140737488351233L };
  static final long[] jjtoSkip = { 62L };
  protected SimpleCharStream input_stream;
  private final int[] jjrounds = new int[''];
  private final int[] jjstateSet = new int['Ĩ'];
  protected char curChar;
  
  public SCDParserTokenManager(SimpleCharStream stream)
  {
    this.input_stream = stream;
  }
  
  public SCDParserTokenManager(SimpleCharStream stream, int lexState)
  {
    this(stream);
    SwitchTo(lexState);
  }
  
  public void ReInit(SimpleCharStream stream)
  {
    this.jjmatchedPos = (this.jjnewStateCnt = 0);
    this.curLexState = this.defaultLexState;
    this.input_stream = stream;
    ReInitRounds();
  }
  
  private final void ReInitRounds()
  {
    this.jjround = -2147483647;
    for (int i = 148; i-- > 0;) {
      this.jjrounds[i] = Integer.MIN_VALUE;
    }
  }
  
  public void ReInit(SimpleCharStream stream, int lexState)
  {
    ReInit(stream);
    SwitchTo(lexState);
  }
  
  public void SwitchTo(int lexState)
  {
    if ((lexState >= 1) || (lexState < 0)) {
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", 2);
    }
    this.curLexState = lexState;
  }
  
  protected Token jjFillToken()
  {
    Token t = Token.newToken(this.jjmatchedKind);
    t.kind = this.jjmatchedKind;
    String im = jjstrLiteralImages[this.jjmatchedKind];
    t.image = (im == null ? this.input_stream.GetImage() : im);
    t.beginLine = this.input_stream.getBeginLine();
    t.beginColumn = this.input_stream.getBeginColumn();
    t.endLine = this.input_stream.getEndLine();
    t.endColumn = this.input_stream.getEndColumn();
    return t;
  }
  
  int curLexState = 0;
  int defaultLexState = 0;
  int jjnewStateCnt;
  int jjround;
  int jjmatchedPos;
  int jjmatchedKind;
  
  public Token getNextToken()
  {
    Token specialToken = null;
    
    int curPos = 0;
    do
    {
      for (;;)
      {
        try
        {
          this.curChar = this.input_stream.BeginToken();
        }
        catch (IOException e)
        {
          this.jjmatchedKind = 0;
          return jjFillToken();
        }
        try
        {
          this.input_stream.backup(0);
          while ((this.curChar <= ' ') && ((0x100003600 & 1L << this.curChar) != 0L)) {
            this.curChar = this.input_stream.BeginToken();
          }
        }
        catch (IOException e1) {}
      }
      this.jjmatchedKind = Integer.MAX_VALUE;
      this.jjmatchedPos = 0;
      curPos = jjMoveStringLiteralDfa0_0();
      if (this.jjmatchedKind == Integer.MAX_VALUE) {
        break;
      }
      if (this.jjmatchedPos + 1 < curPos) {
        this.input_stream.backup(curPos - this.jjmatchedPos - 1);
      }
    } while ((jjtoToken[(this.jjmatchedKind >> 6)] & 1L << (this.jjmatchedKind & 0x3F)) == 0L);
    Token matchedToken = jjFillToken();
    return matchedToken;
    
    int error_line = this.input_stream.getEndLine();
    int error_column = this.input_stream.getEndColumn();
    String error_after = null;
    boolean EOFSeen = false;
    try
    {
      this.input_stream.readChar();this.input_stream.backup(1);
    }
    catch (IOException e1)
    {
      EOFSeen = true;
      error_after = curPos <= 1 ? "" : this.input_stream.GetImage();
      if ((this.curChar == '\n') || (this.curChar == '\r'))
      {
        error_line++;
        error_column = 0;
      }
      else
      {
        error_column++;
      }
    }
    if (!EOFSeen)
    {
      this.input_stream.backup(1);
      error_after = curPos <= 1 ? "" : this.input_stream.GetImage();
    }
    throw new TokenMgrError(EOFSeen, this.curLexState, error_line, error_column, error_after, this.curChar, 0);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\scd\SCDParserTokenManager.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */