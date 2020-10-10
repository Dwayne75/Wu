package com.google.thirdparty.publicsuffix;

import com.google.common.annotations.GwtCompatible;

@GwtCompatible
 enum PublicSuffixType
{
  PRIVATE(':', ','),  ICANN('!', '?');
  
  private final char innerNodeCode;
  private final char leafNodeCode;
  
  private PublicSuffixType(char innerNodeCode, char leafNodeCode)
  {
    this.innerNodeCode = innerNodeCode;
    this.leafNodeCode = leafNodeCode;
  }
  
  char getLeafNodeCode()
  {
    return this.leafNodeCode;
  }
  
  char getInnerNodeCode()
  {
    return this.innerNodeCode;
  }
  
  static PublicSuffixType fromCode(char code)
  {
    for (PublicSuffixType value : ) {
      if ((value.getInnerNodeCode() == code) || (value.getLeafNodeCode() == code)) {
        return value;
      }
    }
    ??? = code;throw new IllegalArgumentException(38 + "No enum corresponding to given code: " + ???);
  }
  
  static PublicSuffixType fromIsPrivate(boolean isPrivate)
  {
    return isPrivate ? PRIVATE : ICANN;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\thirdparty\publicsuffix\PublicSuffixType.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */