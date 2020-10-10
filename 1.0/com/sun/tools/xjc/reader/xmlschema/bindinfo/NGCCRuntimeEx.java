package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.reader.TypeUtil;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.parser.NGCCRuntime;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

public final class NGCCRuntimeEx
  extends NGCCRuntime
{
  public final JCodeModel codeModel;
  public final ErrorHandler errorHandler;
  public final Options options;
  public BindInfo currentBindInfo;
  static final String ERR_UNIMPLEMENTED = "NGCCRuntimeEx.Unimplemented";
  static final String ERR_UNSUPPORTED = "NGCCRuntimeEx.Unsupported";
  static final String ERR_UNDEFINED_PREFIX = "NGCCRuntimeEx.UndefinedPrefix";
  
  public NGCCRuntimeEx(JCodeModel _codeModel, Options opts, ErrorHandler _errorHandler)
  {
    this.codeModel = _codeModel;
    this.options = opts;
    this.errorHandler = _errorHandler;
  }
  
  public final JType getType(String typeName)
    throws SAXException
  {
    return TypeUtil.getType(this.codeModel, typeName, this.errorHandler, getLocator());
  }
  
  public final Locator copyLocator()
  {
    return new LocatorImpl(super.getLocator());
  }
  
  public final String truncateDocComment(String s)
  {
    StringBuffer buf = new StringBuffer(s.length());
    StringTokenizer tokens = new StringTokenizer(s, "\n");
    while (tokens.hasMoreTokens())
    {
      buf.append(tokens.nextToken().trim());
      if (tokens.hasMoreTokens()) {
        buf.append('\n');
      }
    }
    return buf.toString();
  }
  
  public final String escapeMarkup(String s)
  {
    StringBuffer buf = new StringBuffer(s.length());
    for (int i = 0; i < s.length(); i++)
    {
      char ch = s.charAt(i);
      switch (ch)
      {
      case '<': 
        buf.append("&lt;");
        break;
      case '&': 
        buf.append("&amp;");
        break;
      default: 
        buf.append(ch);
      }
    }
    return buf.toString();
  }
  
  public final boolean parseBoolean(String str)
  {
    str = str.trim();
    if ((str.equals("true")) || (str.equals("1"))) {
      return true;
    }
    return false;
  }
  
  public final QName parseQName(String str)
    throws SAXException
  {
    int idx = str.indexOf(':');
    if (idx < 0)
    {
      String uri = resolveNamespacePrefix("");
      
      return new QName(uri, str);
    }
    String prefix = str.substring(0, idx);
    String uri = resolveNamespacePrefix(prefix);
    if (uri == null)
    {
      this.errorHandler.error(new SAXParseException(Messages.format("NGCCRuntimeEx.UndefinedPrefix", prefix), getLocator()));
      
      uri = "undefined";
    }
    return new QName(uri, str.substring(idx + 1));
  }
  
  public void reportUnimplementedFeature(String name)
    throws SAXException
  {
    this.errorHandler.warning(new SAXParseException(Messages.format("NGCCRuntimeEx.Unimplemented", name), getLocator()));
  }
  
  public void reportUnsupportedFeature(String name)
    throws SAXException
  {
    this.errorHandler.warning(new SAXParseException(Messages.format("NGCCRuntimeEx.Unsupported", name), getLocator()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\NGCCRuntimeEx.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */