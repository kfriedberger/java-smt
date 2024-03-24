// This file is part of JavaSMT,
// an API wrapper for a collection of SMT solvers:
// https://github.com/sosy-lab/java-smt
//
// SPDX-FileCopyrightText: 2020 Dirk Beyer <https://www.sosy-lab.org>
//
// SPDX-License-Identifier: Apache-2.0

package org.sosy_lab.java_smt.solvers.mathsat5;

import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_and;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_and;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_ashr;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_concat;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_extract;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_lshl;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_lshr;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_minus;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_neg;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_not;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_number;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_or;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_plus;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_rol;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_ror;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_sdiv;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_sext;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_sleq;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_slt;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_srem;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_times;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_udiv;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_uleq;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_ult;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_urem;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_xor;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_bv_zext;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_equal;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_int_from_sbv;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_int_from_ubv;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_int_to_bv;
import static org.sosy_lab.java_smt.solvers.mathsat5.Mathsat5NativeApi.msat_make_term_ite;

import java.math.BigInteger;
import java.util.function.Function;
import org.sosy_lab.java_smt.api.FormulaType.BitvectorType;
import org.sosy_lab.java_smt.basicimpl.AbstractBitvectorFormulaManager;

/** Mathsat Bitvector Theory, build out of Bitvector*Operations. */
class Mathsat5BitvectorFormulaManager
    extends AbstractBitvectorFormulaManager<Long, Long, Long, Long> {

  private final long mathsatEnv;

  protected Mathsat5BitvectorFormulaManager(
      Mathsat5FormulaCreator pCreator, Mathsat5BooleanFormulaManager pBmgr) {
    super(pCreator, pBmgr);
    this.mathsatEnv = pCreator.getEnv();
  }

  @Override
  public Long concat(Long pFirst, Long pSecond) {
    return msat_make_bv_concat(mathsatEnv, pFirst, pSecond);
  }

  @Override
  public Long extract(Long pFirst, int pMsb, int pLsb) {
    return msat_make_bv_extract(mathsatEnv, pMsb, pLsb, pFirst);
  }

  @Override
  public Long extend(Long pNumber, int pExtensionBits, boolean pSigned) {
    if (pSigned) {
      return msat_make_bv_sext(mathsatEnv, pExtensionBits, pNumber);
    } else {
      return msat_make_bv_zext(mathsatEnv, pExtensionBits, pNumber);
    }
  }

  @Override
  public Long makeBitvectorImpl(int pLength, long pI) {
    int i = (int) pI;
    if (i == pI && i > 0) { // fits into an int
      return Mathsat5NativeApi.msat_make_bv_int_number(mathsatEnv, i, pLength);
    }
    return makeBitvectorImpl(pLength, BigInteger.valueOf(pI));
  }

  @Override
  public Long makeBitvectorImpl(int pLength, BigInteger pI) {
    pI = transformValueToRange(pLength, pI);
    return msat_make_bv_number(mathsatEnv, pI.toString(), pLength, 10);
  }

  @Override
  protected Long makeBitvectorImpl(int pLength, Long pIntegerFormula) {
    return msat_make_int_to_bv(mathsatEnv, pLength, pIntegerFormula);
  }

  @Override
  protected Long toIntegerFormulaImpl(Long pBVFormula, boolean pSigned) {
    if (pSigned) {
      return msat_make_int_from_sbv(mathsatEnv, pBVFormula);
    } else {
      return msat_make_int_from_ubv(mathsatEnv, pBVFormula);
    }
  }

  @Override
  public Long makeVariableImpl(int length, String var) {
    long bvType = getFormulaCreator().getBitvectorType(length);
    return getFormulaCreator().makeVariable(bvType, var);
  }

  /**
   * Return a term representing the (arithmetic if signed is true) right shift of number by toShift.
   */
  @Override
  public Long shiftRight(Long number, Long toShift, boolean signed) {
    long t;
    if (signed) {
      t = msat_make_bv_ashr(mathsatEnv, number, toShift);
    } else {
      t = msat_make_bv_lshr(mathsatEnv, number, toShift);
    }
    return t;
  }

  @Override
  public Long shiftLeft(Long number, Long toShift) {
    return msat_make_bv_lshl(mathsatEnv, number, toShift);
  }

  @Override
  public Long rotateLeftByConstant(Long number, int toRotate) {
    return msat_make_bv_rol(mathsatEnv, toRotate, number);
  }

  @Override
  public Long rotateRightByConstant(Long number, int toRotate) {
    return msat_make_bv_ror(mathsatEnv, toRotate, number);
  }

  @Override
  public Long not(Long pBits) {
    return msat_make_bv_not(mathsatEnv, pBits);
  }

  @Override
  public Long and(Long pBits1, Long pBits2) {
    return msat_make_bv_and(mathsatEnv, pBits1, pBits2);
  }

  @Override
  public Long or(Long pBits1, Long pBits2) {
    return msat_make_bv_or(mathsatEnv, pBits1, pBits2);
  }

  @Override
  public Long xor(Long pBits1, Long pBits2) {
    return msat_make_bv_xor(mathsatEnv, pBits1, pBits2);
  }

  @Override
  public Long negate(Long pNumber) {
    return msat_make_bv_neg(mathsatEnv, pNumber);
  }

  @Override
  public Long add(Long pNumber1, Long pNumber2) {
    return msat_make_bv_plus(mathsatEnv, pNumber1, pNumber2);
  }

  @Override
  public Long subtract(Long pNumber1, Long pNumber2) {
    return msat_make_bv_minus(mathsatEnv, pNumber1, pNumber2);
  }

  @Override
  public Long divide(Long pNumber1, Long pNumber2, boolean signed) {
    if (signed) {
      return msat_make_bv_sdiv(mathsatEnv, pNumber1, pNumber2);
    } else {
      return msat_make_bv_udiv(mathsatEnv, pNumber1, pNumber2);
    }
  }

  @Override
  public Long remainder(Long pNumber1, Long pNumber2, boolean signed) {
    if (signed) {
      return msat_make_bv_srem(mathsatEnv, pNumber1, pNumber2);
    } else {
      return msat_make_bv_urem(mathsatEnv, pNumber1, pNumber2);
    }
  }

  /**
   * Because mathsat does not define an smod operator, we emulate it using the definition (bvsmod s
   * t) abbreviates (let ((?msb_s ((_ extract |m-1| |m-1|) s)) (?msb_t ((_ extract |m-1| |m-1|) t)))
   * (let ((abs_s (ite (= ?msb_s #b0) s (bvneg s))) (abs_t (ite (= ?msb_t #b0) t (bvneg t)))) (let
   * ((u (bvurem abs_s abs_t))) (ite (= u (_ bv0 m)) u (ite (and (= ?msb_s #b0) (= ?msb_t #b0)) u
   * (ite (and (= ?msb_s #b1) (= ?msb_t #b0)) (bvadd (bvneg u) t) (ite (and (= ?msb_s #b0) (= ?msb_t
   * #b1)) (bvadd u t) (bvneg u))))))))
   */
  @Override
  public Long smodulo(Long s, Long t) {
    final int size = ((BitvectorType) formulaCreator.getFormulaType(s)).getSize();
    final Long zero = msat_make_bv_number(mathsatEnv, "0", size, 10);

    final Function<Long, Long> isNegative = pLong -> msat_make_bv_slt(mathsatEnv, pLong, zero);
    final Function<Long, Long> isPositive = pLong -> msat_make_bv_slt(mathsatEnv, zero, pLong);
    final Function<Long, Long> abs =
        pLong ->
            msat_make_term_ite(
                mathsatEnv, isPositive.apply(pLong), pLong, msat_make_bv_neg(mathsatEnv, pLong));
    final Long absS = abs.apply(s);
    final Long absT = abs.apply(t);
    final Long u = msat_make_bv_urem(mathsatEnv, absS, absT);

    final Long sPositive = isPositive.apply(s);
    final Long tPositive = isPositive.apply(t);
    final Long sNegative = isNegative.apply(s);
    final Long tNegative = isNegative.apply(t);

    final Long pp = msat_make_and(mathsatEnv, sPositive, tPositive);
    final Long pn = msat_make_and(mathsatEnv, sPositive, tNegative);
    final Long np = msat_make_and(mathsatEnv, sNegative, tPositive);

    final Long npTerm = msat_make_bv_plus(mathsatEnv, msat_make_bv_neg(mathsatEnv, u), t);
    final Long pnTerm = msat_make_bv_plus(mathsatEnv, u, t);
    final Long nnTerm = msat_make_bv_neg(mathsatEnv, u);

    return msat_make_term_ite(
        mathsatEnv,
        msat_make_equal(mathsatEnv, u, zero),
        u,
        msat_make_term_ite(
            mathsatEnv,
            pp,
            u,
            msat_make_term_ite(
                mathsatEnv, np, npTerm, msat_make_term_ite(mathsatEnv, pn, pnTerm, nnTerm))));
  }

  @Override
  public Long multiply(Long pNumber1, Long pNumber2) {
    return msat_make_bv_times(mathsatEnv, pNumber1, pNumber2);
  }

  @Override
  public Long equal(Long pNumber1, Long pNumber2) {
    return msat_make_equal(mathsatEnv, pNumber1, pNumber2);
  }

  @Override
  public Long lessThan(Long pNumber1, Long pNumber2, boolean signed) {
    if (signed) {
      return msat_make_bv_slt(mathsatEnv, pNumber1, pNumber2);
    } else {
      return msat_make_bv_ult(mathsatEnv, pNumber1, pNumber2);
    }
  }

  @Override
  public Long lessOrEquals(Long pNumber1, Long pNumber2, boolean signed) {
    if (signed) {
      return msat_make_bv_sleq(mathsatEnv, pNumber1, pNumber2);
    } else {
      return msat_make_bv_uleq(mathsatEnv, pNumber1, pNumber2);
    }
  }

  @Override
  public Long greaterThan(Long pNumber1, Long pNumber2, boolean signed) {
    return lessThan(pNumber2, pNumber1, signed);
  }

  @Override
  public Long greaterOrEquals(Long pNumber1, Long pNumber2, boolean signed) {
    return lessOrEquals(pNumber2, pNumber1, signed);
  }
}
