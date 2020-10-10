package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.msv.datatype.xsd.WhiteSpaceProcessor;
import com.sun.tools.xjc.generator.util.WhitespaceNormalizer;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSSimpleType;

public class WhitespaceTransducer
  extends TransducerDecorator
{
  private final JCodeModel codeModel;
  private final WhitespaceNormalizer ws;
  
  private WhitespaceTransducer(Transducer _core, JCodeModel _codeModel, WhitespaceNormalizer _ws)
  {
    super(_core);
    this.codeModel = _codeModel;
    this.ws = _ws;
  }
  
  public static Transducer create(Transducer _core, JCodeModel _codeModel, WhitespaceNormalizer _ws)
  {
    if (_ws == WhitespaceNormalizer.PRESERVE) {
      return _core;
    }
    return new WhitespaceTransducer(_core, _codeModel, _ws);
  }
  
  public static Transducer create(Transducer _core, JCodeModel _codeModel, WhiteSpaceProcessor wsf)
  {
    return create(_core, _codeModel, getNormalizer(wsf));
  }
  
  public static Transducer create(Transducer _core, JCodeModel _codeModel, XSSimpleType t)
  {
    XSFacet f = t.getFacet("whiteSpace");
    if (f == null) {
      return _core;
    }
    return create(_core, _codeModel, WhitespaceNormalizer.parse(f.getValue()));
  }
  
  public boolean isBuiltin()
  {
    return this.core.isBuiltin();
  }
  
  public JExpression generateDeserializer(JExpression literal, DeserializerContext context)
  {
    return super.generateDeserializer(this.ws.generate(this.codeModel, literal), context);
  }
  
  private static WhitespaceNormalizer getNormalizer(WhiteSpaceProcessor proc)
  {
    if (proc == WhiteSpaceProcessor.theCollapse) {
      return WhitespaceNormalizer.COLLAPSE;
    }
    if (proc == WhiteSpaceProcessor.theReplace) {
      return WhitespaceNormalizer.REPLACE;
    }
    if (proc == WhiteSpaceProcessor.thePreserve) {
      return WhitespaceNormalizer.PRESERVE;
    }
    throw new JAXBAssertionError();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\xducer\WhitespaceTransducer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */