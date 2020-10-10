package com.sun.xml.xsom;

public abstract interface XSFacet
  extends XSComponent
{
  public static final String FACET_LENGTH = "length";
  public static final String FACET_MINLENGTH = "minLength";
  public static final String FACET_MAXLENGTH = "maxLength";
  public static final String FACET_PATTERN = "pattern";
  public static final String FACET_ENUMERATION = "enumeration";
  public static final String FACET_TOTALDIGITS = "totalDigits";
  public static final String FACET_FRACTIONDIGITS = "fractionDigits";
  public static final String FACET_MININCLUSIVE = "minInclusive";
  public static final String FACET_MAXINCLUSIVE = "maxInclusive";
  public static final String FACET_MINEXCLUSIVE = "minExclusive";
  public static final String FACET_MAXEXCLUSIVE = "maxExclusive";
  public static final String FACET_WHITESPACE = "whiteSpace";
  
  public abstract String getName();
  
  public abstract XmlString getValue();
  
  public abstract boolean isFixed();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\XSFacet.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */