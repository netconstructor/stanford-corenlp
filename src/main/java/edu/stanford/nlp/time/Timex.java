package edu.stanford.nlp.time;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;
import java.util.regex.Pattern;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

import edu.stanford.nlp.util.Pair;

/**
 * Stores one TIMEX3 expression.  This class is used for both TimeAnnotator and
 * GUTimeAnnotator for storing information for TIMEX3 tags.
 *
 * <p>
 * Example text with TIMEX3 annotation:<br>
 * <code>In Washington &lt;TIMEX3 tid="t1" TYPE="DATE" VAL="PRESENT_REF"
 * temporalFunction="true" valueFromFunction="tf1"
 * anchorTimeID="t0"&gt;today&lt;/TIMEX3&gt;, the Federal Aviation Administration
 * released air traffic control tapes from the night the TWA Flight eight
 * hundred went down.
 * </code>
 * <p>
 * <br>
 * TIMEX3 specification:
 * <br>
 * <pre><code>
 * attributes ::= tid type [functionInDocument] [beginPoint] [endPoint]
 *                [quant] [freq] [temporalFunction] (value | valueFromFunction)
 *                [mod] [anchorTimeID] [comment]
 *
 * tid ::= ID
 *   {tid ::= TimeID
 *    TimeID ::= t<integer>}
 * type ::= 'DATE' | 'TIME' | 'DURATION' | 'SET'
 * beginPoint ::= IDREF
 *    {beginPoint ::= TimeID}
 * endPoint ::= IDREF
 *    {endPoint ::= TimeID}
 * quant ::= CDATA
 * freq ::= Duration
 * functionInDocument ::= 'CREATION_TIME' | 'EXPIRATION_TIME' | 'MODIFICATION_TIME' |
 *                        'PUBLICATION_TIME' | 'RELEASE_TIME'| 'RECEPTION_TIME' |
 *                        'NONE' {default, if absent, is 'NONE'}
 * temporalFunction ::= 'true' | 'false' {default, if absent, is 'false'}
 *    {temporalFunction ::= boolean}
 * value ::= Duration | Date | Time | WeekDate | WeekTime | Season | PartOfYear | PaPrFu
 * valueFromFunction ::= IDREF
 *    {valueFromFunction ::= TemporalFunctionID
 * TemporalFunctionID ::= tf<integer>}
 * mod ::= 'BEFORE' | 'AFTER' | 'ON_OR_BEFORE' | 'ON_OR_AFTER' |'LESS_THAN' | 'MORE_THAN' |
 *         'EQUAL_OR_LESS' | 'EQUAL_OR_MORE' | 'START' | 'MID' | 'END' | 'APPROX'
 * anchorTimeID ::= IDREF
 *   {anchorTimeID ::= TimeID}
 * comment ::= CDATA
 * </code></pre>
 *
 * <p>
 * References
 * <br>
 * Guidelines: <a href="http://www.timeml.org/tempeval2/tempeval2-trial/guidelines/timex3guidelines-072009.pdf">
 * http://www.timeml.org/tempeval2/tempeval2-trial/guidelines/timex3guidelines-072009.pdf</a>
 * <br>
 * Specifications: <a href="http://www.timeml.org/site/publications/timeMLdocs/timeml_1.2.1.html#timex3">
 * http://www.timeml.org/site/publications/timeMLdocs/timeml_1.2.1.html#timex3</a>
 * <br>
 * XSD: <a href="http://www.timeml.org/timeMLdocs/TimeML.xsd">http://www.timeml.org/timeMLdocs/TimeML.xsd</a>
 **/
public class Timex implements Serializable {

  private static final long serialVersionUID = 385847729549981302L;

  /**
   * XML representation of the TIMEX tag
   */
  private String xml;

  /**
   * TIMEX3 value attribute - Time value (given in extended ISO 8601 format).
   */
  private String val;

  /**
   * Alternate representation for time value (not part of TIMEX3 standard).
   * used when value of the time expression cannot be expressed as a standard TIMEX3 value.
   */
  private String altVal;

  /**
   * Actual text that make up the time expression
   */
  private String text;

  /**
   * TIMEX3 type attribute - Type of the time expression (DATE, TIME, DURATION, or SET)
   */
  private String type;

  /**
   * TIMEX3 tid attribute - TimeID.  ID to identify this time expression.
   * Should have the format of {@code t<integer>}
   */
  private String tid;

  // TODO: maybe its easier if these are just strings...
  /**
   * TIMEX3 beginPoint attribute - integer indicating the TimeID of the begin time
   * that anchors this duration/range (-1 is not present).
   */
  private int beginPoint;

  /**
   * TIMEX3 beginPoint attribute - integer indicating the TimeID of the end time
   * that anchors this duration/range (-1 is not present).
   */
  private int endPoint;

  public String value() {
    return val;
  }

  public String altVal() {
    return altVal;
  }

  public String text() {
    return text;
  }

  public String timexType() {
    return type;
  }

  public String tid() {
    return tid;
  }

  public Timex() {
  }

  public Timex(Element element) {
    this.val = null;
    this.beginPoint = -1;
    this.endPoint = -1;

    /*
     * ByteArrayOutputStream os = new ByteArrayOutputStream(); Serializer ser =
     * new Serializer(os, "UTF-8"); ser.setIndent(2); // this is the default in
     * JDOM so let's keep the same ser.setMaxLength(0); // no line wrapping for
     * content ser.write(new Document(element));
     */

    init(element);
  }

  public Timex(String val) {
    this(null, val);
  }

  public Timex(String type, String val) {
    this.val = val;
    this.type = type;
    this.beginPoint = -1;
    this.endPoint = -1;
    this.xml = (val == null ? "<TIMEX3/>" : String.format("<TIMEX3 VAL=\"%s\" TYPE=\"%s\"/>", this.val, this.type));
  }

  private void init(Element element) {
    init(element.toXML(), element);
  }

  private void init(String xml, Element element) {
    this.xml = xml;
    this.text = element.getValue();

    // Mandatory attributes
    this.tid = element.getAttributeValue("tid");
    this.val = element.getAttributeValue("VAL");
    if (this.val == null) {
      this.val = element.getAttributeValue("value");
    }

    this.altVal = element.getAttributeValue("alt_value");

    this.type = element.getAttributeValue("type");
    if (type == null) {
      this.type = element.getAttributeValue("TYPE");
    }
    // if (this.type != null) {
    // this.type = this.type.intern();
    // }

    // Optional attributes
    String beginPoint = element.getAttributeValue("beginPoint");
    this.beginPoint = beginPoint == null ? -1 : Integer.parseInt(beginPoint.substring(1));
    String endPoint = element.getAttributeValue("endPoint");
    this.endPoint = endPoint == null ? -1 : Integer.parseInt(endPoint.substring(1));
  }

  public String toString() {
    return this.xml;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Timex timex = (Timex) o;

    if (beginPoint != timex.beginPoint) {
      return false;
    }
    if (endPoint != timex.endPoint) {
      return false;
    }
    if (type != null ? !type.equals(timex.type) : timex.type != null) {
      return false;
    }
    if (val != null ? !val.equals(timex.val) : timex.val != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = val != null ? val.hashCode() : 0;
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + beginPoint;
    result = 31 * result + endPoint;
    return result;
  }

  public static Timex fromXml(String xml) {
    try {
      Document newNodeDocument = new Builder().build(xml, "");
      Element element = newNodeDocument.getRootElement();
      if ("TIMEX3".equals(element.getLocalName())) {
        Timex t = new Timex();
        t.init(xml, element);
        return t;
      } else {
        throw new IllegalArgumentException("Invalid timex xml: " + xml);
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static Timex fromMap(String text, Map<String, String> map) {
    try {
      Element element = new Element("TIMEX3");
      for (Map.Entry<String, String> entry : map.entrySet()) {
        if (entry.getValue() != null) {
          element.addAttribute(new Attribute(entry.getKey(), entry.getValue()));
        }
      }
      element.appendChild(text);
      return new Timex(element);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Gets the Calendar matching the year, month and day of this Timex.
   *
   * @return The matching Calendar.
   */
  public Calendar getDate() {
    if (Pattern.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d", this.val)) {
      int year = Integer.parseInt(this.val.substring(0, 4));
      int month = Integer.parseInt(this.val.substring(5, 7));
      int day = Integer.parseInt(this.val.substring(8, 10));
      return makeCalendar(year, month, day);
    } else if (Pattern.matches("\\d\\d\\d\\d\\d\\d\\d\\d", this.val)) {
      int year = Integer.parseInt(this.val.substring(0, 4));
      int month = Integer.parseInt(this.val.substring(4, 6));
      int day = Integer.parseInt(this.val.substring(6, 8));
      return makeCalendar(year, month, day);
    }
    throw new UnsupportedOperationException(String.format("%s is not a fully specified date", this));
  }

  /**
   * Gets two Calendars, marking the beginning and ending of this Timex's range.
   *
   * @return The begin point and end point Calendars.
   */
  public Pair<Calendar, Calendar> getRange() {
    return this.getRange(null);
  }

  /**
   * Gets two Calendars, marking the beginning and ending of this Timex's range.
   *
   * @param documentTime
   *          The time the document containing this Timex was written. (Not
   *          necessary for resolving all Timex expressions. This may be
   *          {@code null}, but then relative time expressions cannot be
   *          resolved.)
   * @return The begin point and end point Calendars.
   */
  public Pair<Calendar, Calendar> getRange(Timex documentTime) {

    if (this.val == null) {
      throw new UnsupportedOperationException("no value specified for " + this);
    }

    // YYYYMMDD or YYYYMMDDT... where the time is concatenated directly with the
    // date
    else if (val.length() >= 8 && Pattern.matches("\\d\\d\\d\\d\\d\\d\\d\\d", this.val.substring(0, 8))) {
      int year = Integer.parseInt(this.val.substring(0, 4));
      int month = Integer.parseInt(this.val.substring(4, 6));
      int day = Integer.parseInt(this.val.substring(6, 8));
      return new Pair<Calendar, Calendar>(makeCalendar(year, month, day), makeCalendar(year, month, day));
    }
    // YYYY-MM-DD or YYYY-MM-DDT...
    else if (val.length() >= 10 && Pattern.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d", this.val.substring(0, 10))) {
      int year = Integer.parseInt(this.val.substring(0, 4));
      int month = Integer.parseInt(this.val.substring(5, 7));
      int day = Integer.parseInt(this.val.substring(8, 10));
      return new Pair<Calendar, Calendar>(makeCalendar(year, month, day), makeCalendar(year, month, day));
    }

    // YYYYMMDDL+
    else if (Pattern.matches("\\d\\d\\d\\d\\d\\d\\d\\d[A-Z]+", this.val)) {
      int year = Integer.parseInt(this.val.substring(0, 4));
      int month = Integer.parseInt(this.val.substring(4, 6));
      int day = Integer.parseInt(this.val.substring(6, 8));
      return new Pair<Calendar, Calendar>(makeCalendar(year, month, day), makeCalendar(year, month, day));
    }

    // YYYYMM or YYYYMMT...
    else if (val.length() >= 6 && Pattern.matches("\\d\\d\\d\\d\\d\\d", this.val.substring(0, 6))) {
      int year = Integer.parseInt(this.val.substring(0, 4));
      int month = Integer.parseInt(this.val.substring(4, 6));
      Calendar begin = makeCalendar(year, month, 1);
      int lastDay = begin.getActualMaximum(Calendar.DATE);
      Calendar end = makeCalendar(year, month, lastDay);
      return new Pair<Calendar, Calendar>(begin, end);
    }

    // YYYY-MM or YYYY-MMT...
    else if (val.length() >= 7 && Pattern.matches("\\d\\d\\d\\d-\\d\\d", this.val.substring(0, 7))) {
      int year = Integer.parseInt(this.val.substring(0, 4));
      int month = Integer.parseInt(this.val.substring(5, 7));
      Calendar begin = makeCalendar(year, month, 1);
      int lastDay = begin.getActualMaximum(Calendar.DATE);
      Calendar end = makeCalendar(year, month, lastDay);
      return new Pair<Calendar, Calendar>(begin, end);
    }

    // YYYY or YYYYT...
    else if (val.length() >= 4 && Pattern.matches("\\d\\d\\d\\d", this.val.substring(0, 4))) {
      int year = Integer.parseInt(this.val.substring(0, 4));
      return new Pair<Calendar, Calendar>(makeCalendar(year, 1, 1), makeCalendar(year, 12, 31));
    }

    // PDDY
    if (Pattern.matches("P\\d+Y", this.val) && documentTime != null) {

      Calendar rc = documentTime.getDate();
      int yearRange = Integer.parseInt(this.val.substring(1, this.val.length() - 1));

      // in the future
      if (this.beginPoint < this.endPoint) {
        Calendar start = copyCalendar(rc);
        Calendar end = copyCalendar(rc);
        end.add(Calendar.YEAR, yearRange);
        return new Pair<Calendar, Calendar>(start, end);
      }

      // in the past
      else if (this.beginPoint > this.endPoint) {
        Calendar start = copyCalendar(rc);
        Calendar end = copyCalendar(rc);
        start.add(Calendar.YEAR, 0 - yearRange);
        return new Pair<Calendar, Calendar>(start, end);
      }

      throw new RuntimeException("begin and end are equal " + this);
    }
    // PDDM
    if (Pattern.matches("P\\d+M", this.val) && documentTime != null) {
      Calendar rc = documentTime.getDate();
      int monthRange = Integer.parseInt(this.val.substring(1, this.val.length() - 1));

      // in the future
      if (this.beginPoint < this.endPoint) {
        Calendar start = copyCalendar(rc);
        Calendar end = copyCalendar(rc);
        end.add(Calendar.MONTH, monthRange);
        return new Pair<Calendar, Calendar>(start, end);
      }

      // in the past
      if (this.beginPoint > this.endPoint) {
        Calendar start = copyCalendar(rc);
        Calendar end = copyCalendar(rc);
        start.add(Calendar.MONTH, 0 - monthRange);
        return new Pair<Calendar, Calendar>(start, end);
      }

      throw new RuntimeException("begin and end are equal " + this);
    }
    // PDDD
    if (Pattern.matches("P\\d+D", this.val) && documentTime != null) {
      Calendar rc = documentTime.getDate();
      int dayRange = Integer.parseInt(this.val.substring(1, this.val.length() - 1));

      // in the future
      if (this.beginPoint < this.endPoint) {
        Calendar start = copyCalendar(rc);
        Calendar end = copyCalendar(rc);
        end.add(Calendar.DAY_OF_MONTH, dayRange);
        return new Pair<Calendar, Calendar>(start, end);
      }

      // in the past
      if (this.beginPoint > this.endPoint) {
        Calendar start = copyCalendar(rc);
        Calendar end = copyCalendar(rc);
        start.add(Calendar.DAY_OF_MONTH, 0 - dayRange);
        return new Pair<Calendar, Calendar>(start, end);
      }

      throw new RuntimeException("begin and end are equal " + this);
    }

    // YYYYSP
    if (Pattern.matches("\\d+SP", this.val)) {
      int year = Integer.parseInt(this.val.substring(0, 4));
      Calendar start = makeCalendar(year, 2, 1);
      Calendar end = makeCalendar(year, 4, 31);
      return new Pair<Calendar, Calendar>(start, end);
    }
    // YYYYSU
    if (Pattern.matches("\\d+SU", this.val)) {
      int year = Integer.parseInt(this.val.substring(0, 4));
      Calendar start = makeCalendar(year, 5, 1);
      Calendar end = makeCalendar(year, 7, 31);
      return new Pair<Calendar, Calendar>(start, end);
    }
    // YYYYFA
    if (Pattern.matches("\\d+FA", this.val)) {
      int year = Integer.parseInt(this.val.substring(0, 4));
      Calendar start = makeCalendar(year, 8, 1);
      Calendar end = makeCalendar(year, 10, 31);
      return new Pair<Calendar, Calendar>(start, end);
    }
    // YYYYWI
    if (Pattern.matches("\\d+WI", this.val)) {
      int year = Integer.parseInt(this.val.substring(0, 4));
      Calendar start = makeCalendar(year, 11, 1);
      Calendar end = makeCalendar(year + 1, 1, 29);
      return new Pair<Calendar, Calendar>(start, end);
    }

    // YYYYWDD
    if (Pattern.matches("\\d\\d\\d\\dW\\d+", this.val)) {
      int year = Integer.parseInt(this.val.substring(0, 4));
      int week = Integer.parseInt(this.val.substring(5));
      int startDay = (week - 1) * 7;
      int endDay = startDay + 6;
      Calendar start = makeCalendar(year, startDay);
      Calendar end = makeCalendar(year, endDay);
      return new Pair<Calendar, Calendar>(start, end);
    }

    // PRESENT_REF
    if (this.val.equals("PRESENT_REF")) {
      Calendar rc = documentTime.getDate();  // todo: This case doesn't check for documentTime being null and will NPE
      Calendar start = copyCalendar(rc);
      Calendar end = copyCalendar(rc);
      return new Pair<Calendar, Calendar>(start, end);
    }

    throw new RuntimeException(String.format("unknown value \"%s\" in %s", this.val, this));
  }

  private static Calendar makeCalendar(int year, int month, int day) {
    Calendar date = Calendar.getInstance();
    date.clear();
    date.set(year, month - 1, day, 0, 0, 0);
    return date;
  }

  private static Calendar makeCalendar(int year, int dayOfYear) {
    Calendar date = Calendar.getInstance();
    date.clear();
    date.set(Calendar.YEAR, year);
    date.set(Calendar.DAY_OF_YEAR, dayOfYear);
    return date;
  }

  private static Calendar copyCalendar(Calendar c) {
    Calendar date = Calendar.getInstance();
    date.clear();
    date.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c
        .get(Calendar.MINUTE), c.get(Calendar.SECOND));
    return date;
  }
}
