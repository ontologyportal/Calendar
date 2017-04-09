/* This code is copyright Articulate Software (c) 2016.  Some
portions copyright Teknowledge (c) 2003 and reused under the terms of
the GNU license.  This software is released under the GNU Public
License <http://www.gnu.org/copyleft/gpl.html>.  Users of this code
also consent, by use of this code, to credit Articulate Software and
Teknowledge in any writings, briefings, publications, presentations,
or other representations of any software which incorporates, builds
on, or uses this code.
*/

package com.articulate.calendar;

import com.articulate.sigma.KB;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.nuvl.argue.aba_plus.Sentence;

/**
 * A CalendarKB holds a set of aba_plus Sentences plus other cached values
 * needed by the Calendar application.
 * @author Jeff Thompson, jeff@thefirst.org
 */
public class CalendarKB {
  /**
   * Create a new CalendarKB by extracting sentences from the given Sigma KB.
   * @param kb The Sigma KB.
   */
  public CalendarKB(KB kb) throws FileNotFoundException, IOException
  {
    Pattern predicatePattern = Pattern.compile("^\\(([^ \\)]+)");

    // Copy formulas to ABA_Plus sentences.
    for (String formula : kb.formulaMap.keySet()) {
      Matcher matcher = predicatePattern.matcher(formula);
      if (matcher.find()) {
        Set<Sentence> sentenceSet = sentencesByPredicate_.get(matcher.group(1));
        if (sentenceSet == null)
          sentencesByPredicate_.put(matcher.group(1), (sentenceSet = new HashSet()));
        sentenceSet.add(new Sentence(formula, false));
      }
    }

    Set<String> ianaTimeZones = new HashSet<>();
    try (FileReader file = new FileReader(new File(kb.kbDir, "locationIanaTimeZone.kif"));
         BufferedReader reader = new BufferedReader(file)) {
      String line;
      while ((line = reader.readLine()) != null) {
        Matcher matcher = locationIanaTimeZonePattern_.matcher(line);
        if (!matcher.find())
          throw new Error("Can't match locationIanaTimeZone pattern: " + line);

        locationIanaTimeZone_.put(matcher.group(1), matcher.group(2));
        ianaTimeZones.add(matcher.group(2));
      }
    }

    try (FileReader file = new FileReader(new File(kb.kbDir, "itemTermFormatEnglishLanguage.kif"));
         BufferedReader reader = new BufferedReader(file)) {
      String line;
      while ((line = reader.readLine()) != null) {
        Matcher matcher = itemTermFormatEnglishLanguagePattern_.matcher(line);
        if (!matcher.find())
          throw new Error("Can't match itemTermFormatEnglishLanguage pattern: " + line);

        if (!(locationIanaTimeZone_.containsKey(matcher.group(1)) ||
              ianaTimeZones.contains(matcher.group(1))))
          // For now, only use memory to store labels needed for locations.
          continue;
        String label = removeQuotes(matcher.group(2));
        itemTermFormatEnglishLanguage_.put(matcher.group(1), label);
      }
    }

    try (FileReader file = new FileReader(new File(kb.kbDir, "iataAbbreviation.kif"));
         BufferedReader reader = new BufferedReader(file)) {
      String line;
      while ((line = reader.readLine()) != null) {
        Matcher matcher = iataAbbreviationPattern_.matcher(line);
        if (!matcher.find())
          throw new Error("Can't match iataAbbreviation pattern: " + line);

        String abbreviation = removeQuotes(matcher.group(1));
        iataAbbreviation_.put(abbreviation, matcher.group(2));
      }
    }

    System.out.println(" done.");
  }

  /**
   * A PhysicalTimeInterval holds the name of a Physical term and the begin and
   * end times of a time interval as milliseconds since the UTC Unix epoch.
   */
  public class PhysicalTimeInterval {
    public final String physical;
    public final long beginUtcMillis;
    public final long endUtcMillis;

    public PhysicalTimeInterval
      (String physical, long beginUtcMillis,
       long endUtcMillis)
    {
      this.physical = physical;
      this.beginUtcMillis = beginUtcMillis;
      this.endUtcMillis = endUtcMillis;
    }
  }

  /**
   * Use the day interval begin and end (TimeIntervalFn ?DAYBEGIN ?DAYEND)
   * according to the given timeZone and return a set of answers which satisfy:
   * (and
   *   (equal (WhenFn ?PHYSICAL) (TimeIntervalFn ?BEGIN ?END))
   *   (before ?BEGIN ?DAYEND)
   *   (beforeOrEqual ?DAYBEGIN ?END))
   * We don't use overlapsTemporally since we want to specifically use before
   * instead of beforeOrEqual so that a zero duration interval only matches the
   * beginning of a day but not the end of the previous day.
   * @param date The date.
   * @param timeZone The TimeZone to get the UTC day begin and end. If timeZone
   * is different from the previous call (or this is the first call), this
   * clears the cache and caches all results. (A future implementation may
   * maintain results for different timeZone values, but we want to save memory.)
   * @return A set of PhysicalTimeInterval which match the query above (possibly
   * empty) with ?PHYSICAL plus ?BEGIN ?END as milliseconds since the Unix epoch.
   */
  public Set<PhysicalTimeInterval>
  overlapsDate(LocalDate date, TimeZone timeZone)
  {
    if (timeZone != overlapsDateTimeZone_) {
      // TODO: Check if sentences_ has changed.
      // Set up overlapsDate_.
      Calendar calendar = Calendar.getInstance(timeZone);
      Pattern pattern = Pattern.compile("^\\(equal \\(WhenFn (\\w+)\\) " +
        "\\(TimeIntervalFn \\(SecondsSinceUnixEpochFn (\\d+)\\) \\(SecondsSinceUnixEpochFn (\\d+)\\)\\)\\)$");

      overlapsDate_.clear();
      overlapsDateTimeZone_ = timeZone;
      for (Sentence sentence : sentencesByPredicate_.getOrDefault
           ("equal", emptySentences_)) {
        Matcher matcher = pattern.matcher(sentence.symbol());
        if (!matcher.find())
          continue;
        String physical = matcher.group(1);

        // TODO: Check for NumberFormatException.
        long beginTimeUtcMillis = (long)(Double.parseDouble(matcher.group(2)) * 1000);
        long endTimeUtcMillis = (long)(Double.parseDouble(matcher.group(3)) * 1000);

        // Find dates with dayBeginUtcMillis and dayEndUtcMillis where
        // (beginTimeUtcMillis < dayEndUtcMillis &&
        //  endTimeUtcMillis >= dayBeginUtcMillis)
        calendar.setTimeInMillis(beginTimeUtcMillis);
        LocalDate beginDate = getCalendarLocalDate(calendar);
        LocalDate endDate;
        if (endTimeUtcMillis <= beginTimeUtcMillis)
          // A common and simple case.
          endDate = beginDate;
        else {
          calendar.setTimeInMillis(endTimeUtcMillis);
          endDate = getCalendarLocalDate(calendar);
          if (calendar.get(Calendar.HOUR) == 0 &&
              calendar.get(Calendar.MINUTE) == 0 &&
              calendar.get(Calendar.SECOND) == 0)
            // End before midnight of the next day.
            endDate = endDate.plusDays(-1);
        }

        // Add entries to overlapsDate_ for beginDate to endDate, inclusive.
        LocalDate key = beginDate;
        while(true) {
          Set<PhysicalTimeInterval> timeIntervalSet = overlapsDate_.getOrDefault
            (key, null);
          if (timeIntervalSet == null) {
            timeIntervalSet = new HashSet<>();
            overlapsDate_.put(key, timeIntervalSet);
          }
          timeIntervalSet.add(new PhysicalTimeInterval
            (physical, beginTimeUtcMillis, endTimeUtcMillis));

          if (key.equals(endDate))
            break;
          key = key.plusDays(1);
        }
      }
    }

    return overlapsDate_.getOrDefault(date, emptyPhysicalTimeIntervalSet_);
  }

  /**
   * Find the first Sentence with the given predicate where the given regex
   * pattern matches and has the given group value.
   * @param predicate The Sentence predicate.
   * @param pattern The regex pattern.
   * @param groupNumber The group number of the matched pattern.
   * @param group The value of the group of the patched pattern.
   * @return The regex Matcher object or null if not found.
   */
  public Matcher
  findFirst(String predicate, Pattern pattern, int groupNumber, String group)
  {
    for (Sentence sentence : sentencesByPredicate_.getOrDefault
         (predicate, emptySentences_)) {
      Matcher matcher = pattern.matcher(sentence.symbol());
      if (matcher.find() && matcher.group(groupNumber).equals(group))
        return matcher;
    }

    return null;
  }

  /**
   * Get a LocalDate for the calendar year, month and day.
   * @param calendar The Calendar.
   * @return The LocalDate for the calendar.
   */
  public static LocalDate
  getCalendarLocalDate(Calendar calendar)
  {
    // Calendar months start from 0.
    return LocalDate.of
      (calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
       calendar.get(Calendar.DAY_OF_MONTH));
  }

  /**
   * Assume s is a JSON string with begin and end quotes, so remove them and
   * unescape.
   * @param s The quoted string.
   * @return The unescaped result without quotes.
   */
  public static String 
  removeQuotes(String s) { return gson_.fromJson(s, String.class); }

  /** key: predicate, value: set of Sentence. */
  public final Map<String, Set<Sentence>> sentencesByPredicate_ = new HashMap<>();
  /** key: ID, value: time zone string. */
  public final Map<String, String> locationIanaTimeZone_ = new HashMap<>();
  /** key: ID, value: format string. */
  public final Map<String, String> itemTermFormatEnglishLanguage_ = new HashMap<>();
  /** key: abbreviation, value: ID. */
  public final Map<String, String> iataAbbreviation_ = new HashMap<>();
  
  private TimeZone overlapsDateTimeZone_ = null;
  private final Map<LocalDate, Set<PhysicalTimeInterval>> overlapsDate_ = new HashMap<>();
  private static final Set<PhysicalTimeInterval> emptyPhysicalTimeIntervalSet_ = new HashSet<>();
  private static final Gson gson_ = new Gson();
  private static final Set<Sentence> emptySentences_ = new HashSet<>();
  private static final Pattern locationIanaTimeZonePattern_ = Pattern.compile
    ("^\\(locationIanaTimeZone (\\w+) (\\w+)\\)$");
  private static final Pattern itemTermFormatEnglishLanguagePattern_ = Pattern.compile
    ("^\\(termFormat EnglishLanguage (\\w+) (\".*\")\\)$");
  private static final Pattern iataAbbreviationPattern_ = Pattern.compile
    ("\\(abbreviation (\".*\") (\\w+)\\)");
}
