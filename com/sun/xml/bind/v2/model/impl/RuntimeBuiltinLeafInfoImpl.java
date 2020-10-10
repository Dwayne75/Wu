package com.sun.xml.bind.v2.model.impl;

import com.sun.istack.ByteArrayDataSource;
import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.runtime.RuntimeBuiltinLeafInfo;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.NamespaceContext2;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.output.Pcdata;
import com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.util.ByteArrayOutputStreamEx;
import com.sun.xml.bind.v2.util.DataSourceSource;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.bind.MarshalException;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.SAXException;

public abstract class RuntimeBuiltinLeafInfoImpl<T>
  extends BuiltinLeafInfoImpl<Type, Class>
  implements RuntimeBuiltinLeafInfo, Transducer<T>
{
  private RuntimeBuiltinLeafInfoImpl(Class type, QName... typeNames)
  {
    super(type, typeNames);
    LEAVES.put(type, this);
  }
  
  public final Class getClazz()
  {
    return (Class)getType();
  }
  
  public final Transducer getTransducer()
  {
    return this;
  }
  
  public boolean useNamespace()
  {
    return false;
  }
  
  public final boolean isDefault()
  {
    return true;
  }
  
  public void declareNamespace(T o, XMLSerializer w)
    throws AccessorException
  {}
  
  public QName getTypeName(T instance)
  {
    return null;
  }
  
  private static abstract class StringImpl<T>
    extends RuntimeBuiltinLeafInfoImpl<T>
  {
    protected StringImpl(Class type, QName... typeNames)
    {
      super(typeNames, null);
    }
    
    public abstract String print(T paramT)
      throws AccessorException;
    
    public void writeText(XMLSerializer w, T o, String fieldName)
      throws IOException, SAXException, XMLStreamException, AccessorException
    {
      w.text(print(o), fieldName);
    }
    
    public void writeLeafElement(XMLSerializer w, Name tagName, T o, String fieldName)
      throws IOException, SAXException, XMLStreamException, AccessorException
    {
      w.leafElement(tagName, print(o), fieldName);
    }
  }
  
  private static abstract class PcdataImpl<T>
    extends RuntimeBuiltinLeafInfoImpl<T>
  {
    protected PcdataImpl(Class type, QName... typeNames)
    {
      super(typeNames, null);
    }
    
    public abstract Pcdata print(T paramT)
      throws AccessorException;
    
    public final void writeText(XMLSerializer w, T o, String fieldName)
      throws IOException, SAXException, XMLStreamException, AccessorException
    {
      w.text(print(o), fieldName);
    }
    
    public final void writeLeafElement(XMLSerializer w, Name tagName, T o, String fieldName)
      throws IOException, SAXException, XMLStreamException, AccessorException
    {
      w.leafElement(tagName, print(o), fieldName);
    }
  }
  
  public static final Map<Type, RuntimeBuiltinLeafInfoImpl<?>> LEAVES = new HashMap();
  public static final RuntimeBuiltinLeafInfoImpl<String> STRING = new StringImpl(String.class, new QName[] { createXS("string"), createXS("normalizedString"), createXS("anyURI"), createXS("token"), createXS("language"), createXS("Name"), createXS("NCName"), createXS("NMTOKEN"), createXS("ENTITY") })
  {
    public String parse(CharSequence text)
    {
      return text.toString();
    }
    
    public String print(String s)
    {
      return s;
    }
    
    public final void writeText(XMLSerializer w, String o, String fieldName)
      throws IOException, SAXException, XMLStreamException
    {
      w.text(o, fieldName);
    }
    
    public final void writeLeafElement(XMLSerializer w, Name tagName, String o, String fieldName)
      throws IOException, SAXException, XMLStreamException
    {
      w.leafElement(tagName, o, fieldName);
    }
  };
  public static final List<RuntimeBuiltinLeafInfoImpl<?>> builtinBeanInfos;
  private static final DatatypeFactory datatypeFactory;
  private static final Map<QName, String> xmlGregorianCalendarFormatString;
  private static final Map<QName, Integer> xmlGregorianCalendarFieldRef;
  
  private static byte[] decodeBase64(CharSequence text)
  {
    if ((text instanceof Base64Data))
    {
      Base64Data base64Data = (Base64Data)text;
      return base64Data.getExact();
    }
    return DatatypeConverterImpl._parseBase64Binary(text.toString());
  }
  
  private static QName createXS(String typeName)
  {
    return new QName("http://www.w3.org/2001/XMLSchema", typeName);
  }
  
  private static DatatypeFactory init()
  {
    try
    {
      return DatatypeFactory.newInstance();
    }
    catch (DatatypeConfigurationException e)
    {
      throw new Error(Messages.FAILED_TO_INITIALE_DATATYPE_FACTORY.format(new Object[0]), e);
    }
  }
  
  private static void checkXmlGregorianCalendarFieldRef(QName type, XMLGregorianCalendar cal)
    throws MarshalException
  {
    StringBuffer buf = new StringBuffer();
    int bitField = ((Integer)xmlGregorianCalendarFieldRef.get(type)).intValue();
    int l = 1;
    int pos = 0;
    while (bitField != 0)
    {
      int bit = bitField & 0x1;
      bitField >>>= 4;
      pos++;
      if (bit == 1) {
        switch (pos)
        {
        case 1: 
          if (cal.getSecond() == Integer.MIN_VALUE) {
            buf.append("  " + Messages.XMLGREGORIANCALENDAR_SEC);
          }
          break;
        case 2: 
          if (cal.getMinute() == Integer.MIN_VALUE) {
            buf.append("  " + Messages.XMLGREGORIANCALENDAR_MIN);
          }
          break;
        case 3: 
          if (cal.getHour() == Integer.MIN_VALUE) {
            buf.append("  " + Messages.XMLGREGORIANCALENDAR_HR);
          }
          break;
        case 4: 
          if (cal.getDay() == Integer.MIN_VALUE) {
            buf.append("  " + Messages.XMLGREGORIANCALENDAR_DAY);
          }
          break;
        case 5: 
          if (cal.getMonth() == Integer.MIN_VALUE) {
            buf.append("  " + Messages.XMLGREGORIANCALENDAR_MONTH);
          }
          break;
        case 6: 
          if (cal.getYear() == Integer.MIN_VALUE) {
            buf.append("  " + Messages.XMLGREGORIANCALENDAR_YEAR);
          }
          break;
        }
      }
    }
    if (buf.length() > 0) {
      throw new MarshalException(Messages.XMLGREGORIANCALENDAR_INVALID.format(new Object[] { type.getLocalPart() }) + buf.toString());
    }
  }
  
  static
  {
    RuntimeBuiltinLeafInfoImpl[] secondary = { new StringImpl(Character.class, new QName[] { createXS("unsignedShort") })
    
      new StringImpl
      {
        public Character parse(CharSequence text)
        {
          return Character.valueOf((char)DatatypeConverterImpl._parseInt(text));
        }
        
        public String print(Character v)
        {
          return Integer.toString(v.charValue());
        }
      }, new StringImpl(Calendar.class, new QName[] { DatatypeConstants.DATETIME })
      
      new StringImpl
      {
        public Calendar parse(CharSequence text)
        {
          return DatatypeConverterImpl._parseDateTime(text.toString());
        }
        
        public String print(Calendar v)
        {
          return DatatypeConverterImpl._printDateTime(v);
        }
      }, new StringImpl(GregorianCalendar.class, new QName[] { DatatypeConstants.DATETIME })
      
      new StringImpl
      {
        public GregorianCalendar parse(CharSequence text)
        {
          return DatatypeConverterImpl._parseDateTime(text.toString());
        }
        
        public String print(GregorianCalendar v)
        {
          return DatatypeConverterImpl._printDateTime(v);
        }
      }, new StringImpl(Date.class, new QName[] { DatatypeConstants.DATETIME })
      
      new StringImpl
      {
        public Date parse(CharSequence text)
        {
          return DatatypeConverterImpl._parseDateTime(text.toString()).getTime();
        }
        
        public String print(Date v)
        {
          GregorianCalendar cal = new GregorianCalendar(0, 0, 0);
          cal.setTime(v);
          return DatatypeConverterImpl._printDateTime(cal);
        }
      }, new StringImpl(File.class, new QName[] { createXS("string") })
      
      new StringImpl
      {
        public File parse(CharSequence text)
        {
          return new File(WhiteSpaceProcessor.trim(text).toString());
        }
        
        public String print(File v)
        {
          return v.getPath();
        }
      }, new StringImpl(URL.class, new QName[] { createXS("anyURI") })
      
      new StringImpl
      {
        public URL parse(CharSequence text)
          throws SAXException
        {
          TODO.checkSpec("JSR222 Issue #42");
          try
          {
            return new URL(WhiteSpaceProcessor.trim(text).toString());
          }
          catch (MalformedURLException e)
          {
            UnmarshallingContext.getInstance().handleError(e);
          }
          return null;
        }
        
        public String print(URL v)
        {
          return v.toExternalForm();
        }
      }, new StringImpl(URI.class, new QName[] { createXS("string") })
      
      new StringImpl
      {
        public URI parse(CharSequence text)
          throws SAXException
        {
          try
          {
            return new URI(text.toString());
          }
          catch (URISyntaxException e)
          {
            UnmarshallingContext.getInstance().handleError(e);
          }
          return null;
        }
        
        public String print(URI v)
        {
          return v.toString();
        }
      }, new StringImpl(Class.class, new QName[] { createXS("string") })
      
      new PcdataImpl
      {
        public Class parse(CharSequence text)
          throws SAXException
        {
          TODO.checkSpec("JSR222 Issue #42");
          try
          {
            String name = WhiteSpaceProcessor.trim(text).toString();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) {
              return cl.loadClass(name);
            }
            return Class.forName(name);
          }
          catch (ClassNotFoundException e)
          {
            UnmarshallingContext.getInstance().handleError(e);
          }
          return null;
        }
        
        public String print(Class v)
        {
          return v.getName();
        }
      }, new PcdataImpl(Image.class, new QName[] { createXS("base64Binary") })
      
      new PcdataImpl
      {
        public Image parse(CharSequence text)
          throws SAXException
        {
          try
          {
            InputStream is;
            InputStream is;
            if ((text instanceof Base64Data)) {
              is = ((Base64Data)text).getInputStream();
            } else {
              is = new ByteArrayInputStream(RuntimeBuiltinLeafInfoImpl.decodeBase64(text));
            }
            try
            {
              return ImageIO.read(is);
            }
            finally
            {
              is.close();
            }
            return null;
          }
          catch (IOException e)
          {
            UnmarshallingContext.getInstance().handleError(e);
          }
        }
        
        private BufferedImage convertToBufferedImage(Image image)
          throws IOException
        {
          if ((image instanceof BufferedImage)) {
            return (BufferedImage)image;
          }
          MediaTracker tracker = new MediaTracker(new Component() {});
          tracker.addImage(image, 0);
          try
          {
            tracker.waitForAll();
          }
          catch (InterruptedException e)
          {
            throw new IOException(e.getMessage());
          }
          BufferedImage bufImage = new BufferedImage(image.getWidth(null), image.getHeight(null), 2);
          
          Graphics g = bufImage.createGraphics();
          g.drawImage(image, 0, 0, null);
          return bufImage;
        }
        
        public Base64Data print(Image v)
        {
          ByteArrayOutputStreamEx imageData = new ByteArrayOutputStreamEx();
          XMLSerializer xs = XMLSerializer.getInstance();
          
          String mimeType = xs.getXMIMEContentType();
          if ((mimeType == null) || (mimeType.startsWith("image/*"))) {
            mimeType = "image/png";
          }
          try
          {
            Iterator<ImageWriter> itr = ImageIO.getImageWritersByMIMEType(mimeType);
            if (itr.hasNext())
            {
              ImageWriter w = (ImageWriter)itr.next();
              ImageOutputStream os = ImageIO.createImageOutputStream(imageData);
              w.setOutput(os);
              w.write(convertToBufferedImage(v));
              os.close();
              w.dispose();
            }
            else
            {
              xs.handleEvent(new ValidationEventImpl(1, Messages.NO_IMAGE_WRITER.format(new Object[] { mimeType }), xs.getCurrentLocation(null)));
              
              throw new RuntimeException("no encoder for MIME type " + mimeType);
            }
          }
          catch (IOException e)
          {
            xs.handleError(e);
            
            throw new RuntimeException(e);
          }
          Base64Data bd = new Base64Data();
          imageData.set(bd, mimeType);
          return bd;
        }
      }, new PcdataImpl(DataHandler.class, new QName[] { createXS("base64Binary") })
      
      new PcdataImpl
      {
        public DataHandler parse(CharSequence text)
        {
          if ((text instanceof Base64Data)) {
            return ((Base64Data)text).getDataHandler();
          }
          return new DataHandler(new ByteArrayDataSource(RuntimeBuiltinLeafInfoImpl.decodeBase64(text), UnmarshallingContext.getInstance().getXMIMEContentType()));
        }
        
        public Base64Data print(DataHandler v)
        {
          Base64Data bd = new Base64Data();
          bd.set(v);
          return bd;
        }
      }, new PcdataImpl(Source.class, new QName[] { createXS("base64Binary") })
      
      new StringImpl
      {
        public Source parse(CharSequence text)
          throws SAXException
        {
          try
          {
            if ((text instanceof Base64Data)) {
              return new DataSourceSource(((Base64Data)text).getDataHandler());
            }
            return new DataSourceSource(new ByteArrayDataSource(RuntimeBuiltinLeafInfoImpl.decodeBase64(text), UnmarshallingContext.getInstance().getXMIMEContentType()));
          }
          catch (MimeTypeParseException e)
          {
            UnmarshallingContext.getInstance().handleError(e);
          }
          return null;
        }
        
        public Base64Data print(Source v)
        {
          XMLSerializer xs = XMLSerializer.getInstance();
          Base64Data bd = new Base64Data();
          
          String contentType = xs.getXMIMEContentType();
          MimeType mt = null;
          if (contentType != null) {
            try
            {
              mt = new MimeType(contentType);
            }
            catch (MimeTypeParseException e)
            {
              xs.handleError(e);
            }
          }
          if ((v instanceof DataSourceSource))
          {
            DataSource ds = ((DataSourceSource)v).getDataSource();
            
            String dsct = ds.getContentType();
            if ((dsct != null) && ((contentType == null) || (contentType.equals(dsct))))
            {
              bd.set(new DataHandler(ds));
              return bd;
            }
          }
          String charset = null;
          if (mt != null) {
            charset = mt.getParameter("charset");
          }
          if (charset == null) {
            charset = "UTF-8";
          }
          try
          {
            ByteArrayOutputStreamEx baos = new ByteArrayOutputStreamEx();
            xs.getIdentityTransformer().transform(v, new StreamResult(new OutputStreamWriter(baos, charset)));
            
            baos.set(bd, "application/xml; charset=" + charset);
            return bd;
          }
          catch (TransformerException e)
          {
            xs.handleError(e);
          }
          catch (UnsupportedEncodingException e)
          {
            xs.handleError(e);
          }
          bd.set(new byte[0], "application/xml");
          return bd;
        }
      }, new StringImpl(XMLGregorianCalendar.class, new QName[] { createXS("anySimpleType"), DatatypeConstants.DATE, DatatypeConstants.DATETIME, DatatypeConstants.TIME, DatatypeConstants.GMONTH, DatatypeConstants.GDAY, DatatypeConstants.GYEAR, DatatypeConstants.GYEARMONTH, DatatypeConstants.GMONTHDAY })
      {
        public String print(XMLGregorianCalendar cal)
        {
          XMLSerializer xs = XMLSerializer.getInstance();
          
          QName type = xs.getSchemaType();
          if (type != null) {
            try
            {
              RuntimeBuiltinLeafInfoImpl.checkXmlGregorianCalendarFieldRef(type, cal);
              String format = (String)RuntimeBuiltinLeafInfoImpl.xmlGregorianCalendarFormatString.get(type);
              if (format != null) {
                return format(format, cal);
              }
            }
            catch (MarshalException e)
            {
              System.out.println(e.toString());
              return "";
            }
          }
          return cal.toXMLFormat();
        }
        
        public XMLGregorianCalendar parse(CharSequence lexical)
          throws SAXException
        {
          try
          {
            return RuntimeBuiltinLeafInfoImpl.datatypeFactory.newXMLGregorianCalendar(lexical.toString());
          }
          catch (Exception e)
          {
            UnmarshallingContext.getInstance().handleError(e);
          }
          return null;
        }
        
        private String format(String format, XMLGregorianCalendar value)
        {
          StringBuilder buf = new StringBuilder();
          int fidx = 0;int flen = format.length();
          while (fidx < flen)
          {
            char fch = format.charAt(fidx++);
            if (fch != '%') {
              buf.append(fch);
            } else {
              switch (format.charAt(fidx++))
              {
              case 'Y': 
                printNumber(buf, value.getEonAndYear(), 4);
                break;
              case 'M': 
                printNumber(buf, value.getMonth(), 2);
                break;
              case 'D': 
                printNumber(buf, value.getDay(), 2);
                break;
              case 'h': 
                printNumber(buf, value.getHour(), 2);
                break;
              case 'm': 
                printNumber(buf, value.getMinute(), 2);
                break;
              case 's': 
                printNumber(buf, value.getSecond(), 2);
                if (value.getFractionalSecond() != null)
                {
                  String frac = value.getFractionalSecond().toString();
                  
                  buf.append(frac.substring(1, frac.length()));
                }
                break;
              case 'z': 
                int offset = value.getTimezone();
                if (offset == 0)
                {
                  buf.append('Z');
                }
                else if (offset != Integer.MIN_VALUE)
                {
                  if (offset < 0)
                  {
                    buf.append('-');
                    offset *= -1;
                  }
                  else
                  {
                    buf.append('+');
                  }
                  printNumber(buf, offset / 60, 2);
                  buf.append(':');
                  printNumber(buf, offset % 60, 2);
                }
                break;
              default: 
                throw new InternalError();
              }
            }
          }
          return buf.toString();
        }
        
        private void printNumber(StringBuilder out, BigInteger number, int nDigits)
        {
          String s = number.toString();
          for (int i = s.length(); i < nDigits; i++) {
            out.append('0');
          }
          out.append(s);
        }
        
        private void printNumber(StringBuilder out, int number, int nDigits)
        {
          String s = String.valueOf(number);
          for (int i = s.length(); i < nDigits; i++) {
            out.append('0');
          }
          out.append(s);
        }
        
        public QName getTypeName(XMLGregorianCalendar cal)
        {
          return cal.getXMLSchemaType();
        }
      } };
    RuntimeBuiltinLeafInfoImpl[] primary = { STRING, new StringImpl(Boolean.class, new QName[] { createXS("boolean") })
    
      new PcdataImpl
      {
        public Boolean parse(CharSequence text)
        {
          return Boolean.valueOf(DatatypeConverterImpl._parseBoolean(text));
        }
        
        public String print(Boolean v)
        {
          return v.toString();
        }
      }, new PcdataImpl(byte[].class, new QName[] { createXS("base64Binary"), createXS("hexBinary") })
      
      new StringImpl
      {
        public byte[] parse(CharSequence text)
        {
          return RuntimeBuiltinLeafInfoImpl.decodeBase64(text);
        }
        
        public Base64Data print(byte[] v)
        {
          XMLSerializer w = XMLSerializer.getInstance();
          Base64Data bd = new Base64Data();
          String mimeType = w.getXMIMEContentType();
          bd.set(v, mimeType);
          return bd;
        }
      }, new StringImpl(Byte.class, new QName[] { createXS("byte") })
      
      new StringImpl
      {
        public Byte parse(CharSequence text)
        {
          return Byte.valueOf(DatatypeConverterImpl._parseByte(text));
        }
        
        public String print(Byte v)
        {
          return DatatypeConverterImpl._printByte(v.byteValue());
        }
      }, new StringImpl(Short.class, new QName[] { createXS("short"), createXS("unsignedByte") })
      
      new StringImpl
      {
        public Short parse(CharSequence text)
        {
          return Short.valueOf(DatatypeConverterImpl._parseShort(text));
        }
        
        public String print(Short v)
        {
          return DatatypeConverterImpl._printShort(v.shortValue());
        }
      }, new StringImpl(Integer.class, new QName[] { createXS("int"), createXS("unsignedShort") })
      
      new StringImpl
      {
        public Integer parse(CharSequence text)
        {
          return Integer.valueOf(DatatypeConverterImpl._parseInt(text));
        }
        
        public String print(Integer v)
        {
          return DatatypeConverterImpl._printInt(v.intValue());
        }
      }, new StringImpl(Long.class, new QName[] { createXS("long"), createXS("unsignedInt") })
      
      new StringImpl
      {
        public Long parse(CharSequence text)
        {
          return Long.valueOf(DatatypeConverterImpl._parseLong(text));
        }
        
        public String print(Long v)
        {
          return DatatypeConverterImpl._printLong(v.longValue());
        }
      }, new StringImpl(Float.class, new QName[] { createXS("float") })
      
      new StringImpl
      {
        public Float parse(CharSequence text)
        {
          return Float.valueOf(DatatypeConverterImpl._parseFloat(text.toString()));
        }
        
        public String print(Float v)
        {
          return DatatypeConverterImpl._printFloat(v.floatValue());
        }
      }, new StringImpl(Double.class, new QName[] { createXS("double") })
      
      new StringImpl
      {
        public Double parse(CharSequence text)
        {
          return Double.valueOf(DatatypeConverterImpl._parseDouble(text));
        }
        
        public String print(Double v)
        {
          return DatatypeConverterImpl._printDouble(v.doubleValue());
        }
      }, new StringImpl(BigInteger.class, new QName[] { createXS("integer"), createXS("positiveInteger"), createXS("negativeInteger"), createXS("nonPositiveInteger"), createXS("nonNegativeInteger"), createXS("unsignedLong") })
      
      new StringImpl
      {
        public BigInteger parse(CharSequence text)
        {
          return DatatypeConverterImpl._parseInteger(text);
        }
        
        public String print(BigInteger v)
        {
          return DatatypeConverterImpl._printInteger(v);
        }
      }, new StringImpl(BigDecimal.class, new QName[] { createXS("decimal") })
      
      new StringImpl
      {
        public BigDecimal parse(CharSequence text)
        {
          return DatatypeConverterImpl._parseDecimal(text.toString());
        }
        
        public String print(BigDecimal v)
        {
          return DatatypeConverterImpl._printDecimal(v);
        }
      }, new StringImpl(QName.class, new QName[] { createXS("QName") })
      
      new StringImpl
      {
        public QName parse(CharSequence text)
          throws SAXException
        {
          try
          {
            return DatatypeConverterImpl._parseQName(text.toString(), UnmarshallingContext.getInstance());
          }
          catch (IllegalArgumentException e)
          {
            UnmarshallingContext.getInstance().handleError(e);
          }
          return null;
        }
        
        public String print(QName v)
        {
          return DatatypeConverterImpl._printQName(v, XMLSerializer.getInstance().getNamespaceContext());
        }
        
        public boolean useNamespace()
        {
          return true;
        }
        
        public void declareNamespace(QName v, XMLSerializer w)
        {
          w.getNamespaceContext().declareNamespace(v.getNamespaceURI(), v.getPrefix(), false);
        }
      }, new StringImpl(Duration.class, new QName[] { createXS("duration") })
      
      new StringImpl
      {
        public String print(Duration duration)
        {
          return duration.toString();
        }
        
        public Duration parse(CharSequence lexical)
        {
          TODO.checkSpec("JSR222 Issue #42");
          return RuntimeBuiltinLeafInfoImpl.datatypeFactory.newDuration(lexical.toString());
        }
      }, new StringImpl(Void.class, new QName[0])
      {
        public String print(Void value)
        {
          return "";
        }
        
        public Void parse(CharSequence lexical)
        {
          return null;
        }
      } };
    List<RuntimeBuiltinLeafInfoImpl<?>> l = new ArrayList(secondary.length + primary.length + 1);
    for (RuntimeBuiltinLeafInfoImpl<?> item : secondary) {
      l.add(item);
    }
    try
    {
      l.add(new UUIDImpl());
    }
    catch (LinkageError e) {}
    for (RuntimeBuiltinLeafInfoImpl<?> item : primary) {
      l.add(item);
    }
    builtinBeanInfos = Collections.unmodifiableList(l);
    
    datatypeFactory = init();
    
    xmlGregorianCalendarFormatString = new HashMap();
    
    Map<QName, String> m = xmlGregorianCalendarFormatString;
    
    m.put(DatatypeConstants.DATETIME, "%Y-%M-%DT%h:%m:%s%z");
    m.put(DatatypeConstants.DATE, "%Y-%M-%D%z");
    m.put(DatatypeConstants.TIME, "%h:%m:%s%z");
    m.put(DatatypeConstants.GMONTH, "--%M--%z");
    m.put(DatatypeConstants.GDAY, "---%D%z");
    m.put(DatatypeConstants.GYEAR, "%Y%z");
    m.put(DatatypeConstants.GYEARMONTH, "%Y-%M%z");
    m.put(DatatypeConstants.GMONTHDAY, "--%M-%D%z");
    
    xmlGregorianCalendarFieldRef = new HashMap();
    
    Map<QName, Integer> f = xmlGregorianCalendarFieldRef;
    f.put(DatatypeConstants.DATETIME, Integer.valueOf(17895697));
    f.put(DatatypeConstants.DATE, Integer.valueOf(17895424));
    f.put(DatatypeConstants.TIME, Integer.valueOf(16777489));
    f.put(DatatypeConstants.GDAY, Integer.valueOf(16781312));
    f.put(DatatypeConstants.GMONTH, Integer.valueOf(16842752));
    f.put(DatatypeConstants.GYEAR, Integer.valueOf(17825792));
    f.put(DatatypeConstants.GYEARMONTH, Integer.valueOf(17891328));
    f.put(DatatypeConstants.GMONTHDAY, Integer.valueOf(16846848));
  }
  
  private static class UUIDImpl
    extends RuntimeBuiltinLeafInfoImpl.StringImpl<UUID>
  {
    public UUIDImpl()
    {
      super(new QName[] { RuntimeBuiltinLeafInfoImpl.createXS("string") });
    }
    
    public UUID parse(CharSequence text)
      throws SAXException
    {
      TODO.checkSpec("JSR222 Issue #42");
      try
      {
        return UUID.fromString(WhiteSpaceProcessor.trim(text).toString());
      }
      catch (IllegalArgumentException e)
      {
        UnmarshallingContext.getInstance().handleError(e);
      }
      return null;
    }
    
    public String print(UUID v)
    {
      return v.toString();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\RuntimeBuiltinLeafInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */