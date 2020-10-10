package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.bind.JAXBAssertionError;

abstract class AbstractCTBuilder
  implements CTBuilder
{
  protected final ComplexTypeFieldBuilder builder;
  protected final BGMBuilder bgmBuilder;
  protected final ExpressionPool pool;
  
  protected AbstractCTBuilder(ComplexTypeFieldBuilder _builder)
  {
    this.builder = _builder;
    this.bgmBuilder = this.builder.builder;
    this.pool = this.bgmBuilder.grammar.getPool();
  }
  
  protected static void _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\ct\AbstractCTBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */