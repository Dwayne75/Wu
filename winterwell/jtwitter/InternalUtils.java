package winterwell.jtwitter;

import com.winterwell.jgeoplanet.IPlace;
import com.winterwell.jgeoplanet.MFloat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import winterwell.json.JSONException;
import winterwell.json.JSONObject;

public class InternalUtils
{
  public static <P extends IPlace> P prefer(List<P> places, String prefType, MFloat confidence, float baseConfidence)
  {
    assert (places.size() != 0);
    assert ((baseConfidence >= 0.0F) && (baseConfidence <= 1.0F));
    
    List cities = new ArrayList();
    for (IPlace place : places) {
      if (prefType.equals(place.getType())) {
        cities.add(place);
      }
    }
    if ((cities.size() != 0) && (cities.size() != places.size()))
    {
      if (confidence != null)
      {
        float conf = 0.95F * baseConfidence / cities.size();
        confidence.value = conf;
      }
      places = cities;
    }
    else if (confidence != null)
    {
      confidence.set(baseConfidence / places.size());
    }
    return (IPlace)places.get(0);
  }
  
  public static String stripUrls(String text)
  {
    return Regex.VALID_URL.matcher(text).replaceAll("");
  }
  
  public static final Pattern TAG_REGEX = Pattern.compile("<!?/?[\\[\\-a-zA-Z][^>]*>", 32);
  static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
  static final DateFormat dfMarko = new SimpleDateFormat(
    "EEE MMM dd HH:mm:ss ZZZZZ yyyy");
  public static final Pattern latLongLocn = Pattern.compile("(\\S+:)?\\s*(-?[\\d\\.]+)\\s*,\\s*(-?[\\d\\.]+)");
  static final Comparator<Status> NEWEST_FIRST = new Comparator()
  {
    public int compare(Status o1, Status o2)
    {
      return -o1.id.compareTo(o2.id);
    }
  };
  public static final Pattern REGEX_JUST_DIGITS = Pattern.compile("\\d+");
  /**
   * @deprecated
   */
  static final Pattern URL_REGEX = Pattern.compile("[hf]tt?ps?://[a-zA-Z0-9_%\\-\\.,\\?&\\/=\\+'~#!\\*:]+[a-zA-Z0-9_%\\-&\\/=\\+]");
  static ConcurrentHashMap<String, Long> usage;
  
  public static Map asMap(Object... keyValuePairs)
  {
    assert (keyValuePairs.length % 2 == 0);
    Map m = new HashMap(keyValuePairs.length / 2);
    for (int i = 0; i < keyValuePairs.length; i += 2)
    {
      Object v = keyValuePairs[(i + 1)];
      if (v != null) {
        m.put(keyValuePairs[i], v);
      }
    }
    return m;
  }
  
  /* Error */
  public static void close(java.io.OutputStream output)
  {
    // Byte code:
    //   0: aload_0
    //   1: ifnonnull +4 -> 5
    //   4: return
    //   5: aload_0
    //   6: invokevirtual 225	java/io/OutputStream:flush	()V
    //   9: goto +26 -> 35
    //   12: astore_1
    //   13: aload_0
    //   14: invokevirtual 230	java/io/OutputStream:close	()V
    //   17: goto +26 -> 43
    //   20: astore_3
    //   21: goto +22 -> 43
    //   24: astore_2
    //   25: aload_0
    //   26: invokevirtual 230	java/io/OutputStream:close	()V
    //   29: goto +4 -> 33
    //   32: astore_3
    //   33: aload_2
    //   34: athrow
    //   35: aload_0
    //   36: invokevirtual 230	java/io/OutputStream:close	()V
    //   39: goto +4 -> 43
    //   42: astore_3
    //   43: return
    // Line number table:
    //   Java source line #156	-> byte code offset #0
    //   Java source line #157	-> byte code offset #4
    //   Java source line #160	-> byte code offset #5
    //   Java source line #161	-> byte code offset #9
    //   Java source line #166	-> byte code offset #13
    //   Java source line #167	-> byte code offset #17
    //   Java source line #163	-> byte code offset #24
    //   Java source line #166	-> byte code offset #25
    //   Java source line #167	-> byte code offset #29
    //   Java source line #170	-> byte code offset #33
    //   Java source line #166	-> byte code offset #35
    //   Java source line #167	-> byte code offset #39
    //   Java source line #171	-> byte code offset #43
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	44	0	output	java.io.OutputStream
    //   12	1	1	localException	Exception
    //   24	10	2	localObject	Object
    //   20	1	3	localIOException	IOException
    //   32	1	3	localIOException1	IOException
    //   42	1	3	localIOException2	IOException
    // Exception table:
    //   from	to	target	type
    //   5	9	12	java/lang/Exception
    //   13	17	20	java/io/IOException
    //   5	13	24	finally
    //   25	29	32	java/io/IOException
    //   35	39	42	java/io/IOException
  }
  
  public static void close(InputStream input)
  {
    if (input == null) {
      return;
    }
    try
    {
      input.close();
    }
    catch (IOException localIOException) {}
  }
  
  static void count(String url)
  {
    if (usage == null) {
      return;
    }
    int i = url.indexOf("?");
    if (i != -1) {
      url = url.substring(0, i);
    }
    i = url.indexOf("/1/");
    if (i != -1) {
      url = url.substring(i + 3);
    }
    url = url.replaceAll("\\d+", "");
    for (int j = 0; j < 100; j++)
    {
      Long v = (Long)usage.get(url);
      boolean done;
      boolean done;
      if (v == null)
      {
        Long old = (Long)usage.putIfAbsent(url, Long.valueOf(1L));
        done = old == null;
      }
      else
      {
        long nv = v.longValue() + 1L;
        done = usage.replace(url, v, Long.valueOf(nv));
      }
      if (done) {
        break;
      }
    }
  }
  
  static String encode(Object x)
  {
    try
    {
      encd = URLEncoder.encode(String.valueOf(x), "UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      String encd;
      encd = URLEncoder.encode(String.valueOf(x));
    }
    String encd = encd.replace("*", "%2A");
    return encd.replace("+", "%20");
  }
  
  public static ConcurrentHashMap<String, Long> getAPIUsageStats()
  {
    return usage;
  }
  
  public static Date getDate(int year, String month, int day)
  {
    try
    {
      Field field = GregorianCalendar.class.getField(month.toUpperCase());
      int m = field.getInt(null);
      Calendar date = new GregorianCalendar(year, m, day);
      return date.getTime();
    }
    catch (Exception x)
    {
      throw new IllegalArgumentException(x.getMessage());
    }
  }
  
  static Boolean getOptBoolean(JSONObject obj, String key)
    throws JSONException
  {
    Object o = obj.opt(key);
    if ((o == null) || (o.equals(JSONObject.NULL))) {
      return null;
    }
    if ((o instanceof Boolean)) {
      return (Boolean)o;
    }
    if ((o instanceof String))
    {
      String os = (String)o;
      if (os.equalsIgnoreCase("true")) {
        return Boolean.valueOf(true);
      }
      if (os.equalsIgnoreCase("false")) {
        return Boolean.valueOf(false);
      }
    }
    if ((o instanceof Integer))
    {
      int oi = ((Integer)o).intValue();
      if (oi == 1) {
        return Boolean.valueOf(true);
      }
      if ((oi == 0) || (oi == -1)) {
        return Boolean.valueOf(false);
      }
    }
    System.err.println("JSON parse fail: " + o + " (" + key + ") is not boolean");
    return null;
  }
  
  static String join(List screenNamesOrIds, int first, int last)
  {
    StringBuilder names = new StringBuilder();
    int si = first;
    for (int n = Math.min(last, screenNamesOrIds.size()); si < n; si++)
    {
      names.append(screenNamesOrIds.get(si));
      names.append(",");
    }
    if (names.length() != 0) {
      names.delete(names.length() - 1, names.length());
    }
    return names.toString();
  }
  
  public static String join(String[] screenNames)
  {
    StringBuilder names = new StringBuilder();
    int si = 0;
    for (int n = screenNames.length; si < n; si++)
    {
      names.append(screenNames[si]);
      names.append(",");
    }
    if (names.length() != 0) {
      names.delete(names.length() - 1, names.length());
    }
    return names.toString();
  }
  
  protected static String jsonGet(String key, JSONObject jsonObj)
  {
    assert (key != null) : jsonObj;
    assert (jsonObj != null);
    Object val = jsonObj.opt(key);
    if (val == null) {
      return null;
    }
    if (JSONObject.NULL.equals(val)) {
      return null;
    }
    String s = val.toString();
    return s;
  }
  
  static Date parseDate(String c)
  {
    if (REGEX_JUST_DIGITS.matcher(c).matches()) {
      return new Date(Long.valueOf(c).longValue());
    }
    try
    {
      return new Date(c);
    }
    catch (Exception e)
    {
      try
      {
        return dfMarko.parse(c);
      }
      catch (ParseException e1)
      {
        throw new TwitterException.Parsing(c, e1);
      }
    }
  }
  
  public static void setTrackAPIUsage(boolean on)
  {
    if (!on)
    {
      usage = null;
      return;
    }
    if (usage != null) {
      return;
    }
    usage = new ConcurrentHashMap();
  }
  
  private static final Charset UTF_8 = Charset.forName("UTF-8");
  
  protected static String read(InputStream inputStream)
  {
    try
    {
      Reader reader = new InputStreamReader(inputStream, UTF_8);
      reader = new BufferedReader(reader);
      StringBuilder output = new StringBuilder();
      for (;;)
      {
        int c = reader.read();
        if (c == -1) {
          break;
        }
        output.append((char)c);
      }
      return output.toString();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      close(inputStream);
    }
  }
  
  static String unencode(String text)
  {
    if (text == null) {
      return null;
    }
    text = text.replace("&quot;", "\"");
    text = text.replace("&apos;", "'");
    text = text.replace("&nbsp;", " ");
    text = text.replace("&amp;", "&");
    text = text.replace("&gt;", ">");
    text = text.replace("&lt;", "<");
    if (text.indexOf(0) != -1) {
      text = text.replace('\000', ' ').trim();
    }
    return text;
  }
  
  static URI URI(String uri)
  {
    try
    {
      return new URI(uri);
    }
    catch (URISyntaxException e) {}
    return null;
  }
  
  static User user(String json)
  {
    try
    {
      JSONObject obj = new JSONObject(json);
      return new User(obj, null);
    }
    catch (JSONException e)
    {
      throw new TwitterException(e);
    }
  }
  
  public static String stripTags(String xml)
  {
    if (xml == null) {
      return null;
    }
    if (xml.indexOf('<') == -1) {
      return xml;
    }
    Matcher m4 = pScriptOrStyle.matcher(xml);
    xml = m4.replaceAll("");
    
    Matcher m2 = pComment.matcher(xml);
    String txt = m2.replaceAll("");
    
    Matcher m = TAG_REGEX.matcher(txt);
    String txt2 = m.replaceAll("");
    Matcher m3 = pDocType.matcher(txt2);
    String txt3 = m3.replaceAll("");
    return txt3;
  }
  
  public static final Pattern pComment = Pattern.compile("<!-*.*?-+>", 32);
  public static final Pattern pScriptOrStyle = Pattern.compile("<(script|style)[^<>]*>.+?</(script|style)>", 34);
  public static final Pattern pDocType = Pattern.compile("<!DOCTYPE.*?>", 34);
  
  public static void sleep(long msecs)
  {
    try
    {
      Thread.sleep(msecs);
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  static boolean authoriseIn11(Twitter jtwit)
  {
    return (jtwit.getHttpClient().canAuthenticate()) || 
      (jtwit.TWITTER_URL.endsWith("1.1"));
  }
  
  public static BigInteger getMinId(BigInteger maxId, List<? extends Twitter.ITweet> stati)
  {
    BigInteger min = maxId;
    for (Twitter.ITweet s : stati) {
      if ((min == null) || (min.compareTo(s.getId()) > 0)) {
        min = s.getId();
      }
    }
    if (min != null) {
      min = min.subtract(BigInteger.ONE);
    }
    return min;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\InternalUtils.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */