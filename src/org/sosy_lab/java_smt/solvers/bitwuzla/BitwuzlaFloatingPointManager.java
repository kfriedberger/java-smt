// This file is part of JavaSMT,
// an API wrapper for a collection of SMT solvers:
// https://github.com/sosy-lab/java-smt
//
// SPDX-FileCopyrightText: 2023 Dirk Beyer <https://www.sosy-lab.org>
//
// SPDX-License-Identifier: Apache-2.0

package org.sosy_lab.java_smt.solvers.bitwuzla;

import java.math.BigDecimal;
import java.util.Random;
import org.sosy_lab.common.rationals.Rational;
import org.sosy_lab.java_smt.api.FloatingPointFormula;
import org.sosy_lab.java_smt.api.FloatingPointRoundingMode;
import org.sosy_lab.java_smt.api.FormulaType;
import org.sosy_lab.java_smt.api.FormulaType.FloatingPointType;
import org.sosy_lab.java_smt.basicimpl.AbstractFloatingPointFormulaManager;
import org.sosy_lab.java_smt.solvers.bitwuzla.api.Kind;
import org.sosy_lab.java_smt.solvers.bitwuzla.api.RoundingMode;
import org.sosy_lab.java_smt.solvers.bitwuzla.api.Sort;
import org.sosy_lab.java_smt.solvers.bitwuzla.api.Term;
import org.sosy_lab.java_smt.solvers.bitwuzla.api.TermManager;

public class BitwuzlaFloatingPointManager
    extends AbstractFloatingPointFormulaManager<Term, Sort, Void, BitwuzlaDeclaration> {
  private final BitwuzlaFormulaCreator bitwuzlaCreator;
  private final TermManager termManager;
  private final Term roundingMode;

  protected BitwuzlaFloatingPointManager(
      BitwuzlaFormulaCreator pCreator, FloatingPointRoundingMode pFloatingPointRoundingMode) {
    super(pCreator);
    bitwuzlaCreator = pCreator;
    termManager = pCreator.getTermManager();
    roundingMode = getRoundingModeImpl(pFloatingPointRoundingMode);
  }

  @Override
  protected Term getDefaultRoundingMode() {
    return roundingMode;
  }

  @Override
  protected Term getRoundingModeImpl(FloatingPointRoundingMode pFloatingPointRoundingMode) {
    Term out;
    switch (pFloatingPointRoundingMode) {
      case NEAREST_TIES_TO_EVEN:
        out = termManager.mk_rm_value(RoundingMode.RNE);
        break;
      case NEAREST_TIES_AWAY:
        out = termManager.mk_rm_value(RoundingMode.RNA);
        break;
      case TOWARD_POSITIVE:
        out = termManager.mk_rm_value(RoundingMode.RTP);
        break;
      case TOWARD_NEGATIVE:
        out = termManager.mk_rm_value(RoundingMode.RTN);
        break;
      case TOWARD_ZERO:
        out = termManager.mk_rm_value(RoundingMode.RTZ);
        break;
      default:
        throw new AssertionError("Unexpected value");
    }
    return out;
  }

  @Override
  public FloatingPointFormula makeNumber(Rational n, FormulaType.FloatingPointType type) {
    BigDecimal num = new BigDecimal(n.getNum());
    BigDecimal den = new BigDecimal(n.getDen());
    return makeNumber(num.divide(den), type);
  }

  @Override
  protected Term makeNumberImpl(double n, FloatingPointType type, Term pFloatingPointRoundingMode) {
    return makeNumberImpl(String.valueOf(n), type, pFloatingPointRoundingMode);
  }

  private Sort mkFpaSort(FloatingPointType pType) {
    return getFormulaCreator().getFloatingPointType(pType);
  }

  @Override
  protected Term makeNumberAndRound(
      String pN, FloatingPointType pType, Term pFloatingPointRoundingMode) {
    // Convert input string to "canonical" format, that is without trailing zeroes, but at least
    // one digit after the dot
    String canonical = pN.replaceAll("(\\.[0-9]+?)0*$", "$1");
    if (!canonical.contains(".")) {
      canonical = canonical + ".0";
    }

    // Handle special cases
    if (canonical.equals("-inf")) {
      return termManager.mk_fp_neg_inf(mkFpaSort(pType));
    }
    if (canonical.equals("-0.0")) {
      return termManager.mk_fp_neg_zero(mkFpaSort(pType));
    }
    if (canonical.equals("nan")) {
      return termManager.mk_fp_nan(mkFpaSort(pType));
    }
    if (canonical.equals("inf")) {
      return termManager.mk_fp_pos_inf(mkFpaSort(pType));
    }

    String decimalString = new BigDecimal(canonical).toPlainString();
    return termManager.mk_fp_value(mkFpaSort(pType), pFloatingPointRoundingMode, decimalString);
  }

  @Override
  protected Term makeVariableImpl(String pVar, FloatingPointType pType) {
    return getFormulaCreator().makeVariable(mkFpaSort(pType), pVar);
  }

  @Override
  protected Term makePlusInfinityImpl(FloatingPointType pType) {
    return termManager.mk_fp_pos_inf(mkFpaSort(pType));
  }

  @Override
  protected Term makeMinusInfinityImpl(FloatingPointType pType) {
    return termManager.mk_fp_neg_inf(mkFpaSort(pType));
  }

  @Override
  protected Term makeNaNImpl(FloatingPointType pType) {
    return termManager.mk_fp_nan(mkFpaSort(pType));
  }

  @Override
  protected Term castToImpl(
      Term pNumber, boolean pSigned, FormulaType<?> pTargetType, Term pRoundingMode) {
    if (pTargetType.isFloatingPointType()) {
      FormulaType.FloatingPointType targetType = (FormulaType.FloatingPointType) pTargetType;
      return termManager.mk_term(
          Kind.FP_TO_FP_FROM_FP,
          pRoundingMode,
          pNumber,
          targetType.getExponentSize(),
          targetType.getMantissaSize() + 1);
    } else if (pTargetType.isBitvectorType()) {
      FormulaType.BitvectorType targetType = (FormulaType.BitvectorType) pTargetType;
      if (pSigned) {
        return termManager.mk_term(Kind.FP_TO_SBV, pRoundingMode, pNumber, targetType.getSize());
      } else {
        return termManager.mk_term(Kind.FP_TO_UBV, pRoundingMode, pNumber, targetType.getSize());
      }
    } else {
      throw new UnsupportedOperationException("Attempted cast to an unsupported type.");
    }
  }

  @Override
  protected Term castFromImpl(
      Term pNumber, boolean pSigned, FloatingPointType pTargetType, Term pRoundingMode) {
    FormulaType<?> formulaType = getFormulaCreator().getFormulaType(pNumber);
    if (formulaType.isFloatingPointType()) {
      return castToImpl(pNumber, pSigned, pTargetType, pRoundingMode);
    } else if (formulaType.isBitvectorType()) {
      if (pSigned) {
        return termManager.mk_term(
            Kind.FP_TO_FP_FROM_SBV,
            roundingMode,
            pNumber,
            pTargetType.getExponentSize(),
            pTargetType.getMantissaSize() + 1);
      } else {
        return termManager.mk_term(
            Kind.FP_TO_FP_FROM_UBV,
            roundingMode,
            pNumber,
            pTargetType.getExponentSize(),
            pTargetType.getMantissaSize() + 1);
      }

    } else {
      throw new UnsupportedOperationException("Attempted cast from an unsupported type.");
    }
  }

  @Override
  protected Term fromIeeeBitvectorImpl(Term pNumber, FloatingPointType pTargetType) {
    return termManager.mk_term(
        Kind.FP_TO_FP_FROM_BV,
        pNumber,
        pTargetType.getExponentSize(),
        pTargetType.getMantissaSize() + 1);
  }

  Random randomGenerator = new Random();

  private String generateRandomName(String prefix, int length) {
    int char_a = Character.valueOf('a');
    int char_z = Character.valueOf('z');

    String generated =
        randomGenerator
            .ints(char_a, char_z + 1)
            .limit(length)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    return prefix + "_" + generated;
  }

  @Override
  protected Term toIeeeBitvectorImpl(Term pNumber) {
    // FIXME: We should use a reserved symbol for the fresh variables
    int sizeExp = pNumber.sort().fp_exp_size();
    int sizeSig = pNumber.sort().fp_sig_size();

    Sort bvSort = termManager.mk_bv_sort(sizeExp + sizeSig);

    // Note that NaN is handled as a special case in this method. This is not strictly necessary,
    // but if we just use "fpTerm = to_fp(bvVar)" the NaN will be given a random payload (and
    // sign). Since NaN payloads are not preserved here anyway we might as well pick a canonical
    // representation.
    Term bvNaN =
        termManager.mk_bv_value(bvSort, "0" + "1".repeat(sizeExp + 1) + "0".repeat(sizeSig - 2));
    Term bvVar = termManager.mk_const(bvSort, generateRandomName("toIeeeBitvector", 10));
    Term equal =
        termManager.mk_term(
            Kind.ITE,
            termManager.mk_term(Kind.FP_IS_NAN, pNumber),
            termManager.mk_term(Kind.EQUAL, bvVar, bvNaN),
            termManager.mk_term(
                Kind.EQUAL,
                termManager.mk_term(Kind.FP_TO_FP_FROM_BV, bvVar, sizeExp, sizeSig),
                pNumber));

    bitwuzlaCreator.addVariableCast(equal);
    return bvVar;
  }

  @Override
  protected Term negate(Term pParam1) {
    return termManager.mk_term(Kind.FP_NEG, pParam1);
  }

  @Override
  protected Term abs(Term pParam1) {
    return termManager.mk_term(Kind.FP_ABS, pParam1);
  }

  @Override
  protected Term max(Term pParam1, Term pParam2) {
    return termManager.mk_term(Kind.FP_MAX, pParam1, pParam2);
  }

  @Override
  protected Term min(Term pParam1, Term pParam2) {
    return termManager.mk_term(Kind.FP_MIN, pParam1, pParam2);
  }

  @Override
  protected Term sqrt(Term pNumber, Term pRoundingMode) {
    return termManager.mk_term(Kind.FP_SQRT, pRoundingMode, pNumber);
  }

  @Override
  protected Term add(Term pParam1, Term pParam2, Term pRoundingMode) {
    return termManager.mk_term(Kind.FP_ADD, pRoundingMode, pParam1, pParam2);
  }

  @Override
  protected Term subtract(Term pParam1, Term pParam2, Term pFloatingPointRoundingMode) {
    return termManager.mk_term(Kind.FP_SUB, pFloatingPointRoundingMode, pParam1, pParam2);
  }

  @Override
  protected Term divide(Term pParam1, Term pParam2, Term pFloatingPointRoundingMode) {
    return termManager.mk_term(Kind.FP_DIV, pFloatingPointRoundingMode, pParam1, pParam2);
  }

  @Override
  protected Term multiply(Term pParam1, Term pParam2, Term pFloatingPointRoundingMode) {
    return termManager.mk_term(Kind.FP_MUL, pFloatingPointRoundingMode, pParam1, pParam2);
  }

  @Override
  protected Term assignment(Term pParam1, Term pParam2) {
    return termManager.mk_term(Kind.EQUAL, pParam1, pParam2);
  }

  @Override
  protected Term equalWithFPSemantics(Term pParam1, Term pParam2) {
    return termManager.mk_term(Kind.FP_EQUAL, pParam1, pParam2);
  }

  @Override
  protected Term greaterThan(Term pParam1, Term pParam2) {
    return termManager.mk_term(Kind.FP_GT, pParam1, pParam2);
  }

  @Override
  protected Term greaterOrEquals(Term pParam1, Term pParam2) {
    return termManager.mk_term(Kind.FP_GEQ, pParam1, pParam2);
  }

  @Override
  protected Term lessThan(Term pParam1, Term pParam2) {
    return termManager.mk_term(Kind.FP_LT, pParam1, pParam2);
  }

  @Override
  protected Term lessOrEquals(Term pParam1, Term pParam2) {
    return termManager.mk_term(Kind.FP_LEQ, pParam1, pParam2);
  }

  @Override
  protected Term isNaN(Term pParam) {
    return termManager.mk_term(Kind.FP_IS_NAN, pParam);
  }

  @Override
  protected Term isInfinity(Term pParam) {
    return termManager.mk_term(Kind.FP_IS_INF, pParam);
  }

  @Override
  protected Term isZero(Term pParam) {
    return termManager.mk_term(Kind.FP_IS_ZERO, pParam);
  }

  @Override
  protected Term isSubnormal(Term pParam) {
    return termManager.mk_term(Kind.FP_IS_SUBNORMAL, pParam);
  }

  @Override
  protected Term isNormal(Term pParam) {
    return termManager.mk_term(Kind.FP_IS_NORMAL, pParam);
  }

  @Override
  protected Term isNegative(Term pParam) {
    return termManager.mk_term(Kind.FP_IS_NEG, pParam);
  }

  @Override
  protected Term round(Term pFormula, FloatingPointRoundingMode pRoundingMode) {
    Term rm = getRoundingModeImpl(pRoundingMode);
    return termManager.mk_term(Kind.FP_RTI, rm, pFormula);
  }
}
