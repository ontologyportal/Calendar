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

import com.articulate.sigma.Formula;
import java.util.Set;

/**
 * An Argument holds a set of premises, a conclusion and other information
 * needed to represent a single argument.
 * @author Jeff Thompson, jeff@thefirst.org
 */
public class Argument {
  public Argument(Formula conclusion, Set<String> premises)
  {
    conclusion_ = conclusion;
    premises_ = premises;
  }

  /**
   * Get the argument conclusion.
   * @return The conclusion.
   */
  public Formula getConclusion() { return conclusion_; }

  /**
   * Get the argument premises.
   * @return The premises.
   */
  public Set<String> getPremises() { return premises_; }

  private final Formula conclusion_;
  private final Set<String> premises_;
}
