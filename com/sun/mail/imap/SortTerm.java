package com.sun.mail.imap;

public final class SortTerm
{
  public static final SortTerm ARRIVAL = new SortTerm("ARRIVAL");
  public static final SortTerm CC = new SortTerm("CC");
  public static final SortTerm DATE = new SortTerm("DATE");
  public static final SortTerm FROM = new SortTerm("FROM");
  public static final SortTerm REVERSE = new SortTerm("REVERSE");
  public static final SortTerm SIZE = new SortTerm("SIZE");
  public static final SortTerm SUBJECT = new SortTerm("SUBJECT");
  public static final SortTerm TO = new SortTerm("TO");
  private String term;
  
  private SortTerm(String term)
  {
    this.term = term;
  }
  
  public String toString()
  {
    return this.term;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\imap\SortTerm.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */