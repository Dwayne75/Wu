package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.Options;
import com.sun.xml.xsom.parser.AnnotationParser;
import com.sun.xml.xsom.parser.AnnotationParserFactory;

public class AnnotationParserFactoryImpl
  implements AnnotationParserFactory
{
  private final JCodeModel codeModel;
  private final Options options;
  
  public AnnotationParserFactoryImpl(JCodeModel cm, Options opts)
  {
    this.codeModel = cm;
    this.options = opts;
  }
  
  public AnnotationParser create()
  {
    return new AnnotationParserImpl(this.codeModel, this.options);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\AnnotationParserFactoryImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */