/* This code is copyright Articulate Software (c) 2016.  Some
portions copyright Teknowledge (c) 2003 and reused under the terms of
the GNU license.  This software is released under the GNU Public
License <http://www.gnu.org/copyleft/gpl.html>.  Users of this code
also consent, by use of this code, to credit Articulate Software and
Teknowledge in any writings, briefings, publications, presentations,
or other representations of any software which incorporates, builds
on, or uses this code.
*/

package com.articulate.calendar.argue;

import java.util.Set;

/**
 * An ArgumentSet holds a set of Arguments which may conflict and are the input
 * to finding an extension.
 * @author Jeff Thompson, jeff@thefirst.org
 */
public class ArgumentSet {
  /**
   * Create an ArgumentSet from the set of arguments.
   * @param arguments The set of arguments. This is a set to ensure that there
   * are not duplicate arguments.
   */
  public ArgumentSet(Set<Argument> arguments)
  {
    // TODO: Is there a Java utility to do this conversion?
    Object[] tempArguments = arguments.toArray();
    arguments_ = new Argument[tempArguments.length];
    for (int i = 0; i < tempArguments.length; ++i)
      arguments_[i] = (Argument)tempArguments[i];
  }

  /**
   * Get the array of arguments that was created from the set of arguments
   * given to the constructor. This is an array so that we have a deterministic
   * index integer for each argument.
   * @return The array of arguments.
   */
  public Argument[] getArguments() { return arguments_; }

  private final Argument[] arguments_;
}
