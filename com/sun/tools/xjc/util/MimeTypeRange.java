package com.sun.tools.xjc.util;

import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

public class MimeTypeRange
{
  public final String majorType;
  public final String subType;
  public final Map<String, String> parameters = new HashMap();
  public final float q;
  
  public static List<MimeTypeRange> parseRanges(String s)
    throws ParseException
  {
    StringCutter cutter = new StringCutter(s, true);
    List<MimeTypeRange> r = new ArrayList();
    while (cutter.length() > 0) {
      r.add(new MimeTypeRange(cutter));
    }
    return r;
  }
  
  public MimeTypeRange(String s)
    throws ParseException
  {
    this(new StringCutter(s, true));
  }
  
  private static MimeTypeRange create(String s)
  {
    try
    {
      return new MimeTypeRange(s);
    }
    catch (ParseException e)
    {
      throw new Error(e);
    }
  }
  
  private MimeTypeRange(StringCutter cutter)
    throws ParseException
  {
    this.majorType = cutter.until("/");
    cutter.next("/");
    this.subType = cutter.until("[;,]");
    
    float q = 1.0F;
    while (cutter.length() > 0)
    {
      String sep = cutter.next("[;,]");
      if (sep.equals(",")) {
        break;
      }
      String key = cutter.until("=");
      cutter.next("=");
      
      char ch = cutter.peek();
      String value;
      if (ch == '"')
      {
        cutter.next("\"");
        String value = cutter.until("\"");
        cutter.next("\"");
      }
      else
      {
        value = cutter.until("[;,]");
      }
      if (key.equals("q")) {
        q = Float.parseFloat(value);
      } else {
        this.parameters.put(key, value);
      }
    }
    this.q = q;
  }
  
  public MimeType toMimeType()
    throws MimeTypeParseException
  {
    return new MimeType(toString());
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder(this.majorType + '/' + this.subType);
    if (this.q != 1.0F) {
      sb.append("; q=").append(this.q);
    }
    for (Map.Entry<String, String> p : this.parameters.entrySet()) {
      sb.append("; ").append((String)p.getKey()).append('=').append((String)p.getValue());
    }
    return sb.toString();
  }
  
  public static final MimeTypeRange ALL = create("*/*");
  
  public static MimeTypeRange merge(Collection<MimeTypeRange> types)
  {
    if (types.size() == 0) {
      throw new IllegalArgumentException();
    }
    if (types.size() == 1) {
      return (MimeTypeRange)types.iterator().next();
    }
    String majorType = null;
    for (MimeTypeRange mt : types)
    {
      if (majorType == null) {
        majorType = mt.majorType;
      }
      if (!majorType.equals(mt.majorType)) {
        return ALL;
      }
    }
    return create(majorType + "/*");
  }
  
  public static void main(String[] args)
    throws ParseException
  {
    for (MimeTypeRange m : parseRanges(args[0])) {
      System.out.println(m.toString());
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\util\MimeTypeRange.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */