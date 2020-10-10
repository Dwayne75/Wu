package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.CClassInfoParent.Package;
import com.sun.tools.xjc.model.CEnumConstant;
import com.sun.tools.xjc.model.CEnumLeafInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.xml.bind.api.impl.NameConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.w3c.dom.Element;

public final class BIEnumeration
  implements BIConversion
{
  private final Element e;
  private final TypeUse xducer;
  
  private BIEnumeration(Element _e, TypeUse _xducer)
  {
    this.e = _e;
    this.xducer = _xducer;
  }
  
  public String name()
  {
    return DOMUtil.getAttribute(this.e, "name");
  }
  
  public TypeUse getTransducer()
  {
    return this.xducer;
  }
  
  static BIEnumeration create(Element dom, BindInfo parent)
  {
    return new BIEnumeration(dom, new CEnumLeafInfo(parent.model, null, new CClassInfoParent.Package(parent.getTargetPackage()), DOMUtil.getAttribute(dom, "name"), CBuiltinLeafInfo.STRING, buildMemberList(parent.model, dom), null, null, DOMLocator.getLocationInfo(dom)));
  }
  
  static BIEnumeration create(Element dom, BIElement parent)
  {
    return new BIEnumeration(dom, new CEnumLeafInfo(parent.parent.model, null, parent.clazz, DOMUtil.getAttribute(dom, "name"), CBuiltinLeafInfo.STRING, buildMemberList(parent.parent.model, dom), null, null, DOMLocator.getLocationInfo(dom)));
  }
  
  private static List<CEnumConstant> buildMemberList(Model model, Element dom)
  {
    List<CEnumConstant> r = new ArrayList();
    
    String members = DOMUtil.getAttribute(dom, "members");
    if (members == null) {
      members = "";
    }
    StringTokenizer tokens = new StringTokenizer(members);
    while (tokens.hasMoreTokens())
    {
      String token = tokens.nextToken();
      r.add(new CEnumConstant(model.getNameConverter().toConstantName(token), null, token, null));
    }
    return r;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\dtd\bindinfo\BIEnumeration.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */