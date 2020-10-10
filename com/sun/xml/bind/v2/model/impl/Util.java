package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.AnnotationSource;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSchemaTypes;
import javax.xml.namespace.QName;

final class Util
{
  static <T, C, F, M> QName calcSchemaType(AnnotationReader<T, C, F, M> reader, AnnotationSource primarySource, C enclosingClass, T individualType, Locatable src)
  {
    XmlSchemaType xst = (XmlSchemaType)primarySource.readAnnotation(XmlSchemaType.class);
    if (xst != null) {
      return new QName(xst.namespace(), xst.name());
    }
    XmlSchemaTypes xsts = (XmlSchemaTypes)reader.getPackageAnnotation(XmlSchemaTypes.class, enclosingClass, src);
    XmlSchemaType[] values = null;
    if (xsts != null)
    {
      values = xsts.value();
    }
    else
    {
      xst = (XmlSchemaType)reader.getPackageAnnotation(XmlSchemaType.class, enclosingClass, src);
      if (xst != null)
      {
        values = new XmlSchemaType[1];
        values[0] = xst;
      }
    }
    if (values != null) {
      for (XmlSchemaType item : values) {
        if (reader.getClassValue(item, "type").equals(individualType)) {
          return new QName(item.namespace(), item.name());
        }
      }
    }
    return null;
  }
  
  static MimeType calcExpectedMediaType(AnnotationSource primarySource, ModelBuilder builder)
  {
    XmlMimeType xmt = (XmlMimeType)primarySource.readAnnotation(XmlMimeType.class);
    if (xmt == null) {
      return null;
    }
    try
    {
      return new MimeType(xmt.value());
    }
    catch (MimeTypeParseException e)
    {
      builder.reportError(new IllegalAnnotationException(Messages.ILLEGAL_MIME_TYPE.format(new Object[] { xmt.value(), e.getMessage() }), xmt));
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\Util.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */