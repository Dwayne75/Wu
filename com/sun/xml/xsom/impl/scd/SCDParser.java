package com.sun.xml.xsom.impl.scd;

import com.sun.xml.xsom.impl.UName;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.xml.namespace.NamespaceContext;

public class SCDParser
  implements SCDParserConstants
{
  private NamespaceContext nsc;
  public SCDParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  public Token token;
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  
  public SCDParser(String text, NamespaceContext nsc)
  {
    this(new StringReader(text));
    this.nsc = nsc;
  }
  
  private String trim(String s)
  {
    return s.substring(1, s.length() - 1);
  }
  
  private String resolvePrefix(String prefix)
    throws ParseException
  {
    try
    {
      String r = this.nsc.getNamespaceURI(prefix);
      if (prefix.equals("")) {
        return r;
      }
      if (!r.equals("")) {
        return r;
      }
    }
    catch (IllegalArgumentException e) {}
    throw new ParseException("Unbound prefix: " + prefix);
  }
  
  public final UName QName()
    throws ParseException
  {
    Token l = null;
    Token p = jj_consume_token(12);
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 15: 
      jj_consume_token(15);
      l = jj_consume_token(12);
      break;
    default: 
      this.jj_la1[0] = this.jj_gen;
    }
    if (l == null) {
      return new UName(resolvePrefix(""), p.image);
    }
    return new UName(resolvePrefix(p.image), l.image);
  }
  
  public final String Prefix()
    throws ParseException
  {
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 12: 
      Token p = jj_consume_token(12);
      return resolvePrefix(p.image);
    }
    this.jj_la1[1] = this.jj_gen;
    return resolvePrefix("");
  }
  
  public final List RelativeSchemaComponentPath()
    throws ParseException
  {
    List steps = new ArrayList();
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 16: 
    case 17: 
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 16: 
        jj_consume_token(16);
        steps.add(new Step.Any(Axis.ROOT));
        break;
      case 17: 
        jj_consume_token(17);
        steps.add(new Step.Any(Axis.DESCENDANTS));
        break;
      default: 
        this.jj_la1[2] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    default: 
      this.jj_la1[3] = this.jj_gen;
    }
    Step s = Step();
    steps.add(s);
    for (;;)
    {
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 16: 
      case 17: 
        break;
      default: 
        this.jj_la1[4] = this.jj_gen;
        break;
      }
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 16: 
        jj_consume_token(16);
        break;
      case 17: 
        jj_consume_token(17);
        steps.add(new Step.Any(Axis.DESCENDANTS));
        break;
      default: 
        this.jj_la1[5] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      s = Step();
      steps.add(s);
    }
    return steps;
  }
  
  public final Step Step()
    throws ParseException
  {
    Step s;
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 18: 
    case 19: 
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 18: 
        jj_consume_token(18);
        break;
      case 19: 
        jj_consume_token(19);
        break;
      default: 
        this.jj_la1[6] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      s = NameOrWildcard(Axis.ATTRIBUTE);
      break;
    case 12: 
    case 20: 
    case 45: 
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 20: 
        jj_consume_token(20);
        break;
      default: 
        this.jj_la1[7] = this.jj_gen;
      }
      s = NameOrWildcard(Axis.ELEMENT);
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 13: 
        Predicate(s);
        break;
      default: 
        this.jj_la1[8] = this.jj_gen;
      }
      break;
    case 21: 
      jj_consume_token(21);
      s = NameOrWildcard(Axis.SUBSTITUTION_GROUP);
      break;
    case 22: 
    case 23: 
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 22: 
        jj_consume_token(22);
        break;
      case 23: 
        jj_consume_token(23);
        break;
      default: 
        this.jj_la1[9] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      s = NameOrWildcardOrAnonymous(Axis.TYPE_DEFINITION);
      break;
    case 24: 
      jj_consume_token(24);
      s = NameOrWildcard(Axis.BASETYPE);
      break;
    case 25: 
      jj_consume_token(25);
      s = NameOrWildcard(Axis.PRIMITIVE_TYPE);
      break;
    case 26: 
      jj_consume_token(26);
      s = NameOrWildcardOrAnonymous(Axis.ITEM_TYPE);
      break;
    case 27: 
      jj_consume_token(27);
      s = NameOrWildcardOrAnonymous(Axis.MEMBER_TYPE);
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 13: 
        Predicate(s);
        break;
      default: 
        this.jj_la1[10] = this.jj_gen;
      }
      break;
    case 28: 
      jj_consume_token(28);
      s = NameOrWildcardOrAnonymous(Axis.SCOPE);
      break;
    case 29: 
      jj_consume_token(29);
      s = NameOrWildcard(Axis.ATTRIBUTE_GROUP);
      break;
    case 30: 
      jj_consume_token(30);
      s = NameOrWildcard(Axis.MODEL_GROUP_DECL);
      break;
    case 31: 
      jj_consume_token(31);
      s = NameOrWildcard(Axis.IDENTITY_CONSTRAINT);
      break;
    case 32: 
      jj_consume_token(32);
      s = NameOrWildcard(Axis.REFERENCED_KEY);
      break;
    case 33: 
      jj_consume_token(33);
      s = NameOrWildcard(Axis.NOTATION);
      break;
    case 34: 
      jj_consume_token(34);
      s = new Step.Any(Axis.MODELGROUP_SEQUENCE);
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 13: 
        Predicate(s);
        break;
      default: 
        this.jj_la1[11] = this.jj_gen;
      }
      break;
    case 35: 
      jj_consume_token(35);
      s = new Step.Any(Axis.MODELGROUP_CHOICE);
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 13: 
        Predicate(s);
        break;
      default: 
        this.jj_la1[12] = this.jj_gen;
      }
      break;
    case 36: 
      jj_consume_token(36);
      s = new Step.Any(Axis.MODELGROUP_ALL);
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 13: 
        Predicate(s);
        break;
      default: 
        this.jj_la1[13] = this.jj_gen;
      }
      break;
    case 37: 
      jj_consume_token(37);
      s = new Step.Any(Axis.MODELGROUP_ANY);
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 13: 
        Predicate(s);
        break;
      default: 
        this.jj_la1[14] = this.jj_gen;
      }
      break;
    case 38: 
      jj_consume_token(38);
      s = new Step.Any(Axis.WILDCARD);
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 13: 
        Predicate(s);
        break;
      default: 
        this.jj_la1[15] = this.jj_gen;
      }
      break;
    case 39: 
      jj_consume_token(39);
      s = new Step.Any(Axis.ATTRIBUTE_WILDCARD);
      break;
    case 40: 
      jj_consume_token(40);
      s = new Step.Any(Axis.FACET);
      break;
    case 41: 
      jj_consume_token(41);
      Token n = jj_consume_token(14);
      s = new Step.Facet(Axis.FACET, n.image);
      break;
    case 42: 
      jj_consume_token(42);
      s = new Step.Any(Axis.DESCENDANTS);
      break;
    case 43: 
      jj_consume_token(43);
      String p = Prefix();
      s = new Step.Schema(Axis.X_SCHEMA, p);
      break;
    case 44: 
      jj_consume_token(44);
      s = new Step.Any(Axis.X_SCHEMA);
      break;
    case 13: 
    case 14: 
    case 15: 
    case 16: 
    case 17: 
    default: 
      this.jj_la1[16] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    return s;
  }
  
  public final Step NameOrWildcard(Axis a)
    throws ParseException
  {
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 12: 
      UName un = QName();
      return new Step.Named(a, un);
    case 45: 
      jj_consume_token(45);
      return new Step.Any(a);
    }
    this.jj_la1[17] = this.jj_gen;
    jj_consume_token(-1);
    throw new ParseException();
  }
  
  public final Step NameOrWildcardOrAnonymous(Axis a)
    throws ParseException
  {
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 12: 
      UName un = QName();
      return new Step.Named(a, un);
    case 45: 
      jj_consume_token(45);
      return new Step.Any(a);
    case 46: 
      jj_consume_token(46);
      return new Step.AnonymousType(a);
    }
    this.jj_la1[18] = this.jj_gen;
    jj_consume_token(-1);
    throw new ParseException();
  }
  
  public final int Predicate(Step s)
    throws ParseException
  {
    Token t = jj_consume_token(13);
    return s.predicate = Integer.parseInt(trim(t.image));
  }
  
  private final int[] jj_la1 = new int[19];
  private static int[] jj_la1_0;
  private static int[] jj_la1_1;
  
  static
  {
    jj_la1_0();
    jj_la1_1();
  }
  
  private static void jj_la1_0()
  {
    jj_la1_0 = new int[] { 32768, 4096, 196608, 196608, 196608, 196608, 786432, 1048576, 8192, 12582912, 8192, 8192, 8192, 8192, 8192, 8192, -258048, 4096, 4096 };
  }
  
  private static void jj_la1_1()
  {
    jj_la1_1 = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16383, 8192, 24576 };
  }
  
  public SCDParser(InputStream stream)
  {
    this(stream, null);
  }
  
  public SCDParser(InputStream stream, String encoding)
  {
    try
    {
      this.jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1);
    }
    catch (UnsupportedEncodingException e)
    {
      throw new RuntimeException(e);
    }
    this.token_source = new SCDParserTokenManager(this.jj_input_stream);
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 19; i++) {
      this.jj_la1[i] = -1;
    }
  }
  
  public void ReInit(InputStream stream)
  {
    ReInit(stream, null);
  }
  
  public void ReInit(InputStream stream, String encoding)
  {
    try
    {
      this.jj_input_stream.ReInit(stream, encoding, 1, 1);
    }
    catch (UnsupportedEncodingException e)
    {
      throw new RuntimeException(e);
    }
    this.token_source.ReInit(this.jj_input_stream);
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 19; i++) {
      this.jj_la1[i] = -1;
    }
  }
  
  public SCDParser(Reader stream)
  {
    this.jj_input_stream = new SimpleCharStream(stream, 1, 1);
    this.token_source = new SCDParserTokenManager(this.jj_input_stream);
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 19; i++) {
      this.jj_la1[i] = -1;
    }
  }
  
  public void ReInit(Reader stream)
  {
    this.jj_input_stream.ReInit(stream, 1, 1);
    this.token_source.ReInit(this.jj_input_stream);
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 19; i++) {
      this.jj_la1[i] = -1;
    }
  }
  
  public SCDParser(SCDParserTokenManager tm)
  {
    this.token_source = tm;
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 19; i++) {
      this.jj_la1[i] = -1;
    }
  }
  
  public void ReInit(SCDParserTokenManager tm)
  {
    this.token_source = tm;
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 19; i++) {
      this.jj_la1[i] = -1;
    }
  }
  
  private final Token jj_consume_token(int kind)
    throws ParseException
  {
    Token oldToken;
    if ((oldToken = this.token).next != null) {
      this.token = this.token.next;
    } else {
      this.token = (this.token.next = this.token_source.getNextToken());
    }
    this.jj_ntk = -1;
    if (this.token.kind == kind)
    {
      this.jj_gen += 1;
      return this.token;
    }
    this.token = oldToken;
    this.jj_kind = kind;
    throw generateParseException();
  }
  
  public final Token getNextToken()
  {
    if (this.token.next != null) {
      this.token = this.token.next;
    } else {
      this.token = (this.token.next = this.token_source.getNextToken());
    }
    this.jj_ntk = -1;
    this.jj_gen += 1;
    return this.token;
  }
  
  public final Token getToken(int index)
  {
    Token t = this.token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) {
        t = t.next;
      } else {
        t = t.next = this.token_source.getNextToken();
      }
    }
    return t;
  }
  
  private final int jj_ntk()
  {
    if ((this.jj_nt = this.token.next) == null) {
      return this.jj_ntk = (this.token.next = this.token_source.getNextToken()).kind;
    }
    return this.jj_ntk = this.jj_nt.kind;
  }
  
  private Vector jj_expentries = new Vector();
  private int[] jj_expentry;
  private int jj_kind = -1;
  
  public ParseException generateParseException()
  {
    this.jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[47];
    for (int i = 0; i < 47; i++) {
      la1tokens[i] = false;
    }
    if (this.jj_kind >= 0)
    {
      la1tokens[this.jj_kind] = true;
      this.jj_kind = -1;
    }
    for (int i = 0; i < 19; i++) {
      if (this.jj_la1[i] == this.jj_gen) {
        for (int j = 0; j < 32; j++)
        {
          if ((jj_la1_0[i] & 1 << j) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & 1 << j) != 0) {
            la1tokens[(32 + j)] = true;
          }
        }
      }
    }
    for (int i = 0; i < 47; i++) {
      if (la1tokens[i] != 0)
      {
        this.jj_expentry = new int[1];
        this.jj_expentry[0] = i;
        this.jj_expentries.addElement(this.jj_expentry);
      }
    }
    int[][] exptokseq = new int[this.jj_expentries.size()][];
    for (int i = 0; i < this.jj_expentries.size(); i++) {
      exptokseq[i] = ((int[])(int[])this.jj_expentries.elementAt(i));
    }
    return new ParseException(this.token, exptokseq, tokenImage);
  }
  
  public final void enable_tracing() {}
  
  public final void disable_tracing() {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\scd\SCDParser.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */