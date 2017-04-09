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

import java.time.DayOfWeek;
import java.util.TimeZone;

/**
 * A CalendarPreferences holds user preferences for the Calendar application.
 * @author Jeff Thompson, jeff@thefirst.org
 */
public class CalendarPreferences {
  public CalendarPreferences(String username)
  {
    username_ = username;
  }

  public String getUsername() { return username_; }

  public TimeZone getTimeZone() { return timeZone_; }

  /**
   * Get the start day of the week for displaying a week.
   * @return The start day as a DayOfWeek.
   */
  public DayOfWeek getStartOfWeek() { return startOfWeek_; }

  private final String username_;
  private TimeZone timeZone_ = TimeZone.getDefault();
  private DayOfWeek startOfWeek_ = DayOfWeek.MONDAY;
}
