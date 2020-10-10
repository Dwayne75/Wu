package com.sun.tools.xjc.reader.annotator;

import com.sun.msv.datatype.xsd.EnumerationFacet;
import com.sun.msv.datatype.xsd.ListType;
import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.datatype.xsd.UnionType;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.msv.grammar.DataExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.grammar.util.BreadthFirstExpressionCloner;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DatatypeSimplifier
  extends BreadthFirstExpressionCloner
{
  public DatatypeSimplifier(ExpressionPool pool)
  {
    super(pool);
  }
  
  private final Map dataExps = new HashMap();
  
  public Expression onAnyString()
  {
    return this.pool.createData(StringType.theInstance);
  }
  
  public Expression onData(DataExp exp)
  {
    if (!(exp.dt instanceof XSDatatype)) {
      return exp;
    }
    Expression r = (Expression)this.dataExps.get(exp);
    if (r == null)
    {
      r = processDatatype((XSDatatype)exp.dt, false);
      this.dataExps.put(exp, r);
    }
    return r;
  }
  
  private Expression processDatatype(XSDatatype dt, boolean inList)
  {
    EnumerationFacet ef = (EnumerationFacet)dt.getFacetObject("enumeration");
    if (ef != null) {
      return processEnumeration(dt, ef);
    }
    switch (dt.getVariety())
    {
    case 1: 
      return this.pool.createData(dt);
    case 3: 
      return processUnion(dt, inList);
    case 2: 
      return processList(dt, inList);
    }
    throw new Error();
  }
  
  private Expression processEnumeration(XSDatatype type, EnumerationFacet enums)
  {
    Expression exp = Expression.nullSet;
    
    Iterator itr = enums.values.iterator();
    while (itr.hasNext())
    {
      Object v = itr.next();
      
      exp = this.pool.createChoice(exp, this.pool.createValue(type, null, v));
    }
    return exp;
  }
  
  private Expression processUnion(XSDatatype dt, boolean inList)
  {
    if (dt.getFacetObject("enumeration") != null) {
      throw new Error(Messages.format("DatatypeSimplifier.EnumFacetUnsupported"));
    }
    if (dt.getFacetObject("pattern") != null) {
      throw new Error(Messages.format("DatatypeSimplifier.PatternFacetUnsupported"));
    }
    while (!(dt instanceof UnionType))
    {
      dt = dt.getBaseType();
      if (dt == null) {
        throw new Error();
      }
    }
    UnionType ut = (UnionType)dt;
    Expression exp = Expression.nullSet;
    for (int i = 0; i < ut.memberTypes.length; i++) {
      exp = this.pool.createChoice(exp, processDatatype(ut.memberTypes[i], inList));
    }
    return exp;
  }
  
  private Expression processList(XSDatatype dt, boolean inList)
  {
    if (dt.getFacetObject("enumeration") != null) {
      throw new Error(Messages.format("DatatypeSimplifier.EnumFacetUnsupported"));
    }
    if (dt.getFacetObject("pattern") != null) {
      throw new Error(Messages.format("DatatypeSimplifier.PatternFacetUnsupported"));
    }
    XSDatatype d = dt;
    while (!(d instanceof ListType))
    {
      d = d.getBaseType();
      if (d == null) {
        throw new Error();
      }
    }
    ListType lt = (ListType)d;
    
    Expression item = processDatatype(lt.itemType, true);
    
    Expression exp = this.pool.createZeroOrMore(item);
    if (inList) {
      return exp;
    }
    return this.pool.createList(exp);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\annotator\DatatypeSimplifier.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */