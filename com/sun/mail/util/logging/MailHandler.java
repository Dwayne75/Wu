package com.sun.mail.util.logging;

import com.sun.mail.smtp.SMTPTransport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessageContext;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

public class MailHandler
  extends Handler
{
  private static final Filter[] EMPTY_FILTERS;
  private static final Formatter[] EMPTY_FORMATTERS;
  private static final int MIN_HEADER_SIZE = 1024;
  private static final int offValue;
  private static final GetAndSetContext GET_AND_SET_CCL;
  private static final ThreadLocal MUTEX;
  private static final Object MUTEX_PUBLISH;
  private static final Object MUTEX_REPORT;
  private static final Method REMOVE;
  private volatile boolean sealed;
  private boolean isWriting;
  private Properties mailProps;
  private Authenticator auth;
  private Session session;
  private LogRecord[] data;
  private int size;
  private int capacity;
  private Comparator comparator;
  private Formatter subjectFormatter;
  private Level pushLevel;
  private Filter pushFilter;
  private Filter[] attachmentFilters;
  private Formatter[] attachmentFormatters;
  private Formatter[] attachmentNames;
  private FileTypeMap contentTypes;
  
  static
  {
    EMPTY_FILTERS = new Filter[0];
    
    EMPTY_FORMATTERS = new Formatter[0];
    
    offValue = Level.OFF.intValue();
    
    GET_AND_SET_CCL = new GetAndSetContext(MailHandler.class);
    
    MUTEX = new ThreadLocal();
    
    MUTEX_PUBLISH = Level.ALL;
    
    MUTEX_REPORT = Level.OFF;
    Method m;
    try
    {
      m = ThreadLocal.class.getMethod("remove", (Class[])null);
    }
    catch (RuntimeException noAccess)
    {
      m = null;
    }
    catch (Exception javaOnePointFour)
    {
      m = null;
    }
    REMOVE = m;
  }
  
  public MailHandler()
  {
    init(true);
    this.sealed = true;
  }
  
  public MailHandler(int capacity)
  {
    init(true);
    this.sealed = true;
    setCapacity0(capacity);
  }
  
  public MailHandler(Properties props)
  {
    init(false);
    this.sealed = true;
    setMailProperties0(props);
  }
  
  public boolean isLoggable(LogRecord record)
  {
    int levelValue = getLevel().intValue();
    if ((record.getLevel().intValue() < levelValue) || (levelValue == offValue)) {
      return false;
    }
    Filter body = getFilter();
    if ((body == null) || (body.isLoggable(record))) {
      return true;
    }
    return isAttachmentLoggable(record);
  }
  
  public void publish(LogRecord record)
  {
    if (tryMutex()) {
      try
      {
        if (isLoggable(record))
        {
          record.getSourceMethodName();
          publish0(record);
        }
      }
      finally
      {
        releaseMutex();
      }
    } else {
      reportUnPublishedError(record);
    }
  }
  
  private void publish0(LogRecord record)
  {
    boolean priority;
    MessageContext ctx;
    synchronized (this)
    {
      if ((this.size == this.data.length) && (this.size < this.capacity)) {
        grow();
      }
      MessageContext ctx;
      if (this.size < this.data.length)
      {
        this.data[this.size] = record;
        this.size += 1;
        boolean priority = isPushable(record);
        MessageContext ctx;
        if ((priority) || (this.size >= this.capacity)) {
          ctx = writeLogRecords(1);
        } else {
          ctx = null;
        }
      }
      else
      {
        priority = false;
        ctx = null;
      }
    }
    if (ctx != null) {
      send(ctx, priority, 1);
    }
  }
  
  private void reportUnPublishedError(LogRecord record)
  {
    if (MUTEX_PUBLISH.equals(MUTEX.get()))
    {
      MUTEX.set(MUTEX_REPORT);
      try
      {
        String msg;
        String msg;
        if (record != null)
        {
          SimpleFormatter f = new SimpleFormatter();
          msg = "Log record " + record.getSequenceNumber() + " was not published. " + head(f) + format(f, record) + tail(f, "");
        }
        else
        {
          msg = null;
        }
        Exception e = new IllegalStateException("Recursive publish detected by thread " + Thread.currentThread());
        
        reportError(msg, e, 1);
      }
      finally
      {
        MUTEX.set(MUTEX_PUBLISH);
      }
    }
  }
  
  private boolean tryMutex()
  {
    if (MUTEX.get() == null)
    {
      MUTEX.set(MUTEX_PUBLISH);
      return true;
    }
    return false;
  }
  
  private void releaseMutex()
  {
    if (REMOVE != null) {
      try
      {
        REMOVE.invoke(MUTEX, (Object[])null);
      }
      catch (RuntimeException ignore)
      {
        MUTEX.set(null);
      }
      catch (Exception ignore)
      {
        MUTEX.set(null);
      }
    } else {
      MUTEX.set(null);
    }
  }
  
  public void push()
  {
    push(true, 2);
  }
  
  public void flush()
  {
    push(false, 2);
  }
  
  public void close()
  {
    Object ccl = getAndSetContextClassLoader();
    try
    {
      MessageContext ctx = null;
      synchronized (this)
      {
        super.setLevel(Level.OFF);
        try
        {
          ctx = writeLogRecords(3);
        }
        finally
        {
          if (this.capacity > 0) {
            this.capacity = (-this.capacity);
          }
          if ((this.size == 0) && (this.data.length != 1)) {
            this.data = new LogRecord[1];
          }
        }
      }
      if (ctx != null) {
        send(ctx, false, 3);
      }
    }
    finally
    {
      setContextClassLoader(ccl);
    }
  }
  
  public synchronized void setLevel(Level newLevel)
  {
    if (this.capacity > 0)
    {
      super.setLevel(newLevel);
    }
    else
    {
      if (newLevel == null) {
        throw new NullPointerException();
      }
      checkAccess();
    }
  }
  
  public final synchronized Level getPushLevel()
  {
    return this.pushLevel;
  }
  
  public final synchronized void setPushLevel(Level level)
  {
    checkAccess();
    if (level == null) {
      throw new NullPointerException();
    }
    if (this.isWriting) {
      throw new IllegalStateException();
    }
    this.pushLevel = level;
  }
  
  public final synchronized Filter getPushFilter()
  {
    return this.pushFilter;
  }
  
  public final synchronized void setPushFilter(Filter filter)
  {
    checkAccess();
    if (this.isWriting) {
      throw new IllegalStateException();
    }
    this.pushFilter = filter;
  }
  
  public final synchronized Comparator getComparator()
  {
    return this.comparator;
  }
  
  public final synchronized void setComparator(Comparator c)
  {
    checkAccess();
    if (this.isWriting) {
      throw new IllegalStateException();
    }
    this.comparator = c;
  }
  
  public final synchronized int getCapacity()
  {
    assert ((this.capacity != Integer.MIN_VALUE) && (this.capacity != 0)) : this.capacity;
    return Math.abs(this.capacity);
  }
  
  public final synchronized Authenticator getAuthenticator()
  {
    checkAccess();
    return this.auth;
  }
  
  public final void setAuthenticator(Authenticator auth)
  {
    setAuthenticator0(auth);
  }
  
  public final void setAuthenticator(char[] password)
  {
    if (password == null) {
      setAuthenticator0((Authenticator)null);
    } else {
      setAuthenticator0(new DefaultAuthenticator(new String(password)));
    }
  }
  
  private void setAuthenticator0(Authenticator auth)
  {
    checkAccess();
    Session settings;
    synchronized (this)
    {
      if (this.isWriting) {
        throw new IllegalStateException();
      }
      this.auth = auth;
      settings = fixUpSession();
    }
    verifySettings(settings);
  }
  
  public final void setMailProperties(Properties props)
  {
    setMailProperties0(props);
  }
  
  private void setMailProperties0(Properties props)
  {
    checkAccess();
    props = (Properties)props.clone();
    Session settings;
    synchronized (this)
    {
      if (this.isWriting) {
        throw new IllegalStateException();
      }
      this.mailProps = props;
      settings = fixUpSession();
    }
    verifySettings(settings);
  }
  
  public final Properties getMailProperties()
  {
    checkAccess();
    Properties props;
    synchronized (this)
    {
      props = this.mailProps;
    }
    return (Properties)props.clone();
  }
  
  public final Filter[] getAttachmentFilters()
  {
    return (Filter[])readOnlyAttachmentFilters().clone();
  }
  
  public final void setAttachmentFilters(Filter[] filters)
  {
    checkAccess();
    filters = (Filter[])copyOf(filters, filters.length, new Filter[0].getClass());
    synchronized (this)
    {
      if (this.attachmentFormatters.length != filters.length) {
        throw attachmentMismatch(this.attachmentFormatters.length, filters.length);
      }
      if (this.isWriting) {
        throw new IllegalStateException();
      }
      this.attachmentFilters = filters;
    }
  }
  
  public final Formatter[] getAttachmentFormatters()
  {
    Formatter[] formatters;
    synchronized (this)
    {
      formatters = this.attachmentFormatters;
    }
    return (Formatter[])formatters.clone();
  }
  
  public final void setAttachmentFormatters(Formatter[] formatters)
  {
    checkAccess();
    if (formatters.length == 0)
    {
      formatters = emptyFormatterArray();
    }
    else
    {
      formatters = (Formatter[])copyOf(formatters, formatters.length, new Formatter[0].getClass());
      for (int i = 0; i < formatters.length; i++) {
        if (formatters[i] == null) {
          throw new NullPointerException(atIndexMsg(i));
        }
      }
    }
    synchronized (this)
    {
      if (this.isWriting) {
        throw new IllegalStateException();
      }
      this.attachmentFormatters = formatters;
      fixUpAttachmentFilters();
      fixUpAttachmentNames();
    }
  }
  
  public final Formatter[] getAttachmentNames()
  {
    Formatter[] formatters;
    synchronized (this)
    {
      formatters = this.attachmentNames;
    }
    return (Formatter[])formatters.clone();
  }
  
  public final void setAttachmentNames(String[] names)
  {
    checkAccess();
    Formatter[] formatters;
    Formatter[] formatters;
    if (names.length == 0) {
      formatters = emptyFormatterArray();
    } else {
      formatters = new Formatter[names.length];
    }
    for (int i = 0; i < names.length; i++)
    {
      String name = names[i];
      if (name != null)
      {
        if (name.length() > 0) {
          formatters[i] = new TailNameFormatter(name);
        } else {
          throw new IllegalArgumentException(atIndexMsg(i));
        }
      }
      else {
        throw new NullPointerException(atIndexMsg(i));
      }
    }
    synchronized (this)
    {
      if (this.attachmentFormatters.length != names.length) {
        throw attachmentMismatch(this.attachmentFormatters.length, names.length);
      }
      if (this.isWriting) {
        throw new IllegalStateException();
      }
      this.attachmentNames = formatters;
    }
  }
  
  public final void setAttachmentNames(Formatter[] formatters)
  {
    checkAccess();
    
    formatters = (Formatter[])copyOf(formatters, formatters.length, new Formatter[0].getClass());
    for (int i = 0; i < formatters.length; i++) {
      if (formatters[i] == null) {
        throw new NullPointerException(atIndexMsg(i));
      }
    }
    synchronized (this)
    {
      if (this.attachmentFormatters.length != formatters.length) {
        throw attachmentMismatch(this.attachmentFormatters.length, formatters.length);
      }
      if (this.isWriting) {
        throw new IllegalStateException();
      }
      this.attachmentNames = formatters;
    }
  }
  
  public final synchronized Formatter getSubject()
  {
    return this.subjectFormatter;
  }
  
  public final void setSubject(String subject)
  {
    if (subject != null)
    {
      setSubject(new TailNameFormatter(subject));
    }
    else
    {
      checkAccess();
      throw new NullPointerException();
    }
  }
  
  public final void setSubject(Formatter format)
  {
    checkAccess();
    if (format == null) {
      throw new NullPointerException();
    }
    synchronized (this)
    {
      if (this.isWriting) {
        throw new IllegalStateException();
      }
      this.subjectFormatter = format;
    }
  }
  
  protected void reportError(String msg, Exception ex, int code)
  {
    if (msg != null) {
      super.reportError(Level.SEVERE.getName() + ": " + msg, ex, code);
    } else {
      super.reportError(null, ex, code);
    }
  }
  
  final void checkAccess()
  {
    if (this.sealed) {
      LogManagerProperties.getLogManager().checkAccess();
    }
  }
  
  final String contentTypeOf(String head)
  {
    if ((head != null) && (head.length() > 0))
    {
      int MAX_CHARS = 25;
      if (head.length() > 25) {
        head = head.substring(0, 25);
      }
      try
      {
        String encoding = getEncodingName();
        ByteArrayInputStream in = new ByteArrayInputStream(head.getBytes(encoding));
        
        assert (in.markSupported()) : in.getClass().getName();
        return URLConnection.guessContentTypeFromStream(in);
      }
      catch (IOException IOE)
      {
        reportError(IOE.getMessage(), IOE, 5);
      }
    }
    return null;
  }
  
  final boolean isMissingContent(Message msg, Throwable t)
  {
    for (Throwable cause = t.getCause(); cause != null;)
    {
      t = cause;
      cause = cause.getCause();
    }
    try
    {
      msg.writeTo(new ByteArrayOutputStream(1024));
    }
    catch (RuntimeException RE)
    {
      throw RE;
    }
    catch (Exception noContent)
    {
      String txt = noContent.getMessage();
      if ((!isEmpty(txt)) && (noContent.getClass() == t.getClass())) {
        return txt.equals(t.getMessage());
      }
    }
    return false;
  }
  
  private void reportError(Message msg, Exception ex, int code)
  {
    try
    {
      super.reportError(toRawString(msg), ex, code);
    }
    catch (MessagingException rawMe)
    {
      reportError(toMsgString(rawMe), ex, code);
    }
    catch (IOException rawIo)
    {
      reportError(toMsgString(rawIo), ex, code);
    }
  }
  
  private String getContentType(String name)
  {
    assert (Thread.holdsLock(this));
    String type = this.contentTypes.getContentType(name);
    if ("application/octet-stream".equalsIgnoreCase(type)) {
      return null;
    }
    return type;
  }
  
  private String getEncodingName()
  {
    String encoding = getEncoding();
    if (encoding == null) {
      encoding = MimeUtility.getDefaultJavaCharset();
    }
    return encoding;
  }
  
  private void setContent(MimeBodyPart part, CharSequence buf, String type)
    throws MessagingException
  {
    String encoding = getEncodingName();
    if ((type != null) && (!"text/plain".equalsIgnoreCase(type)))
    {
      type = contentWithEncoding(type, encoding);
      try
      {
        DataSource source = new ByteArrayDataSource(buf.toString(), type);
        part.setDataHandler(new DataHandler(source));
      }
      catch (IOException IOE)
      {
        reportError(IOE.getMessage(), IOE, 5);
        part.setText(buf.toString(), encoding);
      }
    }
    else
    {
      part.setText(buf.toString(), MimeUtility.mimeCharset(encoding));
    }
  }
  
  private String contentWithEncoding(String type, String encoding)
  {
    assert (encoding != null);
    try
    {
      ContentType ct = new ContentType(type);
      ct.setParameter("charset", MimeUtility.mimeCharset(encoding));
      encoding = ct.toString();
      if (!isEmpty(encoding)) {
        type = encoding;
      }
    }
    catch (MessagingException ME)
    {
      reportError(type, ME, 5);
    }
    return type;
  }
  
  private synchronized void setCapacity0(int newCapacity)
  {
    if (newCapacity <= 0) {
      throw new IllegalArgumentException("Capacity must be greater than zero.");
    }
    if (this.isWriting) {
      throw new IllegalStateException();
    }
    if (this.capacity < 0) {
      this.capacity = (-newCapacity);
    } else {
      this.capacity = newCapacity;
    }
  }
  
  private synchronized Filter[] readOnlyAttachmentFilters()
  {
    return this.attachmentFilters;
  }
  
  private static Formatter[] emptyFormatterArray()
  {
    return EMPTY_FORMATTERS;
  }
  
  private static Filter[] emptyFilterArray()
  {
    return EMPTY_FILTERS;
  }
  
  private boolean fixUpAttachmentNames()
  {
    assert (Thread.holdsLock(this));
    boolean fixed = false;
    int expect = this.attachmentFormatters.length;
    int current = this.attachmentNames.length;
    if (current != expect)
    {
      this.attachmentNames = ((Formatter[])copyOf(this.attachmentNames, expect));
      fixed = current != 0;
    }
    if (expect == 0)
    {
      this.attachmentNames = emptyFormatterArray();
      if ((!$assertionsDisabled) && (this.attachmentNames.length != 0)) {
        throw new AssertionError();
      }
    }
    else
    {
      for (int i = 0; i < expect; i++) {
        if (this.attachmentNames[i] == null) {
          this.attachmentNames[i] = new TailNameFormatter(toString(this.attachmentFormatters[i]));
        }
      }
    }
    return fixed;
  }
  
  private boolean fixUpAttachmentFilters()
  {
    assert (Thread.holdsLock(this));
    
    boolean fixed = false;
    int expect = this.attachmentFormatters.length;
    int current = this.attachmentFilters.length;
    if (current != expect)
    {
      this.attachmentFilters = ((Filter[])copyOf(this.attachmentFilters, expect));
      fixed = current != 0;
    }
    if (expect == 0)
    {
      this.attachmentFilters = emptyFilterArray();
      assert (this.attachmentFilters.length == 0);
    }
    return fixed;
  }
  
  private static Object[] copyOf(Object[] a, int size)
  {
    Object[] copy = (Object[])Array.newInstance(a.getClass().getComponentType(), size);
    
    System.arraycopy(a, 0, copy, 0, Math.min(a.length, size));
    return copy;
  }
  
  private static Object[] copyOf(Object[] a, int len, Class type)
  {
    if (type == a.getClass()) {
      return (Object[])a.clone();
    }
    Object[] copy = (Object[])Array.newInstance(type.getComponentType(), len);
    
    System.arraycopy(a, 0, copy, 0, Math.min(len, a.length));
    return copy;
  }
  
  private void reset()
  {
    assert (Thread.holdsLock(this));
    if (this.size < this.data.length) {
      Arrays.fill(this.data, 0, this.size, null);
    } else {
      Arrays.fill(this.data, null);
    }
    this.size = 0;
  }
  
  private void grow()
  {
    assert (Thread.holdsLock(this));
    int len = this.data.length;
    int newCapacity = len + (len >> 1) + 1;
    if ((newCapacity > this.capacity) || (newCapacity < len)) {
      newCapacity = this.capacity;
    }
    assert (len != this.capacity) : len;
    this.data = ((LogRecord[])copyOf(this.data, newCapacity));
  }
  
  private synchronized void init(boolean inherit)
  {
    LogManager manager = LogManagerProperties.getLogManager();
    String p = getClass().getName();
    this.mailProps = new Properties();
    this.contentTypes = FileTypeMap.getDefaultFileTypeMap();
    
    initErrorManager(manager, p);
    
    initLevel(manager, p);
    initFilter(manager, p);
    initCapacity(manager, p);
    initAuthenticator(manager, p);
    
    initEncoding(manager, p);
    initFormatter(manager, p);
    initComparator(manager, p);
    initPushLevel(manager, p);
    initPushFilter(manager, p);
    
    initSubject(manager, p);
    
    initAttachmentFormaters(manager, p);
    initAttachmentFilters(manager, p);
    initAttachmentNames(manager, p);
    if ((inherit) && (manager.getProperty(p.concat(".verify")) != null)) {
      verifySettings(initSession());
    }
  }
  
  private static boolean isEmpty(String s)
  {
    return (s == null) || (s.length() == 0);
  }
  
  private static boolean hasValue(String name)
  {
    return (!isEmpty(name)) && (!"null".equalsIgnoreCase(name));
  }
  
  private void initAttachmentFilters(LogManager manager, String p)
  {
    assert (Thread.holdsLock(this));
    assert (this.attachmentFormatters != null);
    String list = manager.getProperty(p.concat(".attachment.filters"));
    if ((list != null) && (list.length() > 0))
    {
      String[] names = list.split(",");
      Filter[] a = new Filter[names.length];
      for (int i = 0; i < a.length; i++)
      {
        names[i] = names[i].trim();
        if (!"null".equalsIgnoreCase(names[i])) {
          try
          {
            a[i] = LogManagerProperties.newFilter(names[i]);
          }
          catch (SecurityException SE)
          {
            throw SE;
          }
          catch (Exception E)
          {
            reportError(E.getMessage(), E, 4);
          }
        }
      }
      this.attachmentFilters = a;
      if (fixUpAttachmentFilters()) {
        reportError("Attachment filters.", attachmentMismatch("Length mismatch."), 4);
      }
    }
    else
    {
      this.attachmentFilters = emptyFilterArray();
      fixUpAttachmentFilters();
    }
  }
  
  private void initAttachmentFormaters(LogManager manager, String p)
  {
    assert (Thread.holdsLock(this));
    String list = manager.getProperty(p.concat(".attachment.formatters"));
    if ((list != null) && (list.length() > 0))
    {
      String[] names = list.split(",");
      Formatter[] a;
      Formatter[] a;
      if (names.length == 0) {
        a = emptyFormatterArray();
      } else {
        a = new Formatter[names.length];
      }
      for (int i = 0; i < a.length; i++)
      {
        names[i] = names[i].trim();
        if (!"null".equalsIgnoreCase(names[i]))
        {
          try
          {
            a[i] = LogManagerProperties.newFormatter(names[i]);
            if ((a[i] instanceof TailNameFormatter))
            {
              a[i] = new SimpleFormatter();
              Exception CNFE = new ClassNotFoundException(a[i].toString());
              reportError("Attachment formatter.", CNFE, 4);
            }
          }
          catch (SecurityException SE)
          {
            throw SE;
          }
          catch (Exception E)
          {
            a[i] = new SimpleFormatter();
            reportError(E.getMessage(), E, 4);
          }
        }
        else
        {
          a[i] = new SimpleFormatter();
          Exception NPE = new NullPointerException(atIndexMsg(i));
          reportError("Attachment formatter.", NPE, 4);
        }
      }
      this.attachmentFormatters = a;
    }
    else
    {
      this.attachmentFormatters = emptyFormatterArray();
    }
  }
  
  private void initAttachmentNames(LogManager manager, String p)
  {
    assert (Thread.holdsLock(this));
    assert (this.attachmentFormatters != null);
    
    String list = manager.getProperty(p.concat(".attachment.names"));
    if ((list != null) && (list.length() > 0))
    {
      String[] names = list.split(",");
      Formatter[] a = new Formatter[names.length];
      for (int i = 0; i < a.length; i++)
      {
        names[i] = names[i].trim();
        if (!"null".equalsIgnoreCase(names[i]))
        {
          try
          {
            try
            {
              a[i] = LogManagerProperties.newFormatter(names[i]);
            }
            catch (ClassNotFoundException literal)
            {
              a[i] = new TailNameFormatter(names[i]);
            }
            catch (ClassCastException literal)
            {
              a[i] = new TailNameFormatter(names[i]);
            }
          }
          catch (SecurityException SE)
          {
            throw SE;
          }
          catch (Exception E)
          {
            reportError(E.getMessage(), E, 4);
          }
        }
        else
        {
          Exception NPE = new NullPointerException(atIndexMsg(i));
          reportError("Attachment names.", NPE, 4);
        }
      }
      this.attachmentNames = a;
      if (fixUpAttachmentNames()) {
        reportError("Attachment names.", attachmentMismatch("Length mismatch."), 4);
      }
    }
    else
    {
      this.attachmentNames = emptyFormatterArray();
      fixUpAttachmentNames();
    }
  }
  
  private void initAuthenticator(LogManager manager, String p)
  {
    assert (Thread.holdsLock(this));
    String name = manager.getProperty(p.concat(".authenticator"));
    if (hasValue(name)) {
      try
      {
        this.auth = LogManagerProperties.newAuthenticator(name);
      }
      catch (SecurityException SE)
      {
        throw SE;
      }
      catch (ClassNotFoundException literalAuth)
      {
        this.auth = new DefaultAuthenticator(name);
      }
      catch (ClassCastException literalAuth)
      {
        this.auth = new DefaultAuthenticator(name);
      }
      catch (Exception E)
      {
        reportError(E.getMessage(), E, 4);
      }
    }
  }
  
  private void initLevel(LogManager manager, String p)
  {
    assert (Thread.holdsLock(this));
    try
    {
      String val = manager.getProperty(p.concat(".level"));
      if (val != null) {
        super.setLevel(Level.parse(val));
      } else {
        super.setLevel(Level.WARNING);
      }
    }
    catch (SecurityException SE)
    {
      throw SE;
    }
    catch (RuntimeException RE)
    {
      reportError(RE.getMessage(), RE, 4);
      try
      {
        super.setLevel(Level.WARNING);
      }
      catch (RuntimeException fail)
      {
        reportError(fail.getMessage(), fail, 4);
      }
    }
  }
  
  private void initFilter(LogManager manager, String p)
  {
    assert (Thread.holdsLock(this));
    try
    {
      String name = manager.getProperty(p.concat(".filter"));
      if (hasValue(name)) {
        super.setFilter(LogManagerProperties.newFilter(name));
      }
    }
    catch (SecurityException SE)
    {
      throw SE;
    }
    catch (Exception E)
    {
      reportError(E.getMessage(), E, 4);
    }
  }
  
  private void initCapacity(LogManager manager, String p)
  {
    assert (Thread.holdsLock(this));
    int DEFAULT_CAPACITY = 1000;
    try
    {
      String value = manager.getProperty(p.concat(".capacity"));
      if (value != null) {
        setCapacity0(Integer.parseInt(value));
      } else {
        setCapacity0(1000);
      }
    }
    catch (RuntimeException RE)
    {
      reportError(RE.getMessage(), RE, 4);
    }
    if (this.capacity <= 0) {
      this.capacity = 1000;
    }
    this.data = new LogRecord[1];
  }
  
  private void initEncoding(LogManager manager, String p)
  {
    assert (Thread.holdsLock(this));
    try
    {
      super.setEncoding(manager.getProperty(p.concat(".encoding")));
    }
    catch (SecurityException SE)
    {
      throw SE;
    }
    catch (UnsupportedEncodingException UEE)
    {
      reportError(UEE.getMessage(), UEE, 4);
    }
    catch (RuntimeException RE)
    {
      reportError(RE.getMessage(), RE, 4);
    }
  }
  
  private void initErrorManager(LogManager manager, String p)
  {
    assert (Thread.holdsLock(this));
    String name = manager.getProperty(p.concat(".errorManager"));
    if (name != null) {
      try
      {
        ErrorManager em = LogManagerProperties.newErrorManager(name);
        super.setErrorManager(em);
      }
      catch (SecurityException SE)
      {
        throw SE;
      }
      catch (Exception E)
      {
        reportError(E.getMessage(), E, 4);
      }
    }
  }
  
  private void initFormatter(LogManager manager, String p)
  {
    assert (Thread.holdsLock(this));
    String name = manager.getProperty(p.concat(".formatter"));
    if (hasValue(name)) {
      try
      {
        Formatter formatter = LogManagerProperties.newFormatter(name);
        assert (formatter != null);
        if (!(formatter instanceof TailNameFormatter)) {
          super.setFormatter(formatter);
        } else {
          super.setFormatter(new SimpleFormatter());
        }
      }
      catch (SecurityException SE)
      {
        throw SE;
      }
      catch (Exception E)
      {
        reportError(E.getMessage(), E, 4);
        try
        {
          super.setFormatter(new SimpleFormatter());
        }
        catch (RuntimeException fail)
        {
          reportError(fail.getMessage(), fail, 4);
        }
      }
    } else {
      super.setFormatter(new SimpleFormatter());
    }
  }
  
  private void initComparator(LogManager manager, String p)
  {
    assert (Thread.holdsLock(this));
    String name = manager.getProperty(p.concat(".comparator"));
    if (hasValue(name)) {
      try
      {
        this.comparator = LogManagerProperties.newComparator(name);
      }
      catch (SecurityException SE)
      {
        throw SE;
      }
      catch (Exception E)
      {
        reportError(E.getMessage(), E, 4);
      }
    }
  }
  
  private void initPushLevel(LogManager manager, String p)
  {
    assert (Thread.holdsLock(this));
    try
    {
      String val = manager.getProperty(p.concat(".pushLevel"));
      if (val != null) {
        this.pushLevel = Level.parse(val);
      }
    }
    catch (RuntimeException RE)
    {
      reportError(RE.getMessage(), RE, 4);
    }
    if (this.pushLevel == null) {
      this.pushLevel = Level.OFF;
    }
  }
  
  private void initPushFilter(LogManager manager, String p)
  {
    assert (Thread.holdsLock(this));
    String name = manager.getProperty(p.concat(".pushFilter"));
    if (hasValue(name)) {
      try
      {
        this.pushFilter = LogManagerProperties.newFilter(name);
      }
      catch (SecurityException SE)
      {
        throw SE;
      }
      catch (Exception E)
      {
        reportError(E.getMessage(), E, 4);
      }
    }
  }
  
  private void initSubject(LogManager manager, String p)
  {
    assert (Thread.holdsLock(this));
    String name = manager.getProperty(p.concat(".subject"));
    if (hasValue(name)) {
      try
      {
        this.subjectFormatter = LogManagerProperties.newFormatter(name);
      }
      catch (SecurityException SE)
      {
        throw SE;
      }
      catch (ClassNotFoundException literalSubject)
      {
        this.subjectFormatter = new TailNameFormatter(name);
      }
      catch (ClassCastException literalSubject)
      {
        this.subjectFormatter = new TailNameFormatter(name);
      }
      catch (Exception E)
      {
        this.subjectFormatter = new TailNameFormatter(name);
        reportError(E.getMessage(), E, 4);
      }
    }
    if (this.subjectFormatter == null) {
      this.subjectFormatter = new TailNameFormatter("");
    }
  }
  
  private boolean isAttachmentLoggable(LogRecord record)
  {
    Filter[] filters = readOnlyAttachmentFilters();
    for (int i = 0; i < filters.length; i++)
    {
      Filter f = filters[i];
      if ((f == null) || (f.isLoggable(record))) {
        return true;
      }
    }
    return false;
  }
  
  private boolean isPushable(LogRecord record)
  {
    assert (Thread.holdsLock(this));
    int value = getPushLevel().intValue();
    if ((value == offValue) || (record.getLevel().intValue() < value)) {
      return false;
    }
    Filter filter = getPushFilter();
    return (filter == null) || (filter.isLoggable(record));
  }
  
  private void push(boolean priority, int code)
  {
    if (tryMutex()) {
      try
      {
        MessageContext ctx = writeLogRecords(code);
        if (ctx != null) {
          send(ctx, priority, code);
        }
      }
      finally
      {
        releaseMutex();
      }
    } else {
      reportUnPublishedError(null);
    }
  }
  
  private void send(MessageContext ctx, boolean priority, int code)
  {
    Message msg = ctx.getMessage();
    try
    {
      envelopeFor(ctx, priority);
      Transport.send(msg);
    }
    catch (Exception E)
    {
      reportError(msg, E, code);
    }
  }
  
  private void sort()
  {
    assert (Thread.holdsLock(this));
    if (this.comparator != null) {
      try
      {
        if (this.size != 1) {
          Arrays.sort(this.data, 0, this.size, this.comparator);
        } else {
          this.comparator.compare(this.data[0], this.data[0]);
        }
      }
      catch (RuntimeException RE)
      {
        reportError(RE.getMessage(), RE, 5);
      }
    }
  }
  
  private synchronized MessageContext writeLogRecords(int code)
  {
    if ((this.size == 0) || (this.isWriting)) {
      return null;
    }
    this.isWriting = true;
    try
    {
      sort();
      if (this.session == null) {
        initSession();
      }
      MimeMessage msg = new MimeMessage(this.session);
      msg.setDescription(descriptionFrom(this.comparator, this.pushLevel, this.pushFilter));
      
      MimeBodyPart[] parts = new MimeBodyPart[this.attachmentFormatters.length];
      
      StringBuffer[] buffers = new StringBuffer[parts.length];
      
      String contentType = null;
      StringBuffer buf = null;
      
      appendSubject(msg, head(this.subjectFormatter));
      
      MimeBodyPart body = createBodyPart();
      Formatter bodyFormat = getFormatter();
      Filter bodyFilter = getFilter();
      
      Locale lastLocale = null;
      for (int ix = 0; ix < this.size; ix++)
      {
        boolean formatted = false;
        LogRecord r = this.data[ix];
        this.data[ix] = null;
        
        Locale locale = localeFor(r);
        appendSubject(msg, format(this.subjectFormatter, r));
        if ((bodyFilter == null) || (bodyFilter.isLoggable(r)))
        {
          if (buf == null)
          {
            buf = new StringBuffer();
            String head = head(bodyFormat);
            buf.append(head);
            contentType = contentTypeOf(head);
          }
          formatted = true;
          buf.append(format(bodyFormat, r));
          if ((locale != null) && (!locale.equals(lastLocale))) {
            appendContentLang(body, locale);
          }
        }
        for (int i = 0; i < parts.length; i++)
        {
          Filter af = this.attachmentFilters[i];
          if ((af == null) || (af.isLoggable(r)))
          {
            if (parts[i] == null)
            {
              parts[i] = createBodyPart(i);
              buffers[i] = new StringBuffer();
              buffers[i].append(head(this.attachmentFormatters[i]));
              appendFileName(parts[i], head(this.attachmentNames[i]));
            }
            formatted = true;
            appendFileName(parts[i], format(this.attachmentNames[i], r));
            buffers[i].append(format(this.attachmentFormatters[i], r));
            if ((locale != null) && (!locale.equals(lastLocale))) {
              appendContentLang(parts[i], locale);
            }
          }
        }
        if (formatted)
        {
          if ((locale != null) && (!locale.equals(lastLocale))) {
            appendContentLang(msg, locale);
          }
        }
        else {
          reportFilterError(r);
        }
        lastLocale = locale;
      }
      this.size = 0;
      for (int i = parts.length - 1; i >= 0; i--) {
        if (parts[i] != null)
        {
          appendFileName(parts[i], tail(this.attachmentNames[i], "err"));
          buffers[i].append(tail(this.attachmentFormatters[i], ""));
          if (buffers[i].length() > 0)
          {
            String name = parts[i].getFileName();
            if (isEmpty(name))
            {
              name = toString(this.attachmentFormatters[i]);
              parts[i].setFileName(name);
            }
            setContent(parts[i], buffers[i], getContentType(name));
          }
          else
          {
            setIncompleteCopy(msg);
            parts[i] = null;
          }
          buffers[i] = null;
        }
      }
      buffers = null;
      if (buf != null) {
        buf.append(tail(bodyFormat, ""));
      } else {
        buf = new StringBuffer(0);
      }
      appendSubject(msg, tail(this.subjectFormatter, ""));
      
      MimeMultipart multipart = new MimeMultipart();
      String altType = getContentType(bodyFormat.getClass().getName());
      setContent(body, buf, altType == null ? contentType : altType);
      buf = null;
      multipart.addBodyPart(body);
      for (int i = 0; i < parts.length; i++) {
        if (parts[i] != null) {
          multipart.addBodyPart(parts[i]);
        }
      }
      parts = null;
      msg.setContent(multipart);
      return new MessageContext(msg);
    }
    catch (RuntimeException re)
    {
      reportError(re.getMessage(), re, code);
    }
    catch (Exception e)
    {
      reportError(e.getMessage(), e, code);
    }
    finally
    {
      this.isWriting = false;
      if (this.size > 0) {
        reset();
      }
    }
    return null;
  }
  
  private void verifySettings(Session session)
  {
    if (session != null)
    {
      Properties props = session.getProperties();
      Object check = props.put("verify", "");
      if ((check instanceof String))
      {
        String value = (String)check;
        if (hasValue(value)) {
          verifySettings0(session, value);
        }
      }
      else if (check != null)
      {
        verifySettings0(session, check.getClass().toString());
      }
    }
  }
  
  private void verifySettings0(Session session, String verify)
  {
    assert (verify != null) : ((String)null);
    if ((!"local".equals(verify)) && (!"remote".equals(verify)))
    {
      reportError("Verify must be 'local' or 'remote'.", new IllegalArgumentException(verify), 4);
      
      return;
    }
    String msg = "Local address is " + InternetAddress.getLocalAddress(session) + '.';
    try
    {
      Charset.forName(getEncodingName());
    }
    catch (RuntimeException RE)
    {
      IOException UEE = new UnsupportedEncodingException(RE.toString());
      UEE.initCause(RE);
      reportError(msg, UEE, 5);
    }
    MimeMessage abort = new MimeMessage(session);
    synchronized (this)
    {
      appendSubject(abort, head(this.subjectFormatter));
      appendSubject(abort, tail(this.subjectFormatter, ""));
    }
    setIncompleteCopy(abort);
    envelopeFor(new MessageContext(abort), true);
    try
    {
      abort.saveChanges();
    }
    catch (MessagingException ME)
    {
      reportError(msg, ME, 5);
    }
    try
    {
      Address[] all = abort.getAllRecipients();
      if (all == null) {
        all = new InternetAddress[0];
      }
      Transport t;
      try
      {
        Address[] any = all.length != 0 ? all : abort.getFrom();
        if ((any != null) && (any.length != 0))
        {
          Transport t = session.getTransport(any[0]);
          session.getProperty("mail.transport.protocol");
        }
        else
        {
          MessagingException me = new MessagingException("No recipient or from address.");
          
          reportError(msg, me, 4);
          throw me;
        }
      }
      catch (MessagingException protocol)
      {
        try
        {
          t = session.getTransport();
        }
        catch (MessagingException fail)
        {
          throw attach(protocol, fail);
        }
      }
      String host = null;
      if ("remote".equals(verify))
      {
        MessagingException closed = null;
        t.connect();
        try
        {
          try
          {
            if ((t instanceof SMTPTransport)) {
              host = ((SMTPTransport)t).getLocalHost();
            }
            t.sendMessage(abort, all);
          }
          finally
          {
            try
            {
              t.close();
            }
            catch (MessagingException ME)
            {
              closed = ME;
            }
          }
          reportUnexpectedSend(abort, verify, null);
        }
        catch (SendFailedException sfe)
        {
          Address[] recip = sfe.getInvalidAddresses();
          if ((recip != null) && (recip.length != 0))
          {
            fixUpContent(abort, verify, sfe);
            reportError(abort, sfe, 4);
          }
          recip = sfe.getValidSentAddresses();
          if ((recip != null) && (recip.length != 0)) {
            reportUnexpectedSend(abort, verify, sfe);
          }
        }
        catch (MessagingException ME)
        {
          if (!isMissingContent(abort, ME))
          {
            fixUpContent(abort, verify, ME);
            reportError(abort, ME, 4);
          }
        }
        if (closed != null)
        {
          fixUpContent(abort, verify, closed);
          reportError(abort, closed, 3);
        }
      }
      else
      {
        String protocol = t.getURLName().getProtocol();
        session.getProperty("mail.host");
        session.getProperty("mail.user");
        session.getProperty("mail." + protocol + ".host");
        session.getProperty("mail." + protocol + ".port");
        session.getProperty("mail." + protocol + ".user");
        if ((t instanceof SMTPTransport))
        {
          host = ((SMTPTransport)t).getLocalHost();
        }
        else
        {
          host = session.getProperty("mail." + protocol + ".localhost");
          if (isEmpty(host)) {
            host = session.getProperty("mail." + protocol + ".localaddress");
          }
        }
      }
      try
      {
        if (isEmpty(host))
        {
          if (InetAddress.getLocalHost().getCanonicalHostName().length() == 0) {
            throw new UnknownHostException();
          }
        }
        else if (InetAddress.getByName(host).getCanonicalHostName().length() == 0) {
          throw new UnknownHostException(host);
        }
      }
      catch (IOException IOE)
      {
        MessagingException ME = new MessagingException(msg, IOE);
        fixUpContent(abort, verify, ME);
        reportError(abort, ME, 4);
      }
      try
      {
        MimeMultipart multipart = new MimeMultipart();
        MimeBodyPart body = new MimeBodyPart();
        body.setDisposition("inline");
        body.setDescription(verify);
        setAcceptLang(body);
        setContent(body, "", "text/plain");
        multipart.addBodyPart(body);
        abort.setContent(multipart);
        abort.saveChanges();
        abort.writeTo(new ByteArrayOutputStream(1024));
      }
      catch (IOException IOE)
      {
        MessagingException ME = new MessagingException(msg, IOE);
        fixUpContent(abort, verify, ME);
        reportError(abort, ME, 5);
      }
      if (all.length != 0) {
        verifyAddresses(all);
      } else {
        throw new MessagingException("No recipient addresses.");
      }
      Address[] from = abort.getFrom();
      Address sender = abort.getSender();
      if ((sender instanceof InternetAddress)) {
        ((InternetAddress)sender).validate();
      }
      if ((abort.getHeader("From", ",") != null) && (from.length != 0))
      {
        verifyAddresses(from);
        for (int i = 0; i < from.length; i++) {
          if (from[i].equals(sender))
          {
            MessagingException ME = new MessagingException("Sender address '" + sender + "' equals from address.");
            
            throw new MessagingException(msg, ME);
          }
        }
      }
      else if (sender == null)
      {
        MessagingException ME = new MessagingException("No from or sender address.");
        
        throw new MessagingException(msg, ME);
      }
      verifyAddresses(abort.getReplyTo());
    }
    catch (MessagingException ME)
    {
      fixUpContent(abort, verify, ME);
      reportError(abort, ME, 4);
    }
    catch (RuntimeException RE)
    {
      fixUpContent(abort, verify, RE);
      reportError(abort, RE, 4);
    }
  }
  
  private static void verifyAddresses(Address[] all)
    throws AddressException
  {
    if (all != null) {
      for (int i = 0; i < all.length; i++)
      {
        Address a = all[i];
        if ((a instanceof InternetAddress)) {
          ((InternetAddress)a).validate();
        }
      }
    }
  }
  
  private void reportUnexpectedSend(MimeMessage msg, String verify, Exception cause)
  {
    MessagingException write = new MessagingException("An empty message was sent.", cause);
    
    fixUpContent(msg, verify, write);
    reportError(msg, write, 4);
  }
  
  private void fixUpContent(MimeMessage msg, String verify, Throwable t)
  {
    try
    {
      MimeBodyPart body;
      String msgDesc;
      String subjectType;
      synchronized (this)
      {
        body = createBodyPart();
        msgDesc = descriptionFrom(this.comparator, this.pushLevel, this.pushFilter);
        subjectType = getClassId(this.subjectFormatter);
      }
      body.setDescription("Formatted using " + (t == null ? Throwable.class.getName() : t.getClass().getName()) + ", filtered with " + verify + ", and named by " + subjectType + '.');
      
      setContent(body, toMsgString(t), "text/plain");
      MimeMultipart multipart = new MimeMultipart();
      multipart.addBodyPart(body);
      msg.setContent(multipart);
      msg.setDescription(msgDesc);
      setAcceptLang(msg);
      msg.saveChanges();
    }
    catch (MessagingException ME)
    {
      reportError("Unable to create body.", ME, 4);
    }
    catch (RuntimeException RE)
    {
      reportError("Unable to create body.", RE, 4);
    }
  }
  
  private Session fixUpSession()
  {
    assert (Thread.holdsLock(this));
    Session settings;
    if (this.mailProps.getProperty("verify") != null)
    {
      Session settings = initSession();
      if ((!$assertionsDisabled) && (settings != this.session)) {
        throw new AssertionError();
      }
    }
    else
    {
      this.session = null;
      settings = null;
    }
    return settings;
  }
  
  private Session initSession()
  {
    assert (Thread.holdsLock(this));
    String p = getClass().getName();
    LogManagerProperties proxy = new LogManagerProperties(this.mailProps, p);
    this.session = Session.getInstance(proxy, this.auth);
    return this.session;
  }
  
  private void envelopeFor(MessageContext ctx, boolean priority)
  {
    Message msg = ctx.getMessage();
    Properties proxyProps = ctx.getSession().getProperties();
    setAcceptLang(msg);
    setFrom(msg, proxyProps);
    setRecipient(msg, proxyProps, "mail.to", Message.RecipientType.TO);
    setRecipient(msg, proxyProps, "mail.cc", Message.RecipientType.CC);
    setRecipient(msg, proxyProps, "mail.bcc", Message.RecipientType.BCC);
    setReplyTo(msg, proxyProps);
    setSender(msg, proxyProps);
    setMailer(msg);
    setAutoSubmitted(msg);
    if (priority) {
      setPriority(msg);
    }
    try
    {
      msg.setSentDate(new Date());
    }
    catch (MessagingException ME)
    {
      reportError(ME.getMessage(), ME, 5);
    }
  }
  
  private MimeBodyPart createBodyPart()
    throws MessagingException
  {
    assert (Thread.holdsLock(this));
    MimeBodyPart part = new MimeBodyPart();
    part.setDisposition("inline");
    part.setDescription(descriptionFrom(getFormatter(), getFilter(), this.subjectFormatter));
    
    setAcceptLang(part);
    return part;
  }
  
  private MimeBodyPart createBodyPart(int index)
    throws MessagingException
  {
    assert (Thread.holdsLock(this));
    MimeBodyPart part = new MimeBodyPart();
    part.setDisposition("attachment");
    part.setDescription(descriptionFrom(this.attachmentFormatters[index], this.attachmentFilters[index], this.attachmentNames[index]));
    
    setAcceptLang(part);
    return part;
  }
  
  private String descriptionFrom(Comparator c, Level l, Filter f)
  {
    return "Sorted using " + (c == null ? "no comparator" : c.getClass().getName()) + ", pushed when " + l.getName() + ", and " + (f == null ? "no push filter" : f.getClass().getName()) + '.';
  }
  
  private String descriptionFrom(Formatter f, Filter filter, Formatter name)
  {
    return "Formatted using " + getClassId(f) + ", filtered with " + (filter == null ? "no filter" : filter.getClass().getName()) + ", and named by " + getClassId(name) + '.';
  }
  
  private String getClassId(Formatter f)
  {
    if ((f instanceof TailNameFormatter)) {
      return String.class.getName();
    }
    return f.getClass().getName();
  }
  
  private String toString(Formatter f)
  {
    String name = f.toString();
    if (!isEmpty(name)) {
      return name;
    }
    return getClassId(f);
  }
  
  private void appendFileName(Part part, String chunk)
  {
    if (chunk != null)
    {
      if (chunk.length() > 0) {
        appendFileName0(part, chunk);
      }
    }
    else {
      reportNullError(5);
    }
  }
  
  private void appendFileName0(Part part, String chunk)
  {
    try
    {
      String old = part.getFileName();
      part.setFileName(old != null ? old.concat(chunk) : chunk);
    }
    catch (MessagingException ME)
    {
      reportError(ME.getMessage(), ME, 5);
    }
  }
  
  private void appendSubject(Message msg, String chunk)
  {
    if (chunk != null)
    {
      if (chunk.length() > 0) {
        appendSubject0(msg, chunk);
      }
    }
    else {
      reportNullError(5);
    }
  }
  
  private void appendSubject0(Message msg, String chunk)
  {
    try
    {
      String encoding = getEncodingName();
      String old = msg.getSubject();
      assert ((msg instanceof MimeMessage));
      ((MimeMessage)msg).setSubject(old != null ? old.concat(chunk) : chunk, MimeUtility.mimeCharset(encoding));
    }
    catch (MessagingException ME)
    {
      reportError(ME.getMessage(), ME, 5);
    }
  }
  
  private Locale localeFor(LogRecord r)
  {
    ResourceBundle rb = r.getResourceBundle();
    Locale l;
    if (rb != null)
    {
      Locale l = rb.getLocale();
      if ((l == null) || (isEmpty(l.getLanguage()))) {
        l = Locale.getDefault();
      }
    }
    else
    {
      l = null;
    }
    return l;
  }
  
  private void appendContentLang(MimePart p, Locale l)
  {
    try
    {
      String lang = LogManagerProperties.toLanguageTag(l);
      if (lang.length() != 0)
      {
        String header = p.getHeader("Content-Language", null);
        if (isEmpty(header))
        {
          p.setHeader("Content-Language", lang);
        }
        else if (!header.equalsIgnoreCase(lang))
        {
          lang = ",".concat(lang);
          int idx = 0;
          while ((idx = header.indexOf(lang, idx)) > -1)
          {
            idx += lang.length();
            if (idx != header.length()) {
              if (header.charAt(idx) == ',') {
                break;
              }
            }
          }
          if (idx < 0)
          {
            int len = header.lastIndexOf("\r\n\t");
            if (len < 0) {
              len = 20 + header.length();
            } else {
              len = header.length() - len + 8;
            }
            if (len + lang.length() > 76) {
              header = header.concat("\r\n\t".concat(lang));
            } else {
              header = header.concat(lang);
            }
            p.setHeader("Content-Language", header);
          }
        }
      }
    }
    catch (MessagingException ME)
    {
      reportError(ME.getMessage(), ME, 5);
    }
  }
  
  private void setAcceptLang(Part p)
  {
    try
    {
      String lang = LogManagerProperties.toLanguageTag(Locale.getDefault());
      if (lang.length() != 0) {
        p.setHeader("Accept-Language", lang);
      }
    }
    catch (MessagingException ME)
    {
      reportError(ME.getMessage(), ME, 5);
    }
  }
  
  private void reportFilterError(LogRecord record)
  {
    assert (Thread.holdsLock(this));
    SimpleFormatter f = new SimpleFormatter();
    String msg = "Log record " + record.getSequenceNumber() + " was filtered from all message parts.  " + head(f) + format(f, record) + tail(f, "");
    
    String txt = getFilter() + ", " + Arrays.asList(readOnlyAttachmentFilters());
    
    reportError(msg, new IllegalArgumentException(txt), 5);
  }
  
  private void reportNullError(int code)
  {
    reportError("null", new NullPointerException(), code);
  }
  
  private String head(Formatter f)
  {
    try
    {
      return f.getHead(this);
    }
    catch (RuntimeException RE)
    {
      reportError(RE.getMessage(), RE, 5);
    }
    return "";
  }
  
  private String format(Formatter f, LogRecord r)
  {
    try
    {
      return f.format(r);
    }
    catch (RuntimeException RE)
    {
      reportError(RE.getMessage(), RE, 5);
    }
    return "";
  }
  
  private String tail(Formatter f, String def)
  {
    try
    {
      return f.getTail(this);
    }
    catch (RuntimeException RE)
    {
      reportError(RE.getMessage(), RE, 5);
    }
    return def;
  }
  
  private void setMailer(Message msg)
  {
    try
    {
      Class mail = MailHandler.class;
      Class k = getClass();
      String value;
      String value;
      if (k == mail)
      {
        value = mail.getName();
      }
      else
      {
        try
        {
          value = MimeUtility.encodeText(k.getName());
        }
        catch (UnsupportedEncodingException E)
        {
          reportError(E.getMessage(), E, 5);
          value = k.getName().replaceAll("[^\\x00-\\x7F]", "\032");
        }
        value = MimeUtility.fold(10, mail.getName() + " using the " + value + " extension.");
      }
      msg.setHeader("X-Mailer", value);
    }
    catch (MessagingException ME)
    {
      reportError(ME.getMessage(), ME, 5);
    }
  }
  
  private void setPriority(Message msg)
  {
    try
    {
      msg.setHeader("Importance", "High");
      msg.setHeader("Priority", "urgent");
      msg.setHeader("X-Priority", "2");
    }
    catch (MessagingException ME)
    {
      reportError(ME.getMessage(), ME, 5);
    }
  }
  
  private void setIncompleteCopy(Message msg)
  {
    try
    {
      msg.setHeader("Incomplete-Copy", "");
    }
    catch (MessagingException ME)
    {
      reportError(ME.getMessage(), ME, 5);
    }
  }
  
  private void setAutoSubmitted(Message msg)
  {
    try
    {
      msg.setHeader("auto-submitted", "auto-generated");
    }
    catch (MessagingException ME)
    {
      reportError(ME.getMessage(), ME, 5);
    }
  }
  
  private void setFrom(Message msg, Properties props)
  {
    String from = props.getProperty("mail.from");
    if ((from != null) && (from.length() > 0)) {
      try
      {
        Address[] address = InternetAddress.parse(from, false);
        if ((address == null) || (address.length == 0)) {
          setDefaultFrom(msg);
        } else if (address.length == 1) {
          msg.setFrom(address[0]);
        } else {
          msg.addFrom(address);
        }
      }
      catch (MessagingException ME)
      {
        reportError(ME.getMessage(), ME, 5);
        setDefaultFrom(msg);
      }
    } else {
      setDefaultFrom(msg);
    }
  }
  
  private void setDefaultFrom(Message msg)
  {
    try
    {
      msg.setFrom();
    }
    catch (MessagingException ME)
    {
      reportError(ME.getMessage(), ME, 5);
    }
  }
  
  private void setReplyTo(Message msg, Properties props)
  {
    String reply = props.getProperty("mail.reply.to");
    if ((reply != null) && (reply.length() > 0)) {
      try
      {
        Address[] address = InternetAddress.parse(reply, false);
        if ((address != null) && (address.length > 0)) {
          msg.setReplyTo(address);
        }
      }
      catch (MessagingException ME)
      {
        reportError(ME.getMessage(), ME, 5);
      }
    }
  }
  
  private void setSender(Message msg, Properties props)
  {
    assert ((msg instanceof MimeMessage)) : msg;
    String sender = props.getProperty("mail.sender");
    if ((sender != null) && (sender.length() > 0)) {
      try
      {
        InternetAddress[] address = InternetAddress.parse(sender, false);
        if ((address != null) && (address.length > 0))
        {
          ((MimeMessage)msg).setSender(address[0]);
          if (address.length > 1) {
            reportError("Ignoring other senders.", tooManyAddresses(address, 1), 5);
          }
        }
      }
      catch (MessagingException ME)
      {
        reportError(ME.getMessage(), ME, 5);
      }
    }
  }
  
  private static AddressException tooManyAddresses(Address[] address, int offset)
  {
    String msg = Arrays.asList(address).subList(offset, address.length).toString();
    return new AddressException(msg);
  }
  
  private void setRecipient(Message msg, Properties props, String key, Message.RecipientType type)
  {
    String value = props.getProperty(key);
    if ((value != null) && (value.length() > 0)) {
      try
      {
        Address[] address = InternetAddress.parse(value, false);
        if ((address != null) && (address.length > 0)) {
          msg.setRecipients(type, address);
        }
      }
      catch (MessagingException ME)
      {
        reportError(ME.getMessage(), ME, 5);
      }
    }
  }
  
  private String toRawString(Message msg)
    throws MessagingException, IOException
  {
    if (msg != null)
    {
      int nbytes = Math.max(msg.getSize() + 1024, 1024);
      ByteArrayOutputStream out = new ByteArrayOutputStream(nbytes);
      msg.writeTo(out);
      return out.toString("US-ASCII");
    }
    return null;
  }
  
  private String toMsgString(Throwable t)
  {
    if (t == null) {
      return "null";
    }
    String encoding = getEncodingName();
    try
    {
      ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
      
      PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, encoding));
      
      pw.println(t.getMessage());
      t.printStackTrace(pw);
      pw.flush();
      pw.close();
      return out.toString(encoding);
    }
    catch (IOException badMimeCharset)
    {
      return t.toString() + ' ' + badMimeCharset.toString();
    }
  }
  
  private Object getAndSetContextClassLoader()
  {
    try
    {
      return AccessController.doPrivileged(GET_AND_SET_CCL);
    }
    catch (SecurityException ignore) {}
    return GET_AND_SET_CCL;
  }
  
  private void setContextClassLoader(Object ccl)
  {
    if ((ccl == null) || ((ccl instanceof ClassLoader))) {
      AccessController.doPrivileged(new GetAndSetContext(ccl));
    }
  }
  
  private static RuntimeException attachmentMismatch(String msg)
  {
    return new IndexOutOfBoundsException(msg);
  }
  
  private static RuntimeException attachmentMismatch(int expected, int found)
  {
    return attachmentMismatch("Attachments mismatched, expected " + expected + " but given " + found + '.');
  }
  
  private static MessagingException attach(MessagingException required, Exception optional)
  {
    if ((optional != null) && (!required.setNextException(optional)) && 
      ((optional instanceof MessagingException)))
    {
      MessagingException head = (MessagingException)optional;
      if (head.setNextException(required)) {
        return head;
      }
    }
    return required;
  }
  
  private static String atIndexMsg(int i)
  {
    return "At index: " + i + '.';
  }
  
  private static final class DefaultAuthenticator
    extends Authenticator
  {
    private final String pass;
    
    DefaultAuthenticator(String pass)
    {
      assert (pass != null);
      this.pass = pass;
    }
    
    protected final PasswordAuthentication getPasswordAuthentication()
    {
      return new PasswordAuthentication(getDefaultUserName(), this.pass);
    }
  }
  
  private static final class GetAndSetContext
    implements PrivilegedAction
  {
    private final Object source;
    
    GetAndSetContext(Object source)
    {
      this.source = source;
    }
    
    public final Object run()
    {
      Thread current = Thread.currentThread();
      ClassLoader ccl = current.getContextClassLoader();
      ClassLoader loader;
      ClassLoader loader;
      if (this.source == null)
      {
        loader = null;
      }
      else
      {
        ClassLoader loader;
        if ((this.source instanceof ClassLoader))
        {
          loader = (ClassLoader)this.source;
        }
        else
        {
          ClassLoader loader;
          if ((this.source instanceof Class)) {
            loader = ((Class)this.source).getClassLoader();
          } else {
            loader = this.source.getClass().getClassLoader();
          }
        }
      }
      if (ccl != loader)
      {
        current.setContextClassLoader(loader);
        return ccl;
      }
      return this;
    }
  }
  
  private static final class TailNameFormatter
    extends Formatter
  {
    private final String name;
    
    TailNameFormatter(String name)
    {
      assert (name != null);
      this.name = name;
    }
    
    public final String format(LogRecord record)
    {
      return "";
    }
    
    public final String getTail(Handler h)
    {
      return this.name;
    }
    
    public final boolean equals(Object o)
    {
      if ((o instanceof TailNameFormatter)) {
        return this.name.equals(((TailNameFormatter)o).name);
      }
      return false;
    }
    
    public final int hashCode()
    {
      return getClass().hashCode() + this.name.hashCode();
    }
    
    public final String toString()
    {
      return this.name;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\util\logging\MailHandler.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */