package com.sun.dtdparser;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DTDParser
{
  public static final String TYPE_CDATA = "CDATA";
  public static final String TYPE_ID = "ID";
  public static final String TYPE_IDREF = "IDREF";
  public static final String TYPE_IDREFS = "IDREFS";
  public static final String TYPE_ENTITY = "ENTITY";
  public static final String TYPE_ENTITIES = "ENTITIES";
  public static final String TYPE_NMTOKEN = "NMTOKEN";
  public static final String TYPE_NMTOKENS = "NMTOKENS";
  public static final String TYPE_NOTATION = "NOTATION";
  public static final String TYPE_ENUMERATION = "ENUMERATION";
  private InputEntity in;
  private StringBuffer strTmp;
  private char[] nameTmp;
  private NameCache nameCache;
  private char[] charTmp;
  private boolean doLexicalPE;
  protected final Set declaredElements;
  private SimpleHashtable params;
  Hashtable notations;
  SimpleHashtable entities;
  private SimpleHashtable ids;
  private DTDEventListener dtdHandler;
  private EntityResolver resolver;
  private Locale locale;
  static final String strANY = "ANY";
  static final String strEMPTY = "EMPTY";
  private static final String XmlLang = "xml:lang";
  
  public DTDParser()
  {
    this.charTmp = new char[2];
    
    this.declaredElements = new HashSet();
    this.params = new SimpleHashtable(7);
    
    this.notations = new Hashtable(7);
    this.entities = new SimpleHashtable(17);
    
    this.ids = new SimpleHashtable();
  }
  
  public void setLocale(Locale l)
    throws SAXException
  {
    if ((l != null) && (!messages.isLocaleSupported(l.toString()))) {
      throw new SAXException(messages.getMessage(this.locale, "P-078", new Object[] { l }));
    }
    this.locale = l;
  }
  
  public Locale getLocale()
  {
    return this.locale;
  }
  
  public Locale chooseLocale(String[] languages)
    throws SAXException
  {
    Locale l = messages.chooseLocale(languages);
    if (l != null) {
      setLocale(l);
    }
    return l;
  }
  
  public void setEntityResolver(EntityResolver r)
  {
    this.resolver = r;
  }
  
  public EntityResolver getEntityResolver()
  {
    return this.resolver;
  }
  
  public void setDtdHandler(DTDEventListener handler)
  {
    this.dtdHandler = handler;
    if (handler != null) {
      handler.setDocumentLocator(new Locator()
      {
        public String getPublicId()
        {
          return DTDParser.this.getPublicId();
        }
        
        public String getSystemId()
        {
          return DTDParser.this.getSystemId();
        }
        
        public int getLineNumber()
        {
          return DTDParser.this.getLineNumber();
        }
        
        public int getColumnNumber()
        {
          return DTDParser.this.getColumnNumber();
        }
      });
    }
  }
  
  public DTDEventListener getDtdHandler()
  {
    return this.dtdHandler;
  }
  
  public void parse(InputSource in)
    throws IOException, SAXException
  {
    init();
    parseInternal(in);
  }
  
  public void parse(String uri)
    throws IOException, SAXException
  {
    init();
    
    InputSource in = this.resolver.resolveEntity(null, uri);
    if (in == null)
    {
      in = Resolver.createInputSource(new URL(uri), false);
    }
    else if (in.getSystemId() == null)
    {
      warning("P-065", null);
      in.setSystemId(uri);
    }
    parseInternal(in);
  }
  
  private void init()
  {
    this.in = null;
    
    this.strTmp = new StringBuffer();
    this.nameTmp = new char[20];
    this.nameCache = new NameCache();
    
    this.doLexicalPE = false;
    
    this.entities.clear();
    this.notations.clear();
    this.params.clear();
    
    this.declaredElements.clear();
    
    builtin("amp", "&#38;");
    builtin("lt", "&#60;");
    builtin("gt", ">");
    builtin("quot", "\"");
    builtin("apos", "'");
    if (this.locale == null) {
      this.locale = Locale.getDefault();
    }
    if (this.resolver == null) {
      this.resolver = new Resolver();
    }
    if (this.dtdHandler == null) {
      this.dtdHandler = new DTDHandlerBase();
    }
  }
  
  private void builtin(String entityName, String entityValue)
  {
    InternalEntity entity = new InternalEntity(entityName, entityValue.toCharArray());
    this.entities.put(entityName, entity);
  }
  
  private void parseInternal(InputSource input)
    throws IOException, SAXException
  {
    if (input == null) {
      fatal("P-000");
    }
    try
    {
      this.in = InputEntity.getInputEntity(this.dtdHandler, this.locale);
      this.in.init(input, null, null, false);
      
      this.dtdHandler.startDTD(this.in);
      
      ExternalEntity externalSubset = new ExternalEntity(this.in);
      externalParameterEntity(externalSubset);
      if (!this.in.isEOF()) {
        fatal("P-001", new Object[] { Integer.toHexString(getc()) });
      }
      afterRoot();
      this.dtdHandler.endDTD();
    }
    catch (EndOfInputException e)
    {
      if (!this.in.isDocument())
      {
        String name = this.in.getName();
        do
        {
          this.in = this.in.pop();
        } while (this.in.isInternal());
        fatal("P-002", new Object[] { name });
      }
      else
      {
        fatal("P-003", null);
      }
    }
    catch (RuntimeException e)
    {
      System.err.print("Internal DTD parser error: ");
      e.printStackTrace();
      throw new SAXParseException(e.getMessage() != null ? e.getMessage() : e.getClass().getName(), getPublicId(), getSystemId(), getLineNumber(), getColumnNumber());
    }
    finally
    {
      this.strTmp = null;
      this.nameTmp = null;
      this.nameCache = null;
      if (this.in != null)
      {
        this.in.close();
        this.in = null;
      }
      this.params.clear();
      this.entities.clear();
      this.notations.clear();
      this.declaredElements.clear();
      
      this.ids.clear();
    }
  }
  
  void afterRoot()
    throws SAXException
  {
    Enumeration e = this.ids.keys();
    while (e.hasMoreElements())
    {
      String id = (String)e.nextElement();
      Boolean value = (Boolean)this.ids.get(id);
      if (Boolean.FALSE == value) {
        error("V-024", new Object[] { id });
      }
    }
  }
  
  private void whitespace(String roleId)
    throws IOException, SAXException
  {
    if (!maybeWhitespace()) {
      fatal("P-004", new Object[] { messages.getMessage(this.locale, roleId) });
    }
  }
  
  private boolean maybeWhitespace()
    throws IOException, SAXException
  {
    if (!this.doLexicalPE) {
      return this.in.maybeWhitespace();
    }
    char c = getc();
    boolean saw = false;
    while ((c == ' ') || (c == '\t') || (c == '\n') || (c == '\r'))
    {
      saw = true;
      if ((this.in.isEOF()) && (!this.in.isInternal())) {
        return saw;
      }
      c = getc();
    }
    ungetc();
    return saw;
  }
  
  private String maybeGetName()
    throws IOException, SAXException
  {
    NameCacheEntry entry = maybeGetNameCacheEntry();
    return entry == null ? null : entry.name;
  }
  
  private NameCacheEntry maybeGetNameCacheEntry()
    throws IOException, SAXException
  {
    char c = getc();
    if ((!XmlChars.isLetter(c)) && (c != ':') && (c != '_'))
    {
      ungetc();
      return null;
    }
    return nameCharString(c);
  }
  
  private String getNmtoken()
    throws IOException, SAXException
  {
    char c = getc();
    if (!XmlChars.isNameChar(c)) {
      fatal("P-006", new Object[] { new Character(c) });
    }
    return nameCharString(c).name;
  }
  
  private NameCacheEntry nameCharString(char c)
    throws IOException, SAXException
  {
    int i = 1;
    
    this.nameTmp[0] = c;
    while ((c = this.in.getNameChar()) != 0)
    {
      if (i >= this.nameTmp.length)
      {
        char[] tmp = new char[this.nameTmp.length + 10];
        System.arraycopy(this.nameTmp, 0, tmp, 0, this.nameTmp.length);
        this.nameTmp = tmp;
      }
      this.nameTmp[(i++)] = c;
    }
    return this.nameCache.lookupEntry(this.nameTmp, i);
  }
  
  private void parseLiteral(boolean isEntityValue)
    throws IOException, SAXException
  {
    char quote = getc();
    
    InputEntity source = this.in;
    if ((quote != '\'') && (quote != '"')) {
      fatal("P-007");
    }
    this.strTmp = new StringBuffer();
    for (;;)
    {
      if ((this.in != source) && (this.in.isEOF()))
      {
        this.in = this.in.pop();
      }
      else
      {
        char c;
        if (((c = getc()) == quote) && (this.in == source)) {
          break;
        }
        if (c == '&')
        {
          String entityName = maybeGetName();
          if (entityName != null)
          {
            nextChar(';', "F-020", entityName);
            if (isEntityValue)
            {
              this.strTmp.append('&');
              this.strTmp.append(entityName);
              this.strTmp.append(';');
            }
            else
            {
              expandEntityInLiteral(entityName, this.entities, isEntityValue);
            }
          }
          else if ((c = getc()) == '#')
          {
            int tmp = parseCharNumber();
            if (tmp > 65535)
            {
              tmp = surrogatesToCharTmp(tmp);
              this.strTmp.append(this.charTmp[0]);
              if (tmp == 2) {
                this.strTmp.append(this.charTmp[1]);
              }
            }
            else
            {
              this.strTmp.append((char)tmp);
            }
          }
          else
          {
            fatal("P-009");
          }
        }
        else if ((c == '%') && (isEntityValue))
        {
          String entityName = maybeGetName();
          if (entityName != null)
          {
            nextChar(';', "F-021", entityName);
            expandEntityInLiteral(entityName, this.params, isEntityValue);
          }
          else
          {
            fatal("P-011");
          }
        }
        else
        {
          if (!isEntityValue)
          {
            if ((c == ' ') || (c == '\t') || (c == '\n') || (c == '\r'))
            {
              this.strTmp.append(' ');
              continue;
            }
            if (c == '<') {
              fatal("P-012");
            }
          }
          this.strTmp.append(c);
        }
      }
    }
  }
  
  private void expandEntityInLiteral(String name, SimpleHashtable table, boolean isEntityValue)
    throws IOException, SAXException
  {
    Object entity = table.get(name);
    if ((entity instanceof InternalEntity))
    {
      InternalEntity value = (InternalEntity)entity;
      pushReader(value.buf, name, !value.isPE);
    }
    else if ((entity instanceof ExternalEntity))
    {
      if (!isEntityValue) {
        fatal("P-013", new Object[] { name });
      }
      pushReader((ExternalEntity)entity);
    }
    else if (entity == null)
    {
      fatal(table == this.params ? "V-022" : "P-014", new Object[] { name });
    }
  }
  
  private String getQuotedString(String type, String extra)
    throws IOException, SAXException
  {
    char quote = this.in.getc();
    if ((quote != '\'') && (quote != '"')) {
      fatal("P-015", new Object[] { messages.getMessage(this.locale, type, new Object[] { extra }) });
    }
    this.strTmp = new StringBuffer();
    char c;
    while ((c = this.in.getc()) != quote) {
      this.strTmp.append(c);
    }
    return this.strTmp.toString();
  }
  
  private String parsePublicId()
    throws IOException, SAXException
  {
    String retval = getQuotedString("F-033", null);
    for (int i = 0; i < retval.length(); i++)
    {
      char c = retval.charAt(i);
      if ((" \r\n-'()+,./:=?;!*#@$_%0123456789".indexOf(c) == -1) && ((c < 'A') || (c > 'Z')) && ((c < 'a') || (c > 'z'))) {
        fatal("P-016", new Object[] { new Character(c) });
      }
    }
    this.strTmp = new StringBuffer();
    this.strTmp.append(retval);
    return normalize(false);
  }
  
  private boolean maybeComment(boolean skipStart)
    throws IOException, SAXException
  {
    if (!this.in.peek(skipStart ? "!--" : "<!--", null)) {
      return false;
    }
    boolean savedLexicalPE = this.doLexicalPE;
    
    this.doLexicalPE = false;
    boolean saveCommentText = false;
    if (saveCommentText) {
      this.strTmp = new StringBuffer();
    }
    try
    {
      for (;;)
      {
        int c = getc();
        if (c == 45)
        {
          c = getc();
          if (c != 45)
          {
            if (saveCommentText) {
              this.strTmp.append('-');
            }
            ungetc();
          }
          else
          {
            nextChar('>', "F-022", null);
            break;
          }
        }
        else if (saveCommentText)
        {
          this.strTmp.append((char)c);
        }
      }
    }
    catch (EndOfInputException e)
    {
      if (this.in.isInternal()) {
        error("V-021", null);
      }
      fatal("P-017");
    }
    this.doLexicalPE = savedLexicalPE;
    if (saveCommentText) {
      this.dtdHandler.comment(this.strTmp.toString());
    }
    return true;
  }
  
  private boolean maybePI(boolean skipStart)
    throws IOException, SAXException
  {
    boolean savedLexicalPE = this.doLexicalPE;
    if (!this.in.peek(skipStart ? "?" : "<?", null)) {
      return false;
    }
    this.doLexicalPE = false;
    
    String target = maybeGetName();
    if (target == null) {
      fatal("P-018");
    }
    if ("xml".equals(target)) {
      fatal("P-019");
    }
    if ("xml".equalsIgnoreCase(target)) {
      fatal("P-020", new Object[] { target });
    }
    if (maybeWhitespace())
    {
      this.strTmp = new StringBuffer();
      try
      {
        for (;;)
        {
          char c = this.in.getc();
          if ((c == '?') && (this.in.peekc('>'))) {
            break;
          }
          this.strTmp.append(c);
        }
      }
      catch (EndOfInputException e)
      {
        fatal("P-021");
      }
      this.dtdHandler.processingInstruction(target, this.strTmp.toString());
    }
    else
    {
      if (!this.in.peek("?>", null)) {
        fatal("P-022");
      }
      this.dtdHandler.processingInstruction(target, "");
    }
    this.doLexicalPE = savedLexicalPE;
    return true;
  }
  
  private String maybeReadAttribute(String name, boolean must)
    throws IOException, SAXException
  {
    if (!maybeWhitespace())
    {
      if (!must) {
        return null;
      }
      fatal("P-024", new Object[] { name });
    }
    if (!peek(name)) {
      if (must)
      {
        fatal("P-024", new Object[] { name });
      }
      else
      {
        ungetc();
        return null;
      }
    }
    maybeWhitespace();
    nextChar('=', "F-023", null);
    maybeWhitespace();
    
    return getQuotedString("F-035", name);
  }
  
  private void readVersion(boolean must, String versionNum)
    throws IOException, SAXException
  {
    String value = maybeReadAttribute("version", must);
    if ((must) && (value == null)) {
      fatal("P-025", new Object[] { versionNum });
    }
    if (value != null)
    {
      int length = value.length();
      for (int i = 0; i < length; i++)
      {
        char c = value.charAt(i);
        if (((c < '0') || (c > '9')) && (c != '_') && (c != '.') && ((c < 'a') || (c > 'z')) && ((c < 'A') || (c > 'Z')) && (c != ':') && (c != '-')) {
          fatal("P-026", new Object[] { value });
        }
      }
    }
    if ((value != null) && (!value.equals(versionNum))) {
      error("P-027", new Object[] { versionNum, value });
    }
  }
  
  private String getMarkupDeclname(String roleId, boolean qname)
    throws IOException, SAXException
  {
    whitespace(roleId);
    String name = maybeGetName();
    if (name == null) {
      fatal("P-005", new Object[] { messages.getMessage(this.locale, roleId) });
    }
    return name;
  }
  
  private boolean maybeMarkupDecl()
    throws IOException, SAXException
  {
    return (maybeElementDecl()) || (maybeAttlistDecl()) || (maybeEntityDecl()) || (maybeNotationDecl()) || (maybePI(false)) || (maybeComment(false));
  }
  
  private boolean isXmlLang(String value)
  {
    if (value.length() < 2) {
      return false;
    }
    char c = value.charAt(1);
    int nextSuffix;
    if (c == '-')
    {
      c = value.charAt(0);
      if ((c != 'i') && (c != 'I') && (c != 'x') && (c != 'X')) {
        return false;
      }
      nextSuffix = 1;
    }
    else
    {
      int nextSuffix;
      if (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')))
      {
        c = value.charAt(0);
        if (((c < 'a') || (c > 'z')) && ((c < 'A') || (c > 'Z'))) {
          return false;
        }
        nextSuffix = 2;
      }
      else
      {
        return false;
        break label145;
        break label145;
      }
    }
    int nextSuffix;
    for (;;)
    {
      if (nextSuffix >= value.length()) {
        break label189;
      }
      c = value.charAt(nextSuffix);
      if (c != '-') {
        break label189;
      }
      label145:
      nextSuffix++;
      if (nextSuffix < value.length())
      {
        c = value.charAt(nextSuffix);
        if ((c >= 'a') && (c <= 'z')) {
          break;
        }
        if (c >= 'A') {
          if (c <= 'Z') {
            break;
          }
        }
      }
    }
    label189:
    return (value.length() == nextSuffix) && (c != '-');
  }
  
  private boolean maybeElementDecl()
    throws IOException, SAXException
  {
    InputEntity start = peekDeclaration("!ELEMENT");
    if (start == null) {
      return false;
    }
    String name = getMarkupDeclname("F-015", true);
    if (this.declaredElements.contains(name)) {
      error("V-012", new Object[] { name });
    } else {
      this.declaredElements.add(name);
    }
    whitespace("F-000");
    short modelType;
    if (peek("EMPTY"))
    {
      short modelType;
      this.dtdHandler.startContentModel(name, modelType = 0);
    }
    else if (peek("ANY"))
    {
      short modelType;
      this.dtdHandler.startContentModel(name, modelType = 1);
    }
    else
    {
      modelType = getMixedOrChildren(name);
    }
    this.dtdHandler.endContentModel(name, modelType);
    
    maybeWhitespace();
    char c = getc();
    if (c != '>') {
      fatal("P-036", new Object[] { name, new Character(c) });
    }
    if (start != this.in) {
      error("V-013", null);
    }
    return true;
  }
  
  private short getMixedOrChildren(String elementName)
    throws IOException, SAXException
  {
    this.strTmp = new StringBuffer();
    
    nextChar('(', "F-028", elementName);
    InputEntity start = this.in;
    maybeWhitespace();
    this.strTmp.append('(');
    short modelType;
    if (peek("#PCDATA"))
    {
      this.strTmp.append("#PCDATA");
      short modelType;
      this.dtdHandler.startContentModel(elementName, modelType = 2);
      getMixed(elementName, start);
    }
    else
    {
      this.dtdHandler.startContentModel(elementName, modelType = 3);
      getcps(elementName, start);
    }
    return modelType;
  }
  
  private void getcps(String elementName, InputEntity start)
    throws IOException, SAXException
  {
    boolean decided = false;
    char type = '\000';
    
    this.dtdHandler.startModelGroup();
    do
    {
      String tag = maybeGetName();
      if (tag != null)
      {
        this.strTmp.append(tag);
        
        this.dtdHandler.childElement(tag, getFrequency());
      }
      else if (peek("("))
      {
        InputEntity next = this.in;
        this.strTmp.append('(');
        maybeWhitespace();
        
        getcps(elementName, next);
      }
      else
      {
        fatal(type == ',' ? "P-037" : type == 0 ? "P-039" : "P-038", new Object[] { new Character(getc()) });
      }
      maybeWhitespace();
      if (decided)
      {
        char c = getc();
        if (c == type)
        {
          this.strTmp.append(type);
          maybeWhitespace();
          reportConnector(type);
          continue;
        }
        if (c == ')')
        {
          ungetc();
          continue;
        }
        fatal(type == 0 ? "P-041" : "P-040", new Object[] { new Character(c), new Character(type) });
      }
      else
      {
        type = getc();
        switch (type)
        {
        case ',': 
        case '|': 
          reportConnector(type);
          break;
        default: 
          ungetc();
          break;
        }
        decided = true;
        
        this.strTmp.append(type);
      }
      maybeWhitespace();
    } while (!peek(")"));
    if (this.in != start) {
      error("V-014", new Object[] { elementName });
    }
    this.strTmp.append(')');
    
    this.dtdHandler.endModelGroup(getFrequency());
  }
  
  private void reportConnector(char type)
    throws SAXException
  {
    switch (type)
    {
    case '|': 
      this.dtdHandler.connector((short)0);
      return;
    case ',': 
      this.dtdHandler.connector((short)1);
      return;
    }
    throw new Error();
  }
  
  private short getFrequency()
    throws IOException, SAXException
  {
    char c = getc();
    if (c == '?')
    {
      this.strTmp.append(c);
      return 2;
    }
    if (c == '+')
    {
      this.strTmp.append(c);
      return 1;
    }
    if (c == '*')
    {
      this.strTmp.append(c);
      return 0;
    }
    ungetc();
    return 3;
  }
  
  private void getMixed(String elementName, InputEntity start)
    throws IOException, SAXException
  {
    maybeWhitespace();
    if ((peek(")*")) || (peek(")")))
    {
      if (this.in != start) {
        error("V-014", new Object[] { elementName });
      }
      this.strTmp.append(')');
      
      return;
    }
    ArrayList l = new ArrayList();
    while (peek("|"))
    {
      this.strTmp.append('|');
      maybeWhitespace();
      
      this.doLexicalPE = true;
      String name = maybeGetName();
      if (name == null) {
        fatal("P-042", new Object[] { elementName, Integer.toHexString(getc()) });
      }
      if (l.contains(name))
      {
        error("V-015", new Object[] { name });
      }
      else
      {
        l.add(name);
        this.dtdHandler.mixedElement(name);
      }
      this.strTmp.append(name);
      maybeWhitespace();
    }
    if (!peek(")*")) {
      fatal("P-043", new Object[] { elementName, new Character(getc()) });
    }
    if (this.in != start) {
      error("V-014", new Object[] { elementName });
    }
    this.strTmp.append(')');
  }
  
  private boolean maybeAttlistDecl()
    throws IOException, SAXException
  {
    InputEntity start = peekDeclaration("!ATTLIST");
    if (start == null) {
      return false;
    }
    String elementName = getMarkupDeclname("F-016", true);
    while (!peek(">"))
    {
      maybeWhitespace();
      char c = getc();
      if (c == '%')
      {
        String entityName = maybeGetName();
        if (entityName != null)
        {
          nextChar(';', "F-021", entityName);
          whitespace("F-021");
        }
        else
        {
          fatal("P-011");
        }
      }
      else
      {
        ungetc();
        
        String attName = maybeGetName();
        if (attName == null) {
          fatal("P-044", new Object[] { new Character(getc()) });
        }
        whitespace("F-001");
        
        Vector values = null;
        String typeName;
        String typeName;
        if (peek("CDATA"))
        {
          typeName = "CDATA";
        }
        else
        {
          String typeName;
          if (peek("IDREFS"))
          {
            typeName = "IDREFS";
          }
          else
          {
            String typeName;
            if (peek("IDREF"))
            {
              typeName = "IDREF";
            }
            else
            {
              String typeName;
              if (peek("ID"))
              {
                typeName = "ID";
              }
              else
              {
                String typeName;
                if (peek("ENTITY"))
                {
                  typeName = "ENTITY";
                }
                else
                {
                  String typeName;
                  if (peek("ENTITIES"))
                  {
                    typeName = "ENTITIES";
                  }
                  else
                  {
                    String typeName;
                    if (peek("NMTOKENS"))
                    {
                      typeName = "NMTOKENS";
                    }
                    else
                    {
                      String typeName;
                      if (peek("NMTOKEN"))
                      {
                        typeName = "NMTOKEN";
                      }
                      else if (peek("NOTATION"))
                      {
                        String typeName = "NOTATION";
                        whitespace("F-002");
                        nextChar('(', "F-029", null);
                        maybeWhitespace();
                        
                        values = new Vector();
                        do
                        {
                          String name;
                          if ((name = maybeGetName()) == null) {
                            fatal("P-068");
                          }
                          if (this.notations.get(name) == null) {
                            this.notations.put(name, name);
                          }
                          values.addElement(name);
                          maybeWhitespace();
                          if (peek("|")) {
                            maybeWhitespace();
                          }
                        } while (!peek(")"));
                      }
                      else if (peek("("))
                      {
                        String typeName = "ENUMERATION";
                        
                        maybeWhitespace();
                        
                        values = new Vector();
                        do
                        {
                          String name = getNmtoken();
                          
                          values.addElement(name);
                          maybeWhitespace();
                          if (peek("|")) {
                            maybeWhitespace();
                          }
                        } while (!peek(")"));
                      }
                      else
                      {
                        fatal("P-045", new Object[] { attName, new Character(getc()) });
                        
                        typeName = null;
                      }
                    }
                  }
                }
              }
            }
          }
        }
        String defaultValue = null;
        
        whitespace("F-003");
        short attributeUse;
        short attributeUse;
        if (peek("#REQUIRED"))
        {
          attributeUse = 3;
        }
        else if (peek("#FIXED"))
        {
          if (typeName == "ID") {
            error("V-017", new Object[] { attName });
          }
          short attributeUse = 2;
          whitespace("F-004");
          parseLiteral(false);
          if (typeName == "CDATA") {
            defaultValue = normalize(false);
          } else {
            defaultValue = this.strTmp.toString();
          }
        }
        else if (!peek("#IMPLIED"))
        {
          short attributeUse = 1;
          if (typeName == "ID") {
            error("V-018", new Object[] { attName });
          }
          parseLiteral(false);
          if (typeName == "CDATA") {
            defaultValue = normalize(false);
          } else {
            defaultValue = this.strTmp.toString();
          }
        }
        else
        {
          attributeUse = 0;
        }
        if (("xml:lang".equals(attName)) && (defaultValue != null) && (!isXmlLang(defaultValue))) {
          error("P-033", new Object[] { defaultValue });
        }
        String[] v = values != null ? (String[])values.toArray(new String[0]) : null;
        this.dtdHandler.attributeDecl(elementName, attName, typeName, v, attributeUse, defaultValue);
        maybeWhitespace();
      }
    }
    if (start != this.in) {
      error("V-013", null);
    }
    return true;
  }
  
  private String normalize(boolean invalidIfNeeded)
  {
    String s = this.strTmp.toString();
    String s2 = s.trim();
    boolean didStrip = false;
    if (s != s2)
    {
      s = s2;
      s2 = null;
      didStrip = true;
    }
    this.strTmp = new StringBuffer();
    for (int i = 0; i < s.length(); i++)
    {
      char c = s.charAt(i);
      if (!XmlChars.isSpace(c))
      {
        this.strTmp.append(c);
      }
      else
      {
        this.strTmp.append(' ');
        for (;;)
        {
          i++;
          if ((i >= s.length()) || (!XmlChars.isSpace(s.charAt(i)))) {
            break;
          }
          didStrip = true;
        }
        i--;
      }
    }
    if (didStrip) {
      return this.strTmp.toString();
    }
    return s;
  }
  
  private boolean maybeConditionalSect()
    throws IOException, SAXException
  {
    if (!peek("<![")) {
      return false;
    }
    InputEntity start = this.in;
    
    maybeWhitespace();
    String keyword;
    if ((keyword = maybeGetName()) == null) {
      fatal("P-046");
    }
    maybeWhitespace();
    nextChar('[', "F-030", null);
    if ("INCLUDE".equals(keyword)) {
      for (;;)
      {
        if ((this.in.isEOF()) && (this.in != start))
        {
          this.in = this.in.pop();
        }
        else
        {
          if (this.in.isEOF()) {
            error("V-020", null);
          }
          if (peek("]]>")) {
            break;
          }
          this.doLexicalPE = false;
          if ((!maybeWhitespace()) && 
          
            (!maybePEReference()))
          {
            this.doLexicalPE = true;
            if ((!maybeMarkupDecl()) && (!maybeConditionalSect())) {
              fatal("P-047");
            }
          }
        }
      }
    }
    if ("IGNORE".equals(keyword))
    {
      int nestlevel = 1;
      
      this.doLexicalPE = false;
      while (nestlevel > 0)
      {
        char c = getc();
        if (c == '<')
        {
          if (peek("![")) {
            nestlevel++;
          }
        }
        else if (c == ']') {
          if (peek("]>")) {
            nestlevel--;
          }
        }
      }
    }
    else
    {
      fatal("P-048", new Object[] { keyword });
    }
    return true;
  }
  
  private int parseCharNumber()
    throws IOException, SAXException
  {
    int retval = 0;
    if (getc() != 'x')
    {
      ungetc();
      for (;;)
      {
        char c = getc();
        if ((c >= '0') && (c <= '9'))
        {
          retval *= 10;
          retval += c - '0';
        }
        else
        {
          if (c == ';') {
            return retval;
          }
          fatal("P-049");
        }
      }
    }
    for (;;)
    {
      char c = getc();
      if ((c >= '0') && (c <= '9'))
      {
        retval <<= 4;
        retval += c - '0';
      }
      else if ((c >= 'a') && (c <= 'f'))
      {
        retval <<= 4;
        retval += 10 + (c - 'a');
      }
      else if ((c >= 'A') && (c <= 'F'))
      {
        retval <<= 4;
        retval += 10 + (c - 'A');
      }
      else
      {
        if (c == ';') {
          return retval;
        }
        fatal("P-050");
      }
    }
  }
  
  private int surrogatesToCharTmp(int ucs4)
    throws SAXException
  {
    if (ucs4 <= 65535)
    {
      if (XmlChars.isChar(ucs4))
      {
        this.charTmp[0] = ((char)ucs4);
        return 1;
      }
    }
    else if (ucs4 <= 1114111)
    {
      ucs4 -= 65536;
      this.charTmp[0] = ((char)(0xD800 | ucs4 >> 10 & 0x3FF));
      this.charTmp[1] = ((char)(0xDC00 | ucs4 & 0x3FF));
      return 2;
    }
    fatal("P-051", new Object[] { Integer.toHexString(ucs4) });
    
    return -1;
  }
  
  private boolean maybePEReference()
    throws IOException, SAXException
  {
    if (!this.in.peekc('%')) {
      return false;
    }
    String name = maybeGetName();
    if (name == null) {
      fatal("P-011");
    }
    nextChar(';', "F-021", name);
    Object entity = this.params.get(name);
    if ((entity instanceof InternalEntity))
    {
      InternalEntity value = (InternalEntity)entity;
      pushReader(value.buf, name, false);
    }
    else if ((entity instanceof ExternalEntity))
    {
      pushReader((ExternalEntity)entity);
      externalParameterEntity((ExternalEntity)entity);
    }
    else if (entity == null)
    {
      error("V-022", new Object[] { name });
    }
    return true;
  }
  
  private boolean maybeEntityDecl()
    throws IOException, SAXException
  {
    InputEntity start = peekDeclaration("!ENTITY");
    if (start == null) {
      return false;
    }
    this.doLexicalPE = false;
    whitespace("F-005");
    SimpleHashtable defns;
    SimpleHashtable defns;
    if (this.in.peekc('%'))
    {
      whitespace("F-006");
      defns = this.params;
    }
    else
    {
      defns = this.entities;
    }
    ungetc();
    this.doLexicalPE = true;
    String entityName = getMarkupDeclname("F-017", false);
    whitespace("F-007");
    ExternalEntity externalId = maybeExternalID();
    
    boolean doStore = defns.get(entityName) == null;
    if ((!doStore) && (defns == this.entities)) {
      warning("P-054", new Object[] { entityName });
    }
    if (externalId == null)
    {
      this.doLexicalPE = false;
      parseLiteral(true);
      this.doLexicalPE = true;
      if (doStore)
      {
        char[] value = new char[this.strTmp.length()];
        if (value.length != 0) {
          this.strTmp.getChars(0, value.length, value, 0);
        }
        InternalEntity entity = new InternalEntity(entityName, value);
        entity.isPE = (defns == this.params);
        entity.isFromInternalSubset = false;
        defns.put(entityName, entity);
        if (defns == this.entities) {
          this.dtdHandler.internalGeneralEntityDecl(entityName, new String(value));
        }
      }
    }
    else
    {
      if ((defns == this.entities) && (maybeWhitespace()) && (peek("NDATA")))
      {
        externalId.notation = getMarkupDeclname("F-018", false);
        if (this.notations.get(externalId.notation) == null) {
          this.notations.put(externalId.notation, Boolean.TRUE);
        }
      }
      externalId.name = entityName;
      externalId.isPE = (defns == this.params);
      externalId.isFromInternalSubset = false;
      if (doStore)
      {
        defns.put(entityName, externalId);
        if (externalId.notation != null) {
          this.dtdHandler.unparsedEntityDecl(entityName, externalId.publicId, externalId.systemId, externalId.notation);
        } else if (defns == this.entities) {
          this.dtdHandler.externalGeneralEntityDecl(entityName, externalId.publicId, externalId.systemId);
        }
      }
    }
    maybeWhitespace();
    nextChar('>', "F-031", entityName);
    if (start != this.in) {
      error("V-013", null);
    }
    return true;
  }
  
  private ExternalEntity maybeExternalID()
    throws IOException, SAXException
  {
    String temp = null;
    if (peek("PUBLIC"))
    {
      whitespace("F-009");
      temp = parsePublicId();
    }
    else if (!peek("SYSTEM"))
    {
      return null;
    }
    ExternalEntity retval = new ExternalEntity(this.in);
    retval.publicId = temp;
    whitespace("F-008");
    retval.systemId = parseSystemId();
    return retval;
  }
  
  private String parseSystemId()
    throws IOException, SAXException
  {
    String uri = getQuotedString("F-034", null);
    int temp = uri.indexOf(':');
    if ((temp == -1) || (uri.indexOf('/') < temp))
    {
      String baseURI = this.in.getSystemId();
      if (baseURI == null) {
        fatal("P-055", new Object[] { uri });
      }
      if (uri.length() == 0) {
        uri = ".";
      }
      baseURI = baseURI.substring(0, baseURI.lastIndexOf('/') + 1);
      if (uri.charAt(0) != '/') {
        uri = baseURI + uri;
      } else {
        throw new InternalError();
      }
    }
    if (uri.indexOf('#') != -1) {
      error("P-056", new Object[] { uri });
    }
    return uri;
  }
  
  private void maybeTextDecl()
    throws IOException, SAXException
  {
    if (peek("<?xml"))
    {
      readVersion(false, "1.0");
      readEncoding(true);
      maybeWhitespace();
      if (!peek("?>")) {
        fatal("P-057");
      }
    }
  }
  
  private void externalParameterEntity(ExternalEntity next)
    throws IOException, SAXException
  {
    InputEntity pe = this.in;
    maybeTextDecl();
    do
    {
      do
      {
        for (;;)
        {
          if (pe.isEOF()) {
            break label87;
          }
          if (!this.in.isEOF()) {
            break;
          }
          this.in = this.in.pop();
        }
        this.doLexicalPE = false;
      } while ((maybeWhitespace()) || 
      
        (maybePEReference()));
      this.doLexicalPE = true;
    } while ((maybeMarkupDecl()) || (maybeConditionalSect()));
    label87:
    if (!pe.isEOF()) {
      fatal("P-059", new Object[] { this.in.getName() });
    }
  }
  
  private void readEncoding(boolean must)
    throws IOException, SAXException
  {
    String name = maybeReadAttribute("encoding", must);
    if (name == null) {
      return;
    }
    for (int i = 0; i < name.length(); i++)
    {
      char c = name.charAt(i);
      if (((c < 'A') || (c > 'Z')) && ((c < 'a') || (c > 'z'))) {
        if ((i == 0) || (((c < '0') || (c > '9')) && (c != '-') && (c != '_') && (c != '.'))) {
          fatal("P-060", new Object[] { new Character(c) });
        }
      }
    }
    String currentEncoding = this.in.getEncoding();
    if ((currentEncoding != null) && (!name.equalsIgnoreCase(currentEncoding))) {
      warning("P-061", new Object[] { name, currentEncoding });
    }
  }
  
  private boolean maybeNotationDecl()
    throws IOException, SAXException
  {
    InputEntity start = peekDeclaration("!NOTATION");
    if (start == null) {
      return false;
    }
    String name = getMarkupDeclname("F-019", false);
    ExternalEntity entity = new ExternalEntity(this.in);
    
    whitespace("F-011");
    if (peek("PUBLIC"))
    {
      whitespace("F-009");
      entity.publicId = parsePublicId();
      if (maybeWhitespace()) {
        if (!peek(">")) {
          entity.systemId = parseSystemId();
        } else {
          ungetc();
        }
      }
    }
    else if (peek("SYSTEM"))
    {
      whitespace("F-008");
      entity.systemId = parseSystemId();
    }
    else
    {
      fatal("P-062");
    }
    maybeWhitespace();
    nextChar('>', "F-032", name);
    if (start != this.in) {
      error("V-013", null);
    }
    if ((entity.systemId != null) && (entity.systemId.indexOf('#') != -1)) {
      error("P-056", new Object[] { entity.systemId });
    }
    Object value = this.notations.get(name);
    if ((value != null) && ((value instanceof ExternalEntity)))
    {
      warning("P-063", new Object[] { name });
    }
    else
    {
      this.notations.put(name, entity);
      this.dtdHandler.notationDecl(name, entity.publicId, entity.systemId);
    }
    return true;
  }
  
  private char getc()
    throws IOException, SAXException
  {
    if (!this.doLexicalPE)
    {
      char c = this.in.getc();
      return c;
    }
    while (this.in.isEOF()) {
      if ((this.in.isInternal()) || ((this.doLexicalPE) && (!this.in.isDocument()))) {
        this.in = this.in.pop();
      } else {
        fatal("P-064", new Object[] { this.in.getName() });
      }
    }
    char c;
    if (((c = this.in.getc()) == '%') && (this.doLexicalPE))
    {
      String name = maybeGetName();
      if (name == null) {
        fatal("P-011");
      }
      nextChar(';', "F-021", name);
      Object entity = this.params.get(name);
      
      pushReader(" ".toCharArray(), null, false);
      if ((entity instanceof InternalEntity)) {
        pushReader(((InternalEntity)entity).buf, name, false);
      } else if ((entity instanceof ExternalEntity)) {
        pushReader((ExternalEntity)entity);
      } else if (entity == null) {
        fatal("V-022");
      } else {
        throw new InternalError();
      }
      pushReader(" ".toCharArray(), null, false);
      return this.in.getc();
    }
    return c;
  }
  
  private void ungetc()
  {
    this.in.ungetc();
  }
  
  private boolean peek(String s)
    throws IOException, SAXException
  {
    return this.in.peek(s, null);
  }
  
  private InputEntity peekDeclaration(String s)
    throws IOException, SAXException
  {
    if (!this.in.peekc('<')) {
      return null;
    }
    InputEntity start = this.in;
    if (this.in.peek(s, null)) {
      return start;
    }
    this.in.ungetc();
    return null;
  }
  
  private void nextChar(char c, String location, String near)
    throws IOException, SAXException
  {
    while ((this.in.isEOF()) && (!this.in.isDocument())) {
      this.in = this.in.pop();
    }
    if (!this.in.peekc(c)) {
      fatal("P-008", new Object[] { new Character(c), messages.getMessage(this.locale, location), '"' + near + '"' });
    }
  }
  
  private void pushReader(char[] buf, String name, boolean isGeneral)
    throws SAXException
  {
    InputEntity r = InputEntity.getInputEntity(this.dtdHandler, this.locale);
    r.init(buf, name, this.in, !isGeneral);
    this.in = r;
  }
  
  private boolean pushReader(ExternalEntity next)
    throws IOException, SAXException
  {
    InputEntity r = InputEntity.getInputEntity(this.dtdHandler, this.locale);
    InputSource s;
    try
    {
      s = next.getInputSource(this.resolver);
    }
    catch (IOException e)
    {
      String msg = "unable to open the external entity from :" + next.systemId;
      if (next.publicId != null) {
        msg = msg + " (public id:" + next.publicId + ")";
      }
      SAXParseException spe = new SAXParseException(msg, getPublicId(), getSystemId(), getLineNumber(), getColumnNumber(), e);
      
      this.dtdHandler.fatalError(spe);
      throw e;
    }
    r.init(s, next.name, this.in, next.isPE);
    this.in = r;
    return true;
  }
  
  public String getPublicId()
  {
    return this.in == null ? null : this.in.getPublicId();
  }
  
  public String getSystemId()
  {
    return this.in == null ? null : this.in.getSystemId();
  }
  
  public int getLineNumber()
  {
    return this.in == null ? -1 : this.in.getLineNumber();
  }
  
  public int getColumnNumber()
  {
    return this.in == null ? -1 : this.in.getColumnNumber();
  }
  
  private void warning(String messageId, Object[] parameters)
    throws SAXException
  {
    SAXParseException e = new SAXParseException(messages.getMessage(this.locale, messageId, parameters), getPublicId(), getSystemId(), getLineNumber(), getColumnNumber());
    
    this.dtdHandler.warning(e);
  }
  
  void error(String messageId, Object[] parameters)
    throws SAXException
  {
    SAXParseException e = new SAXParseException(messages.getMessage(this.locale, messageId, parameters), getPublicId(), getSystemId(), getLineNumber(), getColumnNumber());
    
    this.dtdHandler.error(e);
  }
  
  private void fatal(String messageId)
    throws SAXException
  {
    fatal(messageId, null);
  }
  
  private void fatal(String messageId, Object[] parameters)
    throws SAXException
  {
    SAXParseException e = new SAXParseException(messages.getMessage(this.locale, messageId, parameters), getPublicId(), getSystemId(), getLineNumber(), getColumnNumber());
    
    this.dtdHandler.fatalError(e);
    
    throw e;
  }
  
  static class NameCache
  {
    DTDParser.NameCacheEntry[] hashtable = new DTDParser.NameCacheEntry['ȝ'];
    
    String lookup(char[] value, int len)
    {
      return lookupEntry(value, len).name;
    }
    
    DTDParser.NameCacheEntry lookupEntry(char[] value, int len)
    {
      int index = 0;
      for (int i = 0; i < len; i++) {
        index = index * 31 + value[i];
      }
      index &= 0x7FFFFFFF;
      index %= this.hashtable.length;
      for (DTDParser.NameCacheEntry entry = this.hashtable[index]; entry != null; entry = entry.next) {
        if (entry.matches(value, len)) {
          return entry;
        }
      }
      entry = new DTDParser.NameCacheEntry();
      entry.chars = new char[len];
      System.arraycopy(value, 0, entry.chars, 0, len);
      entry.name = new String(entry.chars);
      
      entry.name = entry.name.intern();
      entry.next = this.hashtable[index];
      this.hashtable[index] = entry;
      return entry;
    }
  }
  
  static class NameCacheEntry
  {
    String name;
    char[] chars;
    NameCacheEntry next;
    
    boolean matches(char[] value, int len)
    {
      if (this.chars.length != len) {
        return false;
      }
      for (int i = 0; i < len; i++) {
        if (value[i] != this.chars[i]) {
          return false;
        }
      }
      return true;
    }
  }
  
  static final Catalog messages = new Catalog();
  
  static final class Catalog
    extends MessageCatalog
  {
    Catalog()
    {
      super();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\dtdparser\DTDParser.class
 * Java compiler version: 3 (47.0)
 * JD-Core Version:       0.7.1
 */