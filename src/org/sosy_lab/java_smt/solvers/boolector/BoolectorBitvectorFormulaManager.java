/*
 *  JavaSMT is an API wrapper for a collection of SMT solvers.
 *  This file is part of JavaSMT.
 *
 *  Copyright (C) 2007-2019  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.sosy_lab.java_smt.solvers.boolector;

import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_add;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_and;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_concat;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_eq;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_mul;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_neg;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_not;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_or;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_sdiv;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_sext;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_sgt;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_sgte;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_sll;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_slt;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_slte;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_smod;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_sra;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_srl;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_sub;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_udiv;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_uext;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_ugt;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_ugte;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_ult;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_ulte;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_urem;
import static org.sosy_lab.java_smt.solvers.boolector.BtorJNI.boolector_xor;

import java.math.BigInteger;
import org.sosy_lab.java_smt.basicimpl.AbstractBitvectorFormulaManager;

class BoolectorBitvectorFormulaManager
    extends AbstractBitvectorFormulaManager<Long, Long, BoolectorEnvironment, Long> {

  private final long btor;

  public BoolectorBitvectorFormulaManager(BoolectorFormulaCreator pCreator) {
    super(pCreator);
    this.btor = pCreator.getEnv().getBtor();
  }

  @Override
  public Long makeBitvectorImpl(int pLength, long pParam1) {
    checkRange(pLength, BigInteger.valueOf(pParam1));
    int i = (int) pParam1;
    if (i == pParam1 && i > 0) {
      long sort = BtorJNI.boolector_bitvec_sort(btor, pLength);
      return BtorJNI.boolector_int(btor, i, sort);
    }
    return makeBitvectorImpl(pLength, BigInteger.valueOf(pParam1));
  }

  @Override
  public Long toIntegerFormulaImpl(Long pI, boolean pSigned) {
    throw new UnsupportedOperationException("Operation not supported.");
  }

  @Override
  public Long negate(Long bitVec) {
    return boolector_neg(btor, bitVec);
  }

  @Override
  public Long add(Long bitVec1, Long bitVec2) {
    return boolector_add(btor, bitVec1, bitVec2);
  }

  @Override
  public Long subtract(Long bitVec1, Long bitVec2) {
    return boolector_sub(btor, bitVec1, bitVec2);
  }

  @Override
  public Long divide(Long bitVec1, Long bitVec2, boolean signed) {
    if (signed) {
      return boolector_sdiv(btor, bitVec1, bitVec2);
    } else {
      return boolector_udiv(btor, bitVec1, bitVec2);
    }
  }

  @Override
  public Long modulo(Long bitVec1, Long bitVec2, boolean signed) {
    if (signed) {
      return boolector_smod(btor, bitVec1, bitVec2);
    } else {
      return boolector_urem(btor, bitVec1, bitVec2);
    }
  }

  @Override
  public Long multiply(Long bitVec1, Long bitVec2) {
    return boolector_mul(btor, bitVec1, bitVec2);
  }

  @Override
  public Long equal(Long bitVec1, Long bitVec2) {
    return boolector_eq(btor, bitVec1, bitVec2);
  }

  @Override
  public Long greaterThan(Long bitVec1, Long bitVec2, boolean signed) {
    if (signed) {
      return boolector_sgt(btor, bitVec1, bitVec2);
    } else {
      return boolector_ugt(btor, bitVec1, bitVec2);
    }
  }

  @Override
  public Long greaterOrEquals(Long bitVec1, Long bitVec2, boolean signed) {
    if (signed) {
      return boolector_sgte(btor, bitVec1, bitVec2);
    } else {
      return boolector_ugte(btor, bitVec1, bitVec2);
    }
  }

  @Override
  public Long lessThan(Long bitVec1, Long bitVec2, boolean signed) {
    if (signed) {
      return boolector_slt(btor, bitVec1, bitVec2);
    } else {
      return boolector_ult(btor, bitVec1, bitVec2);
    }
  }

  @Override
  public Long lessOrEquals(Long bitVec1, Long bitVec2, boolean signed) {
    if (signed) {
      return boolector_slte(btor, bitVec1, bitVec2);
    } else {
      return boolector_ulte(btor, bitVec1, bitVec2);
    }
  }

  @Override
  public Long not(Long bitVec) {
    return boolector_not(btor, bitVec);
  }

  @Override
  public Long and(Long bitVec1, Long bitVec2) {
    return boolector_and(btor, bitVec1, bitVec2);
  }

  @Override
  public Long or(Long bitVec1, Long bitVec2) {
    return boolector_or(btor, bitVec1, bitVec2);
  }

  @Override
  public Long xor(Long bitVec1, Long bitVec2) {
    return boolector_xor(btor, bitVec1, bitVec2);
  }

  @Override
  public Long makeBitvectorImpl(int pLength, BigInteger pI) {
    checkRange(pLength, pI);
    long sort = BtorJNI.boolector_bitvec_sort(btor, pLength);
    return BtorJNI.boolector_constd(btor, sort, pI.toString());
  }

  @Override
  public Long makeVariableImpl(int pLength, String pVar) {
    if (pLength < pVar.length()) {
      throw new IllegalArgumentException(
          pVar + " is to small for a bitvector with length " + pLength);
    }
    long sort = BtorJNI.boolector_bitvec_sort(btor, pLength);
    return BtorJNI.boolector_var(btor, sort, pVar);
  }

  @Override
  public Long shiftRight(Long bitVec, Long toShift, boolean signed) {
    if(signed) {
      return boolector_sra(btor, bitVec, toShift);
    } else {
      return boolector_srl(btor, bitVec, toShift);
    }
  }

  @Override
  public Long shiftLeft(Long bitVec, Long toShift) {
    return boolector_sll(btor, bitVec, toShift);
  }

  @Override
  public Long concat(Long bitVec, Long bitVecAppend) {
    return boolector_concat(btor, bitVec, bitVecAppend);
  }

  @Override
  public Long extract(Long pNode, int pMsb, int pLsb, boolean pSigned) {
    return BtorJNI.boolector_slice(btor, pNode, pMsb, pLsb);
  }

  @Override
  public Long extend(Long bitVec, int extensionBits, boolean signed) {
    if (signed) {
      return boolector_sext(btor, bitVec, extensionBits);
    } else {
      return boolector_uext(btor, bitVec, extensionBits);
    }
  }

  /**
   * Taken from Z3BitvectorFormulaManager
   */
  private static void checkRange(int pLength, BigInteger pI) {
    if (pI.signum() > 0) {
      BigInteger max = BigInteger.ONE.shiftLeft(pLength);
      if (pI.compareTo(max) >= 0) {
        throw new IllegalArgumentException(
            pI + " is to big for a bitvector with length " + pLength);
      }
    } else if (pI.signum() < 0) {
      BigInteger min = BigInteger.ONE.shiftLeft(pLength).negate();
      if (pI.compareTo(min) <= 0) {
        throw new IllegalArgumentException(
            pI + " is to small for a bitvector with length " + pLength);
      }
    }
  }

  @Override
  protected Long makeBitvectorImpl(int pLength, Long pParam1) {
    checkRange(pLength, BigInteger.valueOf(pParam1));
    int i = (int) pParam1.doubleValue();
    if (i == pParam1 && i > 0) {
      long sort = BtorJNI.boolector_bitvec_sort(btor, pLength);
      return BtorJNI.boolector_int(btor, i, sort);
    }
    return makeBitvectorImpl(pLength, BigInteger.valueOf(pParam1));
  }

}
