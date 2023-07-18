// This file is part of JavaSMT,
// an API wrapper for a collection of SMT solvers:
// https://github.com/sosy-lab/java-smt
//
// SPDX-FileCopyrightText: 2023 Dirk Beyer <https://www.sosy-lab.org>
//
// SPDX-License-Identifier: Apache-2.0

package org.sosy_lab.java_smt.solvers.cvc5;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import io.github.cvc5.Kind;
import io.github.cvc5.Term;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.InterpolatingProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;
import org.sosy_lab.java_smt.api.SolverException;

public class CVC5InterpolatingProver extends CVC5AbstractProver<Term>
    implements InterpolatingProverEnvironment<Term> {

  private final FormulaManager mgr;
  // private final CVC5FormulaCreator creator;
  private final Set<Term> assertedFormulaHash = new HashSet<>(); // comparable to SMTInterpols
  // annotatedTerms

  CVC5InterpolatingProver(
      CVC5FormulaCreator pFormulaCreator,
      ShutdownNotifier pShutdownNotifier,
      @SuppressWarnings("unused") int randomSeed,
      Set<ProverOptions> pOptions,
      FormulaManager pMgr) {
    super(pFormulaCreator, pShutdownNotifier, randomSeed, pOptions, pMgr);
    mgr = pMgr;
    // creator = pFormulaCreator;
  }

  @Override
  public void pop() {
    Preconditions.checkState(!closed);
    assertedFormulaHash.removeAll(assertedFormulas.peek());
    super.pop();
  }

  @Override
  public Term addConstraint(BooleanFormula pConstraint) throws InterruptedException {
    Preconditions.checkState(!closed);
    Term t = creator.extractInfo(pConstraint);
    assertedFormulaHash.add(t);

    super.addConstraint(pConstraint);
    return t;
  }

  @Override
  public <R> R allSat(AllSatCallback<R> pCallback, List<BooleanFormula> pImportant)
      throws InterruptedException, SolverException {
    R result = super.allSat(pCallback, pImportant);
    return result;
  }

  @Override
  public BooleanFormula getInterpolant(Collection<Term> pFormulasOfA)
      throws SolverException, InterruptedException {
    Preconditions.checkState(!closed);

    if (pFormulasOfA.isEmpty()) { // Catch trivial case
      return mgr.getBooleanFormulaManager().makeBoolean(true);
    }

    Set<Term> formulasOfA = ImmutableSet.copyOf(pFormulasOfA);
    // formulasOfB := assertedFormulas - formulas
    Set<Term> formulasOfB =
        assertedFormulaHash.stream()
            .filter(n -> !formulasOfA.contains(n))
            .collect(ImmutableSet.toImmutableSet());

    if (formulasOfB.isEmpty()) { // Catch trivial case
      return mgr.getBooleanFormulaManager().makeBoolean(false);
    }

    // fit the Input to work with getCVC5Interpolation

    ArrayList<Collection<Term>> formAAsList = new ArrayList<>();
    ArrayList<Collection<Term>> formBAsList = new ArrayList<>();

    formAAsList.add(formulasOfA);
    formBAsList.add(formulasOfB);

    ArrayList<ArrayList<Collection<Term>>> interpolPair = new ArrayList<>();
    interpolPair.add(formAAsList);
    interpolPair.add(formBAsList);

    Term itp = getCVC5Interpolation(interpolPair);

    BooleanFormula result = creator.encapsulateBoolean(itp);

    return result;
  }

  @Override
  public List<BooleanFormula> getSeqInterpolants(
      List<? extends Collection<Term>> partitionedFormulas)
      throws SolverException, InterruptedException {
    Preconditions.checkArgument(
        !partitionedFormulas.isEmpty(), "at least one partition should be available.");

    final List<BooleanFormula> itps = new ArrayList<>();
    for (int i = 1; i < partitionedFormulas.size(); i++) {
      itps.add(
          getInterpolant(
              ImmutableList.copyOf(Iterables.concat(partitionedFormulas.subList(0, i)))));
    }
    return itps;
  }

  // Experimental!
  @Override
  public List<BooleanFormula> getTreeInterpolants(
      List<? extends Collection<Term>> pPartitionedFormulas, int[] pStartOfSubTree)
      throws SolverException, InterruptedException {
    Preconditions.checkState(!closed);
    assert InterpolatingProverEnvironment.checkTreeStructure(
        pPartitionedFormulas.size(), pStartOfSubTree);

    // Generate every Interpolation Pair
    ArrayList<ArrayList<ArrayList<Collection<Term>>>> interpolationPairs =
        getTreeInterpolationPairs(pPartitionedFormulas, pStartOfSubTree);

    final ArrayList<Term> itps = new ArrayList<>();
    try { // Interpolate every Interpolation Pair
      for (int i = 0; i < interpolationPairs.size(); i++) {
        itps.add(getCVC5Interpolation(interpolationPairs.get(i)));
      }
    } catch (UnsupportedOperationException e) {
      if (e.getMessage() != null) {
        throw new SolverException(e.getMessage(), e);
      } else {
        throw e;
      }
    }

    final List<BooleanFormula> result = new ArrayList<>();
    for (Term itp : itps) {
      result.add(creator.encapsulateBoolean(itp));
    }
    assert result.size() == pStartOfSubTree.length - 1;
    return result;
  }

  /**
   * Interpolates a Tuple of Interpolants according to Craig-Interpolation using CVC5-Interpolation.
   *
   * @param formulaPair Interpolation Pair
   * @return Interpolation of the Interpolation Pair following the definition of
   *     Craig-Interpolation.
   */
  private Term getCVC5Interpolation(ArrayList<ArrayList<Collection<Term>>> formulaPair) {
    assert formulaPair.size() == 2; // Check that formulaPair is a Tuple

    ArrayList<Collection<Term>> assertedInterpols = formulaPair.get(0);
    ArrayList<Collection<Term>> addedInterpols = formulaPair.get(1);

    // Respect Asserted Formulas not in the Interpolation Pairs
    ArrayList<Collection<Term>> combinedInterpols = new ArrayList<>();
    combinedInterpols.addAll(assertedInterpols);
    combinedInterpols.addAll(addedInterpols);

    Collection<Term> extraAssertions = getAssertedTermsNotInCollection(combinedInterpols);
    Term extraAssert = new Term();

    if (extraAssertions.isEmpty()) {
      extraAssert = solver.mkTrue();
    } else {
      extraAssert = buildConjunctionOfFormulas(extraAssertions);
    }

    Term phim = buildConjunctionOfFormulasOverArray(assertedInterpols);
    Term phip = buildConjunctionOfFormulasOverArray(addedInterpols);

    solver.resetAssertions();
    solver.assertFormula(solver.mkTerm(Kind.AND, extraAssert, phim));

    // with Phip negated, CVC5 Interpolation produces an equivalent interpolation to
    // Craig-Interpolation
    Term interpolant = solver.getInterpolant(solver.mkTerm(Kind.NOT, phip));

    // restore Assertion state
    solver.resetAssertions();

    assertedFormulaHash.forEach((n) -> solver.assertFormula(n));

    return interpolant;
  }

  /**
   * Returns Asserted Formulas (from the formula Stack), not present in the given collection of
   * Terms.
   *
   * @param collTerms asserted Formulas to invert the selection
   * @return asserted Formulas not in collTerms, but in the formula Stack
   */
  private Collection<Term> getAssertedTermsNotInCollection(ArrayList<Collection<Term>> collTerms) {
    ArrayList<Term> retTerms = new ArrayList<>();
    collTerms.forEach((n) -> retTerms.addAll(n));
    Set<Term> filteredAssertedTerms =
        assertedFormulaHash.stream()
            .filter(n -> !retTerms.contains(n))
            .collect(ImmutableSet.toImmutableSet());
    return filteredAssertedTerms;
  }

  /**
   * Turns an Array of Collections of Formulas to a Single Conjunction of the Formulas e.g.:
   * [[A,B],[C]] -> A/\B/\C
   *
   * @param formula Array of Collections of formulas
   * @return concatenated Formulas with AND as CVC5 Term
   */
  private Term buildConjunctionOfFormulasOverArray(ArrayList<Collection<Term>> formula) {
    Collection<Term> currColTerm = formula.get(0);
    formula.remove(0);
    if (formula.size() == 0) {
      return buildConjunctionOfFormulas(currColTerm);
    }
    return solver.mkTerm(
        Kind.AND,
        buildConjunctionOfFormulas(currColTerm),
        buildConjunctionOfFormulasOverArray(formula));
  }

  /**
   * Turns a collection of Formulas to a Single Conjunction of the Formulas e.g.: [A,B,C] -> A/\B/\C
   *
   * @param formulas collection of formulas
   * @return concatenated Formulas with AND as CVC5 Term
   */
  private Term buildConjunctionOfFormulas(Collection<Term> formulas) {
    Preconditions.checkState(!closed);
    Preconditions.checkArgument(!formulas.isEmpty());

    Term formula = formulas.iterator().next();

    Collection<Term> removedFormulas =
        formulas.stream().filter(n -> !formula.equals(n)).collect(ImmutableList.toImmutableList());

    if (removedFormulas.size() == 0) {
      return formula;
    }

    return solver.mkTerm(Kind.AND, formula, buildConjunctionOfFormulas(removedFormulas));
  }

  /**
   * Generates Interpolation Pairs for Tree Interpolation. Experimental!
   *
   * @param pPartitionedFormulas of formulas
   * @param pStartOfSubTree The start of the subtree containing the formula at this index as root.
   * @return An Array of Interpolation Pairs (as Tuple) containing Arrays of Collection of Terms
   */
  @SuppressWarnings("unchecked")
  private ArrayList<ArrayList<ArrayList<Collection<Term>>>> getTreeInterpolationPairs(
      List<? extends Collection<Term>> pPartitionedFormulas, int[] pStartOfSubTree) {
    ArrayList<ArrayList<ArrayList<Collection<Term>>>> result = new ArrayList<>();
    // current generated LHS of Tuple
    ArrayList<Collection<Term>> currA = new ArrayList<>();
    // current generated RHS of Tuple
    ArrayList<Collection<Term>> currB = new ArrayList<>(pPartitionedFormulas);
    // current generated Interpolation Tuple
    ArrayList<ArrayList<Collection<Term>>> betweenRes =
        new ArrayList<>();
    ArrayList<Collection<Term>> copyOfFormulas =
        new ArrayList<>(pPartitionedFormulas);
    List<Integer> leafes = new ArrayList<>();

    // First Interpolation Pair
    leafes.add(pStartOfSubTree[0]);
    currA.add(copyOfFormulas.get(0));
    currB.remove(0);
    betweenRes.add((ArrayList<Collection<Term>>) currA.clone());
    betweenRes.add((ArrayList<Collection<Term>>) currB.clone());
    result.add(betweenRes);
    // clear between Storage
    betweenRes = new ArrayList<>();
    // iterate through Tree structure
    for (int i = 1; i < pStartOfSubTree.length - 1; i++) {
      // if the leave does not change, continue like Sequential Interpolation
      if (pStartOfSubTree[i] == pStartOfSubTree[i - 1]) {
        currA.add(copyOfFormulas.get(i));
        currB.remove(0);
        betweenRes.add((ArrayList<Collection<Term>>) currA.clone());
        betweenRes.add((ArrayList<Collection<Term>>) currB.clone());
      } else {
        // if the leave for the node already existed, rebuild the arrays, split at the node
        if (leafes.contains(pStartOfSubTree[i])) {
          currA = new ArrayList<>();
          currB = new ArrayList<>();
          for (int j = 0; j < pStartOfSubTree.length; j++) {
            if (j <= i) {
              currA.add(copyOfFormulas.get(j));
            } else {
              currB.add(copyOfFormulas.get(j));
            }
          }
          betweenRes.add((ArrayList<Collection<Term>>) currA.clone());
          betweenRes.add((ArrayList<Collection<Term>>) currB.clone());
          // rebuild currA from beginning with new node
        } else {
          currB.addAll(currA);
          currA = new ArrayList<>();
          currA.add(copyOfFormulas.get(i));
          currB.remove(0);
          betweenRes.add((ArrayList<Collection<Term>>) currA.clone());
          betweenRes.add((ArrayList<Collection<Term>>) currB.clone());
          leafes.add(pStartOfSubTree[i]);
        }
      }
      result.add(betweenRes);
      betweenRes = new ArrayList<>();
    }
    assert result.size() == pStartOfSubTree.length - 1;
    return result;
  }
}
