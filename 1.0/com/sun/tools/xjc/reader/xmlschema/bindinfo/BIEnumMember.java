package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.tools.xjc.grammar.xducer.EnumerationXducer.MemberInfo;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public class BIEnumMember
  extends AbstractDeclarationImpl
{
  private final String memberName;
  private final String javadoc;
  
  public BIEnumMember(Locator loc, String _memberName, String _javadoc)
  {
    super(loc);
    this.memberName = _memberName;
    this.javadoc = _javadoc;
  }
  
  public String getMemberName()
  {
    if (this.memberName == null) {
      return null;
    }
    BIGlobalBinding gb = getBuilder().getGlobalBinding();
    if (gb.isJavaNamingConventionEnabled()) {
      return gb.getNameConverter().toConstantName(this.memberName);
    }
    return this.memberName;
  }
  
  public String getJavadoc()
  {
    return this.javadoc;
  }
  
  public EnumerationXducer.MemberInfo createMemberInfo()
  {
    return new EnumerationXducer.MemberInfo(getMemberName(), this.javadoc);
  }
  
  public QName getName()
  {
    return NAME;
  }
  
  public static final QName NAME = new QName("http://java.sun.com/xml/ns/jaxb", "typesafeEnumMember");
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIEnumMember.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */