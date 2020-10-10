package com.sun.tools.xjc.reader.xmlschema.parser;

import com.sun.xml.bind.JAXBAssertionError;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.XMLFilterImpl;

public class ProhibitedFeaturesFilter
  extends XMLFilterImpl
{
  private Locator locator = null;
  private ErrorHandler errorHandler = null;
  private boolean strict = true;
  private static final int REPORT_DISABLED_IN_STRICT_MODE = 1;
  private static final int REPORT_RESTRICTED = 2;
  private static final int REPORT_WARN = 3;
  private static final int REPORT_UNSUPPORTED_ERROR = 4;
  
  public ProhibitedFeaturesFilter(ErrorHandler eh, boolean strict)
  {
    this.errorHandler = eh;
    this.strict = strict;
  }
  
  public void startElement(String uri, String localName, String qName, Attributes atts)
    throws SAXException
  {
    if ((this.strict) && (localName.equals("any")) && ("skip".equals(atts.getValue("processContents"))))
    {
      report(3, "ProhibitedFeaturesFilter.ProcessContentsAttrOfAny", this.locator);
    }
    else if (localName.equals("anyAttribute"))
    {
      report(2, this.strict == true ? "ProhibitedFeaturesFilter.AnyAttr" : "ProhibitedFeaturesFilter.AnyAttrWarning", this.locator);
    }
    else if (localName.equals("complexType"))
    {
      if ((atts.getValue("block") != null) && (!parseComplexTypeBlockAttr(atts.getValue("block")))) {
        report(3, "ProhibitedFeaturesFilter.BlockAttrOfComplexType", this.locator);
      }
      if (atts.getValue("final") != null) {
        report(3, "ProhibitedFeaturesFilter.FinalAttrOfComplexType", this.locator);
      }
    }
    else if (localName.equals("element"))
    {
      if ((atts.getValue("abstract") != null) && (parsedBooleanTrue(atts.getValue("abstract")))) {
        report(1, "ProhibitedFeaturesFilter.AbstractAttrOfElement", this.locator);
      }
      if ((atts.getValue("substitutionGroup") != null) && (!atts.getValue("substitutionGroup").trim().equals(""))) {
        report(1, "ProhibitedFeaturesFilter.SubstitutionGroupAttrOfElement", this.locator);
      }
      if (atts.getValue("final") != null) {
        report(3, "ProhibitedFeaturesFilter.FinalAttrOfElement", this.locator);
      }
      if ((atts.getValue("block") != null) && (!parseElementBlockAttr(atts.getValue("block")))) {
        report(3, "ProhibitedFeaturesFilter.BlockAttrOfElement", this.locator);
      }
    }
    else if (localName.equals("key"))
    {
      report(2, this.strict == true ? "ProhibitedFeaturesFilter.Key" : "ProhibitedFeaturesFilter.KeyWarning", this.locator);
    }
    else if (localName.equals("keyref"))
    {
      report(2, this.strict == true ? "ProhibitedFeaturesFilter.Keyref" : "ProhibitedFeaturesFilter.KeyrefWarning", this.locator);
    }
    else if (localName.equals("notation"))
    {
      report(2, this.strict == true ? "ProhibitedFeaturesFilter.Notation" : "ProhibitedFeaturesFilter.NotationWarning", this.locator);
    }
    else if (localName.equals("unique"))
    {
      report(2, this.strict == true ? "ProhibitedFeaturesFilter.Unique" : "ProhibitedFeaturesFilter.UniqueWarning", this.locator);
    }
    else if (localName.equals("redefine"))
    {
      report(4, "ProhibitedFeaturesFilter.Redefine", this.locator);
    }
    else if (localName.equals("schema"))
    {
      if ((atts.getValue("blockDefault") != null) && (!atts.getValue("blockDefault").equals("#all"))) {
        report(3, "ProhibitedFeaturesFilter.BlockDefaultAttrOfSchema", this.locator);
      }
      if (atts.getValue("finalDefault") != null) {
        report(3, "ProhibitedFeaturesFilter.FinalDefaultAttrOfSchema", this.locator);
      }
      if (atts.getValue("http://java.sun.com/xml/ns/jaxb", "extensionBindingPrefixes") != null) {
        report(1, "ProhibitedFeaturesFilter.ExtensionBindingPrefixesOfSchema", this.locator);
      }
    }
    super.startElement(uri, localName, qName, atts);
  }
  
  public void setDocumentLocator(Locator locator)
  {
    super.setDocumentLocator(locator);
    this.locator = locator;
  }
  
  private void report(int type, String msg, Locator loc)
    throws SAXException
  {
    SAXParseException spe = null;
    if ((type == 2) && (!this.strict)) {
      type = 3;
    }
    if ((type == 1) && (!this.strict)) {
      return;
    }
    switch (type)
    {
    case 1: 
    case 2: 
      spe = new SAXParseException(Messages.format("ProhibitedFeaturesFilter.StrictModePrefix") + "\n\t" + Messages.format(msg), loc);
      
      this.errorHandler.error(spe);
      throw spe;
    case 3: 
      spe = new SAXParseException(Messages.format("ProhibitedFeaturesFilter.WarningPrefix") + " " + Messages.format(msg), loc);
      
      this.errorHandler.warning(spe);
      break;
    case 4: 
      spe = new SAXParseException(Messages.format("ProhibitedFeaturesFilter.UnsupportedPrefix") + " " + Messages.format(msg), loc);
      
      this.errorHandler.error(spe);
      throw spe;
    default: 
      throw new JAXBAssertionError();
    }
  }
  
  private static boolean parsedBooleanTrue(String lexicalBoolean)
    throws SAXParseException
  {
    if ((lexicalBoolean.equals("true")) || (lexicalBoolean.equals("1"))) {
      return true;
    }
    return false;
  }
  
  private static boolean parseElementBlockAttr(String lexicalBlock)
  {
    if ((lexicalBlock.equals("#all")) || ((lexicalBlock.indexOf("restriction") != -1) && (lexicalBlock.indexOf("extension") != -1) && (lexicalBlock.indexOf("substitution") != -1))) {
      return true;
    }
    return false;
  }
  
  private static boolean parseComplexTypeBlockAttr(String lexicalBlock)
  {
    if ((lexicalBlock.equals("#all")) || ((lexicalBlock.indexOf("restriction") != -1) && (lexicalBlock.indexOf("extension") != -1))) {
      return true;
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\parser\ProhibitedFeaturesFilter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */