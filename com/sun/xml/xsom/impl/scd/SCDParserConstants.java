package com.sun.xml.xsom.impl.scd;

public abstract interface SCDParserConstants
{
  public static final int EOF = 0;
  public static final int Letter = 6;
  public static final int BaseChar = 7;
  public static final int Ideographic = 8;
  public static final int CombiningChar = 9;
  public static final int UnicodeDigit = 10;
  public static final int Extender = 11;
  public static final int NCNAME = 12;
  public static final int NUMBER = 13;
  public static final int FACETNAME = 14;
  public static final int DEFAULT = 0;
  public static final String[] tokenImage = { "<EOF>", "\" \"", "\"\\t\"", "\"\\n\"", "\"\\r\"", "\"\\f\"", "<Letter>", "<BaseChar>", "<Ideographic>", "<CombiningChar>", "<UnicodeDigit>", "<Extender>", "<NCNAME>", "<NUMBER>", "<FACETNAME>", "\":\"", "\"/\"", "\"//\"", "\"attribute::\"", "\"@\"", "\"element::\"", "\"substitutionGroup::\"", "\"type::\"", "\"~\"", "\"baseType::\"", "\"primitiveType::\"", "\"itemType::\"", "\"memberType::\"", "\"scope::\"", "\"attributeGroup::\"", "\"group::\"", "\"identityContraint::\"", "\"key::\"", "\"notation::\"", "\"model::sequence\"", "\"model::choice\"", "\"model::all\"", "\"model::*\"", "\"any::*\"", "\"anyAttribute::*\"", "\"facet::*\"", "\"facet::\"", "\"component::*\"", "\"x-schema::\"", "\"x-schema::*\"", "\"*\"", "\"0\"" };
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\scd\SCDParserConstants.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */