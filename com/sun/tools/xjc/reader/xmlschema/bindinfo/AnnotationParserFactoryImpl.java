package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.SchemaCache;
import com.sun.tools.xjc.reader.xmlschema.Messages;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.xsom.parser.AnnotationContext;
import com.sun.xml.xsom.parser.AnnotationParser;
import com.sun.xml.xsom.parser.AnnotationParserFactory;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.validation.ValidatorHandler;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.XMLFilterImpl;

public class AnnotationParserFactoryImpl
  implements AnnotationParserFactory
{
  private final Options options;
  private ValidatorHandler validator;
  
  public AnnotationParserFactoryImpl(Options opts)
  {
    this.options = opts;
  }
  
  public AnnotationParser create()
  {
    new AnnotationParser()
    {
      private Unmarshaller u = BindInfo.getJAXBContext().createUnmarshaller();
      private UnmarshallerHandler handler;
      
      public ContentHandler getContentHandler(AnnotationContext context, String parentElementName, final ErrorHandler errorHandler, EntityResolver entityResolver)
      {
        if (this.handler != null) {
          throw new AssertionError();
        }
        if (AnnotationParserFactoryImpl.this.options.debugMode) {
          try
          {
            this.u.setEventHandler(new DefaultValidationEventHandler());
          }
          catch (JAXBException e)
          {
            throw new AssertionError(e);
          }
        }
        this.handler = this.u.getUnmarshallerHandler();
        
        new ForkingFilter(this.handler)
        {
          public void startElement(String uri, String localName, String qName, Attributes atts)
            throws SAXException
          {
            super.startElement(uri, localName, qName, atts);
            if (((uri.equals("http://java.sun.com/xml/ns/jaxb")) || (uri.equals("http://java.sun.com/xml/ns/jaxb/xjc"))) && (getSideHandler() == null))
            {
              if (AnnotationParserFactoryImpl.this.validator == null) {
                AnnotationParserFactoryImpl.this.validator = BindInfo.bindingFileSchema.newValidator();
              }
              AnnotationParserFactoryImpl.this.validator.setErrorHandler(errorHandler);
              startForking(uri, localName, qName, atts, new AnnotationParserFactoryImpl.ValidatorProtecter(AnnotationParserFactoryImpl.this.validator));
            }
            for (int i = atts.getLength() - 1; i >= 0; i--) {
              if ((atts.getURI(i).equals("http://www.w3.org/2005/05/xmlmime")) && (atts.getLocalName(i).equals("expectedContentTypes"))) {
                errorHandler.warning(new SAXParseException(Messages.format("UnusedCustomizationChecker.WarnUnusedExpectedContentTypes", new Object[0]), getDocumentLocator()));
              }
            }
          }
        };
      }
      
      public BindInfo getResult(Object existing)
      {
        if (this.handler == null) {
          throw new AssertionError();
        }
        try
        {
          BindInfo result = (BindInfo)this.handler.getResult();
          if (existing != null)
          {
            BindInfo bie = (BindInfo)existing;
            bie.absorb(result);
            return bie;
          }
          if (!result.isPointless()) {
            return result;
          }
          return null;
        }
        catch (JAXBException e)
        {
          throw new AssertionError(e);
        }
      }
    };
  }
  
  private static final class ValidatorProtecter
    extends XMLFilterImpl
  {
    public ValidatorProtecter(ContentHandler h)
    {
      setContentHandler(h);
    }
    
    public void startPrefixMapping(String prefix, String uri)
      throws SAXException
    {
      super.startPrefixMapping(prefix.intern(), uri);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\bindinfo\AnnotationParserFactoryImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */