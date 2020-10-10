package com.sun.xml.xsom.impl.parser;

import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.impl.Ref.ContentType;
import com.sun.xml.xsom.impl.Ref.Type;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public final class BaseContentRef
  implements Ref.ContentType, Patch
{
  private final Ref.Type baseType;
  private final Locator loc;
  
  public BaseContentRef(final NGCCRuntimeEx $runtime, Ref.Type _baseType)
  {
    this.baseType = _baseType;
    $runtime.addPatcher(this);
    $runtime.addErrorChecker(new Patch()
    {
      public void run()
        throws SAXException
      {
        XSType t = BaseContentRef.this.baseType.getType();
        if ((t.isComplexType()) && (t.asComplexType().getContentType().asParticle() != null)) {
          $runtime.reportError(Messages.format("SimpleContentExpected", new Object[] { t.getTargetNamespace(), t.getName() }), BaseContentRef.this.loc);
        }
      }
    });
    this.loc = $runtime.copyLocator();
  }
  
  public XSContentType getContentType()
  {
    XSType t = this.baseType.getType();
    if (t.asComplexType() != null) {
      return t.asComplexType().getContentType();
    }
    return t.asSimpleType();
  }
  
  public void run()
    throws SAXException
  {
    if ((this.baseType instanceof Patch)) {
      ((Patch)this.baseType).run();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\parser\BaseContentRef.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */