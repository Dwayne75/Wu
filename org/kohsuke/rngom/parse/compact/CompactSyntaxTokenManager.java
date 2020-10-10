package org.kohsuke.rngom.parse.compact;

import java.io.IOException;
import java.io.PrintStream;

public class CompactSyntaxTokenManager
  implements CompactSyntaxConstants
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
      if ((active0 & 0x1F8C0FE4E0) != 0L)
      {
        this.jjmatchedKind = 54;
        return 43;
      }
      if ((active0 & 0x800000000000000) != 0L)
      {
        this.jjmatchedKind = 60;
        return -1;
      }
      return -1;
    case 1: 
      if ((active0 & 0x1F8C0FE4E0) != 0L)
      {
        this.jjmatchedKind = 54;
        this.jjmatchedPos = 1;
        return 43;
      }
      if ((active0 & 0x800000000000000) != 0L)
      {
        if (this.jjmatchedPos == 0)
        {
          this.jjmatchedKind = 60;
          this.jjmatchedPos = 0;
        }
        return -1;
      }
      return -1;
    case 2: 
      if ((active0 & 0x1F8C0FE4A0) != 0L)
      {
        this.jjmatchedKind = 54;
        this.jjmatchedPos = 2;
        return 43;
      }
      if ((active0 & 0x40) != 0L) {
        return 43;
      }
      return -1;
    case 3: 
      if ((active0 & 0x1F0C0BE4A0) != 0L)
      {
        this.jjmatchedKind = 54;
        this.jjmatchedPos = 3;
        return 43;
      }
      if ((active0 & 0x80040000) != 0L) {
        return 43;
      }
      return -1;
    case 4: 
      if ((active0 & 0xE0C09E480) != 0L)
      {
        this.jjmatchedKind = 54;
        this.jjmatchedPos = 4;
        return 43;
      }
      if ((active0 & 0x1100020020) != 0L) {
        return 43;
      }
      return -1;
    case 5: 
      if ((active0 & 0x20C09E480) != 0L)
      {
        this.jjmatchedKind = 54;
        this.jjmatchedPos = 5;
        return 43;
      }
      if ((active0 & 0xC00000000) != 0L) {
        return 43;
      }
      return -1;
    case 6: 
      if ((active0 & 0x208092000) != 0L)
      {
        this.jjmatchedKind = 54;
        this.jjmatchedPos = 6;
        return 43;
      }
      if ((active0 & 0x400C480) != 0L) {
        return 43;
      }
      return -1;
    case 7: 
      if ((active0 & 0x8092000) != 0L)
      {
        this.jjmatchedKind = 54;
        this.jjmatchedPos = 7;
        return 43;
      }
      if ((active0 & 0x200000000) != 0L) {
        return 43;
      }
      return -1;
    case 8: 
      if ((active0 & 0x80000) != 0L)
      {
        this.jjmatchedKind = 54;
        this.jjmatchedPos = 8;
        return 43;
      }
      if ((active0 & 0x8012000) != 0L) {
        return 43;
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
    case '&': 
      this.jjmatchedKind = 21;
      return jjMoveStringLiteralDfa1_0(8L);
    case '(': 
      return jjStopAtPos(0, 28);
    case ')': 
      return jjStopAtPos(0, 29);
    case '*': 
      return jjStopAtPos(0, 25);
    case '+': 
      return jjStopAtPos(0, 23);
    case ',': 
      return jjStopAtPos(0, 22);
    case '-': 
      return jjStopAtPos(0, 30);
    case '=': 
      return jjStopAtPos(0, 2);
    case '>': 
      return jjMoveStringLiteralDfa1_0(576460752303423488L);
    case '?': 
      return jjStopAtPos(0, 24);
    case '[': 
      return jjStopAtPos(0, 1);
    case ']': 
      return jjStopAtPos(0, 9);
    case 'a': 
      return jjMoveStringLiteralDfa1_0(134217728L);
    case 'd': 
      return jjMoveStringLiteralDfa1_0(81984L);
    case 'e': 
      return jjMoveStringLiteralDfa1_0(8657174528L);
    case 'g': 
      return jjMoveStringLiteralDfa1_0(1024L);
    case 'i': 
      return jjMoveStringLiteralDfa1_0(32896L);
    case 'l': 
      return jjMoveStringLiteralDfa1_0(2147483648L);
    case 'm': 
      return jjMoveStringLiteralDfa1_0(4294967296L);
    case 'n': 
      return jjMoveStringLiteralDfa1_0(532480L);
    case 'p': 
      return jjMoveStringLiteralDfa1_0(17179869184L);
    case 's': 
      return jjMoveStringLiteralDfa1_0(34359738400L);
    case 't': 
      return jjMoveStringLiteralDfa1_0(68719738880L);
    case '{': 
      return jjStopAtPos(0, 11);
    case '|': 
      this.jjmatchedKind = 20;
      return jjMoveStringLiteralDfa1_0(16L);
    case '}': 
      return jjStopAtPos(0, 12);
    case '~': 
      return jjStopAtPos(0, 8);
    }
    return jjMoveNfa_0(3, 0);
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
    case '=': 
      if ((active0 & 0x8) != 0L) {
        return jjStopAtPos(1, 3);
      }
      if ((active0 & 0x10) != 0L) {
        return jjStopAtPos(1, 4);
      }
      break;
    case '>': 
      if ((active0 & 0x800000000000000) != 0L) {
        return jjStopAtPos(1, 59);
      }
      break;
    case 'a': 
      return jjMoveStringLiteralDfa2_0(active0, 17179942912L);
    case 'e': 
      return jjMoveStringLiteralDfa2_0(active0, 278528L);
    case 'i': 
      return jjMoveStringLiteralDfa2_0(active0, 6442451008L);
    case 'l': 
      return jjMoveStringLiteralDfa2_0(active0, 67108864L);
    case 'm': 
      return jjMoveStringLiteralDfa2_0(active0, 131072L);
    case 'n': 
      return jjMoveStringLiteralDfa2_0(active0, 32896L);
    case 'o': 
      return jjMoveStringLiteralDfa2_0(active0, 68720001024L);
    case 'r': 
      return jjMoveStringLiteralDfa2_0(active0, 1024L);
    case 't': 
      return jjMoveStringLiteralDfa2_0(active0, 34493956128L);
    case 'x': 
      return jjMoveStringLiteralDfa2_0(active0, 8589934592L);
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
    case 'a': 
      return jjMoveStringLiteralDfa3_0(active0, 1056L);
    case 'c': 
      return jjMoveStringLiteralDfa3_0(active0, 128L);
    case 'e': 
      return jjMoveStringLiteralDfa3_0(active0, 67108864L);
    case 'f': 
      return jjMoveStringLiteralDfa3_0(active0, 16384L);
    case 'h': 
      return jjMoveStringLiteralDfa3_0(active0, 32768L);
    case 'k': 
      return jjMoveStringLiteralDfa3_0(active0, 68719476736L);
    case 'm': 
      return jjMoveStringLiteralDfa3_0(active0, 8192L);
    case 'p': 
      return jjMoveStringLiteralDfa3_0(active0, 131072L);
    case 'r': 
      return jjMoveStringLiteralDfa3_0(active0, 51539607552L);
    case 's': 
      return jjMoveStringLiteralDfa3_0(active0, 2147483648L);
    case 't': 
      return jjMoveStringLiteralDfa3_0(active0, 8724742144L);
    case 'v': 
      if ((active0 & 0x40) != 0L) {
        return jjStartNfaWithStates_0(2, 6, 43);
      }
      break;
    case 'x': 
      return jjMoveStringLiteralDfa3_0(active0, 4295229440L);
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
    case 'A': 
      return jjMoveStringLiteralDfa4_0(active0, 524288L);
    case 'a': 
      return jjMoveStringLiteralDfa4_0(active0, 81920L);
    case 'e': 
      return jjMoveStringLiteralDfa4_0(active0, 98784288768L);
    case 'i': 
      return jjMoveStringLiteralDfa4_0(active0, 34359738368L);
    case 'l': 
      return jjMoveStringLiteralDfa4_0(active0, 128L);
    case 'm': 
      return jjMoveStringLiteralDfa4_0(active0, 67109888L);
    case 'r': 
      return jjMoveStringLiteralDfa4_0(active0, 134217760L);
    case 't': 
      if ((active0 & 0x40000) != 0L) {
        return jjStartNfaWithStates_0(3, 18, 43);
      }
      if ((active0 & 0x80000000) != 0L) {
        return jjStartNfaWithStates_0(3, 31, 43);
      }
      return jjMoveStringLiteralDfa4_0(active0, 131072L);
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
    case 'd': 
      if ((active0 & 0x100000000) != 0L) {
        return jjStartNfaWithStates_0(4, 32, 43);
      }
      break;
    case 'e': 
      return jjMoveStringLiteralDfa5_0(active0, 67108864L);
    case 'i': 
      return jjMoveStringLiteralDfa5_0(active0, 134217728L);
    case 'l': 
      return jjMoveStringLiteralDfa5_0(active0, 524288L);
    case 'm': 
      return jjMoveStringLiteralDfa5_0(active0, 1024L);
    case 'n': 
      if ((active0 & 0x1000000000) != 0L) {
        return jjStartNfaWithStates_0(4, 36, 43);
      }
      return jjMoveStringLiteralDfa5_0(active0, 51539607552L);
    case 'r': 
      return jjMoveStringLiteralDfa5_0(active0, 8589967360L);
    case 's': 
      return jjMoveStringLiteralDfa5_0(active0, 8192L);
    case 't': 
      if ((active0 & 0x20) != 0L) {
        return jjStartNfaWithStates_0(4, 5, 43);
      }
      return jjMoveStringLiteralDfa5_0(active0, 65536L);
    case 'u': 
      return jjMoveStringLiteralDfa5_0(active0, 16512L);
    case 'y': 
      if ((active0 & 0x20000) != 0L) {
        return jjStartNfaWithStates_0(4, 17, 43);
      }
      break;
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
    case 'a': 
      return jjMoveStringLiteralDfa6_0(active0, 1024L);
    case 'b': 
      return jjMoveStringLiteralDfa6_0(active0, 134217728L);
    case 'd': 
      return jjMoveStringLiteralDfa6_0(active0, 128L);
    case 'g': 
      if ((active0 & 0x800000000) != 0L) {
        return jjStartNfaWithStates_0(5, 35, 43);
      }
      break;
    case 'i': 
      return jjMoveStringLiteralDfa6_0(active0, 32768L);
    case 'l': 
      return jjMoveStringLiteralDfa6_0(active0, 540672L);
    case 'n': 
      return jjMoveStringLiteralDfa6_0(active0, 8657043456L);
    case 'p': 
      return jjMoveStringLiteralDfa6_0(active0, 8192L);
    case 't': 
      if ((active0 & 0x400000000) != 0L) {
        return jjStartNfaWithStates_0(5, 34, 43);
      }
      break;
    case 'y': 
      return jjMoveStringLiteralDfa6_0(active0, 65536L);
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
    case 'a': 
      return jjMoveStringLiteralDfa7_0(active0, 8589942784L);
    case 'e': 
      if ((active0 & 0x80) != 0L) {
        return jjStartNfaWithStates_0(6, 7, 43);
      }
      break;
    case 'o': 
      return jjMoveStringLiteralDfa7_0(active0, 524288L);
    case 'p': 
      return jjMoveStringLiteralDfa7_0(active0, 65536L);
    case 'r': 
      if ((active0 & 0x400) != 0L) {
        return jjStartNfaWithStates_0(6, 10, 43);
      }
      break;
    case 't': 
      if ((active0 & 0x4000) != 0L) {
        return jjStartNfaWithStates_0(6, 14, 43);
      }
      if ((active0 & 0x8000) != 0L) {
        return jjStartNfaWithStates_0(6, 15, 43);
      }
      if ((active0 & 0x4000000) != 0L) {
        return jjStartNfaWithStates_0(6, 26, 43);
      }
      break;
    case 'u': 
      return jjMoveStringLiteralDfa7_0(active0, 134217728L);
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
    case 'c': 
      return jjMoveStringLiteralDfa8_0(active0, 8192L);
    case 'e': 
      return jjMoveStringLiteralDfa8_0(active0, 65536L);
    case 'l': 
      if ((active0 & 0x200000000) != 0L) {
        return jjStartNfaWithStates_0(7, 33, 43);
      }
      break;
    case 't': 
      return jjMoveStringLiteralDfa8_0(active0, 134217728L);
    case 'w': 
      return jjMoveStringLiteralDfa8_0(active0, 524288L);
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
    case 'e': 
      if ((active0 & 0x2000) != 0L) {
        return jjStartNfaWithStates_0(8, 13, 43);
      }
      if ((active0 & 0x8000000) != 0L) {
        return jjStartNfaWithStates_0(8, 27, 43);
      }
      return jjMoveStringLiteralDfa9_0(active0, 524288L);
    case 's': 
      if ((active0 & 0x10000) != 0L) {
        return jjStartNfaWithStates_0(8, 16, 43);
      }
      break;
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
    case 'd': 
      if ((active0 & 0x80000) != 0L) {
        return jjStartNfaWithStates_0(9, 19, 43);
      }
      break;
    }
    return jjStartNfa_0(8, active0);
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
  
  static final long[] jjbitVec0 = { -2L, -1L, -1L, -1L };
  static final long[] jjbitVec2 = { 0L, 0L, -1L, -1L };
  static final long[] jjbitVec3 = { 0L, -16384L, -17590038560769L, 8388607L };
  static final long[] jjbitVec4 = { 0L, 0L, 0L, -36028797027352577L };
  static final long[] jjbitVec5 = { 9219994337134247935L, 9223372036854775294L, -1L, -274156627316187121L };
  static final long[] jjbitVec6 = { 16777215L, -65536L, -576458553280167937L, 3L };
  static final long[] jjbitVec7 = { 0L, 0L, -17179879616L, 4503588160110591L };
  static final long[] jjbitVec8 = { -8194L, -536936449L, -65533L, 234134404065073567L };
  static final long[] jjbitVec9 = { -562949953421312L, -8547991553L, 127L, 1979120929931264L };
  static final long[] jjbitVec10 = { 576460743713488896L, -562949953419266L, 9007199254740991999L, 412319973375L };
  static final long[] jjbitVec11 = { 2594073385365405664L, 17163091968L, 271902628478820320L, 844440767823872L };
  static final long[] jjbitVec12 = { 247132830528276448L, 7881300924956672L, 2589004636761075680L, 4294967296L };
  static final long[] jjbitVec13 = { 2579997437506199520L, 15837691904L, 270153412153034720L, 0L };
  static final long[] jjbitVec14 = { 283724577500946400L, 12884901888L, 283724577500946400L, 13958643712L };
  static final long[] jjbitVec15 = { 288228177128316896L, 12884901888L, 0L, 0L };
  static final long[] jjbitVec16 = { 3799912185593854L, 63L, 2309621682768192918L, 31L };
  static final long[] jjbitVec17 = { 0L, 4398046510847L, 0L, 0L };
  static final long[] jjbitVec18 = { 0L, 0L, -4294967296L, 36028797018898495L };
  static final long[] jjbitVec19 = { 5764607523034749677L, 12493387738468353L, -756383734487318528L, 144405459145588743L };
  static final long[] jjbitVec20 = { -1L, -1L, -4026531841L, 288230376151711743L };
  static final long[] jjbitVec21 = { -3233808385L, 4611686017001275199L, 6908521828386340863L, 2295745090394464220L };
  static final long[] jjbitVec22 = { 83837761617920L, 0L, 7L, 0L };
  static final long[] jjbitVec23 = { 4389456576640L, -2L, -8587837441L, 576460752303423487L };
  static final long[] jjbitVec24 = { 35184372088800L, 0L, 0L, 0L };
  static final long[] jjbitVec25 = { -1L, -1L, 274877906943L, 0L };
  static final long[] jjbitVec26 = { -1L, -1L, 68719476735L, 0L };
  static final long[] jjbitVec27 = { 0L, 0L, 36028797018963968L, -36028797027352577L };
  static final long[] jjbitVec28 = { 16777215L, -65536L, -576458553280167937L, 196611L };
  static final long[] jjbitVec29 = { -1L, 12884901951L, -17179879488L, 4503588160110591L };
  static final long[] jjbitVec30 = { -8194L, -536936449L, -65413L, 234134404065073567L };
  static final long[] jjbitVec31 = { -562949953421312L, -8547991553L, -4899916411759099777L, 1979120929931286L };
  static final long[] jjbitVec32 = { 576460743713488896L, -277081224642561L, 9007199254740991999L, 288017070894841855L };
  static final long[] jjbitVec33 = { -864691128455135250L, 281268803485695L, -3186861885341720594L, 1125692414638495L };
  static final long[] jjbitVec34 = { -3211631683292264476L, 9006925953907079L, -869759877059465234L, 281204393786303L };
  static final long[] jjbitVec35 = { -878767076314341394L, 281215949093263L, -4341532606274353172L, 280925229301191L };
  static final long[] jjbitVec36 = { -4327961440926441490L, 281212990012895L, -4327961440926441492L, 281214063754719L };
  static final long[] jjbitVec37 = { -4323457841299070996L, 281212992110031L, 0L, 0L };
  static final long[] jjbitVec38 = { 576320014815068158L, 67076095L, 4323293666156225942L, 67059551L };
  static final long[] jjbitVec39 = { -4422530440275951616L, -558551906910465L, 215680200883507167L, 0L };
  static final long[] jjbitVec40 = { 0L, 0L, 0L, 9126739968L };
  static final long[] jjbitVec41 = { 17732914942836896L, -2L, -6876561409L, 8646911284551352319L };
  
  private final int jjMoveNfa_0(int startState, int curPos)
  {
    int startsAt = 0;
    this.jjnewStateCnt = 43;
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
          case 3: 
            if ((0xFFFFFFFFFFFFF9FF & l) != 0L) {
              if (kind > 60) {
                kind = 60;
              }
            }
            if ((0x100000601 & l) != 0L)
            {
              if (kind > 39) {
                kind = 39;
              }
              jjCheckNAdd(0);
            }
            else if (this.curChar == '\'')
            {
              this.jjstateSet[(this.jjnewStateCnt++)] = 31;
            }
            else if (this.curChar == '"')
            {
              this.jjstateSet[(this.jjnewStateCnt++)] = 22;
            }
            else if (this.curChar == '#')
            {
              if (kind > 42) {
                kind = 42;
              }
              jjCheckNAdd(5);
            }
            if (this.curChar == '\'') {
              jjCheckNAddTwoStates(13, 14);
            } else if (this.curChar == '"') {
              jjCheckNAddTwoStates(10, 11);
            } else if (this.curChar == '#') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 1;
            }
            break;
          case 43: 
            if ((0x3FF600000000000 & l) != 0L) {
              jjCheckNAddTwoStates(39, 40);
            } else if (this.curChar == ':') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 41;
            }
            if ((0x3FF600000000000 & l) != 0L) {
              jjCheckNAddTwoStates(36, 38);
            } else if (this.curChar == ':') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 37;
            }
            if ((0x3FF600000000000 & l) != 0L)
            {
              if (kind > 54) {
                kind = 54;
              }
              jjCheckNAdd(35);
            }
            break;
          case 0: 
            if ((0x100000601 & l) != 0L)
            {
              if (kind > 39) {
                kind = 39;
              }
              jjCheckNAdd(0);
            }
            break;
          case 1: 
            if (this.curChar == '#')
            {
              if (kind > 40) {
                kind = 40;
              }
              jjCheckNAdd(2);
            }
            break;
          case 2: 
            if ((0xFFFFFFFFFFFFFBFE & l) != 0L)
            {
              if (kind > 40) {
                kind = 40;
              }
              jjCheckNAdd(2);
            }
            break;
          case 4: 
            if (this.curChar == '#')
            {
              if (kind > 42) {
                kind = 42;
              }
              jjCheckNAdd(5);
            }
            break;
          case 5: 
            if ((0xFFFFFFFFFFFFFBFE & l) != 0L)
            {
              if (kind > 42) {
                kind = 42;
              }
              jjCheckNAdd(5);
            }
            break;
          case 8: 
            if ((0x3FF600000000000 & l) != 0L)
            {
              if (kind > 55) {
                kind = 55;
              }
              this.jjstateSet[(this.jjnewStateCnt++)] = 8;
            }
            break;
          case 9: 
            if (this.curChar == '"') {
              jjCheckNAddTwoStates(10, 11);
            }
            break;
          case 10: 
            if ((0xFFFFFFFBFFFFFFFE & l) != 0L) {
              jjCheckNAddTwoStates(10, 11);
            }
            break;
          case 11: 
          case 20: 
            if ((this.curChar == '"') && (kind > 58)) {
              kind = 58;
            }
            break;
          case 12: 
            if (this.curChar == '\'') {
              jjCheckNAddTwoStates(13, 14);
            }
            break;
          case 13: 
            if ((0xFFFFFF7FFFFFFFFE & l) != 0L) {
              jjCheckNAddTwoStates(13, 14);
            }
            break;
          case 14: 
          case 29: 
            if ((this.curChar == '\'') && (kind > 58)) {
              kind = 58;
            }
            break;
          case 15: 
            if (this.curChar == '"') {
              jjCheckNAddStates(0, 2);
            }
            break;
          case 16: 
            if ((0xFFFFFFFBFFFFFFFF & l) != 0L) {
              jjCheckNAddStates(0, 2);
            }
            break;
          case 17: 
          case 19: 
            if (this.curChar == '"') {
              jjCheckNAdd(16);
            }
            break;
          case 18: 
            if (this.curChar == '"') {
              jjAddStates(3, 4);
            }
            break;
          case 21: 
            if (this.curChar == '"') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 20;
            }
            break;
          case 22: 
            if (this.curChar == '"') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 15;
            }
            break;
          case 23: 
            if (this.curChar == '"') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 22;
            }
            break;
          case 24: 
            if (this.curChar == '\'') {
              jjCheckNAddStates(5, 7);
            }
            break;
          case 25: 
            if ((0xFFFFFF7FFFFFFFFF & l) != 0L) {
              jjCheckNAddStates(5, 7);
            }
            break;
          case 26: 
          case 28: 
            if (this.curChar == '\'') {
              jjCheckNAdd(25);
            }
            break;
          case 27: 
            if (this.curChar == '\'') {
              jjAddStates(8, 9);
            }
            break;
          case 30: 
            if (this.curChar == '\'') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 29;
            }
            break;
          case 31: 
            if (this.curChar == '\'') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 24;
            }
            break;
          case 32: 
            if (this.curChar == '\'') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 31;
            }
            break;
          case 33: 
            if (((0xFFFFFFFFFFFFF9FF & l) != 0L) && (kind > 60)) {
              kind = 60;
            }
            break;
          case 35: 
            if ((0x3FF600000000000 & l) != 0L)
            {
              if (kind > 54) {
                kind = 54;
              }
              jjCheckNAdd(35);
            }
            break;
          case 36: 
            if ((0x3FF600000000000 & l) != 0L) {
              jjCheckNAddTwoStates(36, 38);
            }
            break;
          case 37: 
            if ((this.curChar == '*') && (kind > 56)) {
              kind = 56;
            }
            break;
          case 38: 
            if (this.curChar == ':') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 37;
            }
            break;
          case 39: 
            if ((0x3FF600000000000 & l) != 0L) {
              jjCheckNAddTwoStates(39, 40);
            }
            break;
          case 40: 
            if (this.curChar == ':') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 41;
            }
            break;
          case 42: 
            if ((0x3FF600000000000 & l) != 0L)
            {
              if (kind > 57) {
                kind = 57;
              }
              this.jjstateSet[(this.jjnewStateCnt++)] = 42;
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
          case 3: 
            if (kind > 60) {
              kind = 60;
            }
            if ((0x7FFFFFE87FFFFFE & l) != 0L)
            {
              if (kind > 54) {
                kind = 54;
              }
              jjCheckNAddStates(10, 14);
            }
            else if (this.curChar == '\\')
            {
              this.jjstateSet[(this.jjnewStateCnt++)] = 7;
            }
            break;
          case 43: 
            if ((0x7FFFFFE87FFFFFE & l) != 0L) {
              jjCheckNAddTwoStates(39, 40);
            }
            if ((0x7FFFFFE87FFFFFE & l) != 0L) {
              jjCheckNAddTwoStates(36, 38);
            }
            if ((0x7FFFFFE87FFFFFE & l) != 0L)
            {
              if (kind > 54) {
                kind = 54;
              }
              jjCheckNAdd(35);
            }
            break;
          case 2: 
            if (kind > 40) {
              kind = 40;
            }
            this.jjstateSet[(this.jjnewStateCnt++)] = 2;
            break;
          case 5: 
            if (kind > 42) {
              kind = 42;
            }
            this.jjstateSet[(this.jjnewStateCnt++)] = 5;
            break;
          case 6: 
            if (this.curChar == '\\') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 7;
            }
            break;
          case 7: 
          case 8: 
            if ((0x7FFFFFE87FFFFFE & l) != 0L)
            {
              if (kind > 55) {
                kind = 55;
              }
              jjCheckNAdd(8);
            }
            break;
          case 10: 
            jjAddStates(15, 16);
            break;
          case 13: 
            jjAddStates(17, 18);
            break;
          case 16: 
            jjAddStates(0, 2);
            break;
          case 25: 
            jjAddStates(5, 7);
            break;
          case 33: 
            if (kind > 60) {
              kind = 60;
            }
            break;
          case 34: 
            if ((0x7FFFFFE87FFFFFE & l) != 0L)
            {
              if (kind > 54) {
                kind = 54;
              }
              jjCheckNAddStates(10, 14);
            }
            break;
          case 35: 
            if ((0x7FFFFFE87FFFFFE & l) != 0L)
            {
              if (kind > 54) {
                kind = 54;
              }
              jjCheckNAdd(35);
            }
            break;
          case 36: 
            if ((0x7FFFFFE87FFFFFE & l) != 0L) {
              jjCheckNAddTwoStates(36, 38);
            }
            break;
          case 39: 
            if ((0x7FFFFFE87FFFFFE & l) != 0L) {
              jjCheckNAddTwoStates(39, 40);
            }
            break;
          case 41: 
          case 42: 
            if ((0x7FFFFFE87FFFFFE & l) != 0L)
            {
              if (kind > 57) {
                kind = 57;
              }
              jjCheckNAdd(42);
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
          case 3: 
            if (jjCanMove_0(hiByte, i1, i2, l1, l2)) {
              if (kind > 60) {
                kind = 60;
              }
            }
            if (jjCanMove_1(hiByte, i1, i2, l1, l2))
            {
              if (kind > 54) {
                kind = 54;
              }
              jjCheckNAddStates(10, 14);
            }
            break;
          case 43: 
            if (jjCanMove_2(hiByte, i1, i2, l1, l2))
            {
              if (kind > 54) {
                kind = 54;
              }
              jjCheckNAdd(35);
            }
            if (jjCanMove_2(hiByte, i1, i2, l1, l2)) {
              jjCheckNAddTwoStates(36, 38);
            }
            if (jjCanMove_2(hiByte, i1, i2, l1, l2)) {
              jjCheckNAddTwoStates(39, 40);
            }
            break;
          case 2: 
            if (jjCanMove_0(hiByte, i1, i2, l1, l2))
            {
              if (kind > 40) {
                kind = 40;
              }
              this.jjstateSet[(this.jjnewStateCnt++)] = 2;
            }
            break;
          case 5: 
            if (jjCanMove_0(hiByte, i1, i2, l1, l2))
            {
              if (kind > 42) {
                kind = 42;
              }
              this.jjstateSet[(this.jjnewStateCnt++)] = 5;
            }
            break;
          case 7: 
            if (jjCanMove_1(hiByte, i1, i2, l1, l2))
            {
              if (kind > 55) {
                kind = 55;
              }
              jjCheckNAdd(8);
            }
            break;
          case 8: 
            if (jjCanMove_2(hiByte, i1, i2, l1, l2))
            {
              if (kind > 55) {
                kind = 55;
              }
              jjCheckNAdd(8);
            }
            break;
          case 10: 
            if (jjCanMove_0(hiByte, i1, i2, l1, l2)) {
              jjAddStates(15, 16);
            }
            break;
          case 13: 
            if (jjCanMove_0(hiByte, i1, i2, l1, l2)) {
              jjAddStates(17, 18);
            }
            break;
          case 16: 
            if (jjCanMove_0(hiByte, i1, i2, l1, l2)) {
              jjAddStates(0, 2);
            }
            break;
          case 25: 
            if (jjCanMove_0(hiByte, i1, i2, l1, l2)) {
              jjAddStates(5, 7);
            }
            break;
          case 33: 
            if ((jjCanMove_0(hiByte, i1, i2, l1, l2)) && (kind > 60)) {
              kind = 60;
            }
            break;
          case 34: 
            if (jjCanMove_1(hiByte, i1, i2, l1, l2))
            {
              if (kind > 54) {
                kind = 54;
              }
              jjCheckNAddStates(10, 14);
            }
            break;
          case 35: 
            if (jjCanMove_2(hiByte, i1, i2, l1, l2))
            {
              if (kind > 54) {
                kind = 54;
              }
              jjCheckNAdd(35);
            }
            break;
          case 36: 
            if (jjCanMove_2(hiByte, i1, i2, l1, l2)) {
              jjCheckNAddTwoStates(36, 38);
            }
            break;
          case 39: 
            if (jjCanMove_2(hiByte, i1, i2, l1, l2)) {
              jjCheckNAddTwoStates(39, 40);
            }
            break;
          case 41: 
            if (jjCanMove_1(hiByte, i1, i2, l1, l2))
            {
              if (kind > 57) {
                kind = 57;
              }
              jjCheckNAdd(42);
            }
            break;
          case 42: 
            if (jjCanMove_2(hiByte, i1, i2, l1, l2))
            {
              if (kind > 57) {
                kind = 57;
              }
              jjCheckNAdd(42);
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
      if ((i = this.jjnewStateCnt) == (startsAt = 43 - (this.jjnewStateCnt = startsAt))) {
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
  
  private final int jjMoveStringLiteralDfa0_1()
  {
    return jjMoveNfa_1(1, 0);
  }
  
  private final int jjMoveNfa_1(int startState, int curPos)
  {
    int startsAt = 0;
    this.jjnewStateCnt = 10;
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
            if ((0xFFFFFFFFFFFFF9FF & l) != 0L) {
              if (kind > 60) {
                kind = 60;
              }
            }
            if ((0x100000601 & l) != 0L)
            {
              if (kind > 39) {
                kind = 39;
              }
              jjCheckNAdd(0);
            }
            if ((0x401 & l) != 0L) {
              jjCheckNAddStates(19, 22);
            }
            break;
          case 0: 
            if ((0x100000601 & l) != 0L)
            {
              if (kind > 39) {
                kind = 39;
              }
              jjCheckNAdd(0);
            }
            break;
          case 2: 
            if ((0x401 & l) != 0L) {
              jjCheckNAddStates(19, 22);
            }
            break;
          case 3: 
            if ((0x100000200 & l) != 0L) {
              jjCheckNAddTwoStates(3, 6);
            }
            break;
          case 4: 
            if (this.curChar == '#')
            {
              if (kind > 43) {
                kind = 43;
              }
              jjCheckNAdd(5);
            }
            break;
          case 5: 
            if ((0xFFFFFFFFFFFFFBFE & l) != 0L)
            {
              if (kind > 43) {
                kind = 43;
              }
              jjCheckNAdd(5);
            }
            break;
          case 6: 
            if (this.curChar == '#') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 4;
            }
            break;
          case 7: 
            if ((0x100000200 & l) != 0L) {
              jjCheckNAddTwoStates(7, 8);
            }
            break;
          case 8: 
            if (this.curChar == '#')
            {
              if (kind > 44) {
                kind = 44;
              }
              jjCheckNAdd(9);
            }
            break;
          case 9: 
            if ((0xFFFFFFFFFFFFFBFE & l) != 0L)
            {
              if (kind > 44) {
                kind = 44;
              }
              jjCheckNAdd(9);
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
          case 1: 
            if (kind > 60) {
              kind = 60;
            }
            break;
          case 5: 
            if (kind > 43) {
              kind = 43;
            }
            this.jjstateSet[(this.jjnewStateCnt++)] = 5;
            break;
          case 9: 
            if (kind > 44) {
              kind = 44;
            }
            this.jjstateSet[(this.jjnewStateCnt++)] = 9;
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
            if ((jjCanMove_0(hiByte, i1, i2, l1, l2)) && (kind > 60)) {
              kind = 60;
            }
            break;
          case 5: 
            if (jjCanMove_0(hiByte, i1, i2, l1, l2))
            {
              if (kind > 43) {
                kind = 43;
              }
              this.jjstateSet[(this.jjnewStateCnt++)] = 5;
            }
            break;
          case 9: 
            if (jjCanMove_0(hiByte, i1, i2, l1, l2))
            {
              if (kind > 44) {
                kind = 44;
              }
              this.jjstateSet[(this.jjnewStateCnt++)] = 9;
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
      if ((i = this.jjnewStateCnt) == (startsAt = 10 - (this.jjnewStateCnt = startsAt))) {
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
  
  private final int jjMoveStringLiteralDfa0_2()
  {
    return jjMoveNfa_2(1, 0);
  }
  
  private final int jjMoveNfa_2(int startState, int curPos)
  {
    int startsAt = 0;
    this.jjnewStateCnt = 7;
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
            if ((0xFFFFFFFFFFFFF9FF & l) != 0L) {
              if (kind > 60) {
                kind = 60;
              }
            }
            if ((0x100000601 & l) != 0L)
            {
              if (kind > 39) {
                kind = 39;
              }
              jjCheckNAdd(0);
            }
            if ((0x401 & l) != 0L) {
              jjCheckNAddTwoStates(2, 5);
            }
            break;
          case 0: 
            if ((0x100000601 & l) != 0L)
            {
              if (kind > 39) {
                kind = 39;
              }
              jjCheckNAdd(0);
            }
            break;
          case 2: 
            if ((0x100000200 & l) != 0L) {
              jjCheckNAddTwoStates(2, 5);
            }
            break;
          case 3: 
            if (this.curChar == '#')
            {
              if (kind > 41) {
                kind = 41;
              }
              jjCheckNAdd(4);
            }
            break;
          case 4: 
            if ((0xFFFFFFFFFFFFFBFE & l) != 0L)
            {
              if (kind > 41) {
                kind = 41;
              }
              jjCheckNAdd(4);
            }
            break;
          case 5: 
            if (this.curChar == '#') {
              this.jjstateSet[(this.jjnewStateCnt++)] = 3;
            }
            break;
          case 6: 
            if (((0xFFFFFFFFFFFFF9FF & l) != 0L) && (kind > 60)) {
              kind = 60;
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
          case 1: 
            if (kind > 60) {
              kind = 60;
            }
            break;
          case 4: 
            if (kind > 41) {
              kind = 41;
            }
            this.jjstateSet[(this.jjnewStateCnt++)] = 4;
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
            if ((jjCanMove_0(hiByte, i1, i2, l1, l2)) && (kind > 60)) {
              kind = 60;
            }
            break;
          case 4: 
            if (jjCanMove_0(hiByte, i1, i2, l1, l2))
            {
              if (kind > 41) {
                kind = 41;
              }
              this.jjstateSet[(this.jjnewStateCnt++)] = 4;
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
      if ((i = this.jjnewStateCnt) == (startsAt = 7 - (this.jjnewStateCnt = startsAt))) {
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
  
  static final int[] jjnextStates = { 16, 17, 18, 19, 21, 25, 26, 27, 28, 30, 35, 36, 38, 39, 40, 10, 11, 13, 14, 3, 6, 7, 8 };
  
  private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
  {
    switch (hiByte)
    {
    case 0: 
      return (jjbitVec2[i2] & l2) != 0L;
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
      return (jjbitVec4[i2] & l2) != 0L;
    case 1: 
      return (jjbitVec5[i2] & l2) != 0L;
    case 2: 
      return (jjbitVec6[i2] & l2) != 0L;
    case 3: 
      return (jjbitVec7[i2] & l2) != 0L;
    case 4: 
      return (jjbitVec8[i2] & l2) != 0L;
    case 5: 
      return (jjbitVec9[i2] & l2) != 0L;
    case 6: 
      return (jjbitVec10[i2] & l2) != 0L;
    case 9: 
      return (jjbitVec11[i2] & l2) != 0L;
    case 10: 
      return (jjbitVec12[i2] & l2) != 0L;
    case 11: 
      return (jjbitVec13[i2] & l2) != 0L;
    case 12: 
      return (jjbitVec14[i2] & l2) != 0L;
    case 13: 
      return (jjbitVec15[i2] & l2) != 0L;
    case 14: 
      return (jjbitVec16[i2] & l2) != 0L;
    case 15: 
      return (jjbitVec17[i2] & l2) != 0L;
    case 16: 
      return (jjbitVec18[i2] & l2) != 0L;
    case 17: 
      return (jjbitVec19[i2] & l2) != 0L;
    case 30: 
      return (jjbitVec20[i2] & l2) != 0L;
    case 31: 
      return (jjbitVec21[i2] & l2) != 0L;
    case 33: 
      return (jjbitVec22[i2] & l2) != 0L;
    case 48: 
      return (jjbitVec23[i2] & l2) != 0L;
    case 49: 
      return (jjbitVec24[i2] & l2) != 0L;
    case 159: 
      return (jjbitVec25[i2] & l2) != 0L;
    case 215: 
      return (jjbitVec26[i2] & l2) != 0L;
    }
    if ((jjbitVec3[i1] & l1) != 0L) {
      return true;
    }
    return false;
  }
  
  private static final boolean jjCanMove_2(int hiByte, int i1, int i2, long l1, long l2)
  {
    switch (hiByte)
    {
    case 0: 
      return (jjbitVec27[i2] & l2) != 0L;
    case 1: 
      return (jjbitVec5[i2] & l2) != 0L;
    case 2: 
      return (jjbitVec28[i2] & l2) != 0L;
    case 3: 
      return (jjbitVec29[i2] & l2) != 0L;
    case 4: 
      return (jjbitVec30[i2] & l2) != 0L;
    case 5: 
      return (jjbitVec31[i2] & l2) != 0L;
    case 6: 
      return (jjbitVec32[i2] & l2) != 0L;
    case 9: 
      return (jjbitVec33[i2] & l2) != 0L;
    case 10: 
      return (jjbitVec34[i2] & l2) != 0L;
    case 11: 
      return (jjbitVec35[i2] & l2) != 0L;
    case 12: 
      return (jjbitVec36[i2] & l2) != 0L;
    case 13: 
      return (jjbitVec37[i2] & l2) != 0L;
    case 14: 
      return (jjbitVec38[i2] & l2) != 0L;
    case 15: 
      return (jjbitVec39[i2] & l2) != 0L;
    case 16: 
      return (jjbitVec18[i2] & l2) != 0L;
    case 17: 
      return (jjbitVec19[i2] & l2) != 0L;
    case 30: 
      return (jjbitVec20[i2] & l2) != 0L;
    case 31: 
      return (jjbitVec21[i2] & l2) != 0L;
    case 32: 
      return (jjbitVec40[i2] & l2) != 0L;
    case 33: 
      return (jjbitVec22[i2] & l2) != 0L;
    case 48: 
      return (jjbitVec41[i2] & l2) != 0L;
    case 49: 
      return (jjbitVec24[i2] & l2) != 0L;
    case 159: 
      return (jjbitVec25[i2] & l2) != 0L;
    case 215: 
      return (jjbitVec26[i2] & l2) != 0L;
    }
    if ((jjbitVec3[i1] & l1) != 0L) {
      return true;
    }
    return false;
  }
  
  public static final String[] jjstrLiteralImages = { "", "[", "=", "&=", "|=", "start", "div", "include", "~", "]", "grammar", "{", "}", "namespace", "default", "inherit", "datatypes", "empty", "text", "notAllowed", "|", "&", ",", "+", "?", "*", "element", "attribute", "(", ")", "-", "list", "mixed", "external", "parent", "string", "token", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ">>", null };
  public static final String[] lexStateNames = { "DEFAULT", "AFTER_SINGLE_LINE_COMMENT", "AFTER_DOCUMENTATION" };
  public static final int[] jjnewLexState = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 2, -1, 1, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };
  static final long[] jjtoToken = { 2287840842771070975L };
  static final long[] jjtoSkip = { 22539988369408L };
  static final long[] jjtoSpecial = { 21990232555520L };
  protected JavaCharStream input_stream;
  private final int[] jjrounds = new int[43];
  private final int[] jjstateSet = new int[86];
  StringBuffer image;
  int jjimageLen;
  int lengthOfMatch;
  protected char curChar;
  
  public CompactSyntaxTokenManager(JavaCharStream stream)
  {
    this.input_stream = stream;
  }
  
  public CompactSyntaxTokenManager(JavaCharStream stream, int lexState)
  {
    this(stream);
    SwitchTo(lexState);
  }
  
  public void ReInit(JavaCharStream stream)
  {
    this.jjmatchedPos = (this.jjnewStateCnt = 0);
    this.curLexState = this.defaultLexState;
    this.input_stream = stream;
    ReInitRounds();
  }
  
  private final void ReInitRounds()
  {
    this.jjround = -2147483647;
    for (int i = 43; i-- > 0;) {
      this.jjrounds[i] = Integer.MIN_VALUE;
    }
  }
  
  public void ReInit(JavaCharStream stream, int lexState)
  {
    ReInit(stream);
    SwitchTo(lexState);
  }
  
  public void SwitchTo(int lexState)
  {
    if ((lexState >= 3) || (lexState < 0)) {
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
    for (;;)
    {
      try
      {
        this.curChar = this.input_stream.BeginToken();
      }
      catch (IOException e)
      {
        this.jjmatchedKind = 0;
        Token matchedToken = jjFillToken();
        matchedToken.specialToken = specialToken;
        return matchedToken;
      }
      this.image = null;
      this.jjimageLen = 0;
      switch (this.curLexState)
      {
      case 0: 
        this.jjmatchedKind = Integer.MAX_VALUE;
        this.jjmatchedPos = 0;
        curPos = jjMoveStringLiteralDfa0_0();
        break;
      case 1: 
        this.jjmatchedKind = Integer.MAX_VALUE;
        this.jjmatchedPos = 0;
        curPos = jjMoveStringLiteralDfa0_1();
        break;
      case 2: 
        this.jjmatchedKind = Integer.MAX_VALUE;
        this.jjmatchedPos = 0;
        curPos = jjMoveStringLiteralDfa0_2();
      }
      if (this.jjmatchedKind == Integer.MAX_VALUE) {
        break;
      }
      if (this.jjmatchedPos + 1 < curPos) {
        this.input_stream.backup(curPos - this.jjmatchedPos - 1);
      }
      if ((jjtoToken[(this.jjmatchedKind >> 6)] & 1L << (this.jjmatchedKind & 0x3F)) != 0L)
      {
        Token matchedToken = jjFillToken();
        matchedToken.specialToken = specialToken;
        if (jjnewLexState[this.jjmatchedKind] != -1) {
          this.curLexState = jjnewLexState[this.jjmatchedKind];
        }
        return matchedToken;
      }
      if ((jjtoSpecial[(this.jjmatchedKind >> 6)] & 1L << (this.jjmatchedKind & 0x3F)) != 0L)
      {
        Token matchedToken = jjFillToken();
        if (specialToken == null)
        {
          specialToken = matchedToken;
        }
        else
        {
          matchedToken.specialToken = specialToken;
          specialToken = specialToken.next = matchedToken;
        }
        SkipLexicalActions(matchedToken);
      }
      else
      {
        SkipLexicalActions(null);
      }
      if (jjnewLexState[this.jjmatchedKind] != -1) {
        this.curLexState = jjnewLexState[this.jjmatchedKind];
      }
    }
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
  
  void SkipLexicalActions(Token matchedToken)
  {
    switch (this.jjmatchedKind)
    {
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\compact\CompactSyntaxTokenManager.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */