package javax.mail.internet;

import com.sun.mail.util.PropUtil;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import javax.mail.Address;
import javax.mail.Session;

public class InternetAddress
  extends Address
  implements Cloneable
{
  protected String address;
  protected String personal;
  protected String encodedPersonal;
  private static final long serialVersionUID = -7507595530758302903L;
  private static final boolean ignoreBogusGroupName = PropUtil.getBooleanSystemProperty("mail.mime.address.ignorebogusgroupname", true);
  
  public InternetAddress() {}
  
  public InternetAddress(String address)
    throws AddressException
  {
    InternetAddress[] a = parse(address, true);
    if (a.length != 1) {
      throw new AddressException("Illegal address", address);
    }
    this.address = a[0].address;
    this.personal = a[0].personal;
    this.encodedPersonal = a[0].encodedPersonal;
  }
  
  public InternetAddress(String address, boolean strict)
    throws AddressException
  {
    this(address);
    if (strict) {
      if (isGroup()) {
        getGroup(true);
      } else {
        checkAddress(this.address, true, true);
      }
    }
  }
  
  public InternetAddress(String address, String personal)
    throws UnsupportedEncodingException
  {
    this(address, personal, null);
  }
  
  public InternetAddress(String address, String personal, String charset)
    throws UnsupportedEncodingException
  {
    this.address = address;
    setPersonal(personal, charset);
  }
  
  public Object clone()
  {
    InternetAddress a = null;
    try
    {
      a = (InternetAddress)super.clone();
    }
    catch (CloneNotSupportedException e) {}
    return a;
  }
  
  public String getType()
  {
    return "rfc822";
  }
  
  public void setAddress(String address)
  {
    this.address = address;
  }
  
  public void setPersonal(String name, String charset)
    throws UnsupportedEncodingException
  {
    this.personal = name;
    if (name != null) {
      this.encodedPersonal = MimeUtility.encodeWord(name, charset, null);
    } else {
      this.encodedPersonal = null;
    }
  }
  
  public void setPersonal(String name)
    throws UnsupportedEncodingException
  {
    this.personal = name;
    if (name != null) {
      this.encodedPersonal = MimeUtility.encodeWord(name);
    } else {
      this.encodedPersonal = null;
    }
  }
  
  public String getAddress()
  {
    return this.address;
  }
  
  public String getPersonal()
  {
    if (this.personal != null) {
      return this.personal;
    }
    if (this.encodedPersonal != null) {
      try
      {
        this.personal = MimeUtility.decodeText(this.encodedPersonal);
        return this.personal;
      }
      catch (Exception ex)
      {
        return this.encodedPersonal;
      }
    }
    return null;
  }
  
  public String toString()
  {
    if ((this.encodedPersonal == null) && (this.personal != null)) {
      try
      {
        this.encodedPersonal = MimeUtility.encodeWord(this.personal);
      }
      catch (UnsupportedEncodingException ex) {}
    }
    if (this.encodedPersonal != null) {
      return quotePhrase(this.encodedPersonal) + " <" + this.address + ">";
    }
    if ((isGroup()) || (isSimple())) {
      return this.address;
    }
    return "<" + this.address + ">";
  }
  
  public String toUnicodeString()
  {
    String p = getPersonal();
    if (p != null) {
      return quotePhrase(p) + " <" + this.address + ">";
    }
    if ((isGroup()) || (isSimple())) {
      return this.address;
    }
    return "<" + this.address + ">";
  }
  
  private static final String rfc822phrase = "()<>@,;:\\\"\t .[]".replace(' ', '\000').replace('\t', '\000');
  private static final String specialsNoDotNoAt = "()<>,;:\\\"[]";
  private static final String specialsNoDot = "()<>,;:\\\"[]@";
  
  private static String quotePhrase(String phrase)
  {
    int len = phrase.length();
    boolean needQuoting = false;
    for (int i = 0; i < len; i++)
    {
      char c = phrase.charAt(i);
      if ((c == '"') || (c == '\\'))
      {
        StringBuffer sb = new StringBuffer(len + 3);
        sb.append('"');
        for (int j = 0; j < len; j++)
        {
          char cc = phrase.charAt(j);
          if ((cc == '"') || (cc == '\\')) {
            sb.append('\\');
          }
          sb.append(cc);
        }
        sb.append('"');
        return sb.toString();
      }
      if (((c < ' ') && (c != '\r') && (c != '\n') && (c != '\t')) || (c >= '') || (rfc822phrase.indexOf(c) >= 0)) {
        needQuoting = true;
      }
    }
    if (needQuoting)
    {
      StringBuffer sb = new StringBuffer(len + 2);
      sb.append('"').append(phrase).append('"');
      return sb.toString();
    }
    return phrase;
  }
  
  private static String unquote(String s)
  {
    if ((s.startsWith("\"")) && (s.endsWith("\"")))
    {
      s = s.substring(1, s.length() - 1);
      if (s.indexOf('\\') >= 0)
      {
        StringBuffer sb = new StringBuffer(s.length());
        for (int i = 0; i < s.length(); i++)
        {
          char c = s.charAt(i);
          if ((c == '\\') && (i < s.length() - 1)) {
            c = s.charAt(++i);
          }
          sb.append(c);
        }
        s = sb.toString();
      }
    }
    return s;
  }
  
  public boolean equals(Object a)
  {
    if (!(a instanceof InternetAddress)) {
      return false;
    }
    String s = ((InternetAddress)a).getAddress();
    if (s == this.address) {
      return true;
    }
    if ((this.address != null) && (this.address.equalsIgnoreCase(s))) {
      return true;
    }
    return false;
  }
  
  public int hashCode()
  {
    if (this.address == null) {
      return 0;
    }
    return this.address.toLowerCase(Locale.ENGLISH).hashCode();
  }
  
  public static String toString(Address[] addresses)
  {
    return toString(addresses, 0);
  }
  
  public static String toString(Address[] addresses, int used)
  {
    if ((addresses == null) || (addresses.length == 0)) {
      return null;
    }
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < addresses.length; i++)
    {
      if (i != 0)
      {
        sb.append(", ");
        used += 2;
      }
      String s = addresses[i].toString();
      int len = lengthOfFirstSegment(s);
      if (used + len > 76)
      {
        sb.append("\r\n\t");
        used = 8;
      }
      sb.append(s);
      used = lengthOfLastSegment(s, used);
    }
    return sb.toString();
  }
  
  private static int lengthOfFirstSegment(String s)
  {
    int pos;
    if ((pos = s.indexOf("\r\n")) != -1) {
      return pos;
    }
    return s.length();
  }
  
  private static int lengthOfLastSegment(String s, int used)
  {
    int pos;
    if ((pos = s.lastIndexOf("\r\n")) != -1) {
      return s.length() - pos - 2;
    }
    return s.length() + used;
  }
  
  public static InternetAddress getLocalAddress(Session session)
  {
    try
    {
      return _getLocalAddress(session);
    }
    catch (SecurityException sex) {}catch (AddressException ex) {}catch (UnknownHostException ex) {}
    return null;
  }
  
  static InternetAddress _getLocalAddress(Session session)
    throws SecurityException, AddressException, UnknownHostException
  {
    String user = null;String host = null;String address = null;
    if (session == null)
    {
      user = System.getProperty("user.name");
      host = getLocalHostName();
    }
    else
    {
      address = session.getProperty("mail.from");
      if (address == null)
      {
        user = session.getProperty("mail.user");
        if ((user == null) || (user.length() == 0)) {
          user = session.getProperty("user.name");
        }
        if ((user == null) || (user.length() == 0)) {
          user = System.getProperty("user.name");
        }
        host = session.getProperty("mail.host");
        if ((host == null) || (host.length() == 0)) {
          host = getLocalHostName();
        }
      }
    }
    if ((address == null) && (user != null) && (user.length() != 0) && (host != null) && (host.length() != 0)) {
      address = MimeUtility.quote(user.trim(), "()<>,;:\\\"[]@\t ") + "@" + host;
    }
    if (address == null) {
      return null;
    }
    return new InternetAddress(address);
  }
  
  private static String getLocalHostName()
    throws UnknownHostException
  {
    String host = null;
    InetAddress me = InetAddress.getLocalHost();
    if (me != null)
    {
      host = me.getHostName();
      if ((host != null) && (host.length() > 0) && (isInetAddressLiteral(host))) {
        host = '[' + host + ']';
      }
    }
    return host;
  }
  
  private static boolean isInetAddressLiteral(String addr)
  {
    boolean sawHex = false;boolean sawColon = false;
    for (int i = 0; i < addr.length(); i++)
    {
      char c = addr.charAt(i);
      if ((c < '0') || (c > '9')) {
        if (c != '.') {
          if (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))) {
            sawHex = true;
          } else if (c == ':') {
            sawColon = true;
          } else {
            return false;
          }
        }
      }
    }
    return (!sawHex) || (sawColon);
  }
  
  public static InternetAddress[] parse(String addresslist)
    throws AddressException
  {
    return parse(addresslist, true);
  }
  
  public static InternetAddress[] parse(String addresslist, boolean strict)
    throws AddressException
  {
    return parse(addresslist, strict, false);
  }
  
  public static InternetAddress[] parseHeader(String addresslist, boolean strict)
    throws AddressException
  {
    return parse(addresslist, strict, true);
  }
  
  private static InternetAddress[] parse(String s, boolean strict, boolean parseHdr)
    throws AddressException
  {
    int start_personal = -1;int end_personal = -1;
    int length = s.length();
    boolean ignoreErrors = (parseHdr) && (!strict);
    boolean in_group = false;
    boolean route_addr = false;
    boolean rfc822 = false;
    
    List v = new ArrayList();
    int end;
    int start = end = -1;
    for (int index = 0; index < length; index++)
    {
      char c = s.charAt(index);
      switch (c)
      {
      case '(': 
        rfc822 = true;
        if ((start >= 0) && (end == -1)) {
          end = index;
        }
        int pindex = index;
        index++;
        for (int nesting = 1; (index < length) && (nesting > 0); index++)
        {
          c = s.charAt(index);
          switch (c)
          {
          case '\\': 
            index++;
            break;
          case '(': 
            nesting++;
            break;
          case ')': 
            nesting--;
          }
        }
        if (nesting > 0)
        {
          if (!ignoreErrors) {
            throw new AddressException("Missing ')'", s, index);
          }
          index = pindex + 1;
        }
        else
        {
          index--;
          if (start_personal == -1) {
            start_personal = pindex + 1;
          }
          if (end_personal == -1) {
            end_personal = index;
          }
        }
        break;
      case ')': 
        if (!ignoreErrors) {
          throw new AddressException("Missing '('", s, index);
        }
        if (start != -1) {
          continue;
        }
        start = index; break;
      case '<': 
        rfc822 = true;
        if (route_addr)
        {
          if (!ignoreErrors) {
            throw new AddressException("Extra route-addr", s, index);
          }
          if (start == -1)
          {
            route_addr = false;
            rfc822 = false;
            start = end = -1;
            continue;
          }
          if (!in_group)
          {
            if (end == -1) {
              end = index;
            }
            String addr = s.substring(start, end).trim();
            
            InternetAddress ma = new InternetAddress();
            ma.setAddress(addr);
            if (start_personal >= 0) {
              ma.encodedPersonal = unquote(s.substring(start_personal, end_personal).trim());
            }
            v.add(ma);
            
            route_addr = false;
            rfc822 = false;
            start = end = -1;
            start_personal = end_personal = -1;
          }
        }
        int rindex = index;
        boolean inquote = false;
        for (index++; index < length; index++)
        {
          c = s.charAt(index);
          switch (c)
          {
          case '\\': 
            index++;
            break;
          case '"': 
            inquote = !inquote;
            break;
          case '>': 
            if (!inquote) {
              break label615;
            }
          }
        }
        if (inquote)
        {
          if (!ignoreErrors) {
            throw new AddressException("Missing '\"'", s, index);
          }
          for (index = rindex + 1; index < length; index++)
          {
            c = s.charAt(index);
            if (c == '\\') {
              index++;
            } else {
              if (c == '>') {
                break;
              }
            }
          }
        }
        if (index >= length)
        {
          if (!ignoreErrors) {
            throw new AddressException("Missing '>'", s, index);
          }
          index = rindex + 1;
          if (start == -1) {
            start = rindex;
          }
        }
        else
        {
          if (!in_group)
          {
            start_personal = start;
            if (start_personal >= 0) {
              end_personal = rindex;
            }
            start = rindex + 1;
          }
          route_addr = true;
          end = index;
        }
        break;
      case '>': 
        if (!ignoreErrors) {
          throw new AddressException("Missing '<'", s, index);
        }
        if (start != -1) {
          continue;
        }
        start = index; break;
      case '"': 
        int qindex = index;
        rfc822 = true;
        if (start == -1) {
          start = index;
        }
        for (index++; index < length; index++)
        {
          c = s.charAt(index);
          switch (c)
          {
          case '\\': 
            index++;
            break;
          case '"': 
            break label867;
          }
        }
        if (index < length) {
          continue;
        }
        if (!ignoreErrors) {
          throw new AddressException("Missing '\"'", s, index);
        }
        index = qindex + 1; break;
      case '[': 
        rfc822 = true;
        int lindex = index;
        for (index++; index < length; index++)
        {
          c = s.charAt(index);
          switch (c)
          {
          case '\\': 
            index++;
            break;
          case ']': 
            break label971;
          }
        }
        if (index < length) {
          continue;
        }
        if (!ignoreErrors) {
          throw new AddressException("Missing ']'", s, index);
        }
        index = lindex + 1; break;
      case ';': 
        if (start == -1)
        {
          route_addr = false;
          rfc822 = false;
          start = end = -1;
          continue;
        }
        if (in_group)
        {
          in_group = false;
          if ((parseHdr) && (!strict) && (index + 1 < length) && (s.charAt(index + 1) == '@')) {
            continue;
          }
          InternetAddress ma = new InternetAddress();
          end = index + 1;
          ma.setAddress(s.substring(start, end).trim());
          v.add(ma);
          
          route_addr = false;
          rfc822 = false;
          start = end = -1;
          start_personal = end_personal = -1;
          continue;
        }
        if (!ignoreErrors) {
          throw new AddressException("Illegal semicolon, not in group", s, index);
        }
      case ',': 
        if (start == -1)
        {
          route_addr = false;
          rfc822 = false;
          start = end = -1;
        }
        else if (in_group)
        {
          route_addr = false;
        }
        else
        {
          if (end == -1) {
            end = index;
          }
          String addr = s.substring(start, end).trim();
          String pers = null;
          if ((rfc822) && (start_personal >= 0))
          {
            pers = unquote(s.substring(start_personal, end_personal).trim());
            if (pers.trim().length() == 0) {
              pers = null;
            }
          }
          if ((parseHdr) && (!strict) && (pers != null) && (pers.indexOf('@') >= 0) && (addr.indexOf('@') < 0) && (addr.indexOf('!') < 0))
          {
            String tmp = addr;
            addr = pers;
            pers = tmp;
          }
          if ((rfc822) || (strict) || (parseHdr))
          {
            if (!ignoreErrors) {
              checkAddress(addr, route_addr, false);
            }
            InternetAddress ma = new InternetAddress();
            ma.setAddress(addr);
            if (pers != null) {
              ma.encodedPersonal = pers;
            }
            v.add(ma);
          }
          else
          {
            StringTokenizer st = new StringTokenizer(addr);
            while (st.hasMoreTokens())
            {
              String a = st.nextToken();
              checkAddress(a, false, false);
              InternetAddress ma = new InternetAddress();
              ma.setAddress(a);
              v.add(ma);
            }
          }
          route_addr = false;
          rfc822 = false;
          start = end = -1;
          start_personal = end_personal = -1;
        }
        break;
      case ':': 
        rfc822 = true;
        if ((in_group) && 
          (!ignoreErrors)) {
          throw new AddressException("Nested group", s, index);
        }
        if (start == -1) {
          start = index;
        }
        if ((parseHdr) && (!strict))
        {
          if (index + 1 < length)
          {
            String addressSpecials = ")>[]:@\\,.";
            char nc = s.charAt(index + 1);
            if (addressSpecials.indexOf(nc) >= 0)
            {
              if (nc != '@') {
                continue;
              }
              for (int i = index + 2; i < length; i++)
              {
                nc = s.charAt(i);
                if (nc == ';') {
                  break;
                }
                if (addressSpecials.indexOf(nc) >= 0) {
                  break;
                }
              }
              if (nc == ';') {
                continue;
              }
            }
          }
          String gname = s.substring(start, index);
          if ((ignoreBogusGroupName) && ((gname.equalsIgnoreCase("mailto")) || (gname.equalsIgnoreCase("From")) || (gname.equalsIgnoreCase("To")) || (gname.equalsIgnoreCase("Cc")) || (gname.equalsIgnoreCase("Subject")) || (gname.equalsIgnoreCase("Re")))) {
            start = -1;
          } else {
            in_group = true;
          }
        }
        else
        {
          in_group = true;
        }
        break;
      case '\t': 
      case '\n': 
      case '\r': 
      case ' ': 
        label615:
        label867:
        label971:
        break;
      }
      if (start == -1) {
        start = index;
      }
    }
    if (start >= 0)
    {
      if (end == -1) {
        end = length;
      }
      String addr = s.substring(start, end).trim();
      String pers = null;
      if ((rfc822) && (start_personal >= 0))
      {
        pers = unquote(s.substring(start_personal, end_personal).trim());
        if (pers.trim().length() == 0) {
          pers = null;
        }
      }
      if ((parseHdr) && (!strict) && (pers != null) && (pers.indexOf('@') >= 0) && (addr.indexOf('@') < 0) && (addr.indexOf('!') < 0))
      {
        String tmp = addr;
        addr = pers;
        pers = tmp;
      }
      if ((rfc822) || (strict) || (parseHdr))
      {
        if (!ignoreErrors) {
          checkAddress(addr, route_addr, false);
        }
        InternetAddress ma = new InternetAddress();
        ma.setAddress(addr);
        if (pers != null) {
          ma.encodedPersonal = pers;
        }
        v.add(ma);
      }
      else
      {
        StringTokenizer st = new StringTokenizer(addr);
        while (st.hasMoreTokens())
        {
          String a = st.nextToken();
          checkAddress(a, false, false);
          InternetAddress ma = new InternetAddress();
          ma.setAddress(a);
          v.add(ma);
        }
      }
    }
    InternetAddress[] a = new InternetAddress[v.size()];
    v.toArray(a);
    return a;
  }
  
  public void validate()
    throws AddressException
  {
    if (isGroup()) {
      getGroup(true);
    } else {
      checkAddress(getAddress(), true, true);
    }
  }
  
  private static void checkAddress(String addr, boolean routeAddr, boolean validate)
    throws AddressException
  {
    int start = 0;
    
    int len = addr.length();
    if (len == 0) {
      throw new AddressException("Empty address", addr);
    }
    if ((routeAddr) && (addr.charAt(0) == '@'))
    {
      int i;
      for (start = 0; (i = indexOfAny(addr, ",:", start)) >= 0; start = i + 1)
      {
        if (addr.charAt(start) != '@') {
          throw new AddressException("Illegal route-addr", addr);
        }
        if (addr.charAt(i) == ':')
        {
          start = i + 1;
          break;
        }
      }
    }
    char c = 65535;
    char lastc = 65535;
    boolean inquote = false;
    for (int i = start; i < len; i++)
    {
      lastc = c;
      c = addr.charAt(i);
      if ((c != '\\') && (lastc != '\\')) {
        if (c == '"')
        {
          if (inquote)
          {
            if ((validate) && (i + 1 < len) && (addr.charAt(i + 1) != '@')) {
              throw new AddressException("Quote not at end of local address", addr);
            }
            inquote = false;
          }
          else
          {
            if ((validate) && (i != 0)) {
              throw new AddressException("Quote not at start of local address", addr);
            }
            inquote = true;
          }
        }
        else if (!inquote)
        {
          if (c == '@')
          {
            if (i != 0) {
              break;
            }
            throw new AddressException("Missing local name", addr);
          }
          if ((c <= ' ') || (c >= '')) {
            throw new AddressException("Local address contains control or whitespace", addr);
          }
          if ("()<>,;:\\\"[]@".indexOf(c) >= 0) {
            throw new AddressException("Local address contains illegal character", addr);
          }
        }
      }
    }
    if (inquote) {
      throw new AddressException("Unterminated quote", addr);
    }
    if (c != '@')
    {
      if (validate) {
        throw new AddressException("Missing final '@domain'", addr);
      }
      return;
    }
    start = i + 1;
    if (start >= len) {
      throw new AddressException("Missing domain", addr);
    }
    if (addr.charAt(start) == '.') {
      throw new AddressException("Domain starts with dot", addr);
    }
    for (i = start; i < len; i++)
    {
      c = addr.charAt(i);
      if (c == '[') {
        return;
      }
      if ((c <= ' ') || (c >= '')) {
        throw new AddressException("Domain contains control or whitespace", addr);
      }
      if ((!Character.isLetterOrDigit(c)) && (c != '-') && (c != '.')) {
        throw new AddressException("Domain contains illegal character", addr);
      }
      if ((c == '.') && (lastc == '.')) {
        throw new AddressException("Domain contains dot-dot", addr);
      }
      lastc = c;
    }
    if (lastc == '.') {
      throw new AddressException("Domain ends with dot", addr);
    }
  }
  
  private boolean isSimple()
  {
    return (this.address == null) || (indexOfAny(this.address, "()<>,;:\\\"[]") < 0);
  }
  
  public boolean isGroup()
  {
    return (this.address != null) && (this.address.endsWith(";")) && (this.address.indexOf(':') > 0);
  }
  
  public InternetAddress[] getGroup(boolean strict)
    throws AddressException
  {
    String addr = getAddress();
    if (!addr.endsWith(";")) {
      return null;
    }
    int ix = addr.indexOf(':');
    if (ix < 0) {
      return null;
    }
    String list = addr.substring(ix + 1, addr.length() - 1);
    
    return parseHeader(list, strict);
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
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\internet\InternetAddress.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */