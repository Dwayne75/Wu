package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

public class CharsetMapping
{
  private static final Properties CHARSET_CONFIG = new Properties();
  public static final String[] INDEX_TO_CHARSET;
  public static final String[] INDEX_TO_COLLATION;
  private static final Map JAVA_TO_MYSQL_CHARSET_MAP;
  private static final Map JAVA_UC_TO_MYSQL_CHARSET_MAP;
  private static final Map ERROR_MESSAGE_FILE_TO_MYSQL_CHARSET_MAP;
  private static final Map MULTIBYTE_CHARSETS;
  private static final Map MYSQL_TO_JAVA_CHARSET_MAP;
  private static final Map MYSQL_ENCODING_NAME_TO_CHARSET_INDEX_MAP;
  private static final String NOT_USED = "ISO8859_1";
  public static final Map STATIC_CHARSET_TO_NUM_BYTES_MAP;
  
  static
  {
    HashMap tempNumBytesMap = new HashMap();
    
    tempNumBytesMap.put("big5", Constants.integerValueOf(2));
    tempNumBytesMap.put("dec8", Constants.integerValueOf(1));
    tempNumBytesMap.put("cp850", Constants.integerValueOf(1));
    tempNumBytesMap.put("hp8", Constants.integerValueOf(1));
    tempNumBytesMap.put("koi8r", Constants.integerValueOf(1));
    tempNumBytesMap.put("latin1", Constants.integerValueOf(1));
    tempNumBytesMap.put("latin2", Constants.integerValueOf(1));
    tempNumBytesMap.put("swe7", Constants.integerValueOf(1));
    tempNumBytesMap.put("ascii", Constants.integerValueOf(1));
    tempNumBytesMap.put("ujis", Constants.integerValueOf(3));
    tempNumBytesMap.put("sjis", Constants.integerValueOf(2));
    tempNumBytesMap.put("hebrew", Constants.integerValueOf(1));
    tempNumBytesMap.put("tis620", Constants.integerValueOf(1));
    tempNumBytesMap.put("euckr", Constants.integerValueOf(2));
    tempNumBytesMap.put("koi8u", Constants.integerValueOf(1));
    tempNumBytesMap.put("gb2312", Constants.integerValueOf(2));
    tempNumBytesMap.put("greek", Constants.integerValueOf(1));
    tempNumBytesMap.put("cp1250", Constants.integerValueOf(1));
    tempNumBytesMap.put("gbk", Constants.integerValueOf(2));
    tempNumBytesMap.put("latin5", Constants.integerValueOf(1));
    tempNumBytesMap.put("armscii8", Constants.integerValueOf(1));
    tempNumBytesMap.put("utf8", Constants.integerValueOf(3));
    tempNumBytesMap.put("ucs2", Constants.integerValueOf(2));
    tempNumBytesMap.put("cp866", Constants.integerValueOf(1));
    tempNumBytesMap.put("keybcs2", Constants.integerValueOf(1));
    tempNumBytesMap.put("macce", Constants.integerValueOf(1));
    tempNumBytesMap.put("macroman", Constants.integerValueOf(1));
    tempNumBytesMap.put("cp852", Constants.integerValueOf(1));
    tempNumBytesMap.put("latin7", Constants.integerValueOf(1));
    tempNumBytesMap.put("cp1251", Constants.integerValueOf(1));
    tempNumBytesMap.put("cp1256", Constants.integerValueOf(1));
    tempNumBytesMap.put("cp1257", Constants.integerValueOf(1));
    tempNumBytesMap.put("binary", Constants.integerValueOf(1));
    tempNumBytesMap.put("geostd8", Constants.integerValueOf(1));
    tempNumBytesMap.put("cp932", Constants.integerValueOf(2));
    tempNumBytesMap.put("eucjpms", Constants.integerValueOf(3));
    
    STATIC_CHARSET_TO_NUM_BYTES_MAP = Collections.unmodifiableMap(tempNumBytesMap);
    
    CHARSET_CONFIG.setProperty("javaToMysqlMappings", "US-ASCII =\t\t\tusa7,US-ASCII =\t\t\t>4.1.0 ascii,Big5 = \t\t\t\tbig5,GBK = \t\t\t\tgbk,SJIS = \t\t\t\tsjis,EUC_CN = \t\t\tgb2312,EUC_JP = \t\t\tujis,EUC_JP_Solaris = \t>5.0.3 eucjpms,EUC_KR = \t\t\teuc_kr,EUC_KR = \t\t\t>4.1.0 euckr,ISO8859_1 =\t\t\t*latin1,ISO8859_1 =\t\t\tlatin1_de,ISO8859_1 =\t\t\tgerman1,ISO8859_1 =\t\t\tdanish,ISO8859_2 =\t\t\tlatin2,ISO8859_2 =\t\t\tczech,ISO8859_2 =\t\t\thungarian,ISO8859_2  =\t\tcroat,ISO8859_7  =\t\tgreek,ISO8859_7  =\t\tlatin7,ISO8859_8  = \t\thebrew,ISO8859_9  =\t\tlatin5,ISO8859_13 =\t\tlatvian,ISO8859_13 =\t\tlatvian1,ISO8859_13 =\t\testonia,Cp437 =             *>4.1.0 cp850,Cp437 =\t\t\t\tdos,Cp850 =\t\t\t\tcp850,Cp852 = \t\t\tcp852,Cp866 = \t\t\tcp866,KOI8_R = \t\t\tkoi8_ru,KOI8_R = \t\t\t>4.1.0 koi8r,TIS620 = \t\t\ttis620,Cp1250 = \t\t\tcp1250,Cp1250 = \t\t\twin1250,Cp1251 = \t\t\t*>4.1.0 cp1251,Cp1251 = \t\t\twin1251,Cp1251 = \t\t\tcp1251cias,Cp1251 = \t\t\tcp1251csas,Cp1256 = \t\t\tcp1256,Cp1251 = \t\t\twin1251ukr,Cp1252 =             latin1,Cp1257 = \t\t\tcp1257,MacRoman = \t\t\tmacroman,MacCentralEurope = \tmacce,UTF-8 = \t\tutf8,UnicodeBig = \tucs2,US-ASCII =\t\tbinary,Cp943 =        \tsjis,MS932 =\t\t\tsjis,MS932 =        \t>4.1.11 cp932,WINDOWS-31J =\tsjis,WINDOWS-31J = \t>4.1.11 cp932,CP932 =\t\t\tsjis,CP932 =\t\t\t*>4.1.11 cp932,SHIFT_JIS = \tsjis,ASCII =\t\t\tascii,LATIN5 =\t\tlatin5,LATIN7 =\t\tlatin7,HEBREW =\t\thebrew,GREEK =\t\t\tgreek,EUCKR =\t\t\teuckr,GB2312 =\t\tgb2312,LATIN2 =\t\tlatin2,UTF-16 = \t>5.2.0 utf16,UTF-32 = \t>5.2.0 utf32");
    
    HashMap javaToMysqlMap = new HashMap();
    
    populateMapWithKeyValuePairs("javaToMysqlMappings", javaToMysqlMap, true, false);
    
    JAVA_TO_MYSQL_CHARSET_MAP = Collections.unmodifiableMap(javaToMysqlMap);
    
    HashMap mysqlToJavaMap = new HashMap();
    
    Set keySet = JAVA_TO_MYSQL_CHARSET_MAP.keySet();
    
    Iterator javaCharsets = keySet.iterator();
    while (javaCharsets.hasNext())
    {
      Object javaEncodingName = javaCharsets.next();
      List mysqlEncodingList = (List)JAVA_TO_MYSQL_CHARSET_MAP.get(javaEncodingName);
      
      Iterator mysqlEncodings = mysqlEncodingList.iterator();
      
      String mysqlEncodingName = null;
      while (mysqlEncodings.hasNext())
      {
        VersionedStringProperty mysqlProp = (VersionedStringProperty)mysqlEncodings.next();
        
        mysqlEncodingName = mysqlProp.toString();
        
        mysqlToJavaMap.put(mysqlEncodingName, javaEncodingName);
        mysqlToJavaMap.put(mysqlEncodingName.toUpperCase(Locale.ENGLISH), javaEncodingName);
      }
    }
    mysqlToJavaMap.put("cp932", "Windows-31J");
    mysqlToJavaMap.put("CP932", "Windows-31J");
    
    MYSQL_TO_JAVA_CHARSET_MAP = Collections.unmodifiableMap(mysqlToJavaMap);
    
    TreeMap ucMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
    
    Iterator javaNamesKeys = JAVA_TO_MYSQL_CHARSET_MAP.keySet().iterator();
    while (javaNamesKeys.hasNext())
    {
      String key = (String)javaNamesKeys.next();
      
      ucMap.put(key.toUpperCase(Locale.ENGLISH), JAVA_TO_MYSQL_CHARSET_MAP.get(key));
    }
    JAVA_UC_TO_MYSQL_CHARSET_MAP = Collections.unmodifiableMap(ucMap);
    
    HashMap tempMapMulti = new HashMap();
    
    CHARSET_CONFIG.setProperty("multibyteCharsets", "Big5 = \t\t\tbig5,GBK = \t\t\tgbk,SJIS = \t\t\tsjis,EUC_CN = \t\tgb2312,EUC_JP = \t\tujis,EUC_JP_Solaris = eucjpms,EUC_KR = \t\teuc_kr,EUC_KR = \t\t>4.1.0 euckr,Cp943 =        \tsjis,Cp943 = \t\tcp943,WINDOWS-31J =\tsjis,WINDOWS-31J = \tcp932,CP932 =\t\t\tcp932,MS932 =\t\t\tsjis,MS932 =        \tcp932,SHIFT_JIS = \tsjis,EUCKR =\t\t\teuckr,GB2312 =\t\tgb2312,UTF-8 = \t\tutf8,utf8 =          utf8,UnicodeBig = \tucs2");
    
    populateMapWithKeyValuePairs("multibyteCharsets", tempMapMulti, false, true);
    
    MULTIBYTE_CHARSETS = Collections.unmodifiableMap(tempMapMulti);
    
    INDEX_TO_CHARSET = new String['ÿ'];
    try
    {
      INDEX_TO_CHARSET[1] = getJavaEncodingForMysqlEncoding("big5", null);
      INDEX_TO_CHARSET[2] = getJavaEncodingForMysqlEncoding("czech", null);
      INDEX_TO_CHARSET[3] = "ISO8859_1";
      INDEX_TO_CHARSET[4] = "ISO8859_1";
      INDEX_TO_CHARSET[5] = getJavaEncodingForMysqlEncoding("german1", null);
      
      INDEX_TO_CHARSET[6] = "ISO8859_1";
      INDEX_TO_CHARSET[7] = getJavaEncodingForMysqlEncoding("koi8_ru", null);
      
      INDEX_TO_CHARSET[8] = getJavaEncodingForMysqlEncoding("latin1", null);
      
      INDEX_TO_CHARSET[9] = getJavaEncodingForMysqlEncoding("latin2", null);
      
      INDEX_TO_CHARSET[10] = "ISO8859_1";
      INDEX_TO_CHARSET[11] = getJavaEncodingForMysqlEncoding("usa7", null);
      INDEX_TO_CHARSET[12] = getJavaEncodingForMysqlEncoding("ujis", null);
      INDEX_TO_CHARSET[13] = getJavaEncodingForMysqlEncoding("sjis", null);
      INDEX_TO_CHARSET[14] = getJavaEncodingForMysqlEncoding("cp1251", null);
      
      INDEX_TO_CHARSET[15] = getJavaEncodingForMysqlEncoding("danish", null);
      
      INDEX_TO_CHARSET[16] = getJavaEncodingForMysqlEncoding("hebrew", null);
      
      INDEX_TO_CHARSET[17] = "ISO8859_1";
      
      INDEX_TO_CHARSET[18] = getJavaEncodingForMysqlEncoding("tis620", null);
      
      INDEX_TO_CHARSET[19] = getJavaEncodingForMysqlEncoding("euc_kr", null);
      
      INDEX_TO_CHARSET[20] = getJavaEncodingForMysqlEncoding("estonia", null);
      
      INDEX_TO_CHARSET[21] = getJavaEncodingForMysqlEncoding("hungarian", null);
      
      INDEX_TO_CHARSET[22] = "KOI8_R";
      INDEX_TO_CHARSET[23] = getJavaEncodingForMysqlEncoding("win1251ukr", null);
      
      INDEX_TO_CHARSET[24] = getJavaEncodingForMysqlEncoding("gb2312", null);
      
      INDEX_TO_CHARSET[25] = getJavaEncodingForMysqlEncoding("greek", null);
      
      INDEX_TO_CHARSET[26] = getJavaEncodingForMysqlEncoding("win1250", null);
      
      INDEX_TO_CHARSET[27] = getJavaEncodingForMysqlEncoding("croat", null);
      
      INDEX_TO_CHARSET[28] = getJavaEncodingForMysqlEncoding("gbk", null);
      INDEX_TO_CHARSET[29] = getJavaEncodingForMysqlEncoding("cp1257", null);
      
      INDEX_TO_CHARSET[30] = getJavaEncodingForMysqlEncoding("latin5", null);
      
      INDEX_TO_CHARSET[31] = getJavaEncodingForMysqlEncoding("latin1_de", null);
      
      INDEX_TO_CHARSET[32] = "ISO8859_1";
      INDEX_TO_CHARSET[33] = getJavaEncodingForMysqlEncoding("utf8", null);
      INDEX_TO_CHARSET[34] = "Cp1250";
      INDEX_TO_CHARSET[35] = getJavaEncodingForMysqlEncoding("ucs2", null);
      INDEX_TO_CHARSET[36] = getJavaEncodingForMysqlEncoding("cp866", null);
      
      INDEX_TO_CHARSET[37] = "Cp895";
      INDEX_TO_CHARSET[38] = getJavaEncodingForMysqlEncoding("macce", null);
      
      INDEX_TO_CHARSET[39] = getJavaEncodingForMysqlEncoding("macroman", null);
      
      INDEX_TO_CHARSET[40] = "latin2";
      INDEX_TO_CHARSET[41] = getJavaEncodingForMysqlEncoding("latvian", null);
      
      INDEX_TO_CHARSET[42] = getJavaEncodingForMysqlEncoding("latvian1", null);
      
      INDEX_TO_CHARSET[43] = getJavaEncodingForMysqlEncoding("macce", null);
      
      INDEX_TO_CHARSET[44] = getJavaEncodingForMysqlEncoding("macce", null);
      
      INDEX_TO_CHARSET[45] = getJavaEncodingForMysqlEncoding("macce", null);
      
      INDEX_TO_CHARSET[46] = getJavaEncodingForMysqlEncoding("macce", null);
      
      INDEX_TO_CHARSET[47] = getJavaEncodingForMysqlEncoding("latin1", null);
      
      INDEX_TO_CHARSET[48] = getJavaEncodingForMysqlEncoding("latin1", null);
      
      INDEX_TO_CHARSET[49] = getJavaEncodingForMysqlEncoding("latin1", null);
      
      INDEX_TO_CHARSET[50] = getJavaEncodingForMysqlEncoding("cp1251", null);
      
      INDEX_TO_CHARSET[51] = getJavaEncodingForMysqlEncoding("cp1251", null);
      
      INDEX_TO_CHARSET[52] = getJavaEncodingForMysqlEncoding("cp1251", null);
      
      INDEX_TO_CHARSET[53] = getJavaEncodingForMysqlEncoding("macroman", null);
      
      INDEX_TO_CHARSET[54] = getJavaEncodingForMysqlEncoding("macroman", null);
      
      INDEX_TO_CHARSET[55] = getJavaEncodingForMysqlEncoding("macroman", null);
      
      INDEX_TO_CHARSET[56] = getJavaEncodingForMysqlEncoding("macroman", null);
      
      INDEX_TO_CHARSET[57] = getJavaEncodingForMysqlEncoding("cp1256", null);
      
      INDEX_TO_CHARSET[58] = "ISO8859_1";
      INDEX_TO_CHARSET[59] = "ISO8859_1";
      INDEX_TO_CHARSET[60] = "ISO8859_1";
      INDEX_TO_CHARSET[61] = "ISO8859_1";
      INDEX_TO_CHARSET[62] = "ISO8859_1";
      
      INDEX_TO_CHARSET[63] = getJavaEncodingForMysqlEncoding("binary", null);
      
      INDEX_TO_CHARSET[64] = "ISO8859_2";
      INDEX_TO_CHARSET[65] = getJavaEncodingForMysqlEncoding("ascii", null);
      
      INDEX_TO_CHARSET[66] = getJavaEncodingForMysqlEncoding("cp1250", null);
      
      INDEX_TO_CHARSET[67] = getJavaEncodingForMysqlEncoding("cp1256", null);
      
      INDEX_TO_CHARSET[68] = getJavaEncodingForMysqlEncoding("cp866", null);
      
      INDEX_TO_CHARSET[69] = "US-ASCII";
      INDEX_TO_CHARSET[70] = getJavaEncodingForMysqlEncoding("greek", null);
      
      INDEX_TO_CHARSET[71] = getJavaEncodingForMysqlEncoding("hebrew", null);
      
      INDEX_TO_CHARSET[72] = "US-ASCII";
      INDEX_TO_CHARSET[73] = "Cp895";
      INDEX_TO_CHARSET[74] = getJavaEncodingForMysqlEncoding("koi8r", null);
      
      INDEX_TO_CHARSET[75] = "KOI8_r";
      
      INDEX_TO_CHARSET[76] = "ISO8859_1";
      
      INDEX_TO_CHARSET[77] = getJavaEncodingForMysqlEncoding("latin2", null);
      
      INDEX_TO_CHARSET[78] = getJavaEncodingForMysqlEncoding("latin5", null);
      
      INDEX_TO_CHARSET[79] = getJavaEncodingForMysqlEncoding("latin7", null);
      
      INDEX_TO_CHARSET[80] = getJavaEncodingForMysqlEncoding("cp850", null);
      
      INDEX_TO_CHARSET[81] = getJavaEncodingForMysqlEncoding("cp852", null);
      
      INDEX_TO_CHARSET[82] = "ISO8859_1";
      INDEX_TO_CHARSET[83] = getJavaEncodingForMysqlEncoding("utf8", null);
      INDEX_TO_CHARSET[84] = getJavaEncodingForMysqlEncoding("big5", null);
      INDEX_TO_CHARSET[85] = getJavaEncodingForMysqlEncoding("euckr", null);
      
      INDEX_TO_CHARSET[86] = getJavaEncodingForMysqlEncoding("gb2312", null);
      
      INDEX_TO_CHARSET[87] = getJavaEncodingForMysqlEncoding("gbk", null);
      INDEX_TO_CHARSET[88] = getJavaEncodingForMysqlEncoding("sjis", null);
      INDEX_TO_CHARSET[89] = getJavaEncodingForMysqlEncoding("tis620", null);
      
      INDEX_TO_CHARSET[90] = getJavaEncodingForMysqlEncoding("ucs2", null);
      INDEX_TO_CHARSET[91] = getJavaEncodingForMysqlEncoding("ujis", null);
      INDEX_TO_CHARSET[92] = "US-ASCII";
      INDEX_TO_CHARSET[93] = "US-ASCII";
      INDEX_TO_CHARSET[94] = getJavaEncodingForMysqlEncoding("latin1", null);
      
      INDEX_TO_CHARSET[95] = getJavaEncodingForMysqlEncoding("cp932", null);
      
      INDEX_TO_CHARSET[96] = getJavaEncodingForMysqlEncoding("cp932", null);
      
      INDEX_TO_CHARSET[97] = getJavaEncodingForMysqlEncoding("eucjpms", null);
      
      INDEX_TO_CHARSET[98] = getJavaEncodingForMysqlEncoding("eucjpms", null);
      for (int i = 99; i < 128; i++) {
        INDEX_TO_CHARSET[i] = "ISO8859_1";
      }
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      
      INDEX_TO_CHARSET[''] = getJavaEncodingForMysqlEncoding("ucs2", null);
      for (int i = 147; i < 192; i++) {
        INDEX_TO_CHARSET[i] = "ISO8859_1";
      }
      INDEX_TO_CHARSET['À'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['Á'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['Â'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['Ã'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['Ä'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['Å'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['Æ'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['Ç'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['È'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['É'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['Ê'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['Ë'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['Ì'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['Í'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['Î'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['Ï'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['Ð'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['Ñ'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['Ò'] = getJavaEncodingForMysqlEncoding("utf8", null);
      
      INDEX_TO_CHARSET['Ó'] = getJavaEncodingForMysqlEncoding("utf8", null);
      for (int i = 212; i < 224; i++) {
        INDEX_TO_CHARSET[i] = "ISO8859_1";
      }
      for (int i = 224; i <= 243; i++) {
        INDEX_TO_CHARSET[i] = getJavaEncodingForMysqlEncoding("utf8", null);
      }
      for (int i = 101; i <= 120; i++) {
        INDEX_TO_CHARSET[i] = getJavaEncodingForMysqlEncoding("utf16", null);
      }
      for (int i = 160; i <= 179; i++) {
        INDEX_TO_CHARSET[i] = getJavaEncodingForMysqlEncoding("utf32", null);
      }
      for (int i = 244; i < 254; i++) {
        INDEX_TO_CHARSET[i] = "ISO8859_1";
      }
      INDEX_TO_CHARSET['þ'] = getJavaEncodingForMysqlEncoding("utf8", null);
      for (int i = 1; i < INDEX_TO_CHARSET.length; i++) {
        if (INDEX_TO_CHARSET[i] == null) {
          throw new RuntimeException("Assertion failure: No mapping from charset index " + i + " to a Java character set");
        }
      }
    }
    catch (SQLException sqlEx) {}
    INDEX_TO_COLLATION = new String['ÿ'];
    
    INDEX_TO_COLLATION[1] = "big5_chinese_ci";
    INDEX_TO_COLLATION[2] = "latin2_czech_cs";
    INDEX_TO_COLLATION[3] = "dec8_swedish_ci";
    INDEX_TO_COLLATION[4] = "cp850_general_ci";
    INDEX_TO_COLLATION[5] = "latin1_german1_ci";
    INDEX_TO_COLLATION[6] = "hp8_english_ci";
    INDEX_TO_COLLATION[7] = "koi8r_general_ci";
    INDEX_TO_COLLATION[8] = "latin1_swedish_ci";
    INDEX_TO_COLLATION[9] = "latin2_general_ci";
    INDEX_TO_COLLATION[10] = "swe7_swedish_ci";
    INDEX_TO_COLLATION[11] = "ascii_general_ci";
    INDEX_TO_COLLATION[12] = "ujis_japanese_ci";
    INDEX_TO_COLLATION[13] = "sjis_japanese_ci";
    INDEX_TO_COLLATION[14] = "cp1251_bulgarian_ci";
    INDEX_TO_COLLATION[15] = "latin1_danish_ci";
    INDEX_TO_COLLATION[16] = "hebrew_general_ci";
    INDEX_TO_COLLATION[18] = "tis620_thai_ci";
    INDEX_TO_COLLATION[19] = "euckr_korean_ci";
    INDEX_TO_COLLATION[20] = "latin7_estonian_cs";
    INDEX_TO_COLLATION[21] = "latin2_hungarian_ci";
    INDEX_TO_COLLATION[22] = "koi8u_general_ci";
    INDEX_TO_COLLATION[23] = "cp1251_ukrainian_ci";
    INDEX_TO_COLLATION[24] = "gb2312_chinese_ci";
    INDEX_TO_COLLATION[25] = "greek_general_ci";
    INDEX_TO_COLLATION[26] = "cp1250_general_ci";
    INDEX_TO_COLLATION[27] = "latin2_croatian_ci";
    INDEX_TO_COLLATION[28] = "gbk_chinese_ci";
    INDEX_TO_COLLATION[29] = "cp1257_lithuanian_ci";
    INDEX_TO_COLLATION[30] = "latin5_turkish_ci";
    INDEX_TO_COLLATION[31] = "latin1_german2_ci";
    INDEX_TO_COLLATION[32] = "armscii8_general_ci";
    INDEX_TO_COLLATION[33] = "utf8_general_ci";
    INDEX_TO_COLLATION[34] = "cp1250_czech_cs";
    INDEX_TO_COLLATION[35] = "ucs2_general_ci";
    INDEX_TO_COLLATION[36] = "cp866_general_ci";
    INDEX_TO_COLLATION[37] = "keybcs2_general_ci";
    INDEX_TO_COLLATION[38] = "macce_general_ci";
    INDEX_TO_COLLATION[39] = "macroman_general_ci";
    INDEX_TO_COLLATION[40] = "cp852_general_ci";
    INDEX_TO_COLLATION[41] = "latin7_general_ci";
    INDEX_TO_COLLATION[42] = "latin7_general_cs";
    INDEX_TO_COLLATION[43] = "macce_bin";
    INDEX_TO_COLLATION[44] = "cp1250_croatian_ci";
    INDEX_TO_COLLATION[47] = "latin1_bin";
    INDEX_TO_COLLATION[48] = "latin1_general_ci";
    INDEX_TO_COLLATION[49] = "latin1_general_cs";
    INDEX_TO_COLLATION[50] = "cp1251_bin";
    INDEX_TO_COLLATION[51] = "cp1251_general_ci";
    INDEX_TO_COLLATION[52] = "cp1251_general_cs";
    INDEX_TO_COLLATION[53] = "macroman_bin";
    INDEX_TO_COLLATION[57] = "cp1256_general_ci";
    INDEX_TO_COLLATION[58] = "cp1257_bin";
    INDEX_TO_COLLATION[59] = "cp1257_general_ci";
    INDEX_TO_COLLATION[63] = "binary";
    INDEX_TO_COLLATION[64] = "armscii8_bin";
    INDEX_TO_COLLATION[65] = "ascii_bin";
    INDEX_TO_COLLATION[66] = "cp1250_bin";
    INDEX_TO_COLLATION[67] = "cp1256_bin";
    INDEX_TO_COLLATION[68] = "cp866_bin";
    INDEX_TO_COLLATION[69] = "dec8_bin";
    INDEX_TO_COLLATION[70] = "greek_bin";
    INDEX_TO_COLLATION[71] = "hebrew_bin";
    INDEX_TO_COLLATION[72] = "hp8_bin";
    INDEX_TO_COLLATION[73] = "keybcs2_bin";
    INDEX_TO_COLLATION[74] = "koi8r_bin";
    INDEX_TO_COLLATION[75] = "koi8u_bin";
    INDEX_TO_COLLATION[77] = "latin2_bin";
    INDEX_TO_COLLATION[78] = "latin5_bin";
    INDEX_TO_COLLATION[79] = "latin7_bin";
    INDEX_TO_COLLATION[80] = "cp850_bin";
    INDEX_TO_COLLATION[81] = "cp852_bin";
    INDEX_TO_COLLATION[82] = "swe7_bin";
    INDEX_TO_COLLATION[83] = "utf8_bin";
    INDEX_TO_COLLATION[84] = "big5_bin";
    INDEX_TO_COLLATION[85] = "euckr_bin";
    INDEX_TO_COLLATION[86] = "gb2312_bin";
    INDEX_TO_COLLATION[87] = "gbk_bin";
    INDEX_TO_COLLATION[88] = "sjis_bin";
    INDEX_TO_COLLATION[89] = "tis620_bin";
    INDEX_TO_COLLATION[90] = "ucs2_bin";
    INDEX_TO_COLLATION[91] = "ujis_bin";
    INDEX_TO_COLLATION[92] = "geostd8_general_ci";
    INDEX_TO_COLLATION[93] = "geostd8_bin";
    INDEX_TO_COLLATION[94] = "latin1_spanish_ci";
    INDEX_TO_COLLATION[95] = "cp932_japanese_ci";
    INDEX_TO_COLLATION[96] = "cp932_bin";
    INDEX_TO_COLLATION[97] = "eucjpms_japanese_ci";
    INDEX_TO_COLLATION[98] = "eucjpms_bin";
    INDEX_TO_COLLATION[99] = "cp1250_polish_ci";
    INDEX_TO_COLLATION[''] = "ucs2_unicode_ci";
    INDEX_TO_COLLATION[''] = "ucs2_icelandic_ci";
    INDEX_TO_COLLATION[''] = "ucs2_latvian_ci";
    INDEX_TO_COLLATION[''] = "ucs2_romanian_ci";
    INDEX_TO_COLLATION[''] = "ucs2_slovenian_ci";
    INDEX_TO_COLLATION[''] = "ucs2_polish_ci";
    INDEX_TO_COLLATION[''] = "ucs2_estonian_ci";
    INDEX_TO_COLLATION[''] = "ucs2_spanish_ci";
    INDEX_TO_COLLATION[''] = "ucs2_swedish_ci";
    INDEX_TO_COLLATION[''] = "ucs2_turkish_ci";
    INDEX_TO_COLLATION[''] = "ucs2_czech_ci";
    INDEX_TO_COLLATION[''] = "ucs2_danish_ci";
    INDEX_TO_COLLATION[''] = "ucs2_lithuanian_ci ";
    INDEX_TO_COLLATION[''] = "ucs2_slovak_ci";
    INDEX_TO_COLLATION[''] = "ucs2_spanish2_ci";
    INDEX_TO_COLLATION[''] = "ucs2_roman_ci";
    INDEX_TO_COLLATION[''] = "ucs2_persian_ci";
    INDEX_TO_COLLATION[''] = "ucs2_esperanto_ci";
    INDEX_TO_COLLATION[''] = "ucs2_hungarian_ci";
    INDEX_TO_COLLATION['À'] = "utf8_unicode_ci";
    INDEX_TO_COLLATION['Á'] = "utf8_icelandic_ci";
    INDEX_TO_COLLATION['Â'] = "utf8_latvian_ci";
    INDEX_TO_COLLATION['Ã'] = "utf8_romanian_ci";
    INDEX_TO_COLLATION['Ä'] = "utf8_slovenian_ci";
    INDEX_TO_COLLATION['Å'] = "utf8_polish_ci";
    INDEX_TO_COLLATION['Æ'] = "utf8_estonian_ci";
    INDEX_TO_COLLATION['Ç'] = "utf8_spanish_ci";
    INDEX_TO_COLLATION['È'] = "utf8_swedish_ci";
    INDEX_TO_COLLATION['É'] = "utf8_turkish_ci";
    INDEX_TO_COLLATION['Ê'] = "utf8_czech_ci";
    INDEX_TO_COLLATION['Ë'] = "utf8_danish_ci";
    INDEX_TO_COLLATION['Ì'] = "utf8_lithuanian_ci ";
    INDEX_TO_COLLATION['Í'] = "utf8_slovak_ci";
    INDEX_TO_COLLATION['Î'] = "utf8_spanish2_ci";
    INDEX_TO_COLLATION['Ï'] = "utf8_roman_ci";
    INDEX_TO_COLLATION['Ð'] = "utf8_persian_ci";
    INDEX_TO_COLLATION['Ñ'] = "utf8_esperanto_ci";
    INDEX_TO_COLLATION['Ò'] = "utf8_hungarian_ci";
    
    INDEX_TO_COLLATION[33] = "utf8mb3_general_ci";
    INDEX_TO_COLLATION[83] = "utf8mb3_bin";
    INDEX_TO_COLLATION['À'] = "utf8mb3_unicode_ci";
    INDEX_TO_COLLATION['Á'] = "utf8mb3_icelandic_ci";
    INDEX_TO_COLLATION['Â'] = "utf8mb3_latvian_ci";
    INDEX_TO_COLLATION['Ã'] = "utf8mb3_romanian_ci";
    INDEX_TO_COLLATION['Ä'] = "utf8mb3_slovenian_ci";
    INDEX_TO_COLLATION['Å'] = "utf8mb3_polish_ci";
    INDEX_TO_COLLATION['Æ'] = "utf8mb3_estonian_ci";
    INDEX_TO_COLLATION['Ç'] = "utf8mb3_spanish_ci";
    INDEX_TO_COLLATION['È'] = "utf8mb3_swedish_ci";
    INDEX_TO_COLLATION['É'] = "utf8mb3_turkish_ci";
    INDEX_TO_COLLATION['Ê'] = "utf8mb3_czech_ci";
    INDEX_TO_COLLATION['Ë'] = "utf8mb3_danish_ci";
    INDEX_TO_COLLATION['Ì'] = "utf8mb3_lithuanian_ci";
    INDEX_TO_COLLATION['Í'] = "utf8mb3_slovak_ci";
    INDEX_TO_COLLATION['Î'] = "utf8mb3_spanish2_ci";
    INDEX_TO_COLLATION['Ï'] = "utf8mb3_roman_ci";
    INDEX_TO_COLLATION['Ð'] = "utf8mb3_persian_ci";
    INDEX_TO_COLLATION['Ñ'] = "utf8mb3_esperanto_ci";
    INDEX_TO_COLLATION['Ò'] = "utf8mb3_hungarian_ci";
    INDEX_TO_COLLATION['Ó'] = "utf8mb3_sinhala_ci";
    INDEX_TO_COLLATION['þ'] = "utf8mb3_general_cs";
    
    INDEX_TO_COLLATION[45] = "utf8_general_ci";
    INDEX_TO_COLLATION[46] = "utf8_bin";
    INDEX_TO_COLLATION['à'] = "utf8_unicode_ci";
    INDEX_TO_COLLATION['á'] = "utf8_icelandic_ci";
    INDEX_TO_COLLATION['â'] = "utf8_latvian_ci";
    INDEX_TO_COLLATION['ã'] = "utf8_romanian_ci";
    INDEX_TO_COLLATION['ä'] = "utf8_slovenian_ci";
    INDEX_TO_COLLATION['å'] = "utf8_polish_ci";
    INDEX_TO_COLLATION['æ'] = "utf8_estonian_ci";
    INDEX_TO_COLLATION['ç'] = "utf8_spanish_ci";
    INDEX_TO_COLLATION['è'] = "utf8_swedish_ci";
    INDEX_TO_COLLATION['é'] = "utf8_turkish_ci";
    INDEX_TO_COLLATION['ê'] = "utf8_czech_ci";
    INDEX_TO_COLLATION['ë'] = "utf8_danish_ci";
    INDEX_TO_COLLATION['ì'] = "utf8_lithuanian_ci";
    INDEX_TO_COLLATION['í'] = "utf8_slovak_ci";
    INDEX_TO_COLLATION['î'] = "utf8_spanish2_ci";
    INDEX_TO_COLLATION['ï'] = "utf8_roman_ci";
    INDEX_TO_COLLATION['ð'] = "utf8_persian_ci";
    INDEX_TO_COLLATION['ñ'] = "utf8_esperanto_ci";
    INDEX_TO_COLLATION['ò'] = "utf8_hungarian_ci";
    INDEX_TO_COLLATION['ó'] = "utf8_sinhala_ci";
    
    INDEX_TO_COLLATION[54] = "utf16_general_ci";
    INDEX_TO_COLLATION[55] = "utf16_bin";
    INDEX_TO_COLLATION[101] = "utf16_unicode_ci";
    INDEX_TO_COLLATION[102] = "utf16_icelandic_ci";
    INDEX_TO_COLLATION[103] = "utf16_latvian_ci";
    INDEX_TO_COLLATION[104] = "utf16_romanian_ci";
    INDEX_TO_COLLATION[105] = "utf16_slovenian_ci";
    INDEX_TO_COLLATION[106] = "utf16_polish_ci";
    INDEX_TO_COLLATION[107] = "utf16_estonian_ci";
    INDEX_TO_COLLATION[108] = "utf16_spanish_ci";
    INDEX_TO_COLLATION[109] = "utf16_swedish_ci";
    INDEX_TO_COLLATION[110] = "utf16_turkish_ci";
    INDEX_TO_COLLATION[111] = "utf16_czech_ci";
    INDEX_TO_COLLATION[112] = "utf16_danish_ci";
    INDEX_TO_COLLATION[113] = "utf16_lithuanian_ci";
    INDEX_TO_COLLATION[114] = "utf16_slovak_ci";
    INDEX_TO_COLLATION[115] = "utf16_spanish2_ci";
    INDEX_TO_COLLATION[116] = "utf16_roman_ci";
    INDEX_TO_COLLATION[117] = "utf16_persian_ci";
    INDEX_TO_COLLATION[118] = "utf16_esperanto_ci";
    INDEX_TO_COLLATION[119] = "utf16_hungarian_ci";
    INDEX_TO_COLLATION[120] = "utf16_sinhala_ci";
    
    INDEX_TO_COLLATION[60] = "utf32_general_ci";
    INDEX_TO_COLLATION[61] = "utf32_bin";
    INDEX_TO_COLLATION[' '] = "utf32_unicode_ci";
    INDEX_TO_COLLATION['¡'] = "utf32_icelandic_ci";
    INDEX_TO_COLLATION['¢'] = "utf32_latvian_ci";
    INDEX_TO_COLLATION['£'] = "utf32_romanian_ci";
    INDEX_TO_COLLATION['¤'] = "utf32_slovenian_ci";
    INDEX_TO_COLLATION['¥'] = "utf32_polish_ci";
    INDEX_TO_COLLATION['¦'] = "utf32_estonian_ci";
    INDEX_TO_COLLATION['§'] = "utf32_spanish_ci";
    INDEX_TO_COLLATION['¨'] = "utf32_swedish_ci";
    INDEX_TO_COLLATION['©'] = "utf32_turkish_ci";
    INDEX_TO_COLLATION['ª'] = "utf32_czech_ci";
    INDEX_TO_COLLATION['«'] = "utf32_danish_ci";
    INDEX_TO_COLLATION['¬'] = "utf32_lithuanian_ci";
    INDEX_TO_COLLATION['­'] = "utf32_slovak_ci";
    INDEX_TO_COLLATION['®'] = "utf32_spanish2_ci";
    INDEX_TO_COLLATION['¯'] = "utf32_roman_ci";
    INDEX_TO_COLLATION['°'] = "utf32_persian_ci";
    INDEX_TO_COLLATION['±'] = "utf32_esperanto_ci";
    INDEX_TO_COLLATION['²'] = "utf32_hungarian_ci";
    INDEX_TO_COLLATION['³'] = "utf32_sinhala_ci";
    
    Map indexMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
    for (int i = 0; i < INDEX_TO_CHARSET.length; i++)
    {
      String mysqlEncodingName = INDEX_TO_CHARSET[i];
      if (mysqlEncodingName != null) {
        indexMap.put(INDEX_TO_CHARSET[i], Constants.integerValueOf(i));
      }
    }
    MYSQL_ENCODING_NAME_TO_CHARSET_INDEX_MAP = Collections.unmodifiableMap(indexMap);
    
    Map tempMap = new HashMap();
    
    tempMap.put("czech", "latin2");
    tempMap.put("danish", "latin1");
    tempMap.put("dutch", "latin1");
    tempMap.put("english", "latin1");
    tempMap.put("estonian", "latin7");
    tempMap.put("french", "latin1");
    tempMap.put("german", "latin1");
    tempMap.put("greek", "greek");
    tempMap.put("hungarian", "latin2");
    tempMap.put("italian", "latin1");
    tempMap.put("japanese", "ujis");
    tempMap.put("japanese-sjis", "sjis");
    tempMap.put("korean", "euckr");
    tempMap.put("norwegian", "latin1");
    tempMap.put("norwegian-ny", "latin1");
    tempMap.put("polish", "latin2");
    tempMap.put("portuguese", "latin1");
    tempMap.put("romanian", "latin2");
    tempMap.put("russian", "koi8r");
    tempMap.put("serbian", "cp1250");
    tempMap.put("slovak", "latin2");
    tempMap.put("spanish", "latin1");
    tempMap.put("swedish", "latin1");
    tempMap.put("ukrainian", "koi8u");
    
    ERROR_MESSAGE_FILE_TO_MYSQL_CHARSET_MAP = Collections.unmodifiableMap(tempMap);
  }
  
  public static final String getJavaEncodingForMysqlEncoding(String mysqlEncoding, Connection conn)
    throws SQLException
  {
    if ((conn != null) && (conn.versionMeetsMinimum(4, 1, 0)) && ("latin1".equalsIgnoreCase(mysqlEncoding))) {
      return "Cp1252";
    }
    return (String)MYSQL_TO_JAVA_CHARSET_MAP.get(mysqlEncoding);
  }
  
  public static final String getMysqlEncodingForJavaEncoding(String javaEncodingUC, Connection conn)
    throws SQLException
  {
    List mysqlEncodings = (List)JAVA_UC_TO_MYSQL_CHARSET_MAP.get(javaEncodingUC);
    if (mysqlEncodings != null)
    {
      Iterator iter = mysqlEncodings.iterator();
      
      VersionedStringProperty versionedProp = null;
      while (iter.hasNext())
      {
        VersionedStringProperty propToCheck = (VersionedStringProperty)iter.next();
        if (conn == null) {
          return propToCheck.toString();
        }
        if ((versionedProp != null) && (!versionedProp.preferredValue) && 
          (versionedProp.majorVersion == propToCheck.majorVersion) && (versionedProp.minorVersion == propToCheck.minorVersion) && (versionedProp.subminorVersion == propToCheck.subminorVersion)) {
          return versionedProp.toString();
        }
        if (!propToCheck.isOkayForVersion(conn)) {
          break;
        }
        if (propToCheck.preferredValue) {
          return propToCheck.toString();
        }
        versionedProp = propToCheck;
      }
      if (versionedProp != null) {
        return versionedProp.toString();
      }
    }
    return null;
  }
  
  static final int getNumberOfCharsetsConfigured()
  {
    return MYSQL_TO_JAVA_CHARSET_MAP.size() / 2;
  }
  
  static final String getCharacterEncodingForErrorMessages(ConnectionImpl conn)
    throws SQLException
  {
    String errorMessageFile = conn.getServerVariable("language");
    if ((errorMessageFile == null) || (errorMessageFile.length() == 0)) {
      return "Cp1252";
    }
    int endWithoutSlash = errorMessageFile.length();
    if ((errorMessageFile.endsWith("/")) || (errorMessageFile.endsWith("\\"))) {
      endWithoutSlash--;
    }
    int lastSlashIndex = errorMessageFile.lastIndexOf('/', endWithoutSlash - 1);
    if (lastSlashIndex == -1) {
      lastSlashIndex = errorMessageFile.lastIndexOf('\\', endWithoutSlash - 1);
    }
    if (lastSlashIndex == -1) {
      lastSlashIndex = 0;
    }
    if ((lastSlashIndex == endWithoutSlash) || (endWithoutSlash < lastSlashIndex)) {
      return "Cp1252";
    }
    errorMessageFile = errorMessageFile.substring(lastSlashIndex + 1, endWithoutSlash);
    
    String errorMessageEncodingMysql = (String)ERROR_MESSAGE_FILE_TO_MYSQL_CHARSET_MAP.get(errorMessageFile);
    if (errorMessageEncodingMysql == null) {
      return "Cp1252";
    }
    String javaEncoding = getJavaEncodingForMysqlEncoding(errorMessageEncodingMysql, conn);
    if (javaEncoding == null) {
      return "Cp1252";
    }
    return javaEncoding;
  }
  
  static final boolean isAliasForSjis(String encoding)
  {
    return ("SJIS".equalsIgnoreCase(encoding)) || ("WINDOWS-31J".equalsIgnoreCase(encoding)) || ("MS932".equalsIgnoreCase(encoding)) || ("SHIFT_JIS".equalsIgnoreCase(encoding)) || ("CP943".equalsIgnoreCase(encoding));
  }
  
  static final boolean isMultibyteCharset(String javaEncodingName)
  {
    String javaEncodingNameUC = javaEncodingName.toUpperCase(Locale.ENGLISH);
    
    return MULTIBYTE_CHARSETS.containsKey(javaEncodingNameUC);
  }
  
  private static void populateMapWithKeyValuePairs(String configKey, Map mapToPopulate, boolean addVersionedProperties, boolean addUppercaseKeys)
  {
    String javaToMysqlConfig = CHARSET_CONFIG.getProperty(configKey);
    if (javaToMysqlConfig != null)
    {
      List mappings = StringUtils.split(javaToMysqlConfig, ",", true);
      if (mappings != null)
      {
        Iterator mappingsIter = mappings.iterator();
        while (mappingsIter.hasNext())
        {
          String aMapping = (String)mappingsIter.next();
          
          List parsedPair = StringUtils.split(aMapping, "=", true);
          if (parsedPair.size() == 2)
          {
            String key = parsedPair.get(0).toString();
            String value = parsedPair.get(1).toString();
            if (addVersionedProperties)
            {
              List versionedProperties = (List)mapToPopulate.get(key);
              if (versionedProperties == null)
              {
                versionedProperties = new ArrayList();
                mapToPopulate.put(key, versionedProperties);
              }
              VersionedStringProperty verProp = new VersionedStringProperty(value);
              
              versionedProperties.add(verProp);
              if (addUppercaseKeys)
              {
                String keyUc = key.toUpperCase(Locale.ENGLISH);
                
                versionedProperties = (List)mapToPopulate.get(keyUc);
                if (versionedProperties == null)
                {
                  versionedProperties = new ArrayList();
                  mapToPopulate.put(keyUc, versionedProperties);
                }
                versionedProperties.add(verProp);
              }
            }
            else
            {
              mapToPopulate.put(key, value);
              if (addUppercaseKeys) {
                mapToPopulate.put(key.toUpperCase(Locale.ENGLISH), value);
              }
            }
          }
          else
          {
            throw new RuntimeException("Syntax error in Charsets.properties resource for token \"" + aMapping + "\".");
          }
        }
      }
      else
      {
        throw new RuntimeException("Missing/corrupt entry for \"" + configKey + "\" in Charsets.properties.");
      }
    }
    else
    {
      throw new RuntimeException("Could not find configuration value \"" + configKey + "\" in Charsets.properties resource");
    }
  }
  
  public static int getCharsetIndexForMysqlEncodingName(String name)
  {
    if (name == null) {
      return 0;
    }
    Integer asInt = (Integer)MYSQL_ENCODING_NAME_TO_CHARSET_INDEX_MAP.get(name);
    if (asInt == null) {
      return 0;
    }
    return asInt.intValue();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\CharsetMapping.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */