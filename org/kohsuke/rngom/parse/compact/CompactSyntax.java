package org.kohsuke.rngom.parse.compact;

import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.CommentList;
import org.kohsuke.rngom.ast.builder.DataPatternBuilder;
import org.kohsuke.rngom.ast.builder.Div;
import org.kohsuke.rngom.ast.builder.ElementAnnotationBuilder;
import org.kohsuke.rngom.ast.builder.Grammar;
import org.kohsuke.rngom.ast.builder.GrammarSection;
import org.kohsuke.rngom.ast.builder.GrammarSection.Combine;
import org.kohsuke.rngom.ast.builder.Include;
import org.kohsuke.rngom.ast.builder.IncludedGrammar;
import org.kohsuke.rngom.ast.builder.NameClassBuilder;
import org.kohsuke.rngom.ast.builder.SchemaBuilder;
import org.kohsuke.rngom.ast.builder.Scope;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.om.ParsedNameClass;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.parse.Context;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.Parseable;
import org.kohsuke.rngom.util.Localizer;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

public class CompactSyntax
  implements Context, CompactSyntaxConstants
{
  private static final int IN_ELEMENT = 0;
  private static final int IN_ATTRIBUTE = 1;
  private static final int IN_ANY_NAME = 2;
  private static final int IN_NS_NAME = 4;
  private String defaultNamespace;
  private String compatibilityPrefix = null;
  private SchemaBuilder sb;
  private NameClassBuilder ncb;
  private String sourceUri;
  private CompactParseable parseable;
  private ErrorHandler eh;
  private final Hashtable namespaceTable = new Hashtable();
  private final Hashtable datatypesTable = new Hashtable();
  private boolean hadError = false;
  private static final Localizer localizer = new Localizer(new Localizer(Parseable.class), CompactSyntax.class);
  private final Hashtable attributeNameTable = new Hashtable();
  private boolean annotationsIncludeElements = false;
  private String inheritedNs;
  private CommentList topLevelComments;
  
  static final class JJCalls
  {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }
  
  private static final class LookaheadSuccess
    extends Error
  {}
  
  final class LocatedString
  {
    private final String str;
    private final Token tok;
    
    LocatedString(String str, Token tok)
    {
      this.str = str;
      this.tok = tok;
    }
    
    String getString()
    {
      return this.str;
    }
    
    Location getLocation()
    {
      return CompactSyntax.this.makeLocation(this.tok);
    }
    
    Token getToken()
    {
      return this.tok;
    }
  }
  
  public CompactSyntax(CompactParseable parseable, Reader r, String sourceUri, SchemaBuilder sb, ErrorHandler eh, String inheritedNs)
  {
    this(r);
    this.sourceUri = sourceUri;
    this.parseable = parseable;
    this.sb = sb;
    this.ncb = sb.getNameClassBuilder();
    this.eh = eh;
    
    this.topLevelComments = sb.makeCommentList();
    this.inheritedNs = (this.defaultNamespace = new String(inheritedNs));
  }
  
  ParsedPattern parse(Scope scope)
    throws IllegalSchemaException
  {
    try
    {
      ParsedPattern p = Input(scope);
      if (!this.hadError) {
        return p;
      }
    }
    catch (ParseException e)
    {
      error("syntax_error", e.getMessage(), e.currentToken.next);
    }
    catch (EscapeSyntaxException e)
    {
      reportEscapeSyntaxException(e);
    }
    throw new IllegalSchemaException();
  }
  
  ParsedPattern parseInclude(IncludedGrammar g)
    throws IllegalSchemaException
  {
    try
    {
      ParsedPattern p = IncludedGrammar(g);
      if (!this.hadError) {
        return p;
      }
    }
    catch (ParseException e)
    {
      error("syntax_error", e.getMessage(), e.currentToken.next);
    }
    catch (EscapeSyntaxException e)
    {
      reportEscapeSyntaxException(e);
    }
    throw new IllegalSchemaException();
  }
  
  private void checkNsName(int context, LocatedString ns)
  {
    if ((context & 0x4) != 0) {
      error("ns_name_except_contains_ns_name", ns.getToken());
    }
  }
  
  private void checkAnyName(int context, Token t)
  {
    if ((context & 0x4) != 0) {
      error("ns_name_except_contains_any_name", t);
    }
    if ((context & 0x2) != 0) {
      error("any_name_except_contains_any_name", t);
    }
  }
  
  private void error(String key, Token tok)
  {
    doError(localizer.message(key), tok);
  }
  
  private void error(String key, String arg, Token tok)
  {
    doError(localizer.message(key, arg), tok);
  }
  
  private void error(String key, String arg1, String arg2, Token tok)
  {
    doError(localizer.message(key, arg1, arg2), tok);
  }
  
  private void doError(String message, Token tok)
  {
    this.hadError = true;
    if (this.eh != null)
    {
      LocatorImpl loc = new LocatorImpl();
      loc.setLineNumber(tok.beginLine);
      loc.setColumnNumber(tok.beginColumn);
      loc.setSystemId(this.sourceUri);
      try
      {
        this.eh.error(new SAXParseException(message, loc));
      }
      catch (SAXException se)
      {
        throw new BuildException(se);
      }
    }
  }
  
  private void reportEscapeSyntaxException(EscapeSyntaxException e)
  {
    if (this.eh != null)
    {
      LocatorImpl loc = new LocatorImpl();
      loc.setLineNumber(e.getLineNumber());
      loc.setColumnNumber(e.getColumnNumber());
      loc.setSystemId(this.sourceUri);
      try
      {
        this.eh.error(new SAXParseException(localizer.message(e.getKey()), loc));
      }
      catch (SAXException se)
      {
        throw new BuildException(se);
      }
    }
  }
  
  private static String unquote(String s)
  {
    if ((s.length() >= 6) && (s.charAt(0) == s.charAt(1)))
    {
      s = s.replace('\000', '\n');
      return s.substring(3, s.length() - 3);
    }
    return s.substring(1, s.length() - 1);
  }
  
  Location makeLocation(Token t)
  {
    return this.sb.makeLocation(this.sourceUri, t.beginLine, t.beginColumn);
  }
  
  private static ParsedPattern[] addPattern(ParsedPattern[] patterns, int i, ParsedPattern p)
  {
    if (i >= patterns.length)
    {
      ParsedPattern[] oldPatterns = patterns;
      patterns = new ParsedPattern[oldPatterns.length * 2];
      System.arraycopy(oldPatterns, 0, patterns, 0, oldPatterns.length);
    }
    patterns[i] = p;
    return patterns;
  }
  
  String getCompatibilityPrefix()
  {
    if (this.compatibilityPrefix == null)
    {
      this.compatibilityPrefix = "a";
      while (this.namespaceTable.get(this.compatibilityPrefix) != null) {
        this.compatibilityPrefix += "a";
      }
    }
    return this.compatibilityPrefix;
  }
  
  public String resolveNamespacePrefix(String prefix)
  {
    String result = (String)this.namespaceTable.get(prefix);
    if (result.length() == 0) {
      return null;
    }
    return result;
  }
  
  public Enumeration prefixes()
  {
    return this.namespaceTable.keys();
  }
  
  public String getBaseUri()
  {
    return this.sourceUri;
  }
  
  public boolean isUnparsedEntity(String entityName)
  {
    return false;
  }
  
  public boolean isNotation(String notationName)
  {
    return false;
  }
  
  public Context copy()
  {
    return this;
  }
  
  private Context getContext()
  {
    return this;
  }
  
  private CommentList getComments()
  {
    return getComments(getTopLevelComments());
  }
  
  private CommentList getTopLevelComments()
  {
    CommentList tem = this.topLevelComments;
    this.topLevelComments = null;
    return tem;
  }
  
  private void noteTopLevelComments()
  {
    this.topLevelComments = getComments(this.topLevelComments);
  }
  
  private void topLevelComments(GrammarSection section)
  {
    section.topLevelComment(getComments(null));
  }
  
  private Token lastCommentSourceToken = null;
  public CompactSyntaxTokenManager token_source;
  JavaCharStream jj_input_stream;
  public Token token;
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos;
  private Token jj_lastpos;
  private int jj_la;
  
  private CommentList getComments(CommentList comments)
  {
    Token nextToken = getToken(1);
    if (this.lastCommentSourceToken != nextToken)
    {
      if (this.lastCommentSourceToken == null) {
        this.lastCommentSourceToken = this.token;
      }
      do
      {
        this.lastCommentSourceToken = this.lastCommentSourceToken.next;
        Token t = this.lastCommentSourceToken.specialToken;
        if (t != null)
        {
          while (t.specialToken != null) {
            t = t.specialToken;
          }
          if (comments == null) {
            comments = this.sb.makeCommentList();
          }
          for (; t != null; t = t.next)
          {
            String s = mungeComment(t.image);
            Location loc = makeLocation(t);
            if ((t.next != null) && (t.next.kind == 44))
            {
              StringBuffer buf = new StringBuffer(s);
              do
              {
                t = t.next;
                buf.append('\n');
                buf.append(mungeComment(t.image));
              } while ((t.next != null) && (t.next.kind == 44));
              s = buf.toString();
            }
            comments.addComment(s, loc);
          }
        }
      } while (this.lastCommentSourceToken != nextToken);
    }
    return comments;
  }
  
  private ParsedPattern afterComments(ParsedPattern p)
  {
    CommentList comments = getComments(null);
    if (comments == null) {
      return p;
    }
    return this.sb.commentAfter(p, comments);
  }
  
  private ParsedNameClass afterComments(ParsedNameClass nc)
  {
    CommentList comments = getComments(null);
    if (comments == null) {
      return nc;
    }
    return this.ncb.commentAfter(nc, comments);
  }
  
  private static String mungeComment(String image)
  {
    int i = image.indexOf('#') + 1;
    while ((i < image.length()) && (image.charAt(i) == '#')) {
      i++;
    }
    if ((i < image.length()) && (image.charAt(i) == ' ')) {
      i++;
    }
    return image.substring(i);
  }
  
  private Annotations getCommentsAsAnnotations()
  {
    CommentList comments = getComments();
    if (comments == null) {
      return null;
    }
    return this.sb.makeAnnotations(comments, getContext());
  }
  
  private Annotations addCommentsToChildAnnotations(Annotations a)
  {
    CommentList comments = getComments();
    if (comments == null) {
      return a;
    }
    if (a == null) {
      a = this.sb.makeAnnotations(null, getContext());
    }
    a.addComment(comments);
    return a;
  }
  
  private Annotations addCommentsToLeadingAnnotations(Annotations a)
  {
    CommentList comments = getComments();
    if (comments == null) {
      return a;
    }
    if (a == null) {
      return this.sb.makeAnnotations(comments, getContext());
    }
    a.addLeadingComment(comments);
    return a;
  }
  
  private Annotations getTopLevelCommentsAsAnnotations()
  {
    CommentList comments = getTopLevelComments();
    if (comments == null) {
      return null;
    }
    return this.sb.makeAnnotations(comments, getContext());
  }
  
  private void clearAttributeList()
  {
    this.attributeNameTable.clear();
  }
  
  private void addAttribute(Annotations a, String ns, String localName, String prefix, String value, Token tok)
  {
    String key = ns + "#" + localName;
    if (this.attributeNameTable.get(key) != null)
    {
      error("duplicate_attribute", ns, localName, tok);
    }
    else
    {
      this.attributeNameTable.put(key, key);
      a.addAttribute(ns, localName, prefix, value, makeLocation(tok));
    }
  }
  
  private void checkExcept(Token[] except)
  {
    if (except[0] != null) {
      error("except_missing_parentheses", except[0]);
    }
  }
  
  private String lookupPrefix(String prefix, Token t)
  {
    String ns = (String)this.namespaceTable.get(prefix);
    if (ns == null)
    {
      error("undeclared_prefix", prefix, t);
      return "#error";
    }
    return ns;
  }
  
  private String lookupDatatype(String prefix, Token t)
  {
    String ns = (String)this.datatypesTable.get(prefix);
    if (ns == null)
    {
      error("undeclared_prefix", prefix, t);
      return "";
    }
    return ns;
  }
  
  private String resolve(String str)
  {
    try
    {
      return new URL(new URL(this.sourceUri), str).toString();
    }
    catch (MalformedURLException e) {}
    return str;
  }
  
  public final ParsedPattern Input(Scope scope)
    throws ParseException
  {
    Preamble();
    ParsedPattern p;
    ParsedPattern p;
    if (jj_2_1(Integer.MAX_VALUE)) {
      p = TopLevelGrammar(scope);
    } else {
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 1: 
      case 10: 
      case 17: 
      case 18: 
      case 19: 
      case 26: 
      case 27: 
      case 28: 
      case 31: 
      case 32: 
      case 33: 
      case 34: 
      case 35: 
      case 36: 
      case 40: 
      case 43: 
      case 54: 
      case 55: 
      case 57: 
      case 58: 
        p = Expr(true, scope, null, null);
        p = afterComments(p);
        jj_consume_token(0);
        break;
      case 2: 
      case 3: 
      case 4: 
      case 5: 
      case 6: 
      case 7: 
      case 8: 
      case 9: 
      case 11: 
      case 12: 
      case 13: 
      case 14: 
      case 15: 
      case 16: 
      case 20: 
      case 21: 
      case 22: 
      case 23: 
      case 24: 
      case 25: 
      case 29: 
      case 30: 
      case 37: 
      case 38: 
      case 39: 
      case 41: 
      case 42: 
      case 44: 
      case 45: 
      case 46: 
      case 47: 
      case 48: 
      case 49: 
      case 50: 
      case 51: 
      case 52: 
      case 53: 
      case 56: 
      default: 
        this.jj_la1[0] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    return p;
  }
  
  public final void TopLevelLookahead()
    throws ParseException
  {
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 57: 
      jj_consume_token(57);
      jj_consume_token(1);
      break;
    case 54: 
    case 55: 
      Identifier();
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 1: 
        jj_consume_token(1);
        break;
      case 2: 
        jj_consume_token(2);
        break;
      case 3: 
        jj_consume_token(3);
        break;
      case 4: 
        jj_consume_token(4);
        break;
      default: 
        this.jj_la1[1] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    case 5: 
    case 6: 
    case 7: 
      LookaheadGrammarKeyword();
      break;
    case 1: 
      LookaheadBody();
      LookaheadAfterAnnotations();
      break;
    case 40: 
    case 43: 
      LookaheadDocumentation();
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 1: 
        LookaheadBody();
        break;
      default: 
        this.jj_la1[2] = this.jj_gen;
      }
      LookaheadAfterAnnotations();
      break;
    default: 
      this.jj_la1[3] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }
  
  public final void LookaheadAfterAnnotations()
    throws ParseException
  {
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 54: 
    case 55: 
      Identifier();
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 2: 
        jj_consume_token(2);
        break;
      case 3: 
        jj_consume_token(3);
        break;
      case 4: 
        jj_consume_token(4);
        break;
      default: 
        this.jj_la1[4] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    case 5: 
    case 6: 
    case 7: 
      LookaheadGrammarKeyword();
      break;
    default: 
      this.jj_la1[5] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }
  
  public final void LookaheadGrammarKeyword()
    throws ParseException
  {
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 5: 
      jj_consume_token(5);
      break;
    case 6: 
      jj_consume_token(6);
      break;
    case 7: 
      jj_consume_token(7);
      break;
    default: 
      this.jj_la1[6] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }
  
  public final void LookaheadDocumentation()
    throws ParseException
  {
    for (;;)
    {
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 40: 
        jj_consume_token(40);
        break;
      case 43: 
        jj_consume_token(43);
        break;
      default: 
        this.jj_la1[7] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      for (;;)
      {
        switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
        {
        case 41: 
          break;
        default: 
          this.jj_la1[8] = this.jj_gen;
          break;
        }
        jj_consume_token(41);
      }
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      }
    }
    this.jj_la1[9] = this.jj_gen;
  }
  
  public final void LookaheadBody()
    throws ParseException
  {
    jj_consume_token(1);
    for (;;)
    {
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 1: 
      case 2: 
      case 5: 
      case 6: 
      case 7: 
      case 8: 
      case 10: 
      case 13: 
      case 14: 
      case 15: 
      case 16: 
      case 17: 
      case 18: 
      case 19: 
      case 26: 
      case 27: 
      case 31: 
      case 32: 
      case 33: 
      case 34: 
      case 35: 
      case 36: 
      case 54: 
      case 55: 
      case 57: 
      case 58: 
        break;
      case 3: 
      case 4: 
      case 9: 
      case 11: 
      case 12: 
      case 20: 
      case 21: 
      case 22: 
      case 23: 
      case 24: 
      case 25: 
      case 28: 
      case 29: 
      case 30: 
      case 37: 
      case 38: 
      case 39: 
      case 40: 
      case 41: 
      case 42: 
      case 43: 
      case 44: 
      case 45: 
      case 46: 
      case 47: 
      case 48: 
      case 49: 
      case 50: 
      case 51: 
      case 52: 
      case 53: 
      case 56: 
      default: 
        this.jj_la1[10] = this.jj_gen;
        break;
      }
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 57: 
        jj_consume_token(57);
        break;
      case 5: 
      case 6: 
      case 7: 
      case 10: 
      case 13: 
      case 14: 
      case 15: 
      case 16: 
      case 17: 
      case 18: 
      case 19: 
      case 26: 
      case 27: 
      case 31: 
      case 32: 
      case 33: 
      case 34: 
      case 35: 
      case 36: 
      case 54: 
      case 55: 
        UnprefixedName();
        break;
      case 2: 
        jj_consume_token(2);
        break;
      case 58: 
        jj_consume_token(58);
        break;
      case 8: 
        jj_consume_token(8);
        break;
      case 1: 
        LookaheadBody();
      }
    }
    this.jj_la1[11] = this.jj_gen;
    jj_consume_token(-1);
    throw new ParseException();
    
    jj_consume_token(9);
  }
  
  public final ParsedPattern IncludedGrammar(IncludedGrammar g)
    throws ParseException
  {
    Preamble();
    Annotations a;
    Annotations a;
    if (jj_2_2(Integer.MAX_VALUE)) {
      a = GrammarBody(g, g, getTopLevelCommentsAsAnnotations());
    } else {
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 1: 
      case 10: 
      case 40: 
      case 43: 
        a = Annotations();
        jj_consume_token(10);
        jj_consume_token(11);
        a = GrammarBody(g, g, a);
        topLevelComments(g);
        jj_consume_token(12);
        break;
      default: 
        this.jj_la1[12] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    ParsedPattern p = afterComments(g.endIncludedGrammar(this.sb.makeLocation(this.sourceUri, 1, 1), a));
    jj_consume_token(0);
    return p;
  }
  
  public final ParsedPattern TopLevelGrammar(Scope scope)
    throws ParseException
  {
    Annotations a = getTopLevelCommentsAsAnnotations();
    
    Grammar g = this.sb.makeGrammar(scope);
    a = GrammarBody(g, g, a);
    ParsedPattern p = afterComments(g.endGrammar(this.sb.makeLocation(this.sourceUri, 1, 1), a));
    jj_consume_token(0);
    return p;
  }
  
  public final void Preamble()
    throws ParseException
  {
    for (;;)
    {
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 13: 
      case 14: 
      case 16: 
        break;
      case 15: 
      default: 
        this.jj_la1[13] = this.jj_gen;
        break;
      }
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 13: 
      case 14: 
        NamespaceDecl();
        break;
      case 16: 
        DatatypesDecl();
      }
    }
    this.jj_la1[14] = this.jj_gen;
    jj_consume_token(-1);
    throw new ParseException();
    
    this.namespaceTable.put("xml", "http://www.w3.org/XML/1998/namespace");
    if (this.datatypesTable.get("xsd") == null) {
      this.datatypesTable.put("xsd", "http://www.w3.org/2001/XMLSchema-datatypes");
    }
  }
  
  public final void NamespaceDecl()
    throws ParseException
  {
    LocatedString prefix = null;
    boolean isDefault = false;
    
    noteTopLevelComments();
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 13: 
      jj_consume_token(13);
      prefix = UnprefixedName();
      break;
    case 14: 
      jj_consume_token(14);
      isDefault = true;
      jj_consume_token(13);
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 5: 
      case 6: 
      case 7: 
      case 10: 
      case 13: 
      case 14: 
      case 15: 
      case 16: 
      case 17: 
      case 18: 
      case 19: 
      case 26: 
      case 27: 
      case 31: 
      case 32: 
      case 33: 
      case 34: 
      case 35: 
      case 36: 
      case 54: 
      case 55: 
        prefix = UnprefixedName();
        break;
      case 8: 
      case 9: 
      case 11: 
      case 12: 
      case 20: 
      case 21: 
      case 22: 
      case 23: 
      case 24: 
      case 25: 
      case 28: 
      case 29: 
      case 30: 
      case 37: 
      case 38: 
      case 39: 
      case 40: 
      case 41: 
      case 42: 
      case 43: 
      case 44: 
      case 45: 
      case 46: 
      case 47: 
      case 48: 
      case 49: 
      case 50: 
      case 51: 
      case 52: 
      case 53: 
      default: 
        this.jj_la1[15] = this.jj_gen;
      }
      break;
    default: 
      this.jj_la1[16] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(2);
    String namespaceName = NamespaceName();
    if (isDefault) {
      this.defaultNamespace = namespaceName;
    }
    if (prefix != null) {
      if (prefix.getString().equals("xmlns"))
      {
        error("xmlns_prefix", prefix.getToken());
      }
      else if (prefix.getString().equals("xml"))
      {
        if (!namespaceName.equals("http://www.w3.org/XML/1998/namespace")) {
          error("xml_prefix_bad_uri", prefix.getToken());
        }
      }
      else if (namespaceName.equals("http://www.w3.org/XML/1998/namespace"))
      {
        error("xml_uri_bad_prefix", prefix.getToken());
      }
      else
      {
        if (namespaceName.equals("http://relaxng.org/ns/compatibility/annotations/1.0")) {
          this.compatibilityPrefix = prefix.getString();
        }
        this.namespaceTable.put(prefix.getString(), namespaceName);
      }
    }
  }
  
  public final String NamespaceName()
    throws ParseException
  {
    String r;
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 58: 
      r = Literal();
      break;
    case 15: 
      jj_consume_token(15);
      r = this.inheritedNs;
      break;
    default: 
      this.jj_la1[17] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    return r;
  }
  
  public final void DatatypesDecl()
    throws ParseException
  {
    noteTopLevelComments();
    jj_consume_token(16);
    LocatedString prefix = UnprefixedName();
    jj_consume_token(2);
    String uri = Literal();
    this.datatypesTable.put(prefix.getString(), uri);
  }
  
  public final ParsedPattern AnnotatedPrimaryExpr(boolean topLevel, Scope scope, Token[] except)
    throws ParseException
  {
    Annotations a = Annotations();
    ParsedPattern p = PrimaryExpr(topLevel, scope, a, except);
    for (;;)
    {
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 59: 
        break;
      default: 
        this.jj_la1[18] = this.jj_gen;
        break;
      }
      Token t = jj_consume_token(59);
      ParsedElementAnnotation e = AnnotationElement(false);
      if (topLevel) {
        error("top_level_follow_annotation", t);
      } else {
        p = this.sb.annotateAfter(p, e);
      }
    }
    return p;
  }
  
  public final ParsedPattern PrimaryExpr(boolean topLevel, Scope scope, Annotations a, Token[] except)
    throws ParseException
  {
    ParsedPattern p;
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 26: 
      p = ElementExpr(scope, a);
      break;
    case 27: 
      p = AttributeExpr(scope, a);
      break;
    case 10: 
      p = GrammarExpr(scope, a);
      break;
    case 33: 
      p = ExternalRefExpr(scope, a);
      break;
    case 31: 
      p = ListExpr(scope, a);
      break;
    case 32: 
      p = MixedExpr(scope, a);
      break;
    case 28: 
      p = ParenExpr(topLevel, scope, a);
      break;
    case 54: 
    case 55: 
      p = IdentifierExpr(scope, a);
      break;
    case 34: 
      p = ParentExpr(scope, a);
      break;
    case 35: 
    case 36: 
    case 57: 
      p = DataExpr(topLevel, scope, a, except);
      break;
    case 58: 
      p = ValueExpr(topLevel, a);
      break;
    case 18: 
      p = TextExpr(a);
      break;
    case 17: 
      p = EmptyExpr(a);
      break;
    case 19: 
      p = NotAllowedExpr(a);
      break;
    case 11: 
    case 12: 
    case 13: 
    case 14: 
    case 15: 
    case 16: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 24: 
    case 25: 
    case 29: 
    case 30: 
    case 37: 
    case 38: 
    case 39: 
    case 40: 
    case 41: 
    case 42: 
    case 43: 
    case 44: 
    case 45: 
    case 46: 
    case 47: 
    case 48: 
    case 49: 
    case 50: 
    case 51: 
    case 52: 
    case 53: 
    case 56: 
    default: 
      this.jj_la1[19] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    return p;
  }
  
  public final ParsedPattern EmptyExpr(Annotations a)
    throws ParseException
  {
    Token t = jj_consume_token(17);
    return this.sb.makeEmpty(makeLocation(t), a);
  }
  
  public final ParsedPattern TextExpr(Annotations a)
    throws ParseException
  {
    Token t = jj_consume_token(18);
    return this.sb.makeText(makeLocation(t), a);
  }
  
  public final ParsedPattern NotAllowedExpr(Annotations a)
    throws ParseException
  {
    Token t = jj_consume_token(19);
    return this.sb.makeNotAllowed(makeLocation(t), a);
  }
  
  public final ParsedPattern Expr(boolean topLevel, Scope scope, Token t, Annotations a)
    throws ParseException
  {
    List patterns = new ArrayList();
    
    boolean[] hadOccur = new boolean[1];
    Token[] except = new Token[1];
    ParsedPattern p = UnaryExpr(topLevel, scope, hadOccur, except);
    patterns.add(p);
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 20: 
    case 21: 
    case 22: 
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 20: 
        checkExcept(except);
        for (;;)
        {
          t = jj_consume_token(20);
          p = UnaryExpr(topLevel, scope, null, except);
          patterns.add(p);checkExcept(except);
          switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
          {
          }
        }
        this.jj_la1[20] = this.jj_gen;
        
        p = this.sb.makeChoice(patterns, makeLocation(t), a);
        break;
      case 21: 
        for (;;)
        {
          t = jj_consume_token(21);
          p = UnaryExpr(topLevel, scope, null, except);
          patterns.add(p);checkExcept(except);
          switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
          {
          }
        }
        this.jj_la1[21] = this.jj_gen;
        
        p = this.sb.makeInterleave(patterns, makeLocation(t), a);
        break;
      case 22: 
        for (;;)
        {
          t = jj_consume_token(22);
          p = UnaryExpr(topLevel, scope, null, except);
          patterns.add(p);checkExcept(except);
          switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
          {
          }
        }
        this.jj_la1[22] = this.jj_gen;
        
        p = this.sb.makeGroup(patterns, makeLocation(t), a);
        break;
      default: 
        this.jj_la1[23] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    default: 
      this.jj_la1[24] = this.jj_gen;
    }
    if ((patterns.size() == 1) && (a != null)) {
      if (hadOccur[0] != 0) {
        p = this.sb.annotate(p, a);
      } else {
        p = this.sb.makeGroup(patterns, makeLocation(t), a);
      }
    }
    return p;
  }
  
  public final ParsedPattern UnaryExpr(boolean topLevel, Scope scope, boolean[] hadOccur, Token[] except)
    throws ParseException
  {
    ParsedPattern p = AnnotatedPrimaryExpr(topLevel, scope, except);
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 23: 
    case 24: 
    case 25: 
      if (hadOccur != null) {
        hadOccur[0] = true;
      }
      p = afterComments(p);
      Token t;
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 23: 
        t = jj_consume_token(23);
        checkExcept(except);p = this.sb.makeOneOrMore(p, makeLocation(t), null);
        break;
      case 24: 
        t = jj_consume_token(24);
        checkExcept(except);p = this.sb.makeOptional(p, makeLocation(t), null);
        break;
      case 25: 
        t = jj_consume_token(25);
        checkExcept(except);p = this.sb.makeZeroOrMore(p, makeLocation(t), null);
        break;
      default: 
        this.jj_la1[25] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      for (;;)
      {
        switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
        {
        case 59: 
          break;
        default: 
          this.jj_la1[26] = this.jj_gen;
          break;
        }
        t = jj_consume_token(59);
        ParsedElementAnnotation e = AnnotationElement(false);
        if (topLevel) {
          error("top_level_follow_annotation", t);
        } else {
          p = this.sb.annotateAfter(p, e);
        }
      }
    }
    this.jj_la1[27] = this.jj_gen;
    
    return p;
  }
  
  public final ParsedPattern ElementExpr(Scope scope, Annotations a)
    throws ParseException
  {
    Token t = jj_consume_token(26);
    ParsedNameClass nc = NameClass(0, null);
    jj_consume_token(11);
    ParsedPattern p = Expr(false, scope, null, null);
    p = afterComments(p);
    jj_consume_token(12);
    return this.sb.makeElement(nc, p, makeLocation(t), a);
  }
  
  public final ParsedPattern AttributeExpr(Scope scope, Annotations a)
    throws ParseException
  {
    Token t = jj_consume_token(27);
    ParsedNameClass nc = NameClass(1, null);
    jj_consume_token(11);
    ParsedPattern p = Expr(false, scope, null, null);
    p = afterComments(p);
    jj_consume_token(12);
    return this.sb.makeAttribute(nc, p, makeLocation(t), a);
  }
  
  public final ParsedNameClass NameClass(int context, Annotations[] pa)
    throws ParseException
  {
    Annotations a = Annotations();
    ParsedNameClass nc;
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 5: 
    case 6: 
    case 7: 
    case 10: 
    case 13: 
    case 14: 
    case 15: 
    case 16: 
    case 17: 
    case 18: 
    case 19: 
    case 26: 
    case 27: 
    case 28: 
    case 31: 
    case 32: 
    case 33: 
    case 34: 
    case 35: 
    case 36: 
    case 54: 
    case 55: 
    case 57: 
      nc = PrimaryNameClass(context, a);
      nc = AnnotateAfter(nc);
      nc = NameClassAlternatives(context, nc, pa);
      break;
    case 25: 
      nc = AnyNameExceptClass(context, a, pa);
      break;
    case 56: 
      nc = NsNameExceptClass(context, a, pa);
      break;
    case 8: 
    case 9: 
    case 11: 
    case 12: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 24: 
    case 29: 
    case 30: 
    case 37: 
    case 38: 
    case 39: 
    case 40: 
    case 41: 
    case 42: 
    case 43: 
    case 44: 
    case 45: 
    case 46: 
    case 47: 
    case 48: 
    case 49: 
    case 50: 
    case 51: 
    case 52: 
    case 53: 
    default: 
      this.jj_la1[28] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    return nc;
  }
  
  public final ParsedNameClass AnnotateAfter(ParsedNameClass nc)
    throws ParseException
  {
    for (;;)
    {
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 59: 
        break;
      default: 
        this.jj_la1[29] = this.jj_gen;
        break;
      }
      jj_consume_token(59);
      ParsedElementAnnotation e = AnnotationElement(false);
      nc = this.ncb.annotateAfter(nc, e);
    }
    return nc;
  }
  
  public final ParsedNameClass NameClassAlternatives(int context, ParsedNameClass nc, Annotations[] pa)
    throws ParseException
  {
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 20: 
      ParsedNameClass[] nameClasses = new ParsedNameClass[2];
      nameClasses[0] = nc;
      int nNameClasses = 1;
      Token t;
      for (;;)
      {
        t = jj_consume_token(20);
        nc = BasicNameClass(context);
        nc = AnnotateAfter(nc);
        if (nNameClasses >= nameClasses.length)
        {
          ParsedNameClass[] oldNameClasses = nameClasses;
          nameClasses = new ParsedNameClass[oldNameClasses.length * 2];
          System.arraycopy(oldNameClasses, 0, nameClasses, 0, oldNameClasses.length);
        }
        nameClasses[(nNameClasses++)] = nc;
        switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
        {
        }
      }
      this.jj_la1[30] = this.jj_gen;
      Annotations a;
      Annotations a;
      if (pa == null)
      {
        a = null;
      }
      else
      {
        a = pa[0];
        pa[0] = null;
      }
      nc = this.ncb.makeChoice(Arrays.asList(nameClasses).subList(0, nNameClasses), makeLocation(t), a);
      break;
    default: 
      this.jj_la1[31] = this.jj_gen;
    }
    return nc;
  }
  
  public final ParsedNameClass BasicNameClass(int context)
    throws ParseException
  {
    Annotations a = Annotations();
    ParsedNameClass nc;
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 5: 
    case 6: 
    case 7: 
    case 10: 
    case 13: 
    case 14: 
    case 15: 
    case 16: 
    case 17: 
    case 18: 
    case 19: 
    case 26: 
    case 27: 
    case 28: 
    case 31: 
    case 32: 
    case 33: 
    case 34: 
    case 35: 
    case 36: 
    case 54: 
    case 55: 
    case 57: 
      nc = PrimaryNameClass(context, a);
      break;
    case 25: 
    case 56: 
      nc = OpenNameClass(context, a);
      break;
    case 8: 
    case 9: 
    case 11: 
    case 12: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 24: 
    case 29: 
    case 30: 
    case 37: 
    case 38: 
    case 39: 
    case 40: 
    case 41: 
    case 42: 
    case 43: 
    case 44: 
    case 45: 
    case 46: 
    case 47: 
    case 48: 
    case 49: 
    case 50: 
    case 51: 
    case 52: 
    case 53: 
    default: 
      this.jj_la1[32] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    return nc;
  }
  
  public final ParsedNameClass PrimaryNameClass(int context, Annotations a)
    throws ParseException
  {
    ParsedNameClass nc;
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 5: 
    case 6: 
    case 7: 
    case 10: 
    case 13: 
    case 14: 
    case 15: 
    case 16: 
    case 17: 
    case 18: 
    case 19: 
    case 26: 
    case 27: 
    case 31: 
    case 32: 
    case 33: 
    case 34: 
    case 35: 
    case 36: 
    case 54: 
    case 55: 
      nc = UnprefixedNameClass(context, a);
      break;
    case 57: 
      nc = PrefixedNameClass(a);
      break;
    case 28: 
      nc = ParenNameClass(context, a);
      break;
    case 8: 
    case 9: 
    case 11: 
    case 12: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 24: 
    case 25: 
    case 29: 
    case 30: 
    case 37: 
    case 38: 
    case 39: 
    case 40: 
    case 41: 
    case 42: 
    case 43: 
    case 44: 
    case 45: 
    case 46: 
    case 47: 
    case 48: 
    case 49: 
    case 50: 
    case 51: 
    case 52: 
    case 53: 
    case 56: 
    default: 
      this.jj_la1[33] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    return nc;
  }
  
  public final ParsedNameClass OpenNameClass(int context, Annotations a)
    throws ParseException
  {
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 56: 
      LocatedString ns = NsName();
      checkNsName(context, ns);return this.ncb.makeNsName(ns.getString(), ns.getLocation(), a);
    case 25: 
      Token t = jj_consume_token(25);
      checkAnyName(context, t);return this.ncb.makeAnyName(makeLocation(t), a);
    }
    this.jj_la1[34] = this.jj_gen;
    jj_consume_token(-1);
    throw new ParseException();
  }
  
  public final ParsedNameClass UnprefixedNameClass(int context, Annotations a)
    throws ParseException
  {
    LocatedString name = UnprefixedName();
    String ns;
    String ns;
    if ((context & 0x1) == 1) {
      ns = "";
    } else {
      ns = this.defaultNamespace;
    }
    return this.ncb.makeName(ns, name.getString(), null, name.getLocation(), a);
  }
  
  public final ParsedNameClass PrefixedNameClass(Annotations a)
    throws ParseException
  {
    Token t = jj_consume_token(57);
    String qn = t.image;
    int colon = qn.indexOf(':');
    String prefix = qn.substring(0, colon);
    return this.ncb.makeName(lookupPrefix(prefix, t), qn.substring(colon + 1), prefix, makeLocation(t), a);
  }
  
  public final ParsedNameClass NsNameExceptClass(int context, Annotations a, Annotations[] pa)
    throws ParseException
  {
    LocatedString ns = NsName();
    checkNsName(context, ns);
    ParsedNameClass nc;
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 30: 
      nc = ExceptNameClass(context | 0x4);
      nc = this.ncb.makeNsName(ns.getString(), nc, ns.getLocation(), a);
      nc = AnnotateAfter(nc);
      break;
    default: 
      this.jj_la1[35] = this.jj_gen;
      nc = this.ncb.makeNsName(ns.getString(), ns.getLocation(), a);
      nc = AnnotateAfter(nc);
      nc = NameClassAlternatives(context, nc, pa);
    }
    return nc;
  }
  
  public final LocatedString NsName()
    throws ParseException
  {
    Token t = jj_consume_token(56);
    String qn = t.image;
    String prefix = qn.substring(0, qn.length() - 2);
    return new LocatedString(lookupPrefix(prefix, t), t);
  }
  
  public final ParsedNameClass AnyNameExceptClass(int context, Annotations a, Annotations[] pa)
    throws ParseException
  {
    Token t = jj_consume_token(25);
    checkAnyName(context, t);
    ParsedNameClass nc;
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 30: 
      nc = ExceptNameClass(context | 0x2);
      nc = this.ncb.makeAnyName(nc, makeLocation(t), a);
      nc = AnnotateAfter(nc);
      break;
    default: 
      this.jj_la1[36] = this.jj_gen;
      nc = this.ncb.makeAnyName(makeLocation(t), a);
      nc = AnnotateAfter(nc);
      nc = NameClassAlternatives(context, nc, pa);
    }
    return nc;
  }
  
  public final ParsedNameClass ParenNameClass(int context, Annotations a)
    throws ParseException
  {
    Annotations[] pa = { a };
    Token t = jj_consume_token(28);
    ParsedNameClass nc = NameClass(context, pa);
    nc = afterComments(nc);
    jj_consume_token(29);
    if (pa[0] != null) {
      nc = this.ncb.makeChoice(Collections.singletonList(nc), makeLocation(t), pa[0]);
    }
    return nc;
  }
  
  public final ParsedNameClass ExceptNameClass(int context)
    throws ParseException
  {
    jj_consume_token(30);
    ParsedNameClass nc = BasicNameClass(context);
    return nc;
  }
  
  public final ParsedPattern ListExpr(Scope scope, Annotations a)
    throws ParseException
  {
    Token t = jj_consume_token(31);
    jj_consume_token(11);
    ParsedPattern p = Expr(false, scope, null, null);
    p = afterComments(p);
    jj_consume_token(12);
    return this.sb.makeList(p, makeLocation(t), a);
  }
  
  public final ParsedPattern MixedExpr(Scope scope, Annotations a)
    throws ParseException
  {
    Token t = jj_consume_token(32);
    jj_consume_token(11);
    ParsedPattern p = Expr(false, scope, null, null);
    p = afterComments(p);
    jj_consume_token(12);
    return this.sb.makeMixed(p, makeLocation(t), a);
  }
  
  public final ParsedPattern GrammarExpr(Scope scope, Annotations a)
    throws ParseException
  {
    Token t = jj_consume_token(10);
    Grammar g = this.sb.makeGrammar(scope);
    jj_consume_token(11);
    a = GrammarBody(g, g, a);
    topLevelComments(g);
    jj_consume_token(12);
    return g.endGrammar(makeLocation(t), a);
  }
  
  public final ParsedPattern ParenExpr(boolean topLevel, Scope scope, Annotations a)
    throws ParseException
  {
    Token t = jj_consume_token(28);
    ParsedPattern p = Expr(topLevel, scope, t, a);
    p = afterComments(p);
    jj_consume_token(29);
    return p;
  }
  
  public final Annotations GrammarBody(GrammarSection section, Scope scope, Annotations a)
    throws ParseException
  {
    while (jj_2_3(2))
    {
      ParsedElementAnnotation e = AnnotationElementNotKeyword();
      if (a == null) {
        a = this.sb.makeAnnotations(null, getContext());
      }
      a.addElement(e);
    }
    for (;;)
    {
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 1: 
      case 5: 
      case 6: 
      case 7: 
      case 40: 
      case 43: 
      case 54: 
      case 55: 
        break;
      default: 
        this.jj_la1[37] = this.jj_gen;
        break;
      }
      GrammarComponent(section, scope);
    }
    return a;
  }
  
  public final void GrammarComponent(GrammarSection section, Scope scope)
    throws ParseException
  {
    Annotations a = Annotations();
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 5: 
    case 54: 
    case 55: 
      Definition(section, scope, a);
      break;
    case 7: 
      Include(section, scope, a);
      break;
    case 6: 
      Div(section, scope, a);
      break;
    default: 
      this.jj_la1[38] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    while (jj_2_4(2))
    {
      ParsedElementAnnotation e = AnnotationElementNotKeyword();
      section.topLevelAnnotation(e);
    }
  }
  
  public final void Definition(GrammarSection section, Scope scope, Annotations a)
    throws ParseException
  {
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 54: 
    case 55: 
      Define(section, scope, a);
      break;
    case 5: 
      Start(section, scope, a);
      break;
    default: 
      this.jj_la1[39] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }
  
  public final void Start(GrammarSection section, Scope scope, Annotations a)
    throws ParseException
  {
    Token t = jj_consume_token(5);
    GrammarSection.Combine combine = AssignOp();
    ParsedPattern p = Expr(false, scope, null, null);
    section.define("\000#start\000", combine, p, makeLocation(t), a);
  }
  
  public final void Define(GrammarSection section, Scope scope, Annotations a)
    throws ParseException
  {
    LocatedString name = Identifier();
    GrammarSection.Combine combine = AssignOp();
    ParsedPattern p = Expr(false, scope, null, null);
    section.define(name.getString(), combine, p, name.getLocation(), a);
  }
  
  public final GrammarSection.Combine AssignOp()
    throws ParseException
  {
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 2: 
      jj_consume_token(2);
      return null;
    case 4: 
      jj_consume_token(4);
      return GrammarSection.COMBINE_CHOICE;
    case 3: 
      jj_consume_token(3);
      return GrammarSection.COMBINE_INTERLEAVE;
    }
    this.jj_la1[40] = this.jj_gen;
    jj_consume_token(-1);
    throw new ParseException();
  }
  
  public final void Include(GrammarSection section, Scope scope, Annotations a)
    throws ParseException
  {
    Include include = section.makeInclude();
    Token t = jj_consume_token(7);
    String href = Literal();
    String ns = Inherit();
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 11: 
      jj_consume_token(11);
      a = IncludeBody(include, scope, a);
      topLevelComments(include);
      jj_consume_token(12);
      break;
    default: 
      this.jj_la1[41] = this.jj_gen;
    }
    try
    {
      include.endInclude(this.parseable, resolve(href), ns, makeLocation(t), a);
    }
    catch (IllegalSchemaException e) {}
  }
  
  public final Annotations IncludeBody(GrammarSection section, Scope scope, Annotations a)
    throws ParseException
  {
    while (jj_2_5(2))
    {
      ParsedElementAnnotation e = AnnotationElementNotKeyword();
      if (a == null) {
        a = this.sb.makeAnnotations(null, getContext());
      }
      a.addElement(e);
    }
    for (;;)
    {
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 1: 
      case 5: 
      case 6: 
      case 40: 
      case 43: 
      case 54: 
      case 55: 
        break;
      default: 
        this.jj_la1[42] = this.jj_gen;
        break;
      }
      IncludeComponent(section, scope);
    }
    return a;
  }
  
  public final void IncludeComponent(GrammarSection section, Scope scope)
    throws ParseException
  {
    Annotations a = Annotations();
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 5: 
    case 54: 
    case 55: 
      Definition(section, scope, a);
      break;
    case 6: 
      IncludeDiv(section, scope, a);
      break;
    default: 
      this.jj_la1[43] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    while (jj_2_6(2))
    {
      ParsedElementAnnotation e = AnnotationElementNotKeyword();
      section.topLevelAnnotation(e);
    }
  }
  
  public final void Div(GrammarSection section, Scope scope, Annotations a)
    throws ParseException
  {
    Div div = section.makeDiv();
    Token t = jj_consume_token(6);
    jj_consume_token(11);
    a = GrammarBody(div, scope, a);
    topLevelComments(div);
    jj_consume_token(12);
    div.endDiv(makeLocation(t), a);
  }
  
  public final void IncludeDiv(GrammarSection section, Scope scope, Annotations a)
    throws ParseException
  {
    Div div = section.makeDiv();
    Token t = jj_consume_token(6);
    jj_consume_token(11);
    a = IncludeBody(div, scope, a);
    topLevelComments(div);
    jj_consume_token(12);
    div.endDiv(makeLocation(t), a);
  }
  
  public final ParsedPattern ExternalRefExpr(Scope scope, Annotations a)
    throws ParseException
  {
    Token t = jj_consume_token(33);
    String href = Literal();
    String ns = Inherit();
    try
    {
      return this.sb.makeExternalRef(this.parseable, resolve(href), ns, scope, makeLocation(t), a);
    }
    catch (IllegalSchemaException e) {}
    return this.sb.makeErrorPattern();
  }
  
  public final String Inherit()
    throws ParseException
  {
    String ns = null;
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 15: 
      jj_consume_token(15);
      jj_consume_token(2);
      ns = Prefix();
      break;
    default: 
      this.jj_la1[44] = this.jj_gen;
    }
    if (ns == null) {
      ns = this.defaultNamespace;
    }
    return ns;
  }
  
  public final ParsedPattern ParentExpr(Scope scope, Annotations a)
    throws ParseException
  {
    jj_consume_token(34);
    a = addCommentsToChildAnnotations(a);
    LocatedString name = Identifier();
    if (scope == null)
    {
      error("parent_ref_outside_grammar", name.getToken());
      return this.sb.makeErrorPattern();
    }
    return scope.makeParentRef(name.getString(), name.getLocation(), a);
  }
  
  public final ParsedPattern IdentifierExpr(Scope scope, Annotations a)
    throws ParseException
  {
    LocatedString name = Identifier();
    if (scope == null)
    {
      error("ref_outside_grammar", name.getToken());
      return this.sb.makeErrorPattern();
    }
    return scope.makeRef(name.getString(), name.getLocation(), a);
  }
  
  public final ParsedPattern ValueExpr(boolean topLevel, Annotations a)
    throws ParseException
  {
    LocatedString s = LocatedLiteral();
    if ((topLevel) && (this.annotationsIncludeElements))
    {
      error("top_level_follow_annotation", s.getToken());
      a = null;
    }
    return this.sb.makeValue("", "token", s.getString(), getContext(), this.defaultNamespace, s.getLocation(), a);
  }
  
  public final ParsedPattern DataExpr(boolean topLevel, Scope scope, Annotations a, Token[] except)
    throws ParseException
  {
    String datatypeUri = null;
    String s = null;
    ParsedPattern e = null;
    
    Token datatypeToken = DatatypeName();
    String datatype = datatypeToken.image;
    Location loc = makeLocation(datatypeToken);
    int colon = datatype.indexOf(':');
    if (colon < 0)
    {
      datatypeUri = "";
    }
    else
    {
      String prefix = datatype.substring(0, colon);
      datatypeUri = lookupDatatype(prefix, datatypeToken);
      datatype = datatype.substring(colon + 1);
    }
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 58: 
      s = Literal();
      if ((topLevel) && (this.annotationsIncludeElements))
      {
        error("top_level_follow_annotation", datatypeToken);
        a = null;
      }
      return this.sb.makeValue(datatypeUri, datatype, s, getContext(), this.defaultNamespace, loc, a);
    }
    this.jj_la1[48] = this.jj_gen;
    DataPatternBuilder dpb = this.sb.makeDataPatternBuilder(datatypeUri, datatype, loc);
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 11: 
      Params(dpb);
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 30: 
        e = Except(scope, except);
        break;
      default: 
        this.jj_la1[45] = this.jj_gen;
      }
      break;
    default: 
      this.jj_la1[47] = this.jj_gen;
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 30: 
        e = Except(scope, except);
        break;
      default: 
        this.jj_la1[46] = this.jj_gen;
      }
      break;
    }
    return e == null ? dpb.makePattern(loc, a) : dpb.makePattern(e, loc, a);
  }
  
  public final Token DatatypeName()
    throws ParseException
  {
    Token t;
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 35: 
      t = jj_consume_token(35);
      break;
    case 36: 
      t = jj_consume_token(36);
      break;
    case 57: 
      t = jj_consume_token(57);
      break;
    default: 
      this.jj_la1[49] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    return t;
  }
  
  public final LocatedString Identifier()
    throws ParseException
  {
    Token t;
    LocatedString s;
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 54: 
      t = jj_consume_token(54);
      s = new LocatedString(t.image, t);
      break;
    case 55: 
      t = jj_consume_token(55);
      s = new LocatedString(t.image.substring(1), t);
      break;
    default: 
      this.jj_la1[50] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    return s;
  }
  
  public final String Prefix()
    throws ParseException
  {
    Token t;
    String prefix;
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 54: 
      t = jj_consume_token(54);
      prefix = t.image;
      break;
    case 55: 
      t = jj_consume_token(55);
      prefix = t.image.substring(1);
      break;
    case 5: 
    case 6: 
    case 7: 
    case 10: 
    case 13: 
    case 14: 
    case 15: 
    case 16: 
    case 17: 
    case 18: 
    case 19: 
    case 26: 
    case 27: 
    case 31: 
    case 32: 
    case 33: 
    case 34: 
    case 35: 
    case 36: 
      t = Keyword();
      prefix = t.image;
      break;
    case 8: 
    case 9: 
    case 11: 
    case 12: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 24: 
    case 25: 
    case 28: 
    case 29: 
    case 30: 
    case 37: 
    case 38: 
    case 39: 
    case 40: 
    case 41: 
    case 42: 
    case 43: 
    case 44: 
    case 45: 
    case 46: 
    case 47: 
    case 48: 
    case 49: 
    case 50: 
    case 51: 
    case 52: 
    case 53: 
    default: 
      this.jj_la1[51] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    return lookupPrefix(prefix, t);
  }
  
  public final LocatedString UnprefixedName()
    throws ParseException
  {
    LocatedString s;
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 54: 
    case 55: 
      s = Identifier();
      break;
    case 5: 
    case 6: 
    case 7: 
    case 10: 
    case 13: 
    case 14: 
    case 15: 
    case 16: 
    case 17: 
    case 18: 
    case 19: 
    case 26: 
    case 27: 
    case 31: 
    case 32: 
    case 33: 
    case 34: 
    case 35: 
    case 36: 
      Token t = Keyword();
      s = new LocatedString(t.image, t);
      break;
    case 8: 
    case 9: 
    case 11: 
    case 12: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 24: 
    case 25: 
    case 28: 
    case 29: 
    case 30: 
    case 37: 
    case 38: 
    case 39: 
    case 40: 
    case 41: 
    case 42: 
    case 43: 
    case 44: 
    case 45: 
    case 46: 
    case 47: 
    case 48: 
    case 49: 
    case 50: 
    case 51: 
    case 52: 
    case 53: 
    default: 
      this.jj_la1[52] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    return s;
  }
  
  public final void Params(DataPatternBuilder dpb)
    throws ParseException
  {
    jj_consume_token(11);
    for (;;)
    {
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 1: 
      case 5: 
      case 6: 
      case 7: 
      case 10: 
      case 13: 
      case 14: 
      case 15: 
      case 16: 
      case 17: 
      case 18: 
      case 19: 
      case 26: 
      case 27: 
      case 31: 
      case 32: 
      case 33: 
      case 34: 
      case 35: 
      case 36: 
      case 40: 
      case 43: 
      case 54: 
      case 55: 
        break;
      case 2: 
      case 3: 
      case 4: 
      case 8: 
      case 9: 
      case 11: 
      case 12: 
      case 20: 
      case 21: 
      case 22: 
      case 23: 
      case 24: 
      case 25: 
      case 28: 
      case 29: 
      case 30: 
      case 37: 
      case 38: 
      case 39: 
      case 41: 
      case 42: 
      case 44: 
      case 45: 
      case 46: 
      case 47: 
      case 48: 
      case 49: 
      case 50: 
      case 51: 
      case 52: 
      case 53: 
      default: 
        this.jj_la1[53] = this.jj_gen;
        break;
      }
      Param(dpb);
    }
    jj_consume_token(12);
  }
  
  public final void Param(DataPatternBuilder dpb)
    throws ParseException
  {
    Annotations a = Annotations();
    LocatedString name = UnprefixedName();
    jj_consume_token(2);
    a = addCommentsToLeadingAnnotations(a);
    String value = Literal();
    dpb.addParam(name.getString(), value, getContext(), this.defaultNamespace, name.getLocation(), a);
  }
  
  public final ParsedPattern Except(Scope scope, Token[] except)
    throws ParseException
  {
    Token[] innerExcept = new Token[1];
    Token t = jj_consume_token(30);
    Annotations a = Annotations();
    ParsedPattern p = PrimaryExpr(false, scope, a, innerExcept);
    checkExcept(innerExcept);
    except[0] = t;
    return p;
  }
  
  public final ParsedElementAnnotation Documentation()
    throws ParseException
  {
    CommentList comments = getComments();
    Token t;
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 40: 
      t = jj_consume_token(40);
      break;
    case 43: 
      t = jj_consume_token(43);
      break;
    default: 
      this.jj_la1[54] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    ElementAnnotationBuilder eab = this.sb.makeElementAnnotationBuilder("http://relaxng.org/ns/compatibility/annotations/1.0", "documentation", getCompatibilityPrefix(), makeLocation(t), comments, getContext());
    
    eab.addText(mungeComment(t.image), makeLocation(t), null);
    for (;;)
    {
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 41: 
        break;
      default: 
        this.jj_la1[55] = this.jj_gen;
        break;
      }
      t = jj_consume_token(41);
      eab.addText("\n" + mungeComment(t.image), makeLocation(t), null);
    }
    return eab.makeElementAnnotation();
  }
  
  public final Annotations Annotations()
    throws ParseException
  {
    CommentList comments = getComments();
    Annotations a = null;
    ParsedElementAnnotation e;
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 40: 
    case 43: 
      a = this.sb.makeAnnotations(comments, getContext());
      for (;;)
      {
        e = Documentation();
        a.addElement(e);
        switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
        {
        }
      }
      this.jj_la1[56] = this.jj_gen;
      
      comments = getComments();
      if (comments != null) {
        a.addLeadingComment(comments);
      }
      break;
    default: 
      this.jj_la1[57] = this.jj_gen;
    }
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 1: 
      jj_consume_token(1);
      if (a == null) {
        a = this.sb.makeAnnotations(comments, getContext());
      }
      clearAttributeList();this.annotationsIncludeElements = false;
      while (jj_2_7(2)) {
        PrefixedAnnotationAttribute(a, false);
      }
      for (;;)
      {
        switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
        {
        case 5: 
        case 6: 
        case 7: 
        case 10: 
        case 13: 
        case 14: 
        case 15: 
        case 16: 
        case 17: 
        case 18: 
        case 19: 
        case 26: 
        case 27: 
        case 31: 
        case 32: 
        case 33: 
        case 34: 
        case 35: 
        case 36: 
        case 54: 
        case 55: 
        case 57: 
          break;
        case 8: 
        case 9: 
        case 11: 
        case 12: 
        case 20: 
        case 21: 
        case 22: 
        case 23: 
        case 24: 
        case 25: 
        case 28: 
        case 29: 
        case 30: 
        case 37: 
        case 38: 
        case 39: 
        case 40: 
        case 41: 
        case 42: 
        case 43: 
        case 44: 
        case 45: 
        case 46: 
        case 47: 
        case 48: 
        case 49: 
        case 50: 
        case 51: 
        case 52: 
        case 53: 
        case 56: 
        default: 
          this.jj_la1[58] = this.jj_gen;
          break;
        }
        e = AnnotationElement(false);
        a.addElement(e);this.annotationsIncludeElements = true;
      }
      a.addComment(getComments());
      jj_consume_token(9);
      break;
    default: 
      this.jj_la1[59] = this.jj_gen;
    }
    if ((a == null) && (comments != null)) {
      a = this.sb.makeAnnotations(comments, getContext());
    }
    return a;
  }
  
  public final void AnnotationAttribute(Annotations a)
    throws ParseException
  {
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 57: 
      PrefixedAnnotationAttribute(a, true);
      break;
    case 5: 
    case 6: 
    case 7: 
    case 10: 
    case 13: 
    case 14: 
    case 15: 
    case 16: 
    case 17: 
    case 18: 
    case 19: 
    case 26: 
    case 27: 
    case 31: 
    case 32: 
    case 33: 
    case 34: 
    case 35: 
    case 36: 
    case 54: 
    case 55: 
      UnprefixedAnnotationAttribute(a);
      break;
    case 8: 
    case 9: 
    case 11: 
    case 12: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 24: 
    case 25: 
    case 28: 
    case 29: 
    case 30: 
    case 37: 
    case 38: 
    case 39: 
    case 40: 
    case 41: 
    case 42: 
    case 43: 
    case 44: 
    case 45: 
    case 46: 
    case 47: 
    case 48: 
    case 49: 
    case 50: 
    case 51: 
    case 52: 
    case 53: 
    case 56: 
    default: 
      this.jj_la1[60] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }
  
  public final void PrefixedAnnotationAttribute(Annotations a, boolean nested)
    throws ParseException
  {
    Token t = jj_consume_token(57);
    jj_consume_token(2);
    String value = Literal();
    String qn = t.image;
    int colon = qn.indexOf(':');
    String prefix = qn.substring(0, colon);
    String ns = lookupPrefix(prefix, t);
    if (ns == this.inheritedNs)
    {
      error("inherited_annotation_namespace", t);
    }
    else if ((ns.length() == 0) && (!nested))
    {
      error("unqualified_annotation_attribute", t);
    }
    else if ((ns.equals("http://relaxng.org/ns/structure/1.0")) && (!nested))
    {
      error("relax_ng_namespace", t);
    }
    else if (ns.equals("http://www.w3.org/2000/xmlns"))
    {
      error("xmlns_annotation_attribute_uri", t);
    }
    else
    {
      if (ns.length() == 0) {
        prefix = null;
      }
      addAttribute(a, ns, qn.substring(colon + 1), prefix, value, t);
    }
  }
  
  public final void UnprefixedAnnotationAttribute(Annotations a)
    throws ParseException
  {
    LocatedString name = UnprefixedName();
    jj_consume_token(2);
    String value = Literal();
    if (name.getString().equals("xmlns")) {
      error("xmlns_annotation_attribute", name.getToken());
    } else {
      addAttribute(a, "", name.getString(), null, value, name.getToken());
    }
  }
  
  public final ParsedElementAnnotation AnnotationElement(boolean nested)
    throws ParseException
  {
    ParsedElementAnnotation a;
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 57: 
      a = PrefixedAnnotationElement(nested);
      break;
    case 5: 
    case 6: 
    case 7: 
    case 10: 
    case 13: 
    case 14: 
    case 15: 
    case 16: 
    case 17: 
    case 18: 
    case 19: 
    case 26: 
    case 27: 
    case 31: 
    case 32: 
    case 33: 
    case 34: 
    case 35: 
    case 36: 
    case 54: 
    case 55: 
      a = UnprefixedAnnotationElement();
      break;
    case 8: 
    case 9: 
    case 11: 
    case 12: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 24: 
    case 25: 
    case 28: 
    case 29: 
    case 30: 
    case 37: 
    case 38: 
    case 39: 
    case 40: 
    case 41: 
    case 42: 
    case 43: 
    case 44: 
    case 45: 
    case 46: 
    case 47: 
    case 48: 
    case 49: 
    case 50: 
    case 51: 
    case 52: 
    case 53: 
    case 56: 
    default: 
      this.jj_la1[61] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    return a;
  }
  
  public final ParsedElementAnnotation AnnotationElementNotKeyword()
    throws ParseException
  {
    ParsedElementAnnotation a;
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 57: 
      a = PrefixedAnnotationElement(false);
      break;
    case 54: 
    case 55: 
      a = IdentifierAnnotationElement();
      break;
    case 56: 
    default: 
      this.jj_la1[62] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    return a;
  }
  
  public final ParsedElementAnnotation PrefixedAnnotationElement(boolean nested)
    throws ParseException
  {
    CommentList comments = getComments();
    
    Token t = jj_consume_token(57);
    String qn = t.image;
    int colon = qn.indexOf(':');
    String prefix = qn.substring(0, colon);
    String ns = lookupPrefix(prefix, t);
    if (ns == this.inheritedNs)
    {
      error("inherited_annotation_namespace", t);
      ns = "";
    }
    else if ((!nested) && (ns.equals("http://relaxng.org/ns/structure/1.0")))
    {
      error("relax_ng_namespace", t);
      ns = "";
    }
    else if (ns.length() == 0)
    {
      prefix = null;
    }
    ElementAnnotationBuilder eab = this.sb.makeElementAnnotationBuilder(ns, qn.substring(colon + 1), prefix, makeLocation(t), comments, getContext());
    
    AnnotationElementContent(eab);
    return eab.makeElementAnnotation();
  }
  
  public final ParsedElementAnnotation UnprefixedAnnotationElement()
    throws ParseException
  {
    CommentList comments = getComments();
    
    LocatedString name = UnprefixedName();
    ElementAnnotationBuilder eab = this.sb.makeElementAnnotationBuilder("", name.getString(), null, name.getLocation(), comments, getContext());
    
    AnnotationElementContent(eab);
    return eab.makeElementAnnotation();
  }
  
  public final ParsedElementAnnotation IdentifierAnnotationElement()
    throws ParseException
  {
    CommentList comments = getComments();
    
    LocatedString name = Identifier();
    ElementAnnotationBuilder eab = this.sb.makeElementAnnotationBuilder("", name.getString(), null, name.getLocation(), comments, getContext());
    
    AnnotationElementContent(eab);
    return eab.makeElementAnnotation();
  }
  
  public final void AnnotationElementContent(ElementAnnotationBuilder eab)
    throws ParseException
  {
    jj_consume_token(1);
    clearAttributeList();
    while (jj_2_8(2)) {
      AnnotationAttribute(eab);
    }
    for (;;)
    {
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 5: 
      case 6: 
      case 7: 
      case 10: 
      case 13: 
      case 14: 
      case 15: 
      case 16: 
      case 17: 
      case 18: 
      case 19: 
      case 26: 
      case 27: 
      case 31: 
      case 32: 
      case 33: 
      case 34: 
      case 35: 
      case 36: 
      case 54: 
      case 55: 
      case 57: 
      case 58: 
        break;
      case 8: 
      case 9: 
      case 11: 
      case 12: 
      case 20: 
      case 21: 
      case 22: 
      case 23: 
      case 24: 
      case 25: 
      case 28: 
      case 29: 
      case 30: 
      case 37: 
      case 38: 
      case 39: 
      case 40: 
      case 41: 
      case 42: 
      case 43: 
      case 44: 
      case 45: 
      case 46: 
      case 47: 
      case 48: 
      case 49: 
      case 50: 
      case 51: 
      case 52: 
      case 53: 
      case 56: 
      default: 
        this.jj_la1[63] = this.jj_gen;
        break;
      }
      switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
      {
      case 58: 
        AnnotationElementLiteral(eab);
        for (;;)
        {
          switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
          {
          case 8: 
            break;
          default: 
            this.jj_la1[64] = this.jj_gen;
            break;
          }
          jj_consume_token(8);
          AnnotationElementLiteral(eab);
        }
      case 5: 
      case 6: 
      case 7: 
      case 10: 
      case 13: 
      case 14: 
      case 15: 
      case 16: 
      case 17: 
      case 18: 
      case 19: 
      case 26: 
      case 27: 
      case 31: 
      case 32: 
      case 33: 
      case 34: 
      case 35: 
      case 36: 
      case 54: 
      case 55: 
      case 57: 
        ParsedElementAnnotation e = AnnotationElement(true);
        eab.addElement(e);
      }
    }
    this.jj_la1[65] = this.jj_gen;
    jj_consume_token(-1);
    throw new ParseException();
    
    eab.addComment(getComments());
    jj_consume_token(9);
  }
  
  public final void AnnotationElementLiteral(ElementAnnotationBuilder eab)
    throws ParseException
  {
    CommentList comments = getComments();
    Token t = jj_consume_token(58);
    eab.addText(unquote(t.image), makeLocation(t), comments);
  }
  
  public final String Literal()
    throws ParseException
  {
    Token t = jj_consume_token(58);
    String s = unquote(t.image);
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 8: 
      StringBuffer buf = new StringBuffer(s);
      for (;;)
      {
        jj_consume_token(8);
        t = jj_consume_token(58);
        buf.append(unquote(t.image));
        switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
        {
        }
      }
      this.jj_la1[66] = this.jj_gen;
      
      s = buf.toString();
      break;
    default: 
      this.jj_la1[67] = this.jj_gen;
    }
    return s;
  }
  
  public final LocatedString LocatedLiteral()
    throws ParseException
  {
    Token t = jj_consume_token(58);
    String s = unquote(t.image);
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 8: 
      StringBuffer buf = new StringBuffer(s);
      for (;;)
      {
        jj_consume_token(8);
        Token t2 = jj_consume_token(58);
        buf.append(unquote(t2.image));
        switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
        {
        }
      }
      this.jj_la1[68] = this.jj_gen;
      
      s = buf.toString();
      break;
    default: 
      this.jj_la1[69] = this.jj_gen;
    }
    return new LocatedString(s, t);
  }
  
  public final Token Keyword()
    throws ParseException
  {
    Token t;
    switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
    {
    case 26: 
      t = jj_consume_token(26);
      break;
    case 27: 
      t = jj_consume_token(27);
      break;
    case 13: 
      t = jj_consume_token(13);
      break;
    case 31: 
      t = jj_consume_token(31);
      break;
    case 32: 
      t = jj_consume_token(32);
      break;
    case 10: 
      t = jj_consume_token(10);
      break;
    case 17: 
      t = jj_consume_token(17);
      break;
    case 18: 
      t = jj_consume_token(18);
      break;
    case 34: 
      t = jj_consume_token(34);
      break;
    case 33: 
      t = jj_consume_token(33);
      break;
    case 19: 
      t = jj_consume_token(19);
      break;
    case 5: 
      t = jj_consume_token(5);
      break;
    case 7: 
      t = jj_consume_token(7);
      break;
    case 14: 
      t = jj_consume_token(14);
      break;
    case 15: 
      t = jj_consume_token(15);
      break;
    case 35: 
      t = jj_consume_token(35);
      break;
    case 36: 
      t = jj_consume_token(36);
      break;
    case 16: 
      t = jj_consume_token(16);
      break;
    case 6: 
      t = jj_consume_token(6);
      break;
    case 8: 
    case 9: 
    case 11: 
    case 12: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 24: 
    case 25: 
    case 28: 
    case 29: 
    case 30: 
    default: 
      this.jj_la1[70] = this.jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    return t;
  }
  
  private final boolean jj_2_1(int xla)
  {
    this.jj_la = xla;this.jj_lastpos = (this.jj_scanpos = this.token);
    try
    {
      return !jj_3_1();
    }
    catch (LookaheadSuccess ls)
    {
      return true;
    }
    finally
    {
      jj_save(0, xla);
    }
  }
  
  private final boolean jj_2_2(int xla)
  {
    this.jj_la = xla;this.jj_lastpos = (this.jj_scanpos = this.token);
    try
    {
      return !jj_3_2();
    }
    catch (LookaheadSuccess ls)
    {
      return true;
    }
    finally
    {
      jj_save(1, xla);
    }
  }
  
  private final boolean jj_2_3(int xla)
  {
    this.jj_la = xla;this.jj_lastpos = (this.jj_scanpos = this.token);
    try
    {
      return !jj_3_3();
    }
    catch (LookaheadSuccess ls)
    {
      return true;
    }
    finally
    {
      jj_save(2, xla);
    }
  }
  
  private final boolean jj_2_4(int xla)
  {
    this.jj_la = xla;this.jj_lastpos = (this.jj_scanpos = this.token);
    try
    {
      return !jj_3_4();
    }
    catch (LookaheadSuccess ls)
    {
      return true;
    }
    finally
    {
      jj_save(3, xla);
    }
  }
  
  private final boolean jj_2_5(int xla)
  {
    this.jj_la = xla;this.jj_lastpos = (this.jj_scanpos = this.token);
    try
    {
      return !jj_3_5();
    }
    catch (LookaheadSuccess ls)
    {
      return true;
    }
    finally
    {
      jj_save(4, xla);
    }
  }
  
  private final boolean jj_2_6(int xla)
  {
    this.jj_la = xla;this.jj_lastpos = (this.jj_scanpos = this.token);
    try
    {
      return !jj_3_6();
    }
    catch (LookaheadSuccess ls)
    {
      return true;
    }
    finally
    {
      jj_save(5, xla);
    }
  }
  
  private final boolean jj_2_7(int xla)
  {
    this.jj_la = xla;this.jj_lastpos = (this.jj_scanpos = this.token);
    try
    {
      return !jj_3_7();
    }
    catch (LookaheadSuccess ls)
    {
      return true;
    }
    finally
    {
      jj_save(6, xla);
    }
  }
  
  private final boolean jj_2_8(int xla)
  {
    this.jj_la = xla;this.jj_lastpos = (this.jj_scanpos = this.token);
    try
    {
      return !jj_3_8();
    }
    catch (LookaheadSuccess ls)
    {
      return true;
    }
    finally
    {
      jj_save(7, xla);
    }
  }
  
  private final boolean jj_3R_43()
  {
    if (jj_scan_token(1)) {
      return true;
    }
    Token xsp;
    do
    {
      xsp = this.jj_scanpos;
    } while (!jj_3R_52());
    this.jj_scanpos = xsp;
    if (jj_scan_token(9)) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_51()
  {
    if (jj_scan_token(55)) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_50()
  {
    if (jj_scan_token(54)) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_41()
  {
    Token xsp = this.jj_scanpos;
    if (jj_3R_50())
    {
      this.jj_scanpos = xsp;
      if (jj_3R_51()) {
        return true;
      }
    }
    return false;
  }
  
  private final boolean jj_3R_47()
  {
    if (jj_scan_token(57)) {
      return true;
    }
    if (jj_3R_56()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_55()
  {
    Token xsp = this.jj_scanpos;
    if (jj_scan_token(40))
    {
      this.jj_scanpos = xsp;
      if (jj_scan_token(43)) {
        return true;
      }
    }
    do
    {
      xsp = this.jj_scanpos;
    } while (!jj_scan_token(41));
    this.jj_scanpos = xsp;
    
    return false;
  }
  
  private final boolean jj_3R_45()
  {
    if (jj_3R_55()) {
      return true;
    }
    Token xsp;
    do
    {
      xsp = this.jj_scanpos;
    } while (!jj_3R_55());
    this.jj_scanpos = xsp;
    
    return false;
  }
  
  private final boolean jj_3R_38()
  {
    if (jj_3R_48()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_42()
  {
    Token xsp = this.jj_scanpos;
    if (jj_scan_token(5))
    {
      this.jj_scanpos = xsp;
      if (jj_scan_token(6))
      {
        this.jj_scanpos = xsp;
        if (jj_scan_token(7)) {
          return true;
        }
      }
    }
    return false;
  }
  
  private final boolean jj_3R_37()
  {
    if (jj_3R_47()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_54()
  {
    if (jj_3R_42()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_29()
  {
    Token xsp = this.jj_scanpos;
    if (jj_3R_37())
    {
      this.jj_scanpos = xsp;
      if (jj_3R_38()) {
        return true;
      }
    }
    return false;
  }
  
  private final boolean jj_3R_44()
  {
    Token xsp = this.jj_scanpos;
    if (jj_3R_53())
    {
      this.jj_scanpos = xsp;
      if (jj_3R_54()) {
        return true;
      }
    }
    return false;
  }
  
  private final boolean jj_3R_53()
  {
    if (jj_3R_41()) {
      return true;
    }
    Token xsp = this.jj_scanpos;
    if (jj_scan_token(2))
    {
      this.jj_scanpos = xsp;
      if (jj_scan_token(3))
      {
        this.jj_scanpos = xsp;
        if (jj_scan_token(4)) {
          return true;
        }
      }
    }
    return false;
  }
  
  private final boolean jj_3R_36()
  {
    if (jj_3R_45()) {
      return true;
    }
    Token xsp = this.jj_scanpos;
    if (jj_3R_46()) {
      this.jj_scanpos = xsp;
    }
    if (jj_3R_44()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_35()
  {
    if (jj_3R_43()) {
      return true;
    }
    if (jj_3R_44()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_34()
  {
    if (jj_3R_42()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_33()
  {
    if (jj_3R_41()) {
      return true;
    }
    Token xsp = this.jj_scanpos;
    if (jj_scan_token(1))
    {
      this.jj_scanpos = xsp;
      if (jj_scan_token(2))
      {
        this.jj_scanpos = xsp;
        if (jj_scan_token(3))
        {
          this.jj_scanpos = xsp;
          if (jj_scan_token(4)) {
            return true;
          }
        }
      }
    }
    return false;
  }
  
  private final boolean jj_3_1()
  {
    if (jj_3R_28()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_32()
  {
    if (jj_scan_token(57)) {
      return true;
    }
    if (jj_scan_token(1)) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_28()
  {
    Token xsp = this.jj_scanpos;
    if (jj_3R_32())
    {
      this.jj_scanpos = xsp;
      if (jj_3R_33())
      {
        this.jj_scanpos = xsp;
        if (jj_3R_34())
        {
          this.jj_scanpos = xsp;
          if (jj_3R_35())
          {
            this.jj_scanpos = xsp;
            if (jj_3R_36()) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }
  
  private final boolean jj_3R_59()
  {
    if (jj_3R_43()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3_8()
  {
    if (jj_3R_31()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_56()
  {
    if (jj_scan_token(1)) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_49()
  {
    if (jj_3R_57()) {
      return true;
    }
    if (jj_scan_token(2)) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_40()
  {
    if (jj_3R_49()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3_4()
  {
    if (jj_3R_29()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_48()
  {
    if (jj_3R_41()) {
      return true;
    }
    if (jj_3R_56()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3_3()
  {
    if (jj_3R_29()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3_6()
  {
    if (jj_3R_29()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_62()
  {
    Token xsp = this.jj_scanpos;
    if (jj_scan_token(26))
    {
      this.jj_scanpos = xsp;
      if (jj_scan_token(27))
      {
        this.jj_scanpos = xsp;
        if (jj_scan_token(13))
        {
          this.jj_scanpos = xsp;
          if (jj_scan_token(31))
          {
            this.jj_scanpos = xsp;
            if (jj_scan_token(32))
            {
              this.jj_scanpos = xsp;
              if (jj_scan_token(10))
              {
                this.jj_scanpos = xsp;
                if (jj_scan_token(17))
                {
                  this.jj_scanpos = xsp;
                  if (jj_scan_token(18))
                  {
                    this.jj_scanpos = xsp;
                    if (jj_scan_token(34))
                    {
                      this.jj_scanpos = xsp;
                      if (jj_scan_token(33))
                      {
                        this.jj_scanpos = xsp;
                        if (jj_scan_token(19))
                        {
                          this.jj_scanpos = xsp;
                          if (jj_scan_token(5))
                          {
                            this.jj_scanpos = xsp;
                            if (jj_scan_token(7))
                            {
                              this.jj_scanpos = xsp;
                              if (jj_scan_token(14))
                              {
                                this.jj_scanpos = xsp;
                                if (jj_scan_token(15))
                                {
                                  this.jj_scanpos = xsp;
                                  if (jj_scan_token(35))
                                  {
                                    this.jj_scanpos = xsp;
                                    if (jj_scan_token(36))
                                    {
                                      this.jj_scanpos = xsp;
                                      if (jj_scan_token(16))
                                      {
                                        this.jj_scanpos = xsp;
                                        if (jj_scan_token(6)) {
                                          return true;
                                        }
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return false;
  }
  
  private final boolean jj_3R_61()
  {
    if (jj_3R_62()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3_2()
  {
    if (jj_3R_28()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_30()
  {
    if (jj_scan_token(57)) {
      return true;
    }
    if (jj_scan_token(2)) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_60()
  {
    if (jj_3R_41()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_58()
  {
    if (jj_3R_57()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_57()
  {
    Token xsp = this.jj_scanpos;
    if (jj_3R_60())
    {
      this.jj_scanpos = xsp;
      if (jj_3R_61()) {
        return true;
      }
    }
    return false;
  }
  
  private final boolean jj_3_5()
  {
    if (jj_3R_29()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_31()
  {
    Token xsp = this.jj_scanpos;
    if (jj_3R_39())
    {
      this.jj_scanpos = xsp;
      if (jj_3R_40()) {
        return true;
      }
    }
    return false;
  }
  
  private final boolean jj_3R_39()
  {
    if (jj_3R_30()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3_7()
  {
    if (jj_3R_30()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_46()
  {
    if (jj_3R_43()) {
      return true;
    }
    return false;
  }
  
  private final boolean jj_3R_52()
  {
    Token xsp = this.jj_scanpos;
    if (jj_scan_token(57))
    {
      this.jj_scanpos = xsp;
      if (jj_3R_58())
      {
        this.jj_scanpos = xsp;
        if (jj_scan_token(2))
        {
          this.jj_scanpos = xsp;
          if (jj_scan_token(58))
          {
            this.jj_scanpos = xsp;
            if (jj_scan_token(8))
            {
              this.jj_scanpos = xsp;
              if (jj_3R_59()) {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }
  
  public boolean lookingAhead = false;
  private boolean jj_semLA;
  private int jj_gen;
  private final int[] jj_la1 = new int[71];
  private static int[] jj_la1_0;
  private static int[] jj_la1_1;
  
  static
  {
    jj_la1_0();
    jj_la1_1();
  }
  
  private static void jj_la1_0()
  {
    jj_la1_0 = new int[] { -1676803070, 30, 2, 226, 28, 224, 224, 0, 0, 0, -1945115162, -1945115162, 1026, 90112, 90112, -1945115424, 24576, 32768, 0, -1676803072, 1048576, 2097152, 4194304, 7340032, 7340032, 58720256, 0, 58720256, -1643125536, 0, 1048576, 1048576, -1643125536, -1676679968, 33554432, 1073741824, 1073741824, 226, 224, 32, 28, 2048, 98, 96, 32768, 1073741824, 1073741824, 2048, 0, 0, 0, -1945115424, -1945115424, -1945115422, 0, 0, 0, 0, -1945115424, 2, -1945115424, -1945115424, 0, -1945115424, 256, -1945115424, 256, 256, 256, 256, -1945115424 };
  }
  
  private static void jj_la1_1()
  {
    jj_la1_1 = new int[] { 113248543, 0, 0, 46139648, 0, 12582912, 0, 2304, 512, 2304, 113246239, 113246239, 2304, 0, 0, 12582943, 0, 67108864, 134217728, 113246239, 0, 0, 0, 0, 0, 0, 134217728, 0, 62914591, 134217728, 0, 0, 62914591, 46137375, 16777216, 0, 0, 12585216, 12582912, 12582912, 0, 0, 12585216, 12582912, 0, 0, 0, 0, 67108864, 33554456, 12582912, 12582943, 12582943, 12585247, 2304, 512, 2304, 2304, 46137375, 0, 46137375, 46137375, 46137344, 113246239, 0, 113246239, 0, 0, 0, 0, 31 };
  }
  
  private final JJCalls[] jj_2_rtns = new JJCalls[8];
  private boolean jj_rescan = false;
  private int jj_gc = 0;
  
  public CompactSyntax(InputStream stream)
  {
    this.jj_input_stream = new JavaCharStream(stream, 1, 1);
    this.token_source = new CompactSyntaxTokenManager(this.jj_input_stream);
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 71; i++) {
      this.jj_la1[i] = -1;
    }
    for (int i = 0; i < this.jj_2_rtns.length; i++) {
      this.jj_2_rtns[i] = new JJCalls();
    }
  }
  
  public void ReInit(InputStream stream)
  {
    this.jj_input_stream.ReInit(stream, 1, 1);
    this.token_source.ReInit(this.jj_input_stream);
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 71; i++) {
      this.jj_la1[i] = -1;
    }
    for (int i = 0; i < this.jj_2_rtns.length; i++) {
      this.jj_2_rtns[i] = new JJCalls();
    }
  }
  
  public CompactSyntax(Reader stream)
  {
    this.jj_input_stream = new JavaCharStream(stream, 1, 1);
    this.token_source = new CompactSyntaxTokenManager(this.jj_input_stream);
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 71; i++) {
      this.jj_la1[i] = -1;
    }
    for (int i = 0; i < this.jj_2_rtns.length; i++) {
      this.jj_2_rtns[i] = new JJCalls();
    }
  }
  
  public void ReInit(Reader stream)
  {
    this.jj_input_stream.ReInit(stream, 1, 1);
    this.token_source.ReInit(this.jj_input_stream);
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 71; i++) {
      this.jj_la1[i] = -1;
    }
    for (int i = 0; i < this.jj_2_rtns.length; i++) {
      this.jj_2_rtns[i] = new JJCalls();
    }
  }
  
  public CompactSyntax(CompactSyntaxTokenManager tm)
  {
    this.token_source = tm;
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 71; i++) {
      this.jj_la1[i] = -1;
    }
    for (int i = 0; i < this.jj_2_rtns.length; i++) {
      this.jj_2_rtns[i] = new JJCalls();
    }
  }
  
  public void ReInit(CompactSyntaxTokenManager tm)
  {
    this.token_source = tm;
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 71; i++) {
      this.jj_la1[i] = -1;
    }
    for (int i = 0; i < this.jj_2_rtns.length; i++) {
      this.jj_2_rtns[i] = new JJCalls();
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
      if (++this.jj_gc > 100)
      {
        this.jj_gc = 0;
        for (int i = 0; i < this.jj_2_rtns.length; i++)
        {
          JJCalls c = this.jj_2_rtns[i];
          while (c != null)
          {
            if (c.gen < this.jj_gen) {
              c.first = null;
            }
            c = c.next;
          }
        }
      }
      return this.token;
    }
    this.token = oldToken;
    this.jj_kind = kind;
    throw generateParseException();
  }
  
  private final LookaheadSuccess jj_ls = new LookaheadSuccess(null);
  
  private final boolean jj_scan_token(int kind)
  {
    if (this.jj_scanpos == this.jj_lastpos)
    {
      this.jj_la -= 1;
      if (this.jj_scanpos.next == null) {
        this.jj_lastpos = (this.jj_scanpos = this.jj_scanpos.next = this.token_source.getNextToken());
      } else {
        this.jj_lastpos = (this.jj_scanpos = this.jj_scanpos.next);
      }
    }
    else
    {
      this.jj_scanpos = this.jj_scanpos.next;
    }
    if (this.jj_rescan)
    {
      int i = 0;
      for (Token tok = this.token; (tok != null) && (tok != this.jj_scanpos); tok = tok.next) {
        i++;
      }
      if (tok != null) {
        jj_add_error_token(kind, i);
      }
    }
    if (this.jj_scanpos.kind != kind) {
      return true;
    }
    if ((this.jj_la == 0) && (this.jj_scanpos == this.jj_lastpos)) {
      throw this.jj_ls;
    }
    return false;
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
    Token t = this.lookingAhead ? this.jj_scanpos : this.token;
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
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;
  
  private void jj_add_error_token(int kind, int pos)
  {
    if (pos >= 100) {
      return;
    }
    if (pos == this.jj_endpos + 1)
    {
      this.jj_lasttokens[(this.jj_endpos++)] = kind;
    }
    else if (this.jj_endpos != 0)
    {
      this.jj_expentry = new int[this.jj_endpos];
      for (int i = 0; i < this.jj_endpos; i++) {
        this.jj_expentry[i] = this.jj_lasttokens[i];
      }
      boolean exists = false;
      for (Enumeration e = this.jj_expentries.elements(); e.hasMoreElements();)
      {
        int[] oldentry = (int[])e.nextElement();
        if (oldentry.length == this.jj_expentry.length)
        {
          exists = true;
          for (int i = 0; i < this.jj_expentry.length; i++) {
            if (oldentry[i] != this.jj_expentry[i])
            {
              exists = false;
              break;
            }
          }
          if (exists) {
            break;
          }
        }
      }
      if (!exists) {
        this.jj_expentries.addElement(this.jj_expentry);
      }
      if (pos != 0) {
        this.jj_lasttokens[((this.jj_endpos = pos) - 1)] = kind;
      }
    }
  }
  
  public ParseException generateParseException()
  {
    this.jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[61];
    for (int i = 0; i < 61; i++) {
      la1tokens[i] = false;
    }
    if (this.jj_kind >= 0)
    {
      la1tokens[this.jj_kind] = true;
      this.jj_kind = -1;
    }
    for (int i = 0; i < 71; i++) {
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
    for (int i = 0; i < 61; i++) {
      if (la1tokens[i] != 0)
      {
        this.jj_expentry = new int[1];
        this.jj_expentry[0] = i;
        this.jj_expentries.addElement(this.jj_expentry);
      }
    }
    this.jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[this.jj_expentries.size()][];
    for (int i = 0; i < this.jj_expentries.size(); i++) {
      exptokseq[i] = ((int[])(int[])this.jj_expentries.elementAt(i));
    }
    return new ParseException(this.token, exptokseq, tokenImage);
  }
  
  private final void jj_rescan_token()
  {
    this.jj_rescan = true;
    for (int i = 0; i < 8; i++)
    {
      JJCalls p = this.jj_2_rtns[i];
      do
      {
        if (p.gen > this.jj_gen)
        {
          this.jj_la = p.arg;this.jj_lastpos = (this.jj_scanpos = p.first);
          switch (i)
          {
          case 0: 
            jj_3_1(); break;
          case 1: 
            jj_3_2(); break;
          case 2: 
            jj_3_3(); break;
          case 3: 
            jj_3_4(); break;
          case 4: 
            jj_3_5(); break;
          case 5: 
            jj_3_6(); break;
          case 6: 
            jj_3_7(); break;
          case 7: 
            jj_3_8();
          }
        }
        p = p.next;
      } while (p != null);
    }
    this.jj_rescan = false;
  }
  
  private final void jj_save(int index, int xla)
  {
    JJCalls p = this.jj_2_rtns[index];
    while (p.gen > this.jj_gen)
    {
      if (p.next == null)
      {
        p = p.next = new JJCalls(); break;
      }
      p = p.next;
    }
    p.gen = (this.jj_gen + xla - this.jj_la);p.first = this.token;p.arg = xla;
  }
  
  public final void enable_tracing() {}
  
  public final void disable_tracing() {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\compact\CompactSyntax.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */