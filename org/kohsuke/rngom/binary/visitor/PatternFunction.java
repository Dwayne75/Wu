package org.kohsuke.rngom.binary.visitor;

import org.kohsuke.rngom.binary.AfterPattern;
import org.kohsuke.rngom.binary.AttributePattern;
import org.kohsuke.rngom.binary.ChoicePattern;
import org.kohsuke.rngom.binary.DataExceptPattern;
import org.kohsuke.rngom.binary.DataPattern;
import org.kohsuke.rngom.binary.ElementPattern;
import org.kohsuke.rngom.binary.EmptyPattern;
import org.kohsuke.rngom.binary.ErrorPattern;
import org.kohsuke.rngom.binary.GroupPattern;
import org.kohsuke.rngom.binary.InterleavePattern;
import org.kohsuke.rngom.binary.ListPattern;
import org.kohsuke.rngom.binary.NotAllowedPattern;
import org.kohsuke.rngom.binary.OneOrMorePattern;
import org.kohsuke.rngom.binary.RefPattern;
import org.kohsuke.rngom.binary.TextPattern;
import org.kohsuke.rngom.binary.ValuePattern;

public abstract interface PatternFunction
{
  public abstract Object caseEmpty(EmptyPattern paramEmptyPattern);
  
  public abstract Object caseNotAllowed(NotAllowedPattern paramNotAllowedPattern);
  
  public abstract Object caseError(ErrorPattern paramErrorPattern);
  
  public abstract Object caseGroup(GroupPattern paramGroupPattern);
  
  public abstract Object caseInterleave(InterleavePattern paramInterleavePattern);
  
  public abstract Object caseChoice(ChoicePattern paramChoicePattern);
  
  public abstract Object caseOneOrMore(OneOrMorePattern paramOneOrMorePattern);
  
  public abstract Object caseElement(ElementPattern paramElementPattern);
  
  public abstract Object caseAttribute(AttributePattern paramAttributePattern);
  
  public abstract Object caseData(DataPattern paramDataPattern);
  
  public abstract Object caseDataExcept(DataExceptPattern paramDataExceptPattern);
  
  public abstract Object caseValue(ValuePattern paramValuePattern);
  
  public abstract Object caseText(TextPattern paramTextPattern);
  
  public abstract Object caseList(ListPattern paramListPattern);
  
  public abstract Object caseRef(RefPattern paramRefPattern);
  
  public abstract Object caseAfter(AfterPattern paramAfterPattern);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\visitor\PatternFunction.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */