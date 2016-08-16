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

import com.articulate.calendar.argue.Argument;
import com.articulate.sigma.Formula;
import com.articulate.sigma.KB;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A CalendarKB holds a Sigma KB plus other cached values needed by the Calendar
 * application. (When the Sigma KB can efficiently answer queries involving
 * arithmetic, perhaps this help class won't be necessary.)
 * @author Jeff Thompson, jeff@thefirst.org
 */
public class CalendarKB {
  /**
   * Create a new CalendarKB to use the given Sigma KB.
   * @param kb The Sigma KB.
   */
  public CalendarKB(KB kb) {
    this.kb = kb;
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
      // TODO: Check if kb has changed.
      // Set up overlapsDate_.
      Calendar calendar = Calendar.getInstance(timeZone);
      Pattern whenPattern = Pattern.compile("^\\(\\s*WhenFn\\s+(\\w+)\\s*\\)$");
      Pattern timeIntervalPattern = Pattern.compile
        ("^\\(\\s*TimeIntervalFn\\s+\\(\\s*SecondsSinceUnixEpochFn\\s+(\\d+)\\s*\\)" +
                               "\\s+\\(\\s*SecondsSinceUnixEpochFn\\s+(\\d+)\\s*\\)\\s*\\)$");

      overlapsDate_.clear();
      overlapsDateTimeZone_ = timeZone;
      for (Formula formula : kb.ask("arg", 0, "equal")) {
        if (formula.listLength() < 3)
          continue;
        Matcher matcher = whenPattern.matcher(formula.getArgument(1));
        if (!matcher.find())
          continue;
        String physical = matcher.group(1);

        matcher = timeIntervalPattern.matcher(formula.getArgument(2));
        if (!matcher.find())
          continue;

        // TODO: Check for NumberFormatException.
        long beginTimeUtcMillis = (long)(Double.parseDouble(matcher.group(1)) * 1000);
        long endTimeUtcMillis = (long)(Double.parseDouble(matcher.group(2)) * 1000);

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
   * Get a LocalDate for the calendar year, month and day.
   * @param calendar The Calendar.
   * @return The LocalDate for the calendar.
   */
  public static LocalDate getCalendarLocalDate(Calendar calendar)
  {
    // Calendar months start from 0.
    return LocalDate.of
      (calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
       calendar.get(Calendar.DAY_OF_MONTH));
  }

  /**
   * Assume s is a SUMO string with begin and end quotes, so remove them and
   * unescape inner quotes.
   * @param s The quoted string.
   * @return The result without quotes.
   */
  public static String removeQuotes(String s)
  {
    return s.substring(1, s.length() - 1).replace("\\\"", "\"");
  }

  /**
   * The Sigma KB given to the constructor.
   */
  public final KB kb;

  private TimeZone overlapsDateTimeZone_ = null;
  private final Map<LocalDate, Set<PhysicalTimeInterval>> overlapsDate_ = new HashMap<>();
  private static final Set<PhysicalTimeInterval> emptyPhysicalTimeIntervalSet_ = new HashSet<>();
}
