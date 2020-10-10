package com.sun.tools.xjc.reader.xmlschema;

import com.sun.msv.datatype.xsd.DatatypeFactory;
import com.sun.msv.datatype.xsd.ErrorType;
import com.sun.msv.datatype.xsd.TypeIncubator;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.tools.xjc.reader.Const;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.relaxng.datatype.DatatypeException;

public class DatatypeBuilder
  implements XSSimpleTypeFunction
{
  private final BGMBuilder builder;
  
  DatatypeBuilder(BGMBuilder builder, XSSchemaSet schemas)
  {
    this.builder = builder;
    try
    {
      for (int i = 0; i < Const.builtinTypeNames.length; i++)
      {
        XSSimpleType type = schemas.getSimpleType("http://www.w3.org/2001/XMLSchema", Const.builtinTypeNames[i]);
        
        _assert(type != null);
        
        this.cache.put(type, DatatypeFactory.getTypeByName(Const.builtinTypeNames[i]));
      }
    }
    catch (DatatypeException e)
    {
      e.printStackTrace();
      _assert(false);
    }
  }
  
  private final Map cache = new HashMap();
  
  public XSDatatype build(XSSimpleType type)
  {
    return (XSDatatype)type.apply(this);
  }
  
  public Object restrictionSimpleType(XSRestrictionSimpleType type)
  {
    XSDatatype dt = (XSDatatype)this.cache.get(type);
    if (dt != null) {
      return dt;
    }
    try
    {
      TypeIncubator ti = new TypeIncubator(build(type.getSimpleBaseType()));
      
      Iterator itr = type.iterateDeclaredFacets();
      while (itr.hasNext())
      {
        XSFacet facet = (XSFacet)itr.next();
        ti.addFacet(facet.getName(), facet.getValue(), facet.isFixed(), facet.getContext());
      }
      dt = ti.derive(type.getTargetNamespace(), type.getName());
      
      this.cache.put(type, dt);
      return dt;
    }
    catch (DatatypeException e)
    {
      this.builder.errorReporter.error(type.getLocator(), "DatatypeBuilder.DatatypeError", e.getMessage());
    }
    return ErrorType.theInstance;
  }
  
  public Object listSimpleType(XSListSimpleType type)
  {
    XSDatatype dt = (XSDatatype)this.cache.get(type);
    if (dt != null) {
      return dt;
    }
    try
    {
      dt = DatatypeFactory.deriveByList(type.getTargetNamespace(), type.getName(), build(type.getItemType()));
      
      this.cache.put(type, dt);
      return dt;
    }
    catch (DatatypeException e)
    {
      this.builder.errorReporter.error(type.getLocator(), "DatatypeBuilder.DatatypeError", e.getMessage());
    }
    return ErrorType.theInstance;
  }
  
  public Object unionSimpleType(XSUnionSimpleType type)
  {
    XSDatatype dt = (XSDatatype)this.cache.get(type);
    if (dt != null) {
      return dt;
    }
    try
    {
      XSDatatype[] members = new XSDatatype[type.getMemberSize()];
      for (int i = 0; i < members.length; i++) {
        members[i] = build(type.getMember(i));
      }
      dt = DatatypeFactory.deriveByUnion(type.getTargetNamespace(), type.getName(), members);
      
      this.cache.put(type, dt);
      return dt;
    }
    catch (DatatypeException e)
    {
      this.builder.errorReporter.error(type.getLocator(), "DatatypeBuilder.DatatypeError", e.getMessage());
    }
    return ErrorType.theInstance;
  }
  
  private static final void _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\DatatypeBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */