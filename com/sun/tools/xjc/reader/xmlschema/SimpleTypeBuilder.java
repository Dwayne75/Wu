package com.sun.tools.xjc.reader.xmlschema;

import com.sun.codemodel.JJavaName;
import com.sun.codemodel.util.JavadocEscapeWriter;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CClassInfoParent;
import com.sun.tools.xjc.model.CClassInfoParent.Package;
import com.sun.tools.xjc.model.CClassRef;
import com.sun.tools.xjc.model.CEnumConstant;
import com.sun.tools.xjc.model.CEnumLeafInfo;
import com.sun.tools.xjc.model.CNonElement;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.model.TypeUseFactory;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion.Static;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIEnum;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIEnumMember;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.EnumMemberMode;
import com.sun.tools.xjc.util.MimeTypeRange;
import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.bind.v2.runtime.SwaRefAdapter;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.XSVariety;
import com.sun.xml.xsom.XmlString;
import com.sun.xml.xsom.impl.util.SchemaWriter;
import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.activation.MimeTypeParseException;
import org.xml.sax.Locator;

public final class SimpleTypeBuilder
  extends BindingComponent
{
  protected final BGMBuilder builder = (BGMBuilder)Ring.get(BGMBuilder.class);
  private final Model model = (Model)Ring.get(Model.class);
  public final Stack<XSComponent> refererStack = new Stack();
  private final Set<XSComponent> acknowledgedXmimeContentTypes = new HashSet();
  private XSSimpleType initiatingType;
  public static final Map<String, TypeUse> builtinConversions;
  
  public TypeUse build(XSSimpleType type)
  {
    XSSimpleType oldi = this.initiatingType;
    this.initiatingType = type;
    
    TypeUse e = checkRefererCustomization(type);
    if (e == null) {
      e = compose(type);
    }
    this.initiatingType = oldi;
    
    return e;
  }
  
  public TypeUse buildDef(XSSimpleType type)
  {
    XSSimpleType oldi = this.initiatingType;
    this.initiatingType = type;
    
    TypeUse e = (TypeUse)type.apply(this.composer);
    
    this.initiatingType = oldi;
    
    return e;
  }
  
  private BIConversion getRefererCustomization()
  {
    BindInfo info = this.builder.getBindInfo(getReferer());
    BIProperty prop = (BIProperty)info.get(BIProperty.class);
    if (prop == null) {
      return null;
    }
    return prop.getConv();
  }
  
  public XSComponent getReferer()
  {
    return (XSComponent)this.refererStack.peek();
  }
  
  private TypeUse checkRefererCustomization(XSSimpleType type)
  {
    XSComponent top = getReferer();
    if ((top instanceof XSElementDecl))
    {
      XSElementDecl eref = (XSElementDecl)top;
      assert (eref.getType() == type);
      
      BindInfo info = this.builder.getBindInfo(top);
      BIConversion conv = (BIConversion)info.get(BIConversion.class);
      if (conv != null)
      {
        conv.markAsAcknowledged();
        
        return conv.getTypeUse(type);
      }
      detectJavaTypeCustomization();
    }
    else if ((top instanceof XSAttributeDecl))
    {
      XSAttributeDecl aref = (XSAttributeDecl)top;
      assert (aref.getType() == type);
      detectJavaTypeCustomization();
    }
    else if ((top instanceof XSComplexType))
    {
      XSComplexType tref = (XSComplexType)top;
      assert ((tref.getBaseType() == type) || (tref.getContentType() == type));
      detectJavaTypeCustomization();
    }
    else if (top != type)
    {
      if (!$assertionsDisabled) {
        throw new AssertionError();
      }
    }
    BIConversion conv = getRefererCustomization();
    if (conv != null)
    {
      conv.markAsAcknowledged();
      
      return conv.getTypeUse(type);
    }
    return null;
  }
  
  private void detectJavaTypeCustomization()
  {
    BindInfo info = this.builder.getBindInfo(getReferer());
    BIConversion conv = (BIConversion)info.get(BIConversion.class);
    if (conv != null)
    {
      conv.markAsAcknowledged();
      
      getErrorReporter().error(conv.getLocation(), "SimpleTypeBuilder.UnnestedJavaTypeCustomization", new Object[0]);
    }
  }
  
  TypeUse compose(XSSimpleType t)
  {
    TypeUse e = find(t);
    if (e != null) {
      return e;
    }
    return (TypeUse)t.apply(this.composer);
  }
  
  public final XSSimpleTypeFunction<TypeUse> composer = new XSSimpleTypeFunction()
  {
    public TypeUse listSimpleType(XSListSimpleType type)
    {
      XSSimpleType itemType = type.getItemType();
      SimpleTypeBuilder.this.refererStack.push(itemType);
      TypeUse tu = TypeUseFactory.makeCollection(SimpleTypeBuilder.this.build(type.getItemType()));
      SimpleTypeBuilder.this.refererStack.pop();
      return tu;
    }
    
    public TypeUse unionSimpleType(XSUnionSimpleType type)
    {
      boolean isCollection = false;
      for (int i = 0; i < type.getMemberSize(); i++) {
        if (type.getMember(i).getVariety() == XSVariety.LIST)
        {
          isCollection = true;
          break;
        }
      }
      TypeUse r = CBuiltinLeafInfo.STRING;
      if (isCollection) {
        r = TypeUseFactory.makeCollection(r);
      }
      return r;
    }
    
    public TypeUse restrictionSimpleType(XSRestrictionSimpleType type)
    {
      return SimpleTypeBuilder.this.compose(type.getSimpleBaseType());
    }
  };
  private static final Set<String> builtinTypeSafeEnumCapableTypes;
  private static final BigInteger LONG_MIN;
  private static final BigInteger LONG_MAX;
  private static final BigInteger INT_MIN;
  private static final BigInteger INT_MAX;
  
  private TypeUse find(XSSimpleType type)
  {
    boolean noAutoEnum = false;
    
    BindInfo info = this.builder.getBindInfo(type);
    BIConversion conv = (BIConversion)info.get(BIConversion.class);
    if (conv != null)
    {
      conv.markAsAcknowledged();
      return conv.getTypeUse(type);
    }
    BIEnum en = (BIEnum)info.get(BIEnum.class);
    if (en != null)
    {
      en.markAsAcknowledged();
      if (!en.isMapped())
      {
        noAutoEnum = true;
      }
      else
      {
        if (!canBeMappedToTypeSafeEnum(type))
        {
          getErrorReporter().error(en.getLocation(), "ConversionFinder.CannotBeTypeSafeEnum", new Object[0]);
          
          getErrorReporter().error(type.getLocator(), "ConversionFinder.CannotBeTypeSafeEnum.Location", new Object[0]);
          
          return null;
        }
        if (en.ref != null)
        {
          if (!JJavaName.isFullyQualifiedClassName(en.ref))
          {
            ((ErrorReceiver)Ring.get(ErrorReceiver.class)).error(en.getLocation(), Messages.format("ClassSelector.IncorrectClassName", new Object[] { en.ref }));
            
            return null;
          }
          return new CClassRef(this.model, type, en, info.toCustomizationList());
        }
        return bindToTypeSafeEnum((XSRestrictionSimpleType)type, en.className, en.javadoc, en.members, getEnumMemberMode().getModeWithEnum(), en.getLocation());
      }
    }
    if (type.getTargetNamespace().equals("http://www.w3.org/2001/XMLSchema"))
    {
      String name = type.getName();
      if (name != null)
      {
        TypeUse r = lookupBuiltin(name);
        if (r != null) {
          return r;
        }
      }
    }
    if (type.getTargetNamespace().equals("http://ws-i.org/profiles/basic/1.1/xsd"))
    {
      String name = type.getName();
      if ((name != null) && (name.equals("swaRef"))) {
        return CBuiltinLeafInfo.STRING.makeAdapted(SwaRefAdapter.class, false);
      }
    }
    if ((type.isRestriction()) && (!noAutoEnum))
    {
      XSRestrictionSimpleType rst = type.asRestriction();
      if (shouldBeMappedToTypeSafeEnumByDefault(rst))
      {
        TypeUse r = bindToTypeSafeEnum(rst, null, null, Collections.emptyMap(), getEnumMemberMode(), null);
        if (r != null) {
          return r;
        }
      }
    }
    return (CNonElement)getClassSelector()._bindToClass(type, null, false);
  }
  
  private boolean shouldBeMappedToTypeSafeEnumByDefault(XSRestrictionSimpleType type)
  {
    if (type.isLocal()) {
      return false;
    }
    if (type.getRedefinedBy() != null) {
      return false;
    }
    List<XSFacet> facets = type.getDeclaredFacets("enumeration");
    if ((facets.isEmpty()) || (facets.size() > this.builder.getGlobalBinding().getDefaultEnumMemberSizeCap())) {
      return false;
    }
    if (!canBeMappedToTypeSafeEnum(type)) {
      return false;
    }
    for (XSSimpleType t = type; t != null; t = t.getSimpleBaseType()) {
      if ((t.isGlobal()) && (this.builder.getGlobalBinding().canBeMappedToTypeSafeEnum(t))) {
        return true;
      }
    }
    return false;
  }
  
  public static boolean canBeMappedToTypeSafeEnum(XSSimpleType type)
  {
    do
    {
      if ("http://www.w3.org/2001/XMLSchema".equals(type.getTargetNamespace()))
      {
        String localName = type.getName();
        if (localName != null)
        {
          if (localName.equals("anySimpleType")) {
            return false;
          }
          if ((localName.equals("ID")) || (localName.equals("IDREF"))) {
            return false;
          }
          if (builtinTypeSafeEnumCapableTypes.contains(localName)) {
            return true;
          }
        }
      }
      type = type.getSimpleBaseType();
    } while (type != null);
    return false;
  }
  
  private TypeUse bindToTypeSafeEnum(XSRestrictionSimpleType type, String className, String javadoc, Map<String, BIEnumMember> members, EnumMemberMode mode, Locator loc)
  {
    if (loc == null) {
      loc = type.getLocator();
    }
    if (className == null)
    {
      if (!type.isGlobal())
      {
        getErrorReporter().error(loc, "ConversionFinder.NoEnumNameAvailable", new Object[0]);
        
        return CBuiltinLeafInfo.STRING;
      }
      className = type.getName();
    }
    className = this.builder.deriveName(className, type);
    
    StringWriter out = new StringWriter();
    SchemaWriter sw = new SchemaWriter(new JavadocEscapeWriter(out));
    type.visit(sw);
    if (javadoc != null) {
      javadoc = javadoc + "\n\n";
    } else {
      javadoc = "";
    }
    javadoc = javadoc + Messages.format("ClassSelector.JavadocHeading", new Object[] { type.getName() }) + "\n<p>\n<pre>\n" + out.getBuffer() + "</pre>";
    
    this.refererStack.push(type.getSimpleBaseType());
    TypeUse use = build(type.getSimpleBaseType());
    this.refererStack.pop();
    if (use.isCollection()) {
      return null;
    }
    CNonElement baseDt = use.getInfo();
    if ((baseDt instanceof CClassInfo)) {
      return null;
    }
    XSFacet[] errorRef = new XSFacet[1];
    List<CEnumConstant> memberList = buildCEnumConstants(type, false, members, errorRef);
    if ((memberList == null) || (checkMemberNameCollision(memberList) != null)) {
      switch (mode)
      {
      case SKIP: 
        return null;
      case ERROR: 
        if (memberList == null)
        {
          getErrorReporter().error(errorRef[0].getLocator(), "ERR_CANNOT_GENERATE_ENUM_NAME", new Object[] { errorRef[0].getValue() });
        }
        else
        {
          CEnumConstant[] collision = checkMemberNameCollision(memberList);
          getErrorReporter().error(collision[0].getLocator(), "ERR_ENUM_MEMBER_NAME_COLLISION", new Object[] { collision[0].getName() });
          
          getErrorReporter().error(collision[1].getLocator(), "ERR_ENUM_MEMBER_NAME_COLLISION_RELATED", new Object[0]);
        }
        return null;
      case GENERATE: 
        memberList = buildCEnumConstants(type, true, members, null);
      }
    }
    CClassInfoParent scope;
    CClassInfoParent scope;
    if (type.isGlobal()) {
      scope = new CClassInfoParent.Package(getClassSelector().getPackage(type.getTargetNamespace()));
    } else {
      scope = getClassSelector().getClassScope();
    }
    CEnumLeafInfo xducer = new CEnumLeafInfo(this.model, BGMBuilder.getName(type), scope, className, baseDt, memberList, type, this.builder.getBindInfo(type).toCustomizationList(), loc);
    
    xducer.javadoc = javadoc;
    
    BIConversion conv = new BIConversion.Static(type.getLocator(), xducer);
    conv.markAsAcknowledged();
    
    this.builder.getOrCreateBindInfo(type).addDecl(conv);
    
    return conv.getTypeUse(type);
  }
  
  private List<CEnumConstant> buildCEnumConstants(XSRestrictionSimpleType type, boolean needsToGenerateMemberName, Map<String, BIEnumMember> members, XSFacet[] errorRef)
  {
    List<CEnumConstant> memberList = new ArrayList();
    int idx = 1;
    Set<String> enums = new HashSet();
    for (XSFacet facet : type.getDeclaredFacets("enumeration"))
    {
      String name = null;
      String mdoc = this.builder.getBindInfo(facet).getDocumentation();
      if (enums.add(facet.getValue().value))
      {
        if (needsToGenerateMemberName)
        {
          name = "VALUE_" + idx++;
        }
        else
        {
          String facetValue = facet.getValue().value;
          BIEnumMember mem = (BIEnumMember)members.get(facetValue);
          if (mem == null) {
            mem = (BIEnumMember)this.builder.getBindInfo(facet).get(BIEnumMember.class);
          }
          if (mem != null)
          {
            name = mem.name;
            mdoc = mem.javadoc;
          }
          if (name == null)
          {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < facetValue.length(); i++)
            {
              char ch = facetValue.charAt(i);
              if (Character.isJavaIdentifierPart(ch)) {
                sb.append(ch);
              } else {
                sb.append('_');
              }
            }
            name = this.model.getNameConverter().toConstantName(sb.toString());
          }
        }
        if (!JJavaName.isJavaIdentifier(name))
        {
          if (errorRef != null) {
            errorRef[0] = facet;
          }
          return null;
        }
        memberList.add(new CEnumConstant(name, mdoc, facet.getValue().value, facet.getLocator()));
      }
    }
    return memberList;
  }
  
  private CEnumConstant[] checkMemberNameCollision(List<CEnumConstant> memberList)
  {
    Map<String, CEnumConstant> names = new HashMap();
    for (CEnumConstant c : memberList)
    {
      CEnumConstant old = (CEnumConstant)names.put(c.getName(), c);
      if (old != null) {
        return new CEnumConstant[] { old, c };
      }
    }
    return null;
  }
  
  private EnumMemberMode getEnumMemberMode()
  {
    return this.builder.getGlobalBinding().getEnumMemberMode();
  }
  
  private TypeUse lookupBuiltin(String typeLocalName)
  {
    if ((typeLocalName.equals("integer")) || (typeLocalName.equals("long")))
    {
      BigInteger xe = readFacet("maxExclusive", -1);
      BigInteger xi = readFacet("maxInclusive", 0);
      BigInteger max = min(xe, xi);
      if (max != null)
      {
        BigInteger ne = readFacet("minExclusive", 1);
        BigInteger ni = readFacet("minInclusive", 0);
        BigInteger min = max(ne, ni);
        if (min != null) {
          if ((min.compareTo(INT_MIN) >= 0) && (max.compareTo(INT_MAX) <= 0)) {
            typeLocalName = "int";
          } else if ((min.compareTo(LONG_MIN) >= 0) && (max.compareTo(LONG_MAX) <= 0)) {
            typeLocalName = "long";
          }
        }
      }
    }
    else
    {
      if ((typeLocalName.equals("boolean")) && (isRestrictedTo0And1())) {
        return CBuiltinLeafInfo.BOOLEAN_ZERO_OR_ONE;
      }
      if (typeLocalName.equals("base64Binary")) {
        return lookupBinaryTypeBinding();
      }
      if (typeLocalName.equals("anySimpleType"))
      {
        if (((getReferer() instanceof XSAttributeDecl)) || ((getReferer() instanceof XSSimpleType))) {
          return CBuiltinLeafInfo.STRING;
        }
        return CBuiltinLeafInfo.ANYTYPE;
      }
    }
    return (TypeUse)builtinConversions.get(typeLocalName);
  }
  
  private TypeUse lookupBinaryTypeBinding()
  {
    XSComponent referer = getReferer();
    String emt = referer.getForeignAttribute("http://www.w3.org/2005/05/xmlmime", "expectedContentTypes");
    if (emt != null)
    {
      this.acknowledgedXmimeContentTypes.add(referer);
      try
      {
        List<MimeTypeRange> types = MimeTypeRange.parseRanges(emt);
        MimeTypeRange mt = MimeTypeRange.merge(types);
        if (mt.majorType.equals("image")) {
          return CBuiltinLeafInfo.IMAGE.makeMimeTyped(mt.toMimeType());
        }
        if (((mt.majorType.equals("application")) || (mt.majorType.equals("text"))) && (isXml(mt.subType))) {
          return CBuiltinLeafInfo.XML_SOURCE.makeMimeTyped(mt.toMimeType());
        }
        if ((mt.majorType.equals("text")) && (mt.subType.equals("plain"))) {
          return CBuiltinLeafInfo.STRING.makeMimeTyped(mt.toMimeType());
        }
        return CBuiltinLeafInfo.DATA_HANDLER.makeMimeTyped(mt.toMimeType());
      }
      catch (ParseException e)
      {
        getErrorReporter().error(referer.getLocator(), Messages.format("ERR_ILLEGAL_EXPECTED_MIME_TYPE", new Object[] { emt, e.getMessage() }), new Object[0]);
      }
      catch (MimeTypeParseException e)
      {
        getErrorReporter().error(referer.getLocator(), Messages.format("ERR_ILLEGAL_EXPECTED_MIME_TYPE", new Object[] { emt, e.getMessage() }), new Object[0]);
      }
    }
    return CBuiltinLeafInfo.BASE64_BYTE_ARRAY;
  }
  
  public boolean isAcknowledgedXmimeContentTypes(XSComponent c)
  {
    return this.acknowledgedXmimeContentTypes.contains(c);
  }
  
  private boolean isXml(String subType)
  {
    return (subType.equals("xml")) || (subType.endsWith("+xml"));
  }
  
  private boolean isRestrictedTo0And1()
  {
    XSFacet pattern = this.initiatingType.getFacet("pattern");
    if (pattern != null)
    {
      String v = pattern.getValue().value;
      if ((v.equals("0|1")) || (v.equals("1|0")) || (v.equals("\\d"))) {
        return true;
      }
    }
    XSFacet enumf = this.initiatingType.getFacet("enumeration");
    if (enumf != null)
    {
      String v = enumf.getValue().value;
      if ((v.equals("0")) || (v.equals("1"))) {
        return true;
      }
    }
    return false;
  }
  
  private BigInteger readFacet(String facetName, int offset)
  {
    XSFacet me = this.initiatingType.getFacet(facetName);
    if (me == null) {
      return null;
    }
    BigInteger bi = DatatypeConverterImpl._parseInteger(me.getValue().value);
    if (offset != 0) {
      bi = bi.add(BigInteger.valueOf(offset));
    }
    return bi;
  }
  
  private BigInteger min(BigInteger a, BigInteger b)
  {
    if (a == null) {
      return b;
    }
    if (b == null) {
      return a;
    }
    return a.min(b);
  }
  
  private BigInteger max(BigInteger a, BigInteger b)
  {
    if (a == null) {
      return b;
    }
    if (b == null) {
      return a;
    }
    return a.max(b);
  }
  
  static
  {
    builtinConversions = new HashMap();
    
    Set<String> s = new HashSet();
    
    String[] typeNames = { "string", "boolean", "float", "decimal", "double", "anyURI" };
    for (String type : typeNames) {
      s.add(type);
    }
    builtinTypeSafeEnumCapableTypes = Collections.unmodifiableSet(s);
    
    LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
    INT_MIN = BigInteger.valueOf(-2147483648L);
    INT_MAX = BigInteger.valueOf(2147483647L);
    
    Map<String, TypeUse> m = builtinConversions;
    
    m.put("string", CBuiltinLeafInfo.STRING);
    m.put("anyURI", CBuiltinLeafInfo.STRING);
    m.put("boolean", CBuiltinLeafInfo.BOOLEAN);
    
    m.put("hexBinary", CBuiltinLeafInfo.HEXBIN_BYTE_ARRAY);
    m.put("float", CBuiltinLeafInfo.FLOAT);
    m.put("decimal", CBuiltinLeafInfo.BIG_DECIMAL);
    m.put("integer", CBuiltinLeafInfo.BIG_INTEGER);
    m.put("long", CBuiltinLeafInfo.LONG);
    m.put("unsignedInt", CBuiltinLeafInfo.LONG);
    m.put("int", CBuiltinLeafInfo.INT);
    m.put("unsignedShort", CBuiltinLeafInfo.INT);
    m.put("short", CBuiltinLeafInfo.SHORT);
    m.put("unsignedByte", CBuiltinLeafInfo.SHORT);
    m.put("byte", CBuiltinLeafInfo.BYTE);
    m.put("double", CBuiltinLeafInfo.DOUBLE);
    m.put("QName", CBuiltinLeafInfo.QNAME);
    m.put("NOTATION", CBuiltinLeafInfo.QNAME);
    m.put("dateTime", CBuiltinLeafInfo.CALENDAR);
    m.put("date", CBuiltinLeafInfo.CALENDAR);
    m.put("time", CBuiltinLeafInfo.CALENDAR);
    m.put("gYearMonth", CBuiltinLeafInfo.CALENDAR);
    m.put("gYear", CBuiltinLeafInfo.CALENDAR);
    m.put("gMonthDay", CBuiltinLeafInfo.CALENDAR);
    m.put("gDay", CBuiltinLeafInfo.CALENDAR);
    m.put("gMonth", CBuiltinLeafInfo.CALENDAR);
    m.put("duration", CBuiltinLeafInfo.DURATION);
    m.put("token", CBuiltinLeafInfo.TOKEN);
    m.put("normalizedString", CBuiltinLeafInfo.NORMALIZED_STRING);
    m.put("ID", CBuiltinLeafInfo.ID);
    m.put("IDREF", CBuiltinLeafInfo.IDREF);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\SimpleTypeBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */