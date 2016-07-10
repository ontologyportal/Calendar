
//	Copyright 2011 University of Dundee
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.

package javaDungAF;

import java.util.*;

/**
 * Provides two naive methods for removing non-maximal and non-minimal members of collections-of-collections. 
 *
 * <p> The methods simply use {@link java.util.Collection#containsAll(Collection) containsAll(Collection&lt?&gt)} to  
 * compare collections, so their effects depend on the types of the members of the collections. The methods would have 
 * no effect on, for instance, a collection of sets-of-arrays 
 * { {[{@code obj0}, {@code obj1}]}, {[{@code obj0}, {@code obj1}], [{@code obj1}, {@code obj0}]} }, if the two
 * references to '[{@code obj0}, {@code obj1}]' were not to one and the same object. </p>
 *
 */
public class SetComparison {
	
	/**
	 * Removes {@code collColl}'s non-minimal members, simply using 
	 * {@link java.util.Collection#containsAll(Collection) containsAll(Collection&lt?&gt)} to compare the collections.
	 *
	 * @param collColl a {@code Collection} of {@code Collection}s.
	 * @return {@code true} if {@code collColl} changed as a result of the call.
	 */
	public static <T extends Collection> boolean removeNonMinimalMembersOf(Collection<T> collColl) {
		
		return removeNonMinimalOrNonMaximalHelper(collColl, true);
	}
	
	/**
	 * Removes {@code collColl}'s non-maximal members, simply using 
	 * {@link java.util.Collection#containsAll(Collection) containsAll(Collection&lt?&gt)} to compare the collections.
	 *
	 * @param collColl a {@code Collection} of {@code Collection}s.
	 * @return {@code true} if {@code collColl} changed as a result of the call.
	 */
	public static <T extends Collection> boolean removeNonMaximalMembersOf(Collection<T> collColl) {
		
		return removeNonMinimalOrNonMaximalHelper(collColl, false);
	}
	
	/**
	 * Removes {@code collColl}'s non-minimal/non-maximal members, simply using 
	 * {@link java.util.Collection#containsAll(Collection) containsAll(Collection&lt?&gt)} to compare the collections.
	 *
	 * @param collColl a {@code Collection} of {@code Collection}s.
	 * @param removeNonMinimal whether it is the non-minimal or non-maximal members of {@code collColl} 
	 * that are to be removed.
	 * @return {@code true} if {@code collColl} changed as a result of the call.
	 */	
	private static <T extends Collection> boolean removeNonMinimalOrNonMaximalHelper(Collection<T> collColl, 
																					 boolean removeNonMinimal) {	
		
		int origSize = collColl.size();
		ArrayList<T> collCollAsList = new ArrayList<T>(collColl);
		T assumedRetainableColl;
		T assumedRemovableColl;
		
		/* for each member assumedRetainableColl of collColl, compare assumedRetainableColl with the other members of 
		 collColl, until assumedRetainableColl is found to be removable (if ever); while doing so, remove any members
		 of collColl which assumedRetainableColl renders removable. */
		while (!collCollAsList.isEmpty()) {
			assumedRetainableColl = collCollAsList.iterator().next();
			collCollAsList.removeAll(Arrays.asList(assumedRetainableColl));  // to ensure termination.
			
			for (Iterator<T> it = collCollAsList.iterator(); it.hasNext(); ) {
				assumedRemovableColl = it.next();
				if ((assumedRemovableColl.size() > assumedRetainableColl.size())
					&& assumedRemovableColl.containsAll(assumedRetainableColl)) {
					if (removeNonMinimal) {
						/* assumedRemovableColl is indeed non-minimal, so remove all instances of it from collColl... */
						collColl.removeAll(Arrays.asList(assumedRemovableColl));
						/* ...and also from collCollAsSet, because there is no need to compare it with other members of 
						 collCollAsSet. */
						it.remove();
					} else {
						/*assumedRetainableColl is in fact non-maximal, so remove all instances of it from collColl...*/
						collColl.removeAll(Arrays.asList(assumedRetainableColl));
						/* ...and stop using assumedRetainableColl to test maximality of members of collColl. */
						break;
					}
				} else if ((assumedRetainableColl.size() > assumedRemovableColl.size())
						   && (assumedRetainableColl.containsAll(assumedRemovableColl))) {
					if (removeNonMinimal) {
						/*assumedRetainableColl is in fact non-minimal, so remove all instance of it from collColl... */
						collColl.removeAll(Arrays.asList(assumedRetainableColl));
						/* ...and stop using assumedRetainableColl to test minimality of members of collColl. */ 
						break;
					} else {
						/* assumedRemovableColl is indeed non-maximal, so remove all instances of it from collColl... */
						collColl.removeAll(Arrays.asList(assumedRemovableColl));
						/* ...and also from collCollAsSet, because there is no need to compare it with other members of 
						 collCollAsSet. */
						it.remove();	
					}
				}
			}
		}
		
		return (collColl.size() < origSize);
	}
}
