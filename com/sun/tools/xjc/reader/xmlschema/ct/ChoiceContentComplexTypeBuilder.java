package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.BindGreen;
import com.sun.tools.xjc.reader.xmlschema.ParticleBinder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSTerm;
import java.util.Collections;

final class ChoiceContentComplexTypeBuilder
  extends CTBuilder
{
  public boolean isApplicable(XSComplexType ct)
  {
    if (!this.bgmBuilder.getGlobalBinding().isChoiceContentPropertyEnabled()) {
      return false;
    }
    if (ct.getBaseType() != this.schemas.getAnyType()) {
      return false;
    }
    XSParticle p = ct.getContentType().asParticle();
    if (p == null) {
      return false;
    }
    XSModelGroup mg = getTopLevelModelGroup(p);
    if (mg.getCompositor() != XSModelGroup.CHOICE) {
      return false;
    }
    if (p.isRepeated()) {
      return false;
    }
    return true;
  }
  
  private XSModelGroup getTopLevelModelGroup(XSParticle p)
  {
    XSModelGroup mg = p.getTerm().asModelGroup();
    if (p.getTerm().isModelGroupDecl()) {
      mg = p.getTerm().asModelGroupDecl().getModelGroup();
    }
    return mg;
  }
  
  public void build(XSComplexType ct)
  {
    XSParticle p = ct.getContentType().asParticle();
    
    this.builder.recordBindingMode(ct, ComplexTypeBindingMode.NORMAL);
    
    this.bgmBuilder.getParticleBinder().build(p, Collections.singleton(p));
    
    this.green.attContainer(ct);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\ct\ChoiceContentComplexTypeBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */