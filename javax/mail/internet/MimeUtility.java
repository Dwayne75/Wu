package javax.mail.internet;

import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.util.BEncoderStream;
import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.PropUtil;
import com.sun.mail.util.QDecoderStream;
import com.sun.mail.util.QEncoderStream;
import com.sun.mail.util.QPDecoderStream;
import com.sun.mail.util.QPEncoderStream;
import com.sun.mail.util.UUDecoderStream;
import com.sun.mail.util.UUEncoderStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;

public class MimeUtility
{
  public static final int ALL = -1;
  private static final Map nonAsciiCharsetMap = new HashMap();
  private static final boolean decodeStrict = PropUtil.getBooleanSystemProperty("mail.mime.decodetext.strict", true);
  private static final boolean encodeEolStrict = PropUtil.getBooleanSystemProperty("mail.mime.encodeeol.strict", false);
  private static final boolean ignoreUnknownEncoding = PropUtil.getBooleanSystemProperty("mail.mime.ignoreunknownencoding", false);
  private static final boolean foldEncodedWords = PropUtil.getBooleanSystemProperty("mail.mime.foldencodedwords", false);
  private static final boolean foldText = PropUtil.getBooleanSystemProperty("mail.mime.foldtext", true);
  private static String defaultJavaCharset;
  private static String defaultMIMECharset;
  private static Hashtable mime2java;
  
  public static String getEncoding(DataSource ds)
  {
    ContentType cType = null;
    InputStream is = null;
    String encoding = null;
    try
    {
      cType = new ContentType(ds.getContentType());
      is = ds.getInputStream();
      
      boolean isText = cType.match("text/*");
      
      i = checkAscii(is, -1, !isText);
      switch (i)
      {
      case 1: 
        encoding = "7bit";
        break;
      case 2: 
        if ((isText) && (nonAsciiCharset(cType))) {
          encoding = "base64";
        } else {
          encoding = "quoted-printable";
        }
        break;
      default: 
        encoding = "base64";
      }
    }
    catch (Exception ex)
    {
      int i;
      return "base64";
    }
    finally
    {
      try
      {
        if (is != null) {
          is.close();
        }
      }
      catch (IOException ioex) {}
    }
    return encoding;
  }
  
  private static boolean nonAsciiCharset(ContentType ct)
  {
    String charset = ct.getParameter("charset");
    if (charset == null) {
      return false;
    }
    charset = charset.toLowerCase(Locale.ENGLISH);
    Boolean bool;
    synchronized (nonAsciiCharsetMap)
    {
      bool = (Boolean)nonAsciiCharsetMap.get(charset);
    }
    if (bool == null)
    {
      try
      {
        byte[] b = "\r\n".getBytes(charset);
        bool = Boolean.valueOf((b.length != 2) || (b[0] != 13) || (b[1] != 10));
      }
      catch (UnsupportedEncodingException uex)
      {
        bool = Boolean.FALSE;
      }
      catch (RuntimeException ex)
      {
        bool = Boolean.TRUE;
      }
      synchronized (nonAsciiCharsetMap)
      {
        nonAsciiCharsetMap.put(charset, bool);
      }
    }
    return bool.booleanValue();
  }
  
  public static String getEncoding(DataHandler dh)
  {
    ContentType cType = null;
    String encoding = null;
    if (dh.getName() != null) {
      return getEncoding(dh.getDataSource());
    }
    try
    {
      cType = new ContentType(dh.getContentType());
    }
    catch (Exception ex)
    {
      return "base64";
    }
    if (cType.match("text/*"))
    {
      AsciiOutputStream aos = new AsciiOutputStream(false, false);
      try
      {
        dh.writeTo(aos);
      }
      catch (IOException ex) {}
      switch (aos.getAscii())
      {
      case 1: 
        encoding = "7bit";
        break;
      case 2: 
        encoding = "quoted-printable";
        break;
      default: 
        encoding = "base64";
      }
    }
    else
    {
      AsciiOutputStream aos = new AsciiOutputStream(true, encodeEolStrict);
      try
      {
        dh.writeTo(aos);
      }
      catch (IOException ex) {}
      if (aos.getAscii() == 1) {
        encoding = "7bit";
      } else {
        encoding = "base64";
      }
    }
    return encoding;
  }
  
  public static InputStream decode(InputStream is, String encoding)
    throws MessagingException
  {
    if (encoding.equalsIgnoreCase("base64")) {
      return new BASE64DecoderStream(is);
    }
    if (encoding.equalsIgnoreCase("quoted-printable")) {
      return new QPDecoderStream(is);
    }
    if ((encoding.equalsIgnoreCase("uuencode")) || (encoding.equalsIgnoreCase("x-uuencode")) || (encoding.equalsIgnoreCase("x-uue"))) {
      return new UUDecoderStream(is);
    }
    if ((encoding.equalsIgnoreCase("binary")) || (encoding.equalsIgnoreCase("7bit")) || (encoding.equalsIgnoreCase("8bit"))) {
      return is;
    }
    if (!ignoreUnknownEncoding) {
      throw new MessagingException("Unknown encoding: " + encoding);
    }
    return is;
  }
  
  public static OutputStream encode(OutputStream os, String encoding)
    throws MessagingException
  {
    if (encoding == null) {
      return os;
    }
    if (encoding.equalsIgnoreCase("base64")) {
      return new BASE64EncoderStream(os);
    }
    if (encoding.equalsIgnoreCase("quoted-printable")) {
      return new QPEncoderStream(os);
    }
    if ((encoding.equalsIgnoreCase("uuencode")) || (encoding.equalsIgnoreCase("x-uuencode")) || (encoding.equalsIgnoreCase("x-uue"))) {
      return new UUEncoderStream(os);
    }
    if ((encoding.equalsIgnoreCase("binary")) || (encoding.equalsIgnoreCase("7bit")) || (encoding.equalsIgnoreCase("8bit"))) {
      return os;
    }
    throw new MessagingException("Unknown encoding: " + encoding);
  }
  
  public static OutputStream encode(OutputStream os, String encoding, String filename)
    throws MessagingException
  {
    if (encoding == null) {
      return os;
    }
    if (encoding.equalsIgnoreCase("base64")) {
      return new BASE64EncoderStream(os);
    }
    if (encoding.equalsIgnoreCase("quoted-printable")) {
      return new QPEncoderStream(os);
    }
    if ((encoding.equalsIgnoreCase("uuencode")) || (encoding.equalsIgnoreCase("x-uuencode")) || (encoding.equalsIgnoreCase("x-uue"))) {
      return new UUEncoderStream(os, filename);
    }
    if ((encoding.equalsIgnoreCase("binary")) || (encoding.equalsIgnoreCase("7bit")) || (encoding.equalsIgnoreCase("8bit"))) {
      return os;
    }
    throw new MessagingException("Unknown encoding: " + encoding);
  }
  
  public static String encodeText(String text)
    throws UnsupportedEncodingException
  {
    return encodeText(text, null, null);
  }
  
  public static String encodeText(String text, String charset, String encoding)
    throws UnsupportedEncodingException
  {
    return encodeWord(text, charset, encoding, false);
  }
  
  public static String decodeText(String etext)
    throws UnsupportedEncodingException
  {
    String lwsp = " \t\n\r";
    if (etext.indexOf("=?") == -1) {
      return etext;
    }
    StringTokenizer st = new StringTokenizer(etext, lwsp, true);
    StringBuffer sb = new StringBuffer();
    StringBuffer wsb = new StringBuffer();
    boolean prevWasEncoded = false;
    while (st.hasMoreTokens())
    {
      String s = st.nextToken();
      char c;
      if (((c = s.charAt(0)) == ' ') || (c == '\t') || (c == '\r') || (c == '\n'))
      {
        wsb.append(c);
      }
      else
      {
        String word;
        try
        {
          word = decodeWord(s);
          if ((!prevWasEncoded) && (wsb.length() > 0)) {
            sb.append(wsb);
          }
          prevWasEncoded = true;
        }
        catch (ParseException pex)
        {
          word = s;
          if (!decodeStrict)
          {
            String dword = decodeInnerWords(word);
            if (dword != word)
            {
              if ((!prevWasEncoded) || (!word.startsWith("=?"))) {
                if (wsb.length() > 0) {
                  sb.append(wsb);
                }
              }
              prevWasEncoded = word.endsWith("?=");
              word = dword;
            }
            else
            {
              if (wsb.length() > 0) {
                sb.append(wsb);
              }
              prevWasEncoded = false;
            }
          }
          else
          {
            if (wsb.length() > 0) {
              sb.append(wsb);
            }
            prevWasEncoded = false;
          }
        }
        sb.append(word);
        wsb.setLength(0);
      }
    }
    sb.append(wsb);
    return sb.toString();
  }
  
  public static String encodeWord(String word)
    throws UnsupportedEncodingException
  {
    return encodeWord(word, null, null);
  }
  
  public static String encodeWord(String word, String charset, String encoding)
    throws UnsupportedEncodingException
  {
    return encodeWord(word, charset, encoding, true);
  }
  
  private static String encodeWord(String string, String charset, String encoding, boolean encodingWord)
    throws UnsupportedEncodingException
  {
    int ascii = checkAscii(string);
    if (ascii == 1) {
      return string;
    }
    String jcharset;
    if (charset == null)
    {
      String jcharset = getDefaultJavaCharset();
      charset = getDefaultMIMECharset();
    }
    else
    {
      jcharset = javaCharset(charset);
    }
    if (encoding == null) {
      if (ascii != 3) {
        encoding = "Q";
      } else {
        encoding = "B";
      }
    }
    boolean b64;
    if (encoding.equalsIgnoreCase("B"))
    {
      b64 = true;
    }
    else
    {
      boolean b64;
      if (encoding.equalsIgnoreCase("Q")) {
        b64 = false;
      } else {
        throw new UnsupportedEncodingException("Unknown transfer encoding: " + encoding);
      }
    }
    boolean b64;
    StringBuffer outb = new StringBuffer();
    doEncode(string, b64, jcharset, 68 - charset.length(), "=?" + charset + "?" + encoding + "?", true, encodingWord, outb);
    
    return outb.toString();
  }
  
  private static void doEncode(String string, boolean b64, String jcharset, int avail, String prefix, boolean first, boolean encodingWord, StringBuffer buf)
    throws UnsupportedEncodingException
  {
    byte[] bytes = string.getBytes(jcharset);
    int len;
    int len;
    if (b64) {
      len = BEncoderStream.encodedLength(bytes);
    } else {
      len = QEncoderStream.encodedLength(bytes, encodingWord);
    }
    int size;
    if ((len > avail) && ((size = string.length()) > 1))
    {
      doEncode(string.substring(0, size / 2), b64, jcharset, avail, prefix, first, encodingWord, buf);
      
      doEncode(string.substring(size / 2, size), b64, jcharset, avail, prefix, false, encodingWord, buf);
    }
    else
    {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      OutputStream eos;
      OutputStream eos;
      if (b64) {
        eos = new BEncoderStream(os);
      } else {
        eos = new QEncoderStream(os, encodingWord);
      }
      try
      {
        eos.write(bytes);
        eos.close();
      }
      catch (IOException ioex) {}
      byte[] encodedBytes = os.toByteArray();
      if (!first) {
        if (foldEncodedWords) {
          buf.append("\r\n ");
        } else {
          buf.append(" ");
        }
      }
      buf.append(prefix);
      for (int i = 0; i < encodedBytes.length; i++) {
        buf.append((char)encodedBytes[i]);
      }
      buf.append("?=");
    }
  }
  
  public static String decodeWord(String eword)
    throws ParseException, UnsupportedEncodingException
  {
    if (!eword.startsWith("=?")) {
      throw new ParseException("encoded word does not start with \"=?\": " + eword);
    }
    int start = 2;
    int pos;
    if ((pos = eword.indexOf('?', start)) == -1) {
      throw new ParseException("encoded word does not include charset: " + eword);
    }
    String charset = eword.substring(start, pos);
    int lpos = charset.indexOf('*');
    if (lpos >= 0) {
      charset = charset.substring(0, lpos);
    }
    charset = javaCharset(charset);
    
    start = pos + 1;
    if ((pos = eword.indexOf('?', start)) == -1) {
      throw new ParseException("encoded word does not include encoding: " + eword);
    }
    String encoding = eword.substring(start, pos);
    
    start = pos + 1;
    if ((pos = eword.indexOf("?=", start)) == -1) {
      throw new ParseException("encoded word does not end with \"?=\": " + eword);
    }
    String word = eword.substring(start, pos);
    try
    {
      String decodedWord;
      String decodedWord;
      if (word.length() > 0)
      {
        ByteArrayInputStream bis = new ByteArrayInputStream(ASCIIUtility.getBytes(word));
        InputStream is;
        if (encoding.equalsIgnoreCase("B"))
        {
          is = new BASE64DecoderStream(bis);
        }
        else
        {
          InputStream is;
          if (encoding.equalsIgnoreCase("Q")) {
            is = new QDecoderStream(bis);
          } else {
            throw new UnsupportedEncodingException("unknown encoding: " + encoding);
          }
        }
        InputStream is;
        int count = bis.available();
        byte[] bytes = new byte[count];
        
        count = is.read(bytes, 0, count);
        
        decodedWord = count <= 0 ? "" : new String(bytes, 0, count, charset);
      }
      else
      {
        decodedWord = "";
      }
      String rest;
      if (pos + 2 < eword.length())
      {
        rest = eword.substring(pos + 2);
        if (!decodeStrict) {
          rest = decodeInnerWords(rest);
        }
      }
      return decodedWord + rest;
    }
    catch (UnsupportedEncodingException uex)
    {
      throw uex;
    }
    catch (IOException ioex)
    {
      throw new ParseException(ioex.toString());
    }
    catch (IllegalArgumentException iex)
    {
      throw new UnsupportedEncodingException(charset);
    }
  }
  
  private static String decodeInnerWords(String word)
    throws UnsupportedEncodingException
  {
    int start = 0;
    StringBuffer buf = new StringBuffer();
    int i;
    while ((i = word.indexOf("=?", start)) >= 0)
    {
      buf.append(word.substring(start, i));
      
      int end = word.indexOf('?', i + 2);
      if (end < 0) {
        break;
      }
      end = word.indexOf('?', end + 1);
      if (end < 0) {
        break;
      }
      end = word.indexOf("?=", end + 1);
      if (end < 0) {
        break;
      }
      String s = word.substring(i, end + 2);
      try
      {
        s = decodeWord(s);
      }
      catch (ParseException pex) {}
      buf.append(s);
      start = end + 2;
    }
    if (start == 0) {
      return word;
    }
    if (start < word.length()) {
      buf.append(word.substring(start));
    }
    return buf.toString();
  }
  
  public static String quote(String word, String specials)
  {
    int len = word.length();
    if (len == 0) {
      return "\"\"";
    }
    boolean needQuoting = false;
    for (int i = 0; i < len; i++)
    {
      char c = word.charAt(i);
      if ((c == '"') || (c == '\\') || (c == '\r') || (c == '\n'))
      {
        StringBuffer sb = new StringBuffer(len + 3);
        sb.append('"');
        sb.append(word.substring(0, i));
        int lastc = 0;
        for (int j = i; j < len; j++)
        {
          char cc = word.charAt(j);
          if ((cc == '"') || (cc == '\\') || (cc == '\r') || (cc == '\n')) {
            if ((cc != '\n') || (lastc != 13)) {
              sb.append('\\');
            }
          }
          sb.append(cc);
          lastc = cc;
        }
        sb.append('"');
        return sb.toString();
      }
      if ((c < ' ') || (c >= '') || (specials.indexOf(c) >= 0)) {
        needQuoting = true;
      }
    }
    if (needQuoting)
    {
      StringBuffer sb = new StringBuffer(len + 2);
      sb.append('"').append(word).append('"');
      return sb.toString();
    }
    return word;
  }
  
  public static String fold(int used, String s)
  {
    if (!foldText) {
      return s;
    }
    for (int end = s.length() - 1; end >= 0; end--)
    {
      char c = s.charAt(end);
      if ((c != ' ') && (c != '\t') && (c != '\r') && (c != '\n')) {
        break;
      }
    }
    if (end != s.length() - 1) {
      s = s.substring(0, end + 1);
    }
    if (used + s.length() <= 76) {
      return s;
    }
    StringBuffer sb = new StringBuffer(s.length() + 4);
    char lastc = '\000';
    while (used + s.length() > 76)
    {
      int lastspace = -1;
      for (int i = 0; i < s.length(); i++)
      {
        if ((lastspace != -1) && (used + i > 76)) {
          break;
        }
        char c = s.charAt(i);
        if (((c == ' ') || (c == '\t')) && 
          (lastc != ' ') && (lastc != '\t')) {
          lastspace = i;
        }
        lastc = c;
      }
      if (lastspace == -1)
      {
        sb.append(s);
        s = "";
        used = 0;
        break;
      }
      sb.append(s.substring(0, lastspace));
      sb.append("\r\n");
      lastc = s.charAt(lastspace);
      sb.append(lastc);
      s = s.substring(lastspace + 1);
      used = 1;
    }
    sb.append(s);
    return sb.toString();
  }
  
  public static String unfold(String s)
  {
    if (!foldText) {
      return s;
    }
    StringBuffer sb = null;
    int i;
    while ((i = indexOfAny(s, "\r\n")) >= 0)
    {
      int start = i;
      int l = s.length();
      i++;
      if ((i < l) && (s.charAt(i - 1) == '\r') && (s.charAt(i) == '\n')) {
        i++;
      }
      if ((start == 0) || (s.charAt(start - 1) != '\\'))
      {
        char c;
        if ((i < l) && (((c = s.charAt(i)) == ' ') || (c == '\t')))
        {
          i++;
          while ((i < l) && (((c = s.charAt(i)) == ' ') || (c == '\t'))) {
            i++;
          }
          if (sb == null) {
            sb = new StringBuffer(s.length());
          }
          if (start != 0)
          {
            sb.append(s.substring(0, start));
            sb.append(' ');
          }
          s = s.substring(i);
        }
        else
        {
          if (sb == null) {
            sb = new StringBuffer(s.length());
          }
          sb.append(s.substring(0, i));
          s = s.substring(i);
        }
      }
      else
      {
        if (sb == null) {
          sb = new StringBuffer(s.length());
        }
        sb.append(s.substring(0, start - 1));
        sb.append(s.substring(start, i));
        s = s.substring(i);
      }
    }
    if (sb != null)
    {
      sb.append(s);
      return sb.toString();
    }
    return s;
  }
  
  private static int indexOfAny(String s, String any)
  {
    return indexOfAny(s, any, 0);
  }
  
  private static int indexOfAny(String s, String any, int start)
  {
    try
    {
      int len = s.length();
      for (int i = start; i < len; i++) {
        if (any.indexOf(s.charAt(i)) >= 0) {
          return i;
        }
      }
      return -1;
    }
    catch (StringIndexOutOfBoundsException e) {}
    return -1;
  }
  
  public static String javaCharset(String charset)
  {
    if ((mime2java == null) || (charset == null)) {
      return charset;
    }
    String alias = (String)mime2java.get(charset.toLowerCase(Locale.ENGLISH));
    
    return alias == null ? charset : alias;
  }
  
  public static String mimeCharset(String charset)
  {
    if ((java2mime == null) || (charset == null)) {
      return charset;
    }
    String alias = (String)java2mime.get(charset.toLowerCase(Locale.ENGLISH));
    
    return alias == null ? charset : alias;
  }
  
  public static String getDefaultJavaCharset()
  {
    if (defaultJavaCharset == null)
    {
      String mimecs = null;
      try
      {
        mimecs = System.getProperty("mail.mime.charset");
      }
      catch (SecurityException ex) {}
      if ((mimecs != null) && (mimecs.length() > 0))
      {
        defaultJavaCharset = javaCharset(mimecs);
        return defaultJavaCharset;
      }
      try
      {
        defaultJavaCharset = System.getProperty("file.encoding", "8859_1");
      }
      catch (SecurityException sex)
      {
        InputStreamReader reader = new InputStreamReader(new InputStream()
        {
          public int read()
          {
            return 0;
          }
        });
        defaultJavaCharset = reader.getEncoding();
        if (defaultJavaCharset == null) {
          defaultJavaCharset = "8859_1";
        }
      }
    }
    return defaultJavaCharset;
  }
  
  static String getDefaultMIMECharset()
  {
    if (defaultMIMECharset == null) {
      try
      {
        defaultMIMECharset = System.getProperty("mail.mime.charset");
      }
      catch (SecurityException ex) {}
    }
    if (defaultMIMECharset == null) {
      defaultMIMECharset = mimeCharset(getDefaultJavaCharset());
    }
    return defaultMIMECharset;
  }
  
  private static Hashtable java2mime = new Hashtable(40);
  static final int ALL_ASCII = 1;
  static final int MOSTLY_ASCII = 2;
  static final int MOSTLY_NONASCII = 3;
  
  static
  {
    mime2java = new Hashtable(10);
    try
    {
      InputStream is = MimeUtility.class.getResourceAsStream("/META-INF/javamail.charset.map");
      if (is != null) {
        try
        {
          is = new LineInputStream(is);
          
          loadMappings((LineInputStream)is, java2mime);
          
          loadMappings((LineInputStream)is, mime2java);
        }
        finally
        {
          try
          {
            is.close();
          }
          catch (Exception cex) {}
        }
      }
    }
    catch (Exception ex) {}
    if (java2mime.isEmpty())
    {
      java2mime.put("8859_1", "ISO-8859-1");
      java2mime.put("iso8859_1", "ISO-8859-1");
      java2mime.put("iso8859-1", "ISO-8859-1");
      
      java2mime.put("8859_2", "ISO-8859-2");
      java2mime.put("iso8859_2", "ISO-8859-2");
      java2mime.put("iso8859-2", "ISO-8859-2");
      
      java2mime.put("8859_3", "ISO-8859-3");
      java2mime.put("iso8859_3", "ISO-8859-3");
      java2mime.put("iso8859-3", "ISO-8859-3");
      
      java2mime.put("8859_4", "ISO-8859-4");
      java2mime.put("iso8859_4", "ISO-8859-4");
      java2mime.put("iso8859-4", "ISO-8859-4");
      
      java2mime.put("8859_5", "ISO-8859-5");
      java2mime.put("iso8859_5", "ISO-8859-5");
      java2mime.put("iso8859-5", "ISO-8859-5");
      
      java2mime.put("8859_6", "ISO-8859-6");
      java2mime.put("iso8859_6", "ISO-8859-6");
      java2mime.put("iso8859-6", "ISO-8859-6");
      
      java2mime.put("8859_7", "ISO-8859-7");
      java2mime.put("iso8859_7", "ISO-8859-7");
      java2mime.put("iso8859-7", "ISO-8859-7");
      
      java2mime.put("8859_8", "ISO-8859-8");
      java2mime.put("iso8859_8", "ISO-8859-8");
      java2mime.put("iso8859-8", "ISO-8859-8");
      
      java2mime.put("8859_9", "ISO-8859-9");
      java2mime.put("iso8859_9", "ISO-8859-9");
      java2mime.put("iso8859-9", "ISO-8859-9");
      
      java2mime.put("sjis", "Shift_JIS");
      java2mime.put("jis", "ISO-2022-JP");
      java2mime.put("iso2022jp", "ISO-2022-JP");
      java2mime.put("euc_jp", "euc-jp");
      java2mime.put("koi8_r", "koi8-r");
      java2mime.put("euc_cn", "euc-cn");
      java2mime.put("euc_tw", "euc-tw");
      java2mime.put("euc_kr", "euc-kr");
    }
    if (mime2java.isEmpty())
    {
      mime2java.put("iso-2022-cn", "ISO2022CN");
      mime2java.put("iso-2022-kr", "ISO2022KR");
      mime2java.put("utf-8", "UTF8");
      mime2java.put("utf8", "UTF8");
      mime2java.put("ja_jp.iso2022-7", "ISO2022JP");
      mime2java.put("ja_jp.eucjp", "EUCJIS");
      mime2java.put("euc-kr", "KSC5601");
      mime2java.put("euckr", "KSC5601");
      mime2java.put("us-ascii", "ISO-8859-1");
      mime2java.put("x-us-ascii", "ISO-8859-1");
    }
  }
  
  private static void loadMappings(LineInputStream is, Hashtable table)
  {
    for (;;)
    {
      String currLine;
      try
      {
        currLine = is.readLine();
      }
      catch (IOException ioex)
      {
        break;
      }
      if (currLine == null) {
        break;
      }
      if ((currLine.startsWith("--")) && (currLine.endsWith("--"))) {
        break;
      }
      if ((currLine.trim().length() != 0) && (!currLine.startsWith("#")))
      {
        StringTokenizer tk = new StringTokenizer(currLine, " \t");
        try
        {
          String key = tk.nextToken();
          String value = tk.nextToken();
          table.put(key.toLowerCase(Locale.ENGLISH), value);
        }
        catch (NoSuchElementException nex) {}
      }
    }
  }
  
  static int checkAscii(String s)
  {
    int ascii = 0;int non_ascii = 0;
    int l = s.length();
    for (int i = 0; i < l; i++) {
      if (nonascii(s.charAt(i))) {
        non_ascii++;
      } else {
        ascii++;
      }
    }
    if (non_ascii == 0) {
      return 1;
    }
    if (ascii > non_ascii) {
      return 2;
    }
    return 3;
  }
  
  static int checkAscii(byte[] b)
  {
    int ascii = 0;int non_ascii = 0;
    for (int i = 0; i < b.length; i++) {
      if (nonascii(b[i] & 0xFF)) {
        non_ascii++;
      } else {
        ascii++;
      }
    }
    if (non_ascii == 0) {
      return 1;
    }
    if (ascii > non_ascii) {
      return 2;
    }
    return 3;
  }
  
  static int checkAscii(InputStream is, int max, boolean breakOnNonAscii)
  {
    int ascii = 0;int non_ascii = 0;
    
    int block = 4096;
    int linelen = 0;
    boolean longLine = false;boolean badEOL = false;
    boolean checkEOL = (encodeEolStrict) && (breakOnNonAscii);
    byte[] buf = null;
    if (max != 0)
    {
      block = max == -1 ? 4096 : Math.min(max, 4096);
      buf = new byte[block];
    }
    while (max != 0)
    {
      int len;
      try
      {
        if ((len = is.read(buf, 0, block)) == -1) {
          break;
        }
        int lastb = 0;
        for (int i = 0; i < len; i++)
        {
          int b = buf[i] & 0xFF;
          if ((checkEOL) && (((lastb == 13) && (b != 10)) || ((lastb != 13) && (b == 10)))) {
            badEOL = true;
          }
          if ((b == 13) || (b == 10))
          {
            linelen = 0;
          }
          else
          {
            linelen++;
            if (linelen > 998) {
              longLine = true;
            }
          }
          if (nonascii(b))
          {
            if (breakOnNonAscii) {
              return 3;
            }
            non_ascii++;
          }
          else
          {
            ascii++;
          }
          lastb = b;
        }
      }
      catch (IOException ioex)
      {
        break;
      }
      if (max != -1) {
        max -= len;
      }
    }
    if ((max == 0) && (breakOnNonAscii)) {
      return 3;
    }
    if (non_ascii == 0)
    {
      if (badEOL) {
        return 3;
      }
      if (longLine) {
        return 2;
      }
      return 1;
    }
    if (ascii > non_ascii) {
      return 2;
    }
    return 3;
  }
  
  static final boolean nonascii(int b)
  {
    return (b >= 127) || ((b < 32) && (b != 13) && (b != 10) && (b != 9));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\internet\MimeUtility.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */