
//	Copyright 2011 University of Dundee.
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
import com.google.common.collect.Sets;

/**
 * A class to implement Dung's abstract argumentation frameworks (AFs). 
 *
 * <p> A {@code DungAF} represents an AF. Arguments and attacks are represented as {@code String}s and two-element 
 * {@code String}-arrays ([<i>attacker</i>, <i>attacked</i>]), respectively. A {@code DungAF} may also record 
 * information concerning the interpretation of its AF, according to the semantics implemented by this class -
 * 
 * <ul>
 * <li> admissible </li>  
 * <li> complete </li>  
 * <li> eager </li>  
 * <li> grounded </li>  
 * <li> ideal </li>  
 * <li> preferred (credulous and sceptical versions) </li>  
 * <li> semi-stable </li>  
 * <li> stable </li>  
 * </ul>
 * 
 * Hence a {@code DungAF} may record not merely an AF, but also its admissible sets, complete extensions etc. Such 
 * information is recorded whenever it is generated in response to a call, and is discarded whenever the object's AF 
 * is changed. Thus for as long as the object's AF remains unchanged, repeated calls to (for instance) 
 * {@link #getGroundedExt getGroundedExt()} do not result in repeated calculations of the grounded extension. </p> 
 * 
 * <p> The implementations of the admissible semantics and the grounded semantics use existing algorithms - 
 *
 * <ul>
 * <li> <b>admissible</b>: this class uses a simplified and slightly modified version of an algorithm of Vreeswijk's 
 *		for finding the <i>defence-sets</i> around arguments; given the defence-sets, it is largely straightforward to 
 *		find the admissible sets. On defence-sets and Vreeswijk's algorithm, see Vreeswijk's 2006 paper -
 * <br/>
 * <br/>
 * <ul><i>An algorithm to compute minimally grounded and admissible defence sets in argument systems</i>
 * <br/>
 * <u>Proceedings of COMMA'06: pp.109-20</u>.</ul>
 * <br/>
 * <br/> </li>  
 * <li> <b>grounded</b>: this class uses an algorithm which is implied by the definition of the grounded semantics, 
 *		on which see Dung's original 1995 paper -
 * <br/>
 * <br/>
 * <ul><i>On the acceptability of arguments and its fundamental role in nonmonotonic reasoning, logic programming 
 *		  and n-Person games</i>
 * <br/>
 * <u>Artificial Intelligence (77:2): pp.321-58</u>.</ul>
 * <br/> </li>  
 * </ul> 
 *
 * For the other semantics listed above, implementation proceeds straightforwardly from the implementation of 
 * the admissible semantics. Further details on the latter are provided
 * <a href="../admissibleSemantics.pdf">here</a>. </p>
 *
 * <p> <b>Note</b>: henceforth, for the sake of brevity, {@code DungAF}s will often be referred to simply as AFs - that 
 * is, as if each recorded merely an AF, and could not also record its admissible sets, complete extensions, etc. </p>
 *
 * <p> <b>Note</b>: this class is <b>not</b> synchronized. It is not clear what might happen if, for instance, a 
 * {@code DungAF} was asked to add attacks to its AF, while it was generating the admissible sets of its AF. </p>
 *
 * <p> <b>Note</b>: this class depends on Google's 
 * <a href="http://code.google.com/p/guava-libraries/">guava-libraries</a> (version 10.0.01) - specifically, the 
 * implementation of the <i>n</i>-ary cartesian product provided by <a href="http://docs.guava-libraries.googlecode.com/git-history/v10.0.1/javadoc/com/google/common/collect/Sets.html#cartesianProduct(java.util.List)">{@code com.google.common.collect.Sets.cartesianProduct(List<? extends Set<? extends B>>)}</a>. </p>
 */
public class DungAF {
	
	//----- FIELDS -----------------------------------------------------------------------------------------------------
	
	//-------- fundamentals ----------
	/**
	 * This AF's arguments. 
	 */
	private HashSet<String> args;
	
	/**
	 * This AF's attacks. 
	 */
	private HashSet<String[]> atts;
	
	//-------- maps ----------
	/**
	 * A map indicating, for each of this AF's arguments, the arguments attacking it in this AF.  
	 */
	private HashMap<String,HashSet<String>> argsToAttackers;
	
	/**
	 * A map indicating, for each of this AF's arguments, the arguments attacked by it in this AF.
	 */
	private HashMap<String,HashSet<String>> argsToTargets;
	
	/**
	 * A map indicating, for none, some or all of this AF's arguments, the defence-sets around each of those arguments 
	 * in this AF.
	 *
	 * <p> On defence-sets, see {@link #getDefenceSetsAround(String) getDefenceSetsAround(String)}. </p>
	 */
	private HashMap<String,HashSet<HashSet<String>>> argsToDefenceSets;
	
	//-------- other semantics-related fields ----------
	/**
	 * This AF's admissible sets.  
	 */
	private HashSet<HashSet<String>> admissibleSets;
	
	/**
	 * This AF's complete extensions. 
	 */
	private HashSet<HashSet<String>> completeExts;
	
	/**
	 * This AF's eager extension. 
	 */
	private HashSet<String> eagerExt;
	
	/**
	 * This AF's grounded extension. 
	 */
	private HashSet<String> groundedExt;
	
	/**
	 * This AF's ideal extension. 
	 */
	private HashSet<String> idealExt;
	
	/**
	 * This AF's preferred extensions. 
	 */
	private HashSet<HashSet<String>> preferredExts;
	
	/**
	 * The extension prescribed by the <i>sceptical</i> preferred semantics for this AF.  
	 */
	private HashSet<String> preferredScepticalExt;
	
	/**
	 * This AF's semi-stable extensions. 
	 */
	private HashSet<HashSet<String>> semiStableExts;
	
	/**
	 * This AF's stable extensions. 
	 */
	private HashSet<HashSet<String>> stableExts;
		
	//----- CONSTRUCTORS -----------------------------------------------------------------------------------------------
	
	/**
	 * Constructs an empty AF.
	 */		
	public DungAF() {
		
		args = new HashSet<String>(); 
		atts = new HashSet<String[]>(); 
		
		argsToAttackers = new HashMap<String,HashSet<String>>();
		argsToTargets = new HashMap<String,HashSet<String>>();
		argsToDefenceSets = new HashMap<String,HashSet<HashSet<String>>>();
	}
	
	/**
	 * Constructs a copy of {@code anotherAF}. 
	 *
	 * <p> The values of <i>all</i> fields in {@code anotherAF} - including semantics-related fields - are 
	 * copied into the constructed object. </p>
	 *
	 * @param anotherAF a {@code DungAF}.
	 */		
	public DungAF(DungAF anotherAF) {
				
		args = new HashSet<String>(anotherAF.getArgs());
		atts = new HashSet<String[]>(anotherAF.getAtts());
		List<String> implemSemantics = Arrays.asList("eager", "grounded", "ideal", "preferredSceptical", "admissible",
													 "complete", "preferred", "stable", "semiStable");
		List<String> uniqueExtensionSemantics = Arrays.asList("eager", "grounded", "ideal", "preferredSceptical");
		boolean semanticsIsUniqueExtension;
		HashSet<String> argSet;
		HashSet<HashSet<String>> argSets;
		String fieldName;
		String methodName;
		
		
		argsToAttackers = new HashMap<String,HashSet<String>>();
		argsToTargets = new HashMap<String,HashSet<String>>();
		for (String nextArg : args) {
			argsToAttackers.put(nextArg, new HashSet<String>());
			argsToTargets.put(nextArg, new HashSet<String>());
		}
		for (String nextAtt[] : atts) {
			 argsToAttackers.get(nextAtt[1]).add(nextAtt[0]);
			 argsToTargets.get(nextAtt[0]).add(nextAtt[1]);
		}
		
		argsToDefenceSets = new HashMap<String,HashSet<HashSet<String>>>();
		for (String nextArg : args) {
			if (anotherAF.recordsDefenceSetsAround(nextArg)) {
				argsToDefenceSets.put(nextArg, anotherAF.getDefenceSetsAround(nextArg));
			}
		}
		
		for (String nextSemantics : implemSemantics) {
			semanticsIsUniqueExtension = uniqueExtensionSemantics.contains(nextSemantics);
			
			if (nextSemantics.equals("admissible")) {
				fieldName = "admissibleSets";
				methodName = "getAdmissibleSets";
			} else {
				fieldName = nextSemantics + "Ext" + (semanticsIsUniqueExtension ? "" : "s");
				methodName = "get" + nextSemantics.substring(0,1).toUpperCase() + nextSemantics.substring(1);
				methodName += semanticsIsUniqueExtension ? "Ext" : "Exts";
			}
			
			try {
				 if (anotherAF.recordsExtsOfType(nextSemantics)) {
					 if (semanticsIsUniqueExtension) {
						 argSet = (HashSet<String>) DungAF.class.getMethod(methodName).invoke(anotherAF);
					     DungAF.class.getDeclaredField(fieldName).set(this, argSet);						 
					 } else {
						 argSets = (HashSet<HashSet<String>>) DungAF.class.getMethod(methodName).invoke(anotherAF);
						 DungAF.class.getDeclaredField(fieldName).set(this, argSets);
					 }
				 }
			} catch (Exception e) {
				throw new RuntimeException();	// should never happen.
			}
		}
	}
	
	/**
	 * Constructs the AF comprising the set-view of the specified collection of attacks, and all arguments involved in
	 * any of those attacks.
	 *
	 * @param attsParam a {@code Collection} of {@code String}-arrays (which should all denote attacks, and hence have
	 * exactly two elements).
	 * @throws IllegalArgumentException if {@code attsParam} contains a {@code String}-array which does not 
	 * have exactly two elements.
	 */		
	public DungAF(Collection<String[]> attsParam) {		
		
		this(new HashSet<String>(), attsParam);
	}
	
	/**
	 * Constructs the AF (<i>args</i>, <i>atts</i>), where <i>args</i> comprises the set-view of {@code argsParam} and 
	 * all arguments involved in any attack in {@code attsParam}; and <i>atts</i> comprises the set-view of
	 * {@code attsParam}. 
	 *
	 * @param argsParam a {@code Collection} of {@code String}s, denoting arguments.
	 * @param attsParam a {@code Collection} of {@code String}-arrays (which should all denote attacks, and hence have
	 * exactly two elements).
	 * @throws IllegalArgumentException if {@code attsParam} contains a {@code String}-array which does not have 
	 * exactly two elements.
	 */	
	public DungAF(Collection<String> argsParam, Collection<String[]> attsParam) {
				
		if (!arraysDenoteAttacks(attsParam)) {
			throw new IllegalArgumentException(
				"by constructor '" + getClass().getName() + "(argsParam, attsParam)' --- " 
				+ "'attsParam' included at least one String-array which did not have exactly two elements.");
		} else {
			/* set args, ensuring that every argument involved in any attack in attsParam is added to args. */
			args = new HashSet<String>(argsParam);
			for (String[] nextAtt : attsParam) {
				args.addAll(Arrays.asList(nextAtt));
			}				
			
			atts = new HashSet<String[]>();
			for (String[] nextAtt : attsParam) {
				atts.add(Arrays.copyOf(nextAtt, 2));		
			}
			/* attacks are arrays, so atts might contain duplicates */
			removeDuplicateAttacks();
			
			argsToAttackers = new HashMap<String,HashSet<String>>();
			argsToTargets = new HashMap<String,HashSet<String>>();
			for (String nextArg : args) {
				argsToAttackers.put(nextArg, new HashSet<String>());
				argsToTargets.put(nextArg, new HashSet<String>());
			}
			for (String[] nextAtt : attsParam) {
				argsToAttackers.get(nextAtt[1]).add(nextAtt[0]);
				argsToTargets.get(nextAtt[0]).add(nextAtt[1]);
			}
			
			argsToDefenceSets = new HashMap<String,HashSet<HashSet<String>>>();
		}
	}
	
	//----- MISCELLANEOUS BASIC METHODS --------------------------------------------------------------------------------
	
	/**
	 * Returns this AF's arguments.
	 *
	 * @return a set of {@code String}s, denoting this AF's arguments.
	 */	
	 public HashSet<String> getArgs() { 
		
		return new HashSet<String>(args); 
	}
	
	/**
	 * Returns this AF's attacks. 
	 *
	 * @return a set of {@code String}-arrays, denoting this AF's attacks.
	 */
	public HashSet<String[]> getAtts() { 
		
		HashSet<String[]> copiesOfAtts = new HashSet<String[]>();
		
		for (String[] nextAtt : atts) {
			copiesOfAtts.add(Arrays.copyOf(nextAtt, 2));	
		}
		
		return copiesOfAtts; 
	}
	
	/**
	 * Returns the arguments attacking {@code arg} in this AF.
	 *
	 * @param arg a {@code String}, denoting an argument.
	 * @return a set of {@code String}s, denoting the arguments attacking {@code arg} in this AF.
	 */
	public HashSet<String> getAttackersOf(String arg) { 
		
		return args.contains(arg) ? new HashSet<String>(argsToAttackers.get(arg)) : new HashSet<String>(); 
	}	
	
	/**
	 * Returns the arguments attacked by {@code arg} in this AF.
	 *
	 * @param arg a {@code String}, denoting an argument.
	 * @return a set of {@code String}s, denoting the arguments attacked by {@code arg} in this AF.
	 */
	public HashSet<String> getTargetsOf(String arg) {
		
		return args.contains(arg) ? new HashSet<String>(argsToTargets.get(arg)) : new HashSet<String>();
	}	
	
	/**
	 * Adds the arguments in the specified collection to this AF. 
	 *
	 * @param argsToBeAdded a {@code Collection} of {@code String}s, denoting arguments.
	 * @return {@code true} if this AF changed as a result of the call.
	 */	
	public boolean addArgs(Collection<String> argsToBeAdded) {
		
		return addArgs(argsToBeAdded.toArray(new String[0]));
	}
	
	 /**
	 * Adds the specified arguments to this AF.
	 *
	 * @param argsToBeAdded one or more {@code String}s, denoting arguments.
	 * @return {@code true} if this AF changed as a result of the call.
	 */	
	public boolean addArgs(String ... argsToBeAdded) {
		
		for (String nextArg : argsToBeAdded) {
			if (!args.contains(nextArg)) {
				argsToAttackers.put(nextArg, new HashSet<String>());
				argsToTargets.put(nextArg, new HashSet<String>());
			}
		}
		
		if (args.addAll(Arrays.asList(argsToBeAdded))) {
			removeSemanticsInfo();
			return true;			
		} else { 
			return false; 
		}
	}

	/**
	 * Adds the attacks comprising the specified collection to this AF. If {@code attsToBeAdded} do not all denote
	 * attacks, this AF remains unchanged.
	 *
	 * @param attsToBeAdded a {@code Collection} of {@code String}-arrays (which should all denote attacks, and hence
	 * have exactly two elements).
	 * @return {@code true} if this AF changed as a result of the call.
	 * @throws IllegalArgumentException if {@code attsToBeAdded} contains a {@code String}-array which does not have 
	 * exactly two elements.
	 */	
	public boolean addAtts(Collection<String[]> attsToBeAdded) {
		
		return addAtts(attsToBeAdded.toArray(new String[0][]));		
	}
	
	/**
	 * Adds the specified attacks to this AF. If {@code attsToBeAdded} do not all denote attacks, this AF remains
	 * unchanged.
	 *
	 * @param attsToBeAdded one or more {@code String}-arrays (which should all denote attacks, and hence have exactly
	 * two elements).
	 * @return {@code true} if this AF changed as a result of the call.
	 * @throws IllegalArgumentException if {@code attsToBeAdded} contains a {@code String}-array which does not have 
	 * exactly two elements.
	 */	
	public boolean addAtts(String[] ... attsToBeAdded) {
		
		int attsCount;
				
		if (!arraysDenoteAttacks(attsToBeAdded)) {
			throw new IllegalArgumentException(
				"by '" + getClass().getName() + ".addAtts(String[]... )' --- "
				+ "parameter(s) included at least one String-array which did not have exactly two elements.");	
		} else {
			attsCount = atts.size();
			
			for (String[] nextAtt : attsToBeAdded) {
				atts.add(Arrays.copyOf(nextAtt,2)); 
				addArgs(nextAtt[0], nextAtt[1]);
				argsToAttackers.get(nextAtt[1]).add(nextAtt[0]);
				argsToTargets.get(nextAtt[0]).add(nextAtt[1]);
			}
			
			/* attacks are arrays, so atts might contains duplicates. */
			removeDuplicateAttacks();
			
			if (attsCount < atts.size()) {
				removeSemanticsInfo();
				return true;
			} else {
				return false;	
			}
		}
	}
	
	/**
	 * Removes the arguments in the specified collection from this AF.
	 *
	 * @param argsToBeRemoved a {@code Collection} of {@code String}s, denoting arguments.
	 * @return {@code true} if this AF changed as a result of the call.
	 */		
	public boolean removeArgs(Collection<String> argsToBeRemoved) {
				
		return removeArgs(argsToBeRemoved.toArray(new String[0]));
		
	}
	
	/**
	 * Removes the specified arguments from this AF.
	 * 
	 * @param argsToBeRemoved one or more {@code String}s, denoting arguments.
	 * @return {@code true} if this AF changed as a result of the call.
	 */	
	public boolean removeArgs(String ... argsToBeRemoved) {
		
		HashSet<String[]> attsToBeRemoved = new HashSet<String[]>();
		
		if (args.removeAll(Arrays.asList(argsToBeRemoved))) {			
			for (String[] nextAtt : atts) {
				if (!args.contains(nextAtt[0]) || !args.contains(nextAtt[1])) { 
					attsToBeRemoved.add(nextAtt); 
				}
			}
			removeAtts(attsToBeRemoved);
			
			argsToAttackers.keySet().removeAll(Arrays.asList(argsToBeRemoved));
			argsToTargets.keySet().removeAll(Arrays.asList(argsToBeRemoved));
			
			removeSemanticsInfo();
			
			return true;
		} else { 
			return false; 
		}
	}
	
	/**
	 * Removes the attacks comprising the specified collection from this AF. If {@code attsToBeRemoved} do not all
	 * denote attacks, this AF remains unchanged.
	 *
	 * @param attsToBeRemoved a {@code Collection} of {@code String}-arrays (which should all denote attacks, and hence
	 * have exactly two elements).
	 * @return {@code true} if this AF changed as a result of the call.
	 * @throws IllegalArgumentException if {@code attsToBeRemoved} contains a {@code String}-array which does not have 
	 * exactly two elements.
	 */	
	public boolean removeAtts(Collection<String[]> attsToBeRemoved) {
	
		return removeAtts(attsToBeRemoved.toArray(new String[0][]));
	}
	
	/**
	 * Removes the specified attacks from this AF. If {@code attsToBeRemoved} do not all denote attacks, this AF
	 * remains unchanged.
	 *
	 * @param attsToBeRemoved one or more {@code String}-arrays (which should all denote attacks, and hence have exactly
	 * two elements). 
	 * @return {@code true} if this AF changed as a result of the call.
	 * @throws IllegalArgumentException if {@code attsToBeRemoved} contains a {@code String}-array which does not have 
	 * exactly two elements.
	 */	
	public boolean removeAtts(String[] ... attsToBeRemoved) {
		
		int attsCount = atts.size();
		String[] tempAtt;
		
		if (!arraysDenoteAttacks(attsToBeRemoved)) {
			throw new IllegalArgumentException(
				"by '" + getClass().getName() + ".removeAtts(String[]... )' --- "
				+ "parameter(s) included at least one String-array which did not have exactly two elements.");
		} else {
			for (Iterator<String[]> it = atts.iterator(); it.hasNext(); ) {
				tempAtt = it.next();
				for (String[] nextAttToBeRemoved : attsToBeRemoved) {
					if (Arrays.equals(tempAtt, nextAttToBeRemoved)) {
						it.remove();
						break;
					}
				}
			}
			
			if (attsCount > atts.size()) {
				for (String[] nextAtt : attsToBeRemoved) {	 
					argsToAttackers.get(nextAtt[1]).remove(nextAtt[0]); 
					argsToTargets.get(nextAtt[0]).remove(nextAtt[1]); 
				}
				
				removeSemanticsInfo();
				
				return true;
			} else {
				return false;	
			}
		}
	}
	
	/**
	 * Ensures that this AF is a supergraph of {@code anotherAF}.
	 *
	 * @param anotherAF a {@code DungAF}.
	 * @return {@code true} if this AF changed as a result of the call.
	 */		
	public boolean ensureSubsumes(DungAF anotherAF) {
		
		if (addArgs(anotherAF.getArgs()) | addAtts(anotherAF.getAtts())) {
			removeSemanticsInfo();
			return true;
		} else { 
			return false; 
		}
	}
	
	/**
	 * Ensures that this AF contains none of the arguments in {@code anotherAF}. 
	 *
	 * @param anotherAF a {@code DungAF}.
	 * @return {@code true} if this AF changed as a result of the call.
	 */		
	public boolean ensureDisjointWith(DungAF anotherAF) {
		
		if (removeArgs(anotherAF.getArgs())) {
			removeSemanticsInfo();
			return true;
		} else { 
			return false; 
		}
	}
	
	/**
	 * Removes from this object all information concerning the interpretation of its AF. 
	 *
	 * <p> Typically called by methods which can change the AF. </p>
	 */	
	private void removeSemanticsInfo() {
		
		argsToDefenceSets.clear();
		
		admissibleSets = null;
		completeExts = null;
		eagerExt = null;
		groundedExt = null; 
		idealExt = null;
		preferredExts = null;
		preferredScepticalExt = null;
		semiStableExts = null;
		stableExts = null;
	}
	
	/**
	 * Removes all arguments and attacks from this AF.
	 */		
	public void clear() {
	
		args.clear();
		atts.clear();
		argsToTargets.clear();
		argsToAttackers.clear();
	}	
	
	/**
	 * Returns {@code true} if this object and {@code anotherAF} record the same AF.
	 *
	 * <p> Thus two 'equal' objects may differ with respect to the information they record about their AF's 
	 * interpretation. </p>
	 *
	 * @param anotherAF a {@code DungAF}.
	 * @return {@code true} if this object and {@code anotherAF} record the same AF.
	 */		
	public boolean equals(DungAF anotherAF) {
						
		if (args.equals(anotherAF.getArgs()) && (atts.size() == anotherAF.getAtts().size())) {			
			for (String nextArg : args) {
				if (!getAttackersOf(nextArg).equals(anotherAF.getAttackersOf(nextArg))) {
					return false;
				}
			}
			
			return true;
		} else {
			return false;	
		}
	}
	
	/**
	 * Returns {@code true} if this AF is a (strict or non-strict) supergraph of {@code anotherAF}.
	 *
	 * @param anotherAF a {@code DungAF}.
	 * @return {@code true} if this AF is a (strict or non-strict) supergraph of {@code anotherAF}.
	 */		
	public boolean subsumes(DungAF anotherAF) {
		
		if (args.containsAll(anotherAF.getArgs()) && (atts.size() >= anotherAF.getAtts().size())) {			
			for (String nextArg : args) {
				if (!getAttackersOf(nextArg).containsAll(anotherAF.getAttackersOf(nextArg))) {
					return false;
				}
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns {@code true} if this AF contains contains none of the arguments in {@code anotherAF}.
	 *
	 * @param anotherAF a {@code DungAF}.
	 * @return {@code true} if this AF contains none of the arguments in {@code anotherAF}.
	 */		
	public boolean isDisjointWith(DungAF anotherAF) {
		
		return Collections.disjoint(args, anotherAF.getArgs());
	}
	
	/**
	 * Returns {@code true} if this object records the extension(s) prescribed by {@code semantics} for its AF, where 
	 * {@code semantics} is a semantics implemented by this class.
	 *
	 * <p> The semantics implemented by this class are recognized by the following names (case-sensitive) - 
	 * <ul>
	 * <li> admissible </li>  
	 * <li> complete </li>  
	 * <li> eager </li>  
	 * <li> grounded </li>  
	 * <li> ideal </li>  
	 * <li> preferred </li>  
	 * <li> preferredSceptical </li>  
	 * <li> semiStable </li>  
	 * <li> stable </li>  
	 * </ul>
	 * </p>
	 *
	 * @param semantics a {@code String}, being the name of a semantics implemented by this class.
	 * @return {@code true} if the relevant field of this object is non-{@code null}.
	 * @throws IllegalArgumentException if {@code semantics} is not the name of a semantics implemented by this class. 
	 */			
	public boolean recordsExtsOfType(String semantics) {
				
		List<String> uniqueExtensionSemantics = Arrays.asList("eager", "grounded", "ideal", "preferredSceptical");
		List<String> otherImplemSemantics = Arrays.asList("admissible", "complete", 
														  "preferred", "semiStable", "stable");
		String fieldName;
				
		if (semantics.equals("admissible")) {
			fieldName = "admissibleSets";
		} else if (uniqueExtensionSemantics.contains(semantics)) {
			fieldName = semantics + "Ext";
		} else if (otherImplemSemantics.contains(semantics)) {
			fieldName = semantics + "Exts";
		} else {
			throw new IllegalArgumentException("by '" + getClass().getName() + ".recordsExtsOfType(String semantics)' "
											   + "--- \"" + semantics + "\" is not a semantics implemented by " 
											   + getClass().getName() + ".");
		}
		
		try {
			return (null != DungAF.class.getDeclaredField(fieldName).get(this));
		} catch (Exception e) {
			throw new RuntimeException();	// should never happen.
		}
	}
	
	/**
	 * Returns {@code true} if this object records the defence-sets around the specified arguments in its AF.
	 *
	 * <p> On defence-sets, see {@link #getDefenceSetsAround(String) getDefenceSetsAround(String)}. </p>
	 *
	 * @param args one or more {@code String}s, denoting arguments.
	 * @return {@code true} if this object records the defence-sets around the specified arguments in its AF.
	 */			
	public boolean recordsDefenceSetsAround(String... args) {
		
		for (String nextArg : args) {
			if (null == argsToDefenceSets.get(nextArg)) {
				return false;
			}
		}
				
		return true;
	}
	
	/**
	 * Returns a representation of this AF in the conventional format - 
	 * ({<i>arg1</i>, <i>arg2</i>,...}, {(<i>arg1</i>, <i>arg2</i>), (<i>arg2</i>, <i>arg1</i>),...}).
	 *
	 * @return a {@code String} representation of this AF.
	 */		
	public String toString() {
		
		HashSet<String> formattedAttacks = new HashSet<String>();
		for (String[] nextAtt : atts) {
			formattedAttacks.add("("+nextAtt[0]+", "+nextAtt[1]+")");	
		}
		
		return "({"+ args.toString().substring(1,args.toString().length()-1) + "}, {" 
		+ formattedAttacks.toString().substring(1,formattedAttacks.toString().length()-1)  + "})";
	}
	
	
	/**
	 * Returns {@code true} if the specified {@code String}-arrays all denote attacks.
	 *
	 * @param strArrs one or more {@code String}-arrays.
	 * @return {@code true} if {@code strArrs} contains only two-element {@code String}-arrays.
	 */
	 static public boolean arraysDenoteAttacks(String[] ... strArrs) {
		 
		 return arraysDenoteAttacks(Arrays.asList(strArrs));
	 }
	
	/**
	 * Returns {@code true} if the specified collection comprises {@code String}-arrays which denote attacks.
	 *
	 * @param strArrs a {@code Collection} of {@code String}-arrays.
	 * @return {@code true} if {@code strArrs} contains only two-element {@code String}-arrays.
	 */		
	static public boolean arraysDenoteAttacks(Collection<String[]> strArrs) {
		
		for (String[] nextArr : strArrs) {
			if (nextArr.length != 2) {
				return false;
			}
		}
		
		return true;
	}	
	
	/**
	 * Ensures that this object records only a single copy of each of the attacks in its AF. 
	 *
	 * <p> Attacks are recorded as arrays, so this method is useful when constructing {@code DungAF}s, and when  
	 * expanding the AFs they record. </p>
	 *
	 * @return {@code true} if this AF changed as a result of the call.
	 */	
	private boolean removeDuplicateAttacks() {
	
		int attsCount = atts.size();
		String[] tempAtt;
		
		for (Iterator<String[]> it = atts.iterator(); it.hasNext(); ) {
			tempAtt = it.next();
			for (String[] nextAtt : atts) {
				if ((nextAtt != tempAtt) && Arrays.equals(tempAtt, nextAtt)) {
					it.remove();
					break; // else concurrentModificationException.
				}
			}
		}
		
		return attsCount > atts.size();
	}
	
	
	//----- MISCELLANEOUS STATIC METHODS -------------------------------------------------------------------------------
	
	/**
	 * Returns an AF which satisfies the constraints passed as parameters, but is otherwise constructed at random.
	 *
	 * @param minArgs an {@code int}, being the lower bound on the number of arguments. 
	 * @param maxArgs an {@code int}, being the upper bound on the number of arguments. 
	 * @param minAtts an {@code int}, being the lower bound on the number of attacks.
	 * @param maxAtts an {@code int}, being the upper bound on the number of attacks.
	 * @param argPool a {@code Collection} of {@code String}s, denoting the arguments which may be used.
	 *
	 * @return a {@code DungAF} recording an AF which satisfies the constraints passed as parameters.
	 *
	 * @throws IllegalArgumentException if the specified constraints cannot be satisfied.
	 */	
	static public DungAF getRandomDungAF(int minArgs, int maxArgs, int minAtts, int maxAtts, 
										 Collection<String> argPool) {
		
		int numOfArgs;
		int numOfAtts;
		ArrayList<String> uniqueArgs;
		HashSet<String[]> atts = new HashSet<String[]>();
		HashSet<ArrayList<String>> attsAsLists = new HashSet<ArrayList<String>>();
		String tempAtt[] = new String[2];
		
		// initialize uniqueArgs from the set-view of argPool.
		uniqueArgs = new ArrayList<String>(new HashSet<String>(argPool));
		
		// check upper-bound constraints make sense, and for inconsistency in constraints.
		if (Integer.signum(maxArgs) == -1 || Integer.signum(maxAtts) == -1) {
			throw new IllegalArgumentException("by 'DungAF.getRandomDungAF(minArgs, maxArgs, minAtts, "
											   + "maxAtts, argPool)' --- 'maxArgs' or 'maxAtts' is negative.");
		} else if (minArgs > maxArgs) {
			throw new IllegalArgumentException("by 'DungAF.getRandomDungAF(minArgs, maxArgs, minAtts, "
											   + "maxAtts, argPool)' --- 'minArgs' is greater than 'maxArgs'.");
		} else if (minArgs > uniqueArgs.size()) { 
			throw new IllegalArgumentException("by 'DungAF.getRandomDungAF(minArgs, maxArgs, minAtts, "
											   + "maxAtts, argPool)' --- 'argPool' contains fewer than 'minArgs' "
											   + "unique arguments."); 
		} else if (minAtts > maxAtts) {
			throw new IllegalArgumentException("by 'DungAF.getRandomDungAF(minArgs, maxArgs, minAtts, "
											   + "maxAtts, argPool)' --- 'minAtts' is greater than 'maxAtts'."); 
		} else if (minAtts > Math.pow(uniqueArgs.size(), 2)) {
			throw new IllegalArgumentException("by 'DungAF.getRandomDungAF(minArgs, maxArgs, minAtts, "
											   + "maxAtts, argPool)' --- 'argPool' contains too few unique arguments "
											   + "for 'minAtts'.");
		} else if (minAtts > Math.pow(maxArgs, 2)) {
			throw new IllegalArgumentException("by 'DungAF.getRandomDungAF(minArgs, maxArgs, minAtts, "
											   + "maxAtts, argPool)' --- 'maxArgs' is too small for 'minAtts'.");
		}
		
		// adjust minArgs, maxArgs, minAtts and maxAtts, if appropriate.
		if (minArgs < 0) {
			minArgs = 0;	
		}
		
		if (minAtts < 0) {
			minAtts = 0;	
		}
		
		if (maxArgs > uniqueArgs.size()) {
			maxArgs = uniqueArgs.size();
		}
		
		if (maxAtts > (Math.pow(maxArgs, 2))) {
			maxAtts = (int) Math.pow(maxArgs, 2);	
		}
		
		// fix the numbers of arguments and attacks at random values within the required ranges.		
		numOfArgs = minArgs + (int) Math.rint((Math.random()*(maxArgs-minArgs)));
		numOfAtts = minAtts + (int) Math.rint((Math.random()*(maxAtts-minAtts)));
		
		// reduce uniqueArgs by random removal.
		while (uniqueArgs.size() > numOfArgs) { 
			uniqueArgs.remove(uniqueArgs.get((int) Math.rint(Math.random()*(uniqueArgs.size()-1)))); 
		} 
		
		// define attacks between remaining arguments at random.
		for (int i = 0; i < numOfAtts; i++) { 			
			tempAtt[0] = uniqueArgs.get((int) Math.rint(Math.random()*(uniqueArgs.size()-1)));
			tempAtt[1] = uniqueArgs.get((int) Math.rint(Math.random()*(uniqueArgs.size()-1))); 
			if (attsAsLists.add(new ArrayList(Arrays.asList(tempAtt)))) { 
				atts.add(Arrays.copyOf(tempAtt, 2)); 
			} else {
				i--;
			}
		}
		
		return new DungAF(new HashSet<String>(uniqueArgs), atts);		
	}
	
	
	//----- METHODS CONCERNING CONFLICT BETWEEN ARGUMENTS --------------------------------------------------------------
	
	/**
	 * Returns {@code true} if the specified arguments form a conflict-free set in this AF.
	 *
	 * @see #containsNoConflictAmong(String[]) containsNoConflictAmong(String... )
	 * @param argsParam one or more {@code String}s, denoting arguments.
	 * @return {@code true} if this AF contains all of {@code argsParam}; and, in this AF, none of those arguments  
	 * attacks itself or any other argument in {@code argsParam}.
	 */		
	public boolean hasAsConflictFreeSet(String ... argsParam) {
		
		return hasAsConflictFreeSet(Arrays.asList(argsParam));
	}
	
	/**
	 * Returns {@code true} if the set-view of the specified collection is a conflict-free set in this AF.
	 *
	 * @see #containsNoConflictAmong(Collection) containsNoConflictAmong(Collection&ltString&gt) 
	 * @param argsParam a {@code Collection} of {@code String}s, denoting arguments.
	 * @return {@code true} if this AF contains all of {@code argsParam}; and, in this AF, none of those arguments  
	 * attacks itself or any other argument in {@code argsParam}.
	 */		
	public boolean hasAsConflictFreeSet(Collection<String> argsParam) {
		
		for (String nextArg : argsParam) {
			if (!args.contains(nextArg) || !Collections.disjoint(getAttackersOf(nextArg), argsParam)) {
				return false;	
			}
		}
		
		return true;
	}
	
	/**
	 * Returns {@code true} if, in this AF, none of the specified arguments attacks itself or any of the other specified
	 * arguments.
	 *
	 * @see #hasAsConflictFreeSet(String[]) hasAsConflictFreeSet(String... )
	 * @param argsParam one or more {@code String}s, denoting arguments.
	 * @return {@code true} if, in this AF, none of the arguments in {@code argsParam} attacks itself or any other 
	 * argument in {@code argsParam}.
	 */		
	public boolean containsNoConflictAmong(String ... argsParam) {
		
		return containsNoConflictAmong(Arrays.asList(argsParam));
	}
	
	/**
	 * Returns {@code true} if, in this AF, no argument in the specified collection attacks itself or any other 
	 * argument in the collection.
	 *
	 * @see #hasAsConflictFreeSet(Collection) hasAsConflictFreeSet(Collection&ltString&gt)
	 * @param argsParam a {@code Collection} of {@code String}s, denoting arguments.
	 * @return {@code true} if, in this AF, none of the arguments in {@code argsParam} attacks itself or any other
	 * argument in {@code argsParam}.
	 */		
	public boolean containsNoConflictAmong(Collection<String> argsParam) {
		
		for (String nextArg : argsParam) {
			if (!Collections.disjoint(getAttackersOf(nextArg), argsParam)) {
				return false;	
			}
		}
		
		return true;
	}	
	
	/**
	 * Returns {@code true} if the specified collections unite to form a conflict-free set in this AF.
	 *
	 * @see #containsNoConflictAmongUnionOf(Collection) 
	 * containsNoConflictAmongUnionOf(Collection&ltT extends Collection&ltString&gt&gt)
	 * @param argColls a {@code Collection} of {@code String}-{@code Collection}s, denoting argument-collections.
	 * @return {@code true} if this AF contains all of the arguments in the union of {@code argColls}; contains no 
	 * attack by any of those arguments on itself; and contains no attack between any two of those arguments. 
	 */
	public <T extends Collection<String>> boolean hasUnionOfAsConflictFreeSet(Collection<T> argColls) {
		
		HashSet<String> union = new HashSet<String>();
		
		for (Collection<String> nextArgColl : argColls) { 
			union.addAll(nextArgColl);
		}
		
		return (args.containsAll(union) && hasAsConflictFreeSet(union));
	}
	
	/**
	 * Returns {@code true} if, in this AF, none of the arguments in the union of the specified collections 
	 * attacks itself or any other argument in the union.
	 *
	 * @see #hasUnionOfAsConflictFreeSet(Collection) 
	 * hasUnionOfAsConflictFreeSet(Collection&ltT extends Collection&ltString&gt&gt)
	 * @param argColls a {@code Collection} of {@code String}-{@code Collection}s, denoting argument-collections.
	 * @return {@code true} if, in this AF, none of the arguments in the union of {@code argColls} attacks itself or any
	 * other argument in the union.
	 */
	public <T extends Collection<String>> boolean containsNoConflictAmongUnionOf(Collection<T> argColls) {
		
		HashSet<String> union = new HashSet<String>();
		
		for (Collection<String> nextArgColl : argColls) { 
			union.addAll(nextArgColl);
		}
		
		return containsNoConflictAmong(union);
	}
	
	/**
	 * Returns {@code true} if, in this AF, any of the arguments in the specified collection is in conflict with any 
	 * of the specified arguments.
	 *
	 * @param argsParam0 a {@code Collection} of {@code String}s, denoting arguments.
	 * @param argsParam1 one or more {@code String}s, denoting arguments.
	 * @return {@code true} if, in this AF, any argument in {@code argsParam0} is attacked by/attacks any argument 
	 * in {@code argsParam1}.
	 */		
	public boolean collnIsInConflictWithAnyOf(Collection<String> argsParam0, String ... argsParam1) {
		
		for (String nextArg : argsParam1) {
			if (!(Collections.disjoint(argsParam0, getAttackersOf(nextArg)) 
				   && Collections.disjoint(argsParam0, getTargetsOf(nextArg)))) {
				return true;	
			}
		}
		
		return false;
	}
	
	//----- METHODS CONCERNING ACCEPTABILITY ---------------------------------------------------------------------------
	
	/**
	 * Returns {@code true} if all of {@code argsToCheck} are acceptable with respect to the set-view of 
	 * {@code acceptingArgColl} in this AF. 
	 *
	 * @param acceptingArgColl a {@code Collection} of {@code String}s, denoting arguments.
	 * @param argsToCheck a {@code Collection} of {@code String}s, denoting arguments.
	 * @return {@code true} if all of {@code argsToCheck} are acceptable with respect to the set-view of  
	 * {@code acceptingArgColl} in this AF.
	 */		
	public boolean argsAccept(Collection<String> acceptingArgColl, Collection<String> argsToCheck) {
	
		return argsAccept(acceptingArgColl, argsToCheck.toArray(new String[0]));		
	}
	
	/**
	 * Returns {@code true} if the specified arguments are all acceptable with respect to the set-view of 
	 * {@code acceptingArgColl} in this AF. 
	 *
	 * @param acceptingArgColl a {@code Collection} of {@code String}s, denoting arguments.
	 * @param argsToCheck one or more {@code String}s, denoting arguments.
	 * @return {@code true} if all of {@code argsToCheck} are acceptable with respect to the set-view of  
	 * {@code acceptingArgColl} in this AF.
	 */		
	public boolean argsAccept(Collection<String> acceptingArgColl, String ... argsToCheck) {
		
		HashSet<String> targetsOfArgSet = new HashSet<String>();
		
		for (String nextArg : acceptingArgColl) {
			targetsOfArgSet.addAll(getTargetsOf(nextArg));
		}
		
		for (String nextArg : argsToCheck) {
			for (String nextAttacker : getAttackersOf(nextArg)) {
				if (!targetsOfArgSet.contains(nextAttacker)) {
					return false;
				}
			}
		}

		return true;
	}
	
	/**
	 * Returns the arguments which are acceptable with respect to the set comprising the specified arguments in this AF. 
	 *
	 * @param argsParam one or more {@code String}s, denoting arguments.
	 * @return a set of {@code String}s, denoting the arguments which are acceptable with respect to the set comprising
	 * {@code argsParam} in this AF.
	 */		
	public HashSet<String> getArgsAcceptedBy(String ... argsParam) {
			
		return getArgsAcceptedBy(Arrays.asList(argsParam));
	}
	
	/**
	 * Returns the arguments which are acceptable with respect to the set-view of {@code argsParam} in this AF. 
	 *
	 * @param argsParam a {@code Collection} of {@code String}s, denoting arguments.
	 * @return a set of {@code String}s, denoting the arguments which are acceptable with respect to the set-view of
	 * {@code argsParam} in this AF.
	 */		
	public HashSet<String> getArgsAcceptedBy(Collection<String> argsParam) {
		
		HashSet<String> targets = new HashSet<String>();
		HashSet<String> acceptableArgs = new HashSet<String>();
		
		for (String nextArg : argsParam) {
			targets.addAll(getTargetsOf(nextArg));
		}
		
		for (String nextArg : getArgs()) {
			if (targets.containsAll(argsToAttackers.get(nextArg))) {
				acceptableArgs.add(nextArg);
			}
		}
			
		return acceptableArgs;
	}
				
	
	//----- 3. METHODS CONCERNING SEMANTICS ----------------------------------------------------------------------------
	
	/**
	 * Returns the union of the extensions prescribed by {@code semantics} for this AF, where {@code semantics} is a 
	 * multiple-extension semantics implemented by this class.
	 *
	 * <p> The multiple-extension semantics implemented by this class are recognized by the following names 
	 * (case-sensitive) - 
	 * <ul>
	 * <li> admissible </li>  
	 * <li> complete </li>  
	 * <li> preferred </li>  
	 * <li> semiStable </li>  
	 * <li> stable </li>  
	 * </ul>
	 </p>
	 *
	 * @param semantics a {@code String}, being the name of a multiple-extension semantics implemented by this class.	 
	 * @return a set of {@code String}s, denoting the union of the extensions prescribed by {@code semantics} for this 
	 * AF.
	 * @throws IllegalArgumentException if {@code semantics} is not the name of a multiple-extension semantics 
	 * implemented by this class.
	 */	
	public HashSet<String> getExtsUnion(String semantics) {
		
		List<String> multiExtSemantics = Arrays.asList("admissible", "complete", 
													   "preferred", "semiStable", "stable");
		HashSet<String> extsUnion;
		String methodName;
		
		if (multiExtSemantics.contains(semantics)) {
			/* all admissible sets and complete extensions are subsumed by preferred extensions; and the latter are
			 more easily calculated. */
			if (semantics.equals("preferred") || semantics.equals("admissible") || semantics.equals("complete")) {
				methodName = "getPreferredExts";
			} else {
				methodName = "get" + semantics.substring(0,1).toUpperCase() + semantics.substring(1) + "Exts";
			}
		} else {
			throw new IllegalArgumentException(
									   "by '" + getClass().getName() + ".getExtsUnion(String semantics)' --- " 
									   + "\"" + semantics + "\" is not a multiple-extension semantics implemented by " 
									   + getClass().getName() + ".");
		}
		
		try {		
			extsUnion = new HashSet<String>();
			for (HashSet<String> nextExt : (HashSet<HashSet<String>>) DungAF.class.getMethod(methodName).invoke(this)) {
				extsUnion.addAll(nextExt);
			}
		} catch (Exception e) {
			throw new RuntimeException();	// should never happen.
		}
		
		return extsUnion;
	}	
	
	/**
	 * Returns this AF's grounded extension. 
	 *
	 * @return a set of {@code String}s, denoting this AF's grounded extension.
	 */	
	public HashSet<String> getGroundedExt() {
		
		HashSet<String> defeatedArgs = new HashSet<String>();		// arguments attacked by groundedExt.
		ArrayList<String> candidateArgs = new ArrayList<String>();	// arguments not in groundedExt or defeatedArgs. 		
		
		String tempArg;
		
		/* the grounded extension might already be recorded */
		if (null != groundedExt) {				
			return new HashSet<String>(groundedExt); 
		}	
		
		groundedExt = new HashSet<String>();
		
		do {				
			candidateArgs.addAll(args); 
			candidateArgs.removeAll(groundedExt);
			candidateArgs.removeAll(defeatedArgs);
			
			/* ensure that candidateArgs contains no argument that's not acceptable wrt groundedExt; 
			 add remainder to groundedExt. */
			for (int i = 0; i < candidateArgs.size(); i++) {
				tempArg = candidateArgs.get(i);
				
				if (Collections.disjoint(getAttackersOf(tempArg), candidateArgs)) {
					groundedExt.add(tempArg);
					defeatedArgs.addAll(getTargetsOf(tempArg));
				}
			}
			
			candidateArgs.retainAll(groundedExt);
		}
		while (!candidateArgs.isEmpty());
		
		return new HashSet<String>(groundedExt);
	}

	/**
	 * Returns {@code true} if, for each of the specified collections, its set-view is admissible in this AF.
	 *
	 * <p> If {@code argColls} are all sets, this method returns the same as
	 * {@code getAdmissibleSets().containsAll(argColls)}, but without calculating the admissible sets. </p>
	 *
	 * @param argColls one or more {@code Collection}s of {@code String}s, denoting argument-collections.
	 * @return {@code true} if, for each of the specified collections, its set-view is admissible in this AF.
	 */	
	public boolean admissibleSetsContain(Collection<String> ... argColls) {
		
		HashSet<String> attackers;
		HashSet<String> targets;
		
		/* NOTE: because getAdmissibleSets() relies on this method, the following approach would cause errors - 
		
		 if (null != admissibleSets) {
			return admissibleSets.containsAll(argColls);		 
		 } else ...
		
		*/
		
		for (Collection<String> nextArgColl : argColls) {			
			attackers = new HashSet<String>();
			targets = new HashSet<String>();
			
			if (hasAsConflictFreeSet(nextArgColl)) {
				for (String nextArg : nextArgColl) {
					attackers.addAll(getAttackersOf(nextArg));
					targets.addAll(getTargetsOf(nextArg)); 
				}
				if (!targets.containsAll(attackers)) {
					return false;	
				}
			} else {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Returns the union of this AF's admissible sets.
	 *
	 * @return a set of {@code String}s, denoting the union of this AF's admissible sets.
	 */	
	public HashSet<String> getAdmissibleArgs() {
		
		return getExtsUnion("admissible");
	}
	
	/**
	 * Returns this AF's admissible sets. 
	 *
	 * @return a set of {@code String}-sets, denoting this AF's admissible sets.
	 */	
	public HashSet<HashSet<String>> getAdmissibleSets() {
		
		boolean argIsDefended;
		HashSet<String> admiSetCandidate;
		HashSet<HashSet<String>> copiesOfAdmiSets = new HashSet<HashSet<String>>();
		HashSet<HashSet<String>> toDoAdmiSets = new HashSet<HashSet<String>>();
		HashSet<HashSet<String>> newAdmiSets = new HashSet<HashSet<String>>();
		
		/* the admissible sets might already be recorded */
		if (null != admissibleSets) { 
			for (HashSet<String> nextAdmiSet : admissibleSets) {	
				copiesOfAdmiSets.add(new HashSet<String>(nextAdmiSet)); 
			}
			return copiesOfAdmiSets;
		}
		
		/* the empty set is always admissible. */ 
		admissibleSets = new HashSet<HashSet<String>>(Collections.singleton(new HashSet<String>()));
		
		/* find the minimal non-empty admissible sets. */
		for (String nextArg : args) {
			for (HashSet<String> nextDefSet : getDefenceSetsAround(nextArg)) {
				admissibleSets.add(nextDefSet);				
			}
		}
		/* find the maximal admissible sets. */
		for (HashSet<String> nextPrefExt : getPreferredExts()) { 
			admissibleSets.add(nextPrefExt);				
		}
		
		/* find the intermediate-sized admissible sets. To do this, proceed from the preferred extensions. 
		 For each preferred extension argSet, for each arg1 in argSet, proceed as follows. Remove arg1 from argSet. 
		 Then (i) remove every arg2 from argSet, such that arg2 is not acceptable wrt argSet; and 
		 (ii) repeat (i) until argSet is admissible. 
		 If argSet is already in admissibleSets, discard it; otherwise add it to admissibleSets, and repeat the 
		 whole exercise on it. */
		toDoAdmiSets.addAll(preferredExts);
		
		while (!toDoAdmiSets.isEmpty()) {			
			for (HashSet<String> nextAdmiSet : toDoAdmiSets) {
				/* find all of the admissible sets subsumed by nextAdmiSet. */
				for (String nextArg : nextAdmiSet) {
					/* create admiSetCandidate by removing nextArg from nextAdmiSet. */
					admiSetCandidate = new HashSet<String>(nextAdmiSet);	
					admiSetCandidate.remove(nextArg);	
					/* revise admiSetCandidate, until it is admissible. */
					while (!argsAccept(admiSetCandidate, admiSetCandidate)) {
						for (Iterator<String> it = admiSetCandidate.iterator(); it.hasNext();) {
							/* assume that admiSetCandidate subsumes none of the argument's defence-sets. */
							argIsDefended = false;
							/* check that assumption. */
							for (HashSet<String> nextDefenceSet : argsToDefenceSets.get(it.next())) { 
								if (admiSetCandidate.containsAll(nextDefenceSet)) { 
									argIsDefended = true;
									break; 
								} 
							}
							
							if (!argIsDefended) { 
								it.remove();
							}	
						}
					}
					/* if it has already been found, admiSetCandidate must be disregarded */						
					if (admissibleSets.add(admiSetCandidate)) { 
						newAdmiSets.add(admiSetCandidate); 
					}
				}
			}					
			/* every newly-found admissible set is a toDoAdmiSet, because it might subsume other admissible sets. */
			toDoAdmiSets = new HashSet<HashSet<String>>(newAdmiSets);
			newAdmiSets.clear();
		}					
		
		for (HashSet<String> nextAdmiSet : admissibleSets) {	
			copiesOfAdmiSets.add(new HashSet<String>(nextAdmiSet)); 
		}		
		return copiesOfAdmiSets;
	}
	
	/**
	 * Returns the defence-sets around {@code arg} in this AF.
	 *
	 * <p> Vreeswijk defined <i>defence-sets</i> for arguments in his 2006 paper -
	 * <br/>
	 * <ul><i>An algorithm to compute minimally grounded and admissible defence sets in argument systems</i></ul> 
	 * <ul><u>Proceedings of COMMA'06: pp.109-20</u>.</ul>
	 * <br/>
	 * Given an AF <i>af</i>, an argument <i>arg</i>, and an argument-set <i>argSet</i>, <i>argSet</i> is 
	 * a defence-set around <i>arg</i> in <i>af</i>, if and only if -
	 * <br/><br/>
	 * <ol>
	 * <li> <i>argSet</i> contains <i>arg</i>; and </li>  
	 * <li> <i>argSet</i> is admissible in <i>af</i>; and </li>  
	 * <li> no strict subset of <i>argSet</i> fulfils (1) and (2). </li>  
	 * </ol>
	 * </p> 
	 *
	 * @param  arg a {@code String}, denoting an argument.
	 * @return a set of {@code String}-sets, denoting the defence-sets around {@code arg} in this AF.
	 */	
	public HashSet<HashSet<String>> getDefenceSetsAround(String arg) {
		
		HashSet<HashSet<String>> copiesOfDefenceSets = new HashSet<HashSet<String>>();
		
		if (!args.contains(arg)) {
		 	return new HashSet<HashSet<String>>();
		} else if (null == argsToDefenceSets.get(arg)) {
			argsToDefenceSets.put(arg, getDefenceSetsAroundHelper(arg, new ArrayList<String>(), 
										new HashSet<HashSet<String>>(Collections.singleton(new HashSet<String>()))));	
		} 
		
		for (HashSet<String> nextSet : argsToDefenceSets.get(arg)) {
			copiesOfDefenceSets.add(new HashSet<String>(nextSet));
		}
		
		return copiesOfDefenceSets;
	}
	
	/**
	 * Finds the defence-sets around {@code arg} in this AF, if called by another method (and passed {@code arg}, the 
	 * empty list and the singleton set containing the empty set). On defence-sets, see
	 * {@link #getDefenceSetsAround(String) getDefenceSetsAround(String)}.
	 *
	 * <p> This method implements a simplified and slightly modified version of Vreeswijk's algorithm for generating 
	 * <i>labelled</i> defence-sets (the label denoting, in each case, whether the defence-set is merely an admissible 
	 * set, or an admissible set which is also a subset of the grounded extension), as described in his 2006 paper -
	 *
	 * <br/>
	 * <ul><i>An algorithm to compute minimally grounded and admissible defence sets in argument systems</i></ul> 
	 * <ul><u>Proceedings of COMMA'06: pp.109-20</u>.</ul>
	 * <br/>
	 *
	 * This method's simplified and slightly modified version generates unlabelled defence-sets. Full details are  
	 * provided <a href="../admissibleSemantics.pdf">here</a>. </p> 
	 *
	 * <p> While an externally-called instance of this method returns the defence-sets around the specified argument, a 
	 * recursively-called instance does not generally do so. Instead, it returns a (perhaps empty) set of argument-sets. 
	 * Each returned argument-set is conflict-free, but is not necessarily a defence-set of the 
	 * argument {@code externInstArg} passed to the externally-called instance. The output of a recursively-called 
	 * instance represents a stage in one branch of a search for {@code externInstArg}'s defence-sets. 
	 * As such, it might include not just (i) defence-sets of {@code externInstArg}, but also (ii) sets which are 
	 * non-admissible (on account of not being acceptable with respect to themselves), and (iii) sets which strictly 
	 * subsume defence-sets of {@code externInstArg}. </p> 
	 *
	 * <p> Following Vreeswijk's usage, the argument-sets passed to and returned by this method are called 
	 * <i>candidate-solutions</i>. A candidate-solution is an argument-set which is 'promising'. The algorithm builds 
	 * each defence-set by addition, proceeding from the empty set. A candidate-solution <i>cs</i> is (loosely speaking) 
	 * such that the algorithm has not yet established that <i>cs</i> is neither (a) a defence-set around the argument 
	 * passed to the externally-called instance, nor (b) a subset of such a defence-set. </p>
	 *
	 * @param arg a {@code String}, denoting an argument.
	 * @param path a list of {@code String}s, denoting arguments. 
	 * @param canSols a set of {@code String}-sets, denoting candidate-solutions.
	 * @return a set of {@code String}-sets, denoting either (a) the defence-sets around {@code arg} in this AF 
	 * (if called by another method) or (b) candidate-solutions (if called recursively).
	 */		
	private HashSet<HashSet<String>> getDefenceSetsAroundHelper(String arg, List<String> path, 
																HashSet<HashSet<String>> canSols) {
		
		HashSet<HashSet<String>> accumulatedCanSols = new HashSet<HashSet<String>>();
		HashSet<HashSet<String>> filteredCanSols = new HashSet<HashSet<String>>();
		HashSet<HashSet<String>> canSolsAttackingNextAttr = new HashSet<HashSet<String>>();
		boolean onPropArg;
		
		HashSet<String> tempSetStr;
		HashSet<HashSet<String>> tempSetSetStr;
		
		/* set onPropArg. onPropArg means that arg is a propArg - i.e. arg is being treated as a potential addition to 
		 every candidate-solution in canSols, and hence as a potential member of at least one defence-set (and hence a
		 'proponent' of the argument passed to the externally-called instance). Otherwise arg is an oppArg - i.e. arg is
		 being treated as an argument which prevents each candidate-solution in canSols from being admissible, by 
		 (i) attacking some argument which is in every candidate-solution in canSols, while (ii) not being attacked by 
		 any of those candidate-solutions. */
		onPropArg = (path.size()%2 == 0);
		
		/* extend path with arg - to ensure that, if this instance recurses, each recursively-called instance correctly 
		 determines whether its argument is a propArg or an oppArg. */
		path.add(arg);

		if (onPropArg) {
			if (getAttackersOf(arg).contains(arg)) {
				/* if arg attacks itself, it cannot be in any admissible set; so as it is a propArg, there can be no 
				 defence-sets this way. So clear canSols. */
				canSols.clear(); 
			} else {
				/* otherwise arg might be in an admissible set, so there might be defence-sets this way. 
				 So create new, augmented versions of all members of canSols. */
				tempSetSetStr = new HashSet<HashSet<String>>();
				for (HashSet<String> nextSet : canSols) {
					tempSetStr = new HashSet<String>(nextSet);
					tempSetStr.add(arg);
					tempSetSetStr.add(tempSetStr);
				}
				canSols = tempSetSetStr;
			}
		}
				
		/* if there might be defence-sets this way and arg is attacked...  */
		if (!canSols.isEmpty() && !getAttackersOf(arg).isEmpty()) {
			/* ...for each attacker nextAttacker... */
			for (String nextAttacker : getAttackersOf(arg)) {
				if (onPropArg) { 
					canSolsAttackingNextAttr.clear(); 
				}	
				
				/* ...find those candidate-solutions in canSols, such that nextAttacker is relevant to them. */
				filteredCanSols.clear();
				for (HashSet<String> nextCanSol : canSols) { 
					if (onPropArg) {
						/* if arg is a propArg, a candidate-solution is relevant, if it DOES NOT attack nextAttacker, 
						 and hence is rendered non-admissible by nextAttacker. However, we need to record those 
						 candidate-solutions which do defend themselves against nextAttacker. So add nextCanSol to 
						 either filteredCanSols or canSolsAttackingNextAttr. */	
						if (Collections.disjoint(getAttackersOf(nextAttacker), nextCanSol)) { 
							/* it is not necessary to use a *copy* of nextCanSol, because the method nowhere changes 
							 any candidate-solution. */
							filteredCanSols.add(nextCanSol); 
						} else {
							canSolsAttackingNextAttr.add(nextCanSol);								
						}
					} else if (!collnIsInConflictWithAnyOf(nextCanSol, nextAttacker)) {
						/* if arg is an oppArg, nextCanSol is relevant, if nextAttacker might 'usefully' defend it 
						 against arg - so nextCanSol is *not* relevant, if it is in conflict with nextAttacker. Even if
						 no such conflict exists, nextAttacker's 'usefulness' as a defender is treated merely as a 
						 *possibility*, because there might be no admissible sets 
						 subsuming ({nextAttacker} U nextCanSol). */
						filteredCanSols.add(nextCanSol); 
					} 
				}
								
				if (onPropArg) { 
					/* if arg is a propArg, attend to those candidate-solutions which do not defend themselves against 
					 nextAttacker - try to expand them into sets which are not deficient in that way (and which subsume 
					 defence-sets of the added arguments, and which are conflict-free), and record such
					 expanded sets in canSols. */
					canSols = getDefenceSetsAroundHelper(nextAttacker, path, filteredCanSols);
					/* reinstate those canSols which were found to defend themselves against nextAttacker. */
					canSols.addAll(canSolsAttackingNextAttr);
					
					if (canSols.isEmpty()) { 
						/* if canSols is empty, there are no defence-sets this way, so there is no need to consider any 
						 further attackers of arg... */
						break; 
					} else {
						/* ...otherwise, remove all non-minimal members of canSols, to ensure that the externally-called
						 instance returns no strict superset of a defence-set. */
						SetComparison.removeNonMinimalMembersOf(canSols);
					}
				} else { 
					/* if arg is an oppArg, attend to every candidate-solution, such that nextAttacker might 'usefully' 
					 defend it against arg - try to expand such candidate-solutions into sets which 
					 (i) are conflict-free, (ii) include nextAttacker, and (iii) subsume defence-sets of nextAttacker 
					 and of all subsequently-added arguments. */
					accumulatedCanSols.addAll(getDefenceSetsAroundHelper(nextAttacker, path, filteredCanSols));
					/* remove all non-minimal members of canSols, to ensure that the externally-called instance returns 
					 no strict superset of a defence-set. */
					SetComparison.removeNonMinimalMembersOf(accumulatedCanSols);
				}
			}
		}
		
		/* remove arg from path - to ensure that, if (i) this instance was recursively-called and (ii) the calling 
		 instance proceeds to call another instance, then the other instance will correctly determine whether its 
		 argument is a propArg or an oppArg. */	
		path.remove(path.size()-1);
		
		if (!onPropArg) {
			canSols = accumulatedCanSols;	
		}
		
		return canSols;
	}
	
	/**
	 * Returns {@code true} if, for each of the specified collections, its set-view is a preferred extension 
	 * of this AF.
	 *
	 * <p> If {@code argColls} are all sets, this method returns the same as
	 * {@code getPreferredExts().containsAll(argColls)}, but without calculating the preferred extensions. </p>
	 *
	 * @param argColls one or more {@code Collection}s of {@code String}s, denoting argument-collections.
	 * @return {@code true} if, for each of the specified collections, its set-view is a preferred extension of this AF.
	 */	
	public boolean preferredExtsContain(Collection<String> ... argColls) {
		
		HashSet<String> candidate;
		
		for (Collection<String> nextArgColl : argColls) {
			if ((null != preferredExts) && !preferredExts.contains(nextArgColl)){ 
				return false; 
			} else if (!admissibleSetsContain(nextArgColl)) { 
				return false; 
			} else {
				/* every preferred extension comprises a union of defence-sets. So check that for every defence-set 
				 defSet0, nextArgColl either subsumes or is in conflict with defSet0. */
				for (String nextArg : args) {
					for (HashSet<String> nextSet : getDefenceSetsAround(nextArg)) {
						candidate = new HashSet<String>(nextArgColl);
						if (candidate.addAll(nextSet) && containsNoConflictAmong(candidate)) { 
							return false; 
						} 
					}
				}			
			}
		}
		
		return true;
	}
	
	/**
	 * Returns this AF's preferred extensions. 
	 *
	 * @return a set of {@code String}-sets, denoting this AF's preferred extensions.
	 */	
	public HashSet<HashSet<String>> getPreferredExts() {
		
		boolean argIsDefended;
		boolean disqualifiedByPrefExts;
		HashSet<String> revisedPrefExtCan;
		HashSet<String> admiArgs = new HashSet<String>();
		HashSet<HashSet<String>> copiesOfPrefExts = new HashSet<HashSet<String>>();
		HashSet<HashSet<String>> pairsInConflict = new HashSet<HashSet<String>>();
		HashSet<HashSet<String>> minimalRemovalSets = new HashSet<HashSet<String>>();
		HashSet<HashSet<String>> prefExtCandidates = new HashSet<HashSet<String>>();
		HashSet<HashSet<String>> revisedPrefExtCandidates = new HashSet<HashSet<String>>();
		
		HashSet<String> tempSetStr;
		HashSet<HashSet<String>> tempSetSetStr;
		
		/* the preferred extensions might already be recorded. */
		if (null != preferredExts) { 
			for (HashSet<String> nextPrefExt : preferredExts) {	
				copiesOfPrefExts.add(new HashSet<String>(nextPrefExt)); 
			}
			return copiesOfPrefExts;
		}
		
		/* ensure that the defence-sets of all arguments are recorded in this object, and record which arguments are 
		 admissible. */
		for (String nextArg : args) {
			if (!getDefenceSetsAround(nextArg).isEmpty()) {
				admiArgs.add(nextArg);	
			}
		}
		
		/* for every admissible argument admiArg0, find the set 
		 argSet = { admiArg1 | there is an admissible set including admiArg0 and admiArg1 }. argSet is such that for 
		 each arg0 in argSet, arg0 is acceptable wrt argSet. Hence argSet is either (a) a preferred extension, or 
		 (b) a (non-conflict-free) superset of at least one preferred extension. */
		for (String nextArg0 : admiArgs) {
			tempSetStr = new HashSet<String>();
			
			for (String nextArg1 : admiArgs) {
				if (containsNoConflictAmong(nextArg0, nextArg1)) {
					seekCommonAdmiSet : {
						for (HashSet<String> nextDefSetOfNextArg0 : argsToDefenceSets.get(nextArg0)) {
							for (HashSet<String> nextDefSetOfNextArg1 : argsToDefenceSets.get(nextArg1)) {
								tempSetSetStr = 
									new HashSet<HashSet<String>>(Arrays.asList(nextDefSetOfNextArg0,
																			   nextDefSetOfNextArg1));
								if (containsNoConflictAmongUnionOf(tempSetSetStr)) {
									tempSetStr.add(nextArg1);
									break seekCommonAdmiSet;
								}
							}
						}
					}
				}
			}			
			prefExtCandidates.add(tempSetStr);
		}
		
		/* identify preferred extensions in preferredExtCandidates. And for ever other set in 
		 preferredExtCandidates, find the preferred extension(s) subsumed by it. */
		for (HashSet<String> nextExtCan : prefExtCandidates) {
			/* find every conflicting pair of arguments in nextExtCan. */
			pairsInConflict.clear();
			
			for (String nextArg : nextExtCan) {
				tempSetStr = getAttackersOf(nextArg);
				tempSetStr.retainAll(nextExtCan);
				
				for (String nextAttacker : tempSetStr) { 
					pairsInConflict.add(new HashSet<String>(Arrays.asList(nextArg, nextAttacker)));
				}
			}
			
			/* if there are no pairs-in-conflict, nextExtCan is a preferred extension; for convenience, record it in 
			 revisedPrefExtCandidates. Doing this facilitates the identification of the preferred extension(s) 
			 subsumed by other members of prefExtCandidates. */
			if (pairsInConflict.isEmpty()) { 
				revisedPrefExtCandidates.add(nextExtCan); 
			} else {			
				/* otherwise, for every preferred extension prefExt1 subsumed by nextExtCan, prefExt1 is such that, 
				 for some *minimal* member argSet1 of the cartesian product of pairsInConflict, (nextExtCan\argSet1)
				 is a (strict or non-strict) superset of prefExt1. */
				minimalRemovalSets.clear();
				for (List<String> nextList : Sets.cartesianProduct(new ArrayList<HashSet<String>>(pairsInConflict))) {
					minimalRemovalSets.add(new HashSet<String>(nextList));
				}
				/* minimalRemovalSets might contain comparable members, because one argument might be in conflict with 
				 multiple arguments. */
				SetComparison.removeNonMinimalMembersOf(minimalRemovalSets);
				
				/* For each member argSet1 of minimalRemovalSets, (nextExtCan\argSet1) is a maximal conflict-free subset 
				 of nextExtCan. Hence argSet1 either is a preferred extension, or it does not adequately defend itself. 
				 So for each argSet1, (i) define revisedPrefExtCan = (nextExtCan\argSet1); then (ii) remove from 
				 revisedPrefExtCan every argument arg1, such that arg1 is not acceptable wrt revisedPrefExtCan. If 
				 revisedPrefExtCan is now admissible, it is a preferred extension. Otherwise, keep repeating (ii), until 
				 revisedPrefExtCan is found to be either (a) a strict subset of a preferred extension which has already 
				 been found; or (b) admissible. If (a), discard revisedPrefExtCan; if (b), revisedPrefExtCan is 
				 *perhaps* a preferred extension.  */
				for (HashSet<String> nextMinimalRemovalSet : minimalRemovalSets) {
					revisedPrefExtCan = new HashSet<String>(nextExtCan);
					revisedPrefExtCan.removeAll(nextMinimalRemovalSet);
					
					disqualifiedByPrefExts = false;
					
					while (!disqualifiedByPrefExts && !admissibleSetsContain(revisedPrefExtCan)) {
						for (Iterator<String> it = revisedPrefExtCan.iterator(); it.hasNext();) {
							/* assume that revisedPrefExtCan subsumes none of the next argument's defence-sets. */
							argIsDefended = false;
							/* check that assumption. */
							for (HashSet<String> nextDefenceSet : argsToDefenceSets.get(it.next())) { 
								if (revisedPrefExtCan.containsAll(nextDefenceSet)) { 
									argIsDefended = true;
									break; 
								} 
							}

							if (!argIsDefended) { 
								it.remove();
							}	
						}
						
						/* revisedPrefExtCan might now be too small. */						
						for (HashSet<String> nextRevCan : revisedPrefExtCandidates) { 
							if (nextRevCan.containsAll(revisedPrefExtCan)) { 
								disqualifiedByPrefExts = true;
								break;
							} 
						}
					}
					
					if (!disqualifiedByPrefExts) {
						revisedPrefExtCandidates.add(revisedPrefExtCan);
					}
				}
			}
			
			/* the above process might place non-preferred admissible sets into revisedPrefExtCandidates - if 
			 (i) nextExtCan was reduced to such a set nonPrefAdmiSet, and (ii) none of the preferred extensions 
			 subsuming nonPrefAdmiSet had yet been found, then nonPrefAdmiSet would be added to 
			 revisedPrefExtCandidates. */
			SetComparison.removeNonMaximalMembersOf(revisedPrefExtCandidates);
		}
		
		/* if revisedPrefExtCandidates is empty, there is just one preferred extension: the empty set. */
		preferredExts = revisedPrefExtCandidates.isEmpty() ? 
			new HashSet<HashSet<String>>(Collections.singleton(new HashSet<String>()))
			: revisedPrefExtCandidates;
		
		for (HashSet<String> nextPrefExt : preferredExts) {	
			copiesOfPrefExts.add(new HashSet<String>(nextPrefExt)); 
		}
				
		return copiesOfPrefExts; 
	}
	
	/**
	 * Returns the extension prescribed by the <i>sceptical</i> preferred semantics for this AF. 
	 *
	 * @return a set of {@code String}s, denoting the intersection of this AF's preferred extensions.
	 */	
	public HashSet<String> getPreferredScepticalExt() {
		
		if (null == preferredScepticalExt) {
			preferredScepticalExt = new HashSet<String>(getPreferredExts().iterator().next());
			for (HashSet<String> nextExt : preferredExts) { 
				preferredScepticalExt.retainAll(nextExt);
			} 
		}
			
		return new HashSet<String>(preferredScepticalExt);	
	}
	
	/**
	 * Returns {@code true} if, for each of the specified collections, its set-view is a complete extension of 
	 * this AF.
	 *
	 * <p> If {@code argColls} are all sets, this method returns the same as
	 * {@code getCompleteExts().containsAll(argColls)}, but without calculating the complete extensions. </p>
	 *
	 * @param argColls one or more {@code Collection}s of {@code String}s, denoting argument-collections.
	 * @return {@code true} if, for each of the specified collections, its set-view is a complete extension of this AF.
	 */	
	public boolean completeExtsContain(Collection<String> ... argColls) {
		
		for (Collection<String> nextArgColl : argColls) {	
			if ((null != completeExts) && !completeExts.contains(nextArgColl)) { 
				return false; 
			} else if (!hasAsConflictFreeSet(nextArgColl)) { 
				return false; 
			} else if (!getArgsAcceptedBy(nextArgColl).equals(nextArgColl)) {
				return false;
			} 
		}
		
		return true;
	}
	
	/**
	 * Returns this AF's complete extensions. 
	 *
	 * @return a set of {@code String}-sets, denoting this AF's complete extensions.
	 */
	public HashSet<HashSet<String>> getCompleteExts() {
		
		HashSet<HashSet<String>> nonPreferredAdmiSets;
		HashSet<HashSet<String>> copiesOfCompleteExts = new HashSet<HashSet<String>>();
		
		/* the complete extensions might already be recorded */
		if (null != completeExts) {				
			for (HashSet<String> nextExt : completeExts) {
				copiesOfCompleteExts.add(new HashSet<String>(nextExt));
			}			
			return copiesOfCompleteExts;
		}	
		
		/* all preferred extensions are also complete extensions. */
		completeExts = getPreferredExts();
		
		nonPreferredAdmiSets = getAdmissibleSets();
		nonPreferredAdmiSets.removeAll(getPreferredExts());
		
		for (HashSet<String> nextAdmiSet : nonPreferredAdmiSets) {				
			if (getArgsAcceptedBy(nextAdmiSet).equals(nextAdmiSet)) {
				completeExts.add(new HashSet<String>(nextAdmiSet));
			}			
		}
		
		for (HashSet<String> nextExt : completeExts) {
			copiesOfCompleteExts.add(new HashSet<String>(nextExt));
		}
		
		return copiesOfCompleteExts;		
	}
	
	/**
	 * Returns {@code true} if, for each of the specified collections, its set-view is a stable extension of 
	 * this AF.
	 *
	 * <p> If {@code argColls} are all sets, this method returns the same as
	 * {@code getStableExts().containsAll(argColls)}, but without calculating the stable extensions. </p>
	 *
	 * @param argColls one or more {@code Collection}s of {@code String}s, denoting argument-collections.
	 * @return {@code true} if, for each of the specified collections, its set-view is a stable extension of this AF.
	 */	
	public boolean stableExtsContain(Collection<String> ... argColls) {
		
		HashSet<String> argsCopy;
		
		for (Collection<String> nextArgColl : argColls) {
			argsCopy = getArgs();
			
			if ((null != stableExts) && !stableExts.contains(nextArgColl)){ 
				return false; 
			} else if (!admissibleSetsContain(nextArgColl)) { 
				return false; 
			} else {
				for (String nextArg : nextArgColl) { 
					argsCopy.removeAll(getTargetsOf(nextArg)); 
				}
				if (!argsCopy.equals(nextArgColl)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Returns the union of this AF's stable extensions.
	 *
	 * @return a set of {@code String}s, denoting the union of this AF's stable extensions.
	 */	
	public HashSet<String> getStableArgs() {
		
		return getExtsUnion("stable");
	}
	
	/**
	 * Returns this AF's stable extensions. 
	 *
	 * @return a set of {@code String}-sets, denoting this AF's stable extensions.
	 */	
	public HashSet<HashSet<String>> getStableExts() {
		
		HashSet<HashSet<String>> copiesOfStableExts = new HashSet<HashSet<String>>();
		HashSet<String> argsNotAttackedByNextExt;
		
		if (null == stableExts) {
			stableExts = new HashSet<HashSet<String>>();
			/* seek stable extensions among the preferred extensions. */
			for (HashSet<String> nextExt : getPreferredExts()) { 				
				argsNotAttackedByNextExt = getArgs();
				for (String nextArg : nextExt) {
					argsNotAttackedByNextExt.removeAll(getTargetsOf(nextArg));						
				}
				if (argsNotAttackedByNextExt.equals(nextExt)) {
					stableExts.add(new HashSet<String>(nextExt));
				}
			}
		}
		
		for (HashSet<String> nextExt : stableExts) { 
			copiesOfStableExts.add(new HashSet<String>(nextExt)); 
		}	
		
		return copiesOfStableExts;
	}
	
	/**
	 * Returns {@code true} if, for each of the specified collections, its set-view is a semi-stable extension of 
	 * this AF.
	 *
	 * <p> If {@code argColls} are all sets, this method returns the same as
	 * {@code getSemiStableExts().containsAll(argColls)}, but without calculating the semi-stable extensions. It does, 
	 * however, calculate the preferred extensions and the stable extensions, if they are not already recorded. </p>
	 *
	 * @param argColls one or more {@code Collection}s of {@code String}s, denoting argument-collections.
	 * @return {@code true} if, for each of the specified collections, its set-view is a semi-stable extension 
	 * of this AF.
	 */	
	public boolean semiStableExtsContain(Collection<String> ... argColls) {
		
		/* Terminology - the 'range' of a preferred extension is the union of itself and all arguments attacked by its 
		 members - see Verheij's 1996 paper 'Two approaches to dialectical argumentation: admissible sets and 
		 argumentation stages' (Proceedings of NAIC'96: pp.357-68) and Caminada's 2006 paper 'Semi-stable semantics' 
		 (Proceedings of COMMA'06: pp.121-30). */
		HashSet<String> nextArgCollRange;
		HashSet<String> nextExtRange; 
		
		for (Collection<String> nextArgColl : argColls) {
			if ((null != semiStableExts) && !semiStableExts.contains(nextArgColl)) { 
				return false; 
			} else if (!getStableExts().isEmpty()) { 
				return stableExts.contains(nextArgColl); 
			} else if (!preferredExtsContain(nextArgColl)) { 
				return false; 
			} else {
				/* compare range of nextArgColl with the range of every other preferred extension. */
				nextArgCollRange = new HashSet<String>();
				for (String nextArg : nextArgColl) {
					nextArgCollRange.add(nextArg);
					nextArgCollRange.addAll(getTargetsOf(nextArg)); 
				}
				
				for (HashSet<String> nextExt : getPreferredExts()) {
					nextExtRange = new HashSet<String>();
					for (String nextArg : nextExt) { 
						nextExtRange.add(nextArg);
						nextExtRange.addAll(getTargetsOf(nextArg)); 
					}
					
					if ((nextExtRange.size() > nextArgCollRange.size()) 
						&& nextExtRange.containsAll(nextArgCollRange)) { 
						return false; 
					}			
				}
			}
		}
		
		return true;	
	}
	
	/**
	 * Returns the union of this AF's semi-stable extensions.
	 *
	 * @return a set of {@code String}s, denoting the union of this AF's semi-stable extensions.
	 */		
	public HashSet<String> getSemiStableArgs() {
		
		return getExtsUnion("semiStable");
	}
	
	/**
	 * Returns this AF's semi-stable extensions. 
	 *
	 * @return a set of {@code String}-sets, denoting this AF's semi-stable extensions.
	 */	
	public HashSet<HashSet<String>> getSemiStableExts() {
			
		HashSet<String> candidateSet;
		HashSet<String> altCandidateSet;
		HashSet<String> candidateSetRange;
		HashSet<String> altCandidateSetRange;
		ArrayList<HashSet<String>> candidateSets;
		
		HashSet<HashSet<String>> copiesOfSemiStableExts = new HashSet<HashSet<String>>();
		
		/* the semi-stable extensions might already be recorded... */
		if (null != semiStableExts) { 
			for (HashSet<String> nextExt : semiStableExts) {	
				copiesOfSemiStableExts.add(new HashSet<String>(nextExt)); 
			}
			return copiesOfSemiStableExts;
		}
		
		semiStableExts = new HashSet<HashSet<String>>();
		
		/* ...or there might be stable extensions... */
		if (!getStableExts().isEmpty()) {
			for (HashSet<String> nextExt : stableExts) { 
				semiStableExts.add(new HashSet<String>(nextExt)); 
			}
		}
		
		/* ...otherwise, seek the semi-stable extensions among the preferred extensions. */
		candidateSets = new ArrayList<HashSet<String>>(getPreferredExts());
		
		/* proceed through candidateSets, comparing their ranges */
		for (int i = 0; i < candidateSets.size(); i++) {
			candidateSet = candidateSets.get(i);
			
			candidateSetRange = new HashSet<String>();
			for (String nextArg : candidateSet) { 
				candidateSetRange.add(nextArg);
				candidateSetRange.addAll(getTargetsOf(nextArg)); 
			}
			
			/* compare candidateSetRange with the ranges of the alternative candidate-sets, breaking if an alternative 
			 candidate-set with greater range is found. */
			for (int j = (i+1); j < candidateSets.size(); j++) {
				altCandidateSet = candidateSets.get(j);
				
				altCandidateSetRange = new HashSet<String>();
				for (String nextArg : altCandidateSet) { 
					altCandidateSetRange.add(nextArg);
					altCandidateSetRange.addAll(getTargetsOf(nextArg)); 
				}
				
				if ((candidateSetRange.size() > altCandidateSetRange.size())
					&& candidateSetRange.containsAll(altCandidateSetRange)) {
					/* altCandidateSet cannot be semi-stable. */
					candidateSets.remove(j);
					j--;
				} else if ((altCandidateSetRange.size() > candidateSetRange.size())
						   && altCandidateSetRange.containsAll(candidateSetRange)) {
					/* candidateSet cannot be semi-stable, so remove it from candidateSets, and break. */
					candidateSets.remove(i);
					i--;
					break;
				}
			}
		}
		
		semiStableExts.addAll(candidateSets);
		
		for (HashSet<String> nextExt : semiStableExts) {
			copiesOfSemiStableExts.add(new HashSet<String>(nextExt));
		}
		
		return copiesOfSemiStableExts;
	}
	
	/**
	 * Returns this AF's ideal extension. 
	 *
	 * @return a set of {@code String}s, denoting this AF's ideal extension.
	 */	
	public HashSet<String> getIdealExt() {
		
		if (null == idealExt) { 
			findIdealExtOrEagerExt("ideal"); 
		}
		
		return new HashSet<String>(idealExt);
	}
		
	/**
	 * Returns this AF's eager extension. 
	 *
	 * @return a set of {@code String}s, denoting this AF's eager extension.
	 */	
	public HashSet<String> getEagerExt() {
		
		if (null == eagerExt) { 
			findIdealExtOrEagerExt("eager"); 
		}
		
		return new HashSet<String>(eagerExt);
	}	
	
	/**
	 * Finds and records this AF's ideal extension or eager extension. 
	 *
	 * @param semantics either "ideal" or "eager", depending on whether the ideal extension or the eager extension 
	 * is required.
	 * @throws IllegalArgumentException if {@code semantics} is neither "ideal" nor "eager".
	 */		
	private void findIdealExtOrEagerExt(String semantics) {
		
		HashSet<String> requiredExt = new HashSet<String>();
		HashSet<HashSet<String>> relevantExts;
		
		/* find the intersection of the relevant extensions. */
		if (semantics.equals("ideal")) {
			relevantExts = getPreferredExts();
		} else if (semantics.equals("eager")) {
			relevantExts = getSemiStableExts();
		} else {
			throw new IllegalArgumentException("parameter 'semantics' is neither \"ideal\" nor \"eager\".");	
		}
		requiredExt.addAll(relevantExts.iterator().next());		
		for (HashSet<String> nextSet : relevantExts) {
			requiredExt.retainAll(nextSet); 
		}
		
		/* remove all members of requiredExt which are not acceptable wrt requiredExt; and do so repeatedly, 
		 until requiredExt is admissible. */
		while (!argsAccept(requiredExt, requiredExt)) {															
			for (Iterator<String> it = requiredExt.iterator(); it.hasNext(); ) {				
				if (!argsAccept(requiredExt, it.next())) {
					it.remove();
				}			
			}
		}
		
		if (semantics.equals("ideal")) {
			idealExt = requiredExt;
		} else {
			eagerExt = requiredExt;
		}
	}	
}
