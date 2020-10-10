package com.sun.tools.xjc.reader.xmlschema;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JJavaName;
import com.sun.codemodel.util.JavadocEscapeWriter;
import com.sun.msv.datatype.xsd.DatatypeFactory;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.xducer.BuiltinDatatypeTransducerFactory;
import com.sun.tools.xjc.grammar.xducer.EnumerationXducer;
import com.sun.tools.xjc.grammar.xducer.EnumerationXducer.MemberInfo;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIEnum;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIEnumMember;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.xjc.reader.xmlschema.cs.ClassSelector;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.impl.util.SchemaWriter;
import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.relaxng.datatype.DatatypeException;
import org.xml.sax.Locator;

final class ConversionFinder
{
  private static final HashMap emptyHashMap = new HashMap();
  private final BGMBuilder builder;
  private final Map builtinConversions = new Hashtable();
  
  ConversionFinder(BGMBuilder _builder)
  {
    this.builder = _builder;
    
    String[] names = { "anySimpleType", "ID", "IDREF", "boolean", "base64Binary", "hexBinary", "float", "decimal", "integer", "long", "unsignedInt", "int", "unsignedShort", "short", "unsignedByte", "byte", "double", "QName", "token", "normalizedString", "date", "dateTime", "time" };
    try
    {
      for (int i = 0; i < names.length; i++) {
        this.builtinConversions.put(names[i], BuiltinDatatypeTransducerFactory.getWithoutWhitespaceNormalization(this.builder.grammar, DatatypeFactory.getTypeByName(names[i])));
      }
    }
    catch (DatatypeException e)
    {
      e.printStackTrace();
      throw new JAXBAssertionError();
    }
  }
  
  public Transducer find(XSSimpleType type)
  {
    return (Transducer)type.apply(this.functor);
  }
  
  private final XSSimpleTypeFunction functor = new ConversionFinder.1(this);
  private static final Set builtinTypeSafeEnumCapableTypes;
  
  private boolean shouldBeMappedToTypeSafeEnumByDefault(XSRestrictionSimpleType type)
  {
    if (type.isLocal()) {
      return false;
    }
    if (!canBeMappedToTypeSafeEnum(type)) {
      return false;
    }
    if (type.getDeclaredFacet("enumeration") == null) {
      return false;
    }
    XSSimpleType t = type;
    do
    {
      if ((t.isGlobal()) && (this.builder.getGlobalBinding().canBeMappedToTypeSafeEnum(t))) {
        return true;
      }
      t = t.getSimpleBaseType();
    } while (t != null);
    return false;
  }
  
  static
  {
    Set s = new HashSet();
    
    String[] typeNames = { "string", "boolean", "float", "decimal", "double", "anyURI" };
    for (int i = 0; i < typeNames.length; i++) {
      s.add(typeNames[i]);
    }
    builtinTypeSafeEnumCapableTypes = Collections.unmodifiableSet(s);
  }
  
  private boolean canBeMappedToTypeSafeEnum(XSSimpleType type)
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
  
  private Transducer bindToTypeSafeEnum(XSRestrictionSimpleType type, String className, String javadoc, HashMap members, Locator loc)
  {
    if (loc == null) {
      loc = type.getLocator();
    }
    if (className == null)
    {
      if (!type.isGlobal())
      {
        this.builder.errorReporter.error(loc, "ConversionFinder.NoEnumNameAvailable");
        
        return new IdentityTransducer(this.builder.grammar.codeModel);
      }
      className = type.getName();
    }
    className = this.builder.getNameConverter().toClassName(className);
    
    JDefinedClass clazz = this.builder.selector.codeModelClassFactory.createClass(this.builder.selector.getPackage(type.getTargetNamespace()), className, type.getLocator());
    
    StringWriter out = new StringWriter();
    SchemaWriter sw = new SchemaWriter(new JavadocEscapeWriter(out));
    type.visit(sw);
    
    JDocComment jdoc = clazz.javadoc();
    jdoc.appendComment(javadoc != null ? javadoc + "\n\n" : "");
    jdoc.appendComment(Messages.format("ClassSelector.JavadocHeading", type.getName()));
    
    jdoc.appendComment("\n<p>\n<pre>\n");
    jdoc.appendComment(out.getBuffer().toString());
    jdoc.appendComment("</pre>");
    
    boolean needsToGenerateMemberName = checkIfMemberNamesNeedToBeGenerated(type, members);
    
    HashMap memberMap = new HashMap();
    int idx = 1;
    
    Expression exp = Expression.nullSet;
    
    XSDatatype baseDt = this.builder.simpleTypeBuilder.datatypeBuilder.build(type.getSimpleBaseType());
    
    Iterator itr = type.iterateDeclaredFacets();
    while (itr.hasNext())
    {
      XSFacet facet = (XSFacet)itr.next();
      if (facet.getName().equals("enumeration"))
      {
        Expression vexp = this.builder.pool.createValue(baseDt, baseDt.createValue(facet.getValue(), facet.getContext()));
        if (needsToGenerateMemberName)
        {
          memberMap.put(vexp, new EnumerationXducer.MemberInfo("value" + idx++, null));
        }
        else
        {
          BIEnumMember mem = (BIEnumMember)members.get(facet.getValue());
          if (mem == null) {
            mem = (BIEnumMember)this.builder.getBindInfo(facet).get(BIEnumMember.NAME);
          }
          if (mem != null) {
            memberMap.put(vexp, mem.createMemberInfo());
          }
        }
        exp = this.builder.pool.createChoice(exp, vexp);
      }
    }
    if (memberMap.isEmpty()) {
      memberMap = emptyHashMap;
    }
    BIConversion conv = new BIConversion(type.getLocator(), new EnumerationXducer(NameConverter.standard, clazz, exp, memberMap, loc));
    
    conv.markAsAcknowledged();
    
    this.builder.getOrCreateBindInfo(type).addDecl(conv);
    
    return conv.getTransducer();
  }
  
  private boolean checkIfMemberNamesNeedToBeGenerated(XSRestrictionSimpleType type, HashMap members)
  {
    Iterator itr = type.iterateDeclaredFacets();
    while (itr.hasNext())
    {
      XSFacet facet = (XSFacet)itr.next();
      if (facet.getName().equals("enumeration"))
      {
        String value = facet.getValue();
        if (!members.containsKey(value)) {
          if (!JJavaName.isJavaIdentifier(this.builder.getNameConverter().toConstantName(facet.getValue()))) {
            return this.builder.getGlobalBinding().needsToGenerateEnumMemberName();
          }
        }
      }
    }
    return false;
  }
  
  private Transducer lookup(XSSimpleType type)
  {
    BindInfo info = this.builder.getBindInfo(type);
    BIConversion conv = (BIConversion)info.get(BIConversion.NAME);
    if (conv != null)
    {
      conv.markAsAcknowledged();
      return conv.getTransducer();
    }
    BIEnum en = (BIEnum)info.get(BIEnum.NAME);
    if (en != null)
    {
      en.markAsAcknowledged();
      if (!canBeMappedToTypeSafeEnum(type))
      {
        this.builder.errorReporter.error(en.getLocation(), "ConversionFinder.CannotBeTypeSafeEnum");
        
        this.builder.errorReporter.error(type.getLocator(), "ConversionFinder.CannotBeTypeSafeEnum.Location");
        
        return null;
      }
      return bindToTypeSafeEnum((XSRestrictionSimpleType)type, en.getClassName(), en.getJavadoc(), en.getMembers(), en.getLocation());
    }
    if (type.getTargetNamespace().equals("http://www.w3.org/2001/XMLSchema"))
    {
      String name = type.getName();
      if (name != null) {
        return lookupBuiltin(name);
      }
    }
    return null;
  }
  
  private Transducer lookupBuiltin(String typeName)
  {
    return (Transducer)this.builtinConversions.get(typeName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\ConversionFinder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */