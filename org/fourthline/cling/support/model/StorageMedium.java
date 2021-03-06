package org.fourthline.cling.support.model;

import java.util.HashMap;
import java.util.Map;
import org.fourthline.cling.model.ModelUtil;

public enum StorageMedium
{
  UNKNOWN,  DV,  MINI_DV("MINI-DV"),  VHS,  W_VHS("W-VHS"),  S_VHS("S-VHS"),  D_VHS("D-VHS"),  VHSC,  VIDEO8,  HI8,  CD_ROM("CD-ROM"),  CD_DA("CD-DA"),  CD_R("CD-R"),  CD_RW("CD-RW"),  VIDEO_CD("VIDEO-CD"),  SACD,  MD_AUDIO("M-AUDIO"),  MD_PICTURE("MD-PICTURE"),  DVD_ROM("DVD-ROM"),  DVD_VIDEO("DVD-VIDEO"),  DVD_R("DVD-R"),  DVD_PLUS_RW("DVD+RW"),  DVD_MINUS_RW("DVD-RW"),  DVD_RAM("DVD-RAM"),  DVD_AUDIO("DVD-AUDIO"),  DAT,  LD,  HDD,  MICRO_MV("MICRO_MV"),  NETWORK,  NONE,  NOT_IMPLEMENTED,  VENDOR_SPECIFIC;
  
  private static Map<String, StorageMedium> byProtocolString = new HashMap() {};
  private String protocolString;
  
  private StorageMedium()
  {
    this(null);
  }
  
  private StorageMedium(String protocolString)
  {
    this.protocolString = (protocolString == null ? name() : protocolString);
  }
  
  public String toString()
  {
    return this.protocolString;
  }
  
  public static StorageMedium valueOrExceptionOf(String s)
  {
    StorageMedium sm = (StorageMedium)byProtocolString.get(s);
    if (sm != null) {
      return sm;
    }
    throw new IllegalArgumentException("Invalid storage medium string: " + s);
  }
  
  public static StorageMedium valueOrVendorSpecificOf(String s)
  {
    StorageMedium sm = (StorageMedium)byProtocolString.get(s);
    return sm != null ? sm : VENDOR_SPECIFIC;
  }
  
  public static StorageMedium[] valueOfCommaSeparatedList(String s)
  {
    String[] strings = ModelUtil.fromCommaSeparatedList(s);
    if (strings == null) {
      return new StorageMedium[0];
    }
    StorageMedium[] result = new StorageMedium[strings.length];
    for (int i = 0; i < strings.length; i++) {
      result[i] = valueOrVendorSpecificOf(strings[i]);
    }
    return result;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\StorageMedium.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */