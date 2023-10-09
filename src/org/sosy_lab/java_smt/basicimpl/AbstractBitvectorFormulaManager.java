// This file is part of JavaSMT,
// an API wrapper for a collection of SMT solvers:
// https://github.com/sosy-lab/java-smt
//
// SPDX-FileCopyrightText: 2020 Dirk Beyer <https://www.sosy-lab.org>
//
// SPDX-License-Identifier: Apache-2.0

package org.sosy_lab.java_smt.basicimpl;

import static com.google.common.base.Preconditions.checkArgument;
import static org.sosy_lab.java_smt.basicimpl.AbstractFormulaManager.checkVariableName;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.sosy_lab.java_smt.api.BitvectorFormula;
import org.sosy_lab.java_smt.api.BitvectorFormulaManager;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.FormulaType;
import org.sosy_lab.java_smt.api.FormulaType.BitvectorType;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.utils.Generator;

@SuppressWarnings("ClassTypeParameterName")
public abstract class AbstractBitvectorFormulaManager<TFormulaInfo, TType, TEnv, TFuncDecl>
    extends AbstractBaseFormulaManager<TFormulaInfo, TType, TEnv, TFuncDecl>
    implements BitvectorFormulaManager {

  private final AbstractBooleanFormulaManager<TFormulaInfo, TType, TEnv, TFuncDecl> bmgr;

  protected AbstractBitvectorFormulaManager(
      FormulaCreator<TFormulaInfo, TType, TEnv, TFuncDecl> pCreator,
      AbstractBooleanFormulaManager<TFormulaInfo, TType, TEnv, TFuncDecl> pBmgr) {
    super(pCreator);
    bmgr = pBmgr;
  }

  private BitvectorFormula wrap(TFormulaInfo pTerm) {
    return getFormulaCreator().encapsulateBitvector(pTerm);
  }

  private void checkSameSize(
      BitvectorFormula pNumber1, BitvectorFormula pNumber2, String operation) {
    final int len1 = getLength(pNumber1);
    final int len2 = getLength(pNumber2);
    checkArgument(
        len1 == len2,
        "Can't %s bitvectors with different sizes (%s and %s)",
        operation,
        len1,
        len2);
  }

  @Override
  public BitvectorFormula makeBitvector(int length, IntegerFormula pI) {
    TFormulaInfo param1 = extractInfo(pI);
    BitvectorFormula result = wrap(makeBitvectorImpl(length, param1));
    Generator.logMakeBitVector(result, length, pI);
    return result;
  }

  protected abstract TFormulaInfo makeBitvectorImpl(int length, TFormulaInfo pParam1);

  @Override
  public IntegerFormula toIntegerFormula(BitvectorFormula pI, boolean signed) {
    TFormulaInfo param1 = extractInfo(pI);
    return getFormulaCreator()
        .encapsulate(FormulaType.IntegerType, toIntegerFormulaImpl(param1, signed));
  }

  protected abstract TFormulaInfo toIntegerFormulaImpl(TFormulaInfo pI, boolean signed);

  @Override
  public BitvectorFormula negate(BitvectorFormula pNumber) {
    TFormulaInfo param1 = extractInfo(pNumber);
    BitvectorFormula result = wrap(negate(param1));
    Generator.logBVNegate(result, pNumber);
    return result;
  }

  protected abstract TFormulaInfo negate(TFormulaInfo pParam1);

  @Override
  public BitvectorFormula add(BitvectorFormula pNumber1, BitvectorFormula pNumber2) {
    checkSameSize(pNumber1, pNumber2, "add");
    TFormulaInfo param1 = extractInfo(pNumber1);
    TFormulaInfo param2 = extractInfo(pNumber2);

    return wrap(add(param1, param2));
  }

  protected abstract TFormulaInfo add(TFormulaInfo pParam1, TFormulaInfo pParam2);

  @Override
  public BitvectorFormula subtract(BitvectorFormula pNumber1, BitvectorFormula pNumber2) {
    checkSameSize(pNumber1, pNumber2, "subtract");
    TFormulaInfo param1 = extractInfo(pNumber1);
    TFormulaInfo param2 = extractInfo(pNumber2);

    return wrap(subtract(param1, param2));
  }

  protected abstract TFormulaInfo subtract(TFormulaInfo pParam1, TFormulaInfo pParam2);

  @Override
  public BitvectorFormula divide(
      BitvectorFormula pNumber1, BitvectorFormula pNumber2, boolean signed) {
    checkSameSize(pNumber1, pNumber2, "divide");
    TFormulaInfo param1 = extractInfo(pNumber1);
    TFormulaInfo param2 = extractInfo(pNumber2);

    return wrap(divide(param1, param2, signed));
  }

  protected abstract TFormulaInfo divide(
      TFormulaInfo pParam1, TFormulaInfo pParam2, boolean signed);

  @Override
  public BitvectorFormula modulo(
      BitvectorFormula pNumber1, BitvectorFormula pNumber2, boolean signed) {
    checkSameSize(pNumber1, pNumber2, "modulo");
    TFormulaInfo param1 = extractInfo(pNumber1);
    TFormulaInfo param2 = extractInfo(pNumber2);

    return wrap(modulo(param1, param2, signed));
  }

  protected abstract TFormulaInfo modulo(
      TFormulaInfo pParam1, TFormulaInfo pParam2, boolean signed);

  @Override
  public BitvectorFormula multiply(BitvectorFormula pNumber1, BitvectorFormula pNumber2) {
    checkSameSize(pNumber1, pNumber2, "modulo");
    TFormulaInfo param1 = extractInfo(pNumber1);
    TFormulaInfo param2 = extractInfo(pNumber2);

    return wrap(multiply(param1, param2));
  }

  protected abstract TFormulaInfo multiply(TFormulaInfo pParam1, TFormulaInfo pParam2);

  @Override
  public BooleanFormula equal(BitvectorFormula pNumber1, BitvectorFormula pNumber2) {
    checkSameSize(pNumber1, pNumber2, "compare");
    TFormulaInfo param1 = extractInfo(pNumber1);
    TFormulaInfo param2 = extractInfo(pNumber2);
    BooleanFormula result = wrapBool(equal(param1, param2));
    Generator.logBVEqual(result, pNumber1, pNumber2);
    return result;
  }

  protected abstract TFormulaInfo equal(TFormulaInfo pParam1, TFormulaInfo pParam2);

  @Override
  public BooleanFormula greaterThan(
      BitvectorFormula pNumber1, BitvectorFormula pNumber2, boolean signed) {
    checkSameSize(pNumber1, pNumber2, "compare");
    TFormulaInfo param1 = extractInfo(pNumber1);
    TFormulaInfo param2 = extractInfo(pNumber2);

    return wrapBool(greaterThan(param1, param2, signed));
  }

  protected abstract TFormulaInfo greaterThan(
      TFormulaInfo pParam1, TFormulaInfo pParam2, boolean signed);

  @Override
  public BooleanFormula greaterOrEquals(
      BitvectorFormula pNumber1, BitvectorFormula pNumber2, boolean signed) {
    checkSameSize(pNumber1, pNumber2, "compare");
    TFormulaInfo param1 = extractInfo(pNumber1);
    TFormulaInfo param2 = extractInfo(pNumber2);

    return wrapBool(greaterOrEquals(param1, param2, signed));
  }

  protected abstract TFormulaInfo greaterOrEquals(
      TFormulaInfo pParam1, TFormulaInfo pParam2, boolean signed);

  @Override
  public BooleanFormula lessThan(
      BitvectorFormula pNumber1, BitvectorFormula pNumber2, boolean signed) {
    checkSameSize(pNumber1, pNumber2, "compare");
    TFormulaInfo param1 = extractInfo(pNumber1);
    TFormulaInfo param2 = extractInfo(pNumber2);

    return wrapBool(lessThan(param1, param2, signed));
  }

  protected abstract TFormulaInfo lessThan(
      TFormulaInfo pParam1, TFormulaInfo pParam2, boolean signed);

  @Override
  public BooleanFormula lessOrEquals(
      BitvectorFormula pNumber1, BitvectorFormula pNumber2, boolean signed) {
    checkSameSize(pNumber1, pNumber2, "compare");
    TFormulaInfo param1 = extractInfo(pNumber1);
    TFormulaInfo param2 = extractInfo(pNumber2);

    return wrapBool(lessOrEquals(param1, param2, signed));
  }

  protected abstract TFormulaInfo lessOrEquals(
      TFormulaInfo pParam1, TFormulaInfo pParam2, boolean signed);

  @Override
  public BitvectorFormula not(BitvectorFormula pBits) {
    TFormulaInfo param1 = extractInfo(pBits);
    return wrap(not(param1));
  }

  protected abstract TFormulaInfo not(TFormulaInfo pParam1);

  @Override
  public BitvectorFormula and(BitvectorFormula pBits1, BitvectorFormula pBits2) {
    checkSameSize(pBits1, pBits2, "combine");
    TFormulaInfo param1 = extractInfo(pBits1);
    TFormulaInfo param2 = extractInfo(pBits2);

    return wrap(and(param1, param2));
  }

  protected abstract TFormulaInfo and(TFormulaInfo pParam1, TFormulaInfo pParam2);

  @Override
  public BitvectorFormula or(BitvectorFormula pBits1, BitvectorFormula pBits2) {
    checkSameSize(pBits1, pBits2, "combine");
    TFormulaInfo param1 = extractInfo(pBits1);
    TFormulaInfo param2 = extractInfo(pBits2);

    return wrap(or(param1, param2));
  }

  protected abstract TFormulaInfo or(TFormulaInfo pParam1, TFormulaInfo pParam2);

  @Override
  public BitvectorFormula xor(BitvectorFormula pBits1, BitvectorFormula pBits2) {
    checkSameSize(pBits1, pBits2, "combine");
    TFormulaInfo param1 = extractInfo(pBits1);
    TFormulaInfo param2 = extractInfo(pBits2);

    return wrap(xor(param1, param2));
  }

  protected abstract TFormulaInfo xor(TFormulaInfo pParam1, TFormulaInfo pParam2);

  @Override
  public BitvectorFormula makeBitvector(int pLength, long i) {
    BitvectorFormula result = wrap(makeBitvectorImpl(pLength, i));
    Generator.logMakeBitVector(result, pLength, i);
    return result;
  }

  protected TFormulaInfo makeBitvectorImpl(int pLength, long pI) {
    return makeBitvectorImpl(pLength, BigInteger.valueOf(pI));
  }

  @Override
  public BitvectorFormula makeBitvector(int pLength, BigInteger i) {
    return wrap(makeBitvectorImpl(pLength, i));
  }

  protected abstract TFormulaInfo makeBitvectorImpl(int pLength, BigInteger pI);

  /**
   * transform a negative value into its positive counterpart.
   *
   * @throws IllegalArgumentException if the value is out of range for the given size.
   */
  protected final BigInteger transformValueToRange(int pLength, BigInteger pI) {
    final BigInteger max = BigInteger.valueOf(2).pow(pLength);
    if (pI.signum() < 0) {
      BigInteger min = BigInteger.valueOf(2).pow(pLength - 1).negate();
      Preconditions.checkArgument(
          pI.compareTo(min) >= 0, "%s is to small for a bitvector with length %s", pI, pLength);
      pI = pI.add(max);
    } else {
      Preconditions.checkArgument(
          pI.compareTo(max) < 0, "%s is to large for a bitvector with length %s", pI, pLength);
    }
    return pI;
  }

  @Override
  public BitvectorFormula makeVariable(BitvectorType type, String pVar) {
    return makeVariable(type.getSize(), pVar);
  }

  @Override
  public BitvectorFormula makeVariable(int pLength, String pVar) {
    checkVariableName(pVar);
    return wrap(makeVariableImpl(pLength, pVar));
  }

  protected abstract TFormulaInfo makeVariableImpl(int pLength, String pVar);

  /**
   * Return a term representing the (arithmetic if signed is true) right shift of number by toShift.
   */
  @Override
  public BitvectorFormula shiftRight(
      BitvectorFormula pNumber, BitvectorFormula toShift, boolean signed) {
    TFormulaInfo param1 = extractInfo(pNumber);
    TFormulaInfo param2 = extractInfo(toShift);

    return wrap(shiftRight(param1, param2, signed));
  }

  protected abstract TFormulaInfo shiftRight(
      TFormulaInfo pNumber, TFormulaInfo toShift, boolean signed);

  @Override
  public BitvectorFormula shiftLeft(BitvectorFormula pNumber, BitvectorFormula toShift) {
    TFormulaInfo param1 = extractInfo(pNumber);
    TFormulaInfo param2 = extractInfo(toShift);

    return wrap(shiftLeft(param1, param2));
  }

  protected abstract TFormulaInfo shiftLeft(TFormulaInfo pExtract, TFormulaInfo pExtract2);

  @Override
  public final BitvectorFormula concat(BitvectorFormula pNumber, BitvectorFormula pAppend) {
    TFormulaInfo param1 = extractInfo(pNumber);
    TFormulaInfo param2 = extractInfo(pAppend);

    return wrap(concat(param1, param2));
  }

  protected abstract TFormulaInfo concat(TFormulaInfo number, TFormulaInfo pAppend);

  @Override
  public final BitvectorFormula extract(BitvectorFormula pNumber, int pMsb, int pLsb) {
    final int bitsize = getLength(pNumber);
    checkArgument(0 <= pLsb, "index out of bounds (negative index %s)", pLsb);
    checkArgument(pLsb <= pMsb, "invalid range (lsb %s larger than msb %s)", pLsb, pMsb);
    checkArgument(pMsb < bitsize, "index out of bounds (index %s beyond length %s)", pMsb, bitsize);
    return wrap(extract(extractInfo(pNumber), pMsb, pLsb));
  }

  protected abstract TFormulaInfo extract(TFormulaInfo pNumber, int pMsb, int pLsb);

  @Override
  public final BitvectorFormula extend(
      BitvectorFormula pNumber, int pExtensionBits, boolean pSigned) {
    checkArgument(0 <= pExtensionBits, "can not extend a negative number of bits");
    return wrap(extend(extractInfo(pNumber), pExtensionBits, pSigned));
  }

  protected abstract TFormulaInfo extend(TFormulaInfo pNumber, int pExtensionBits, boolean pSigned);

  @Override
  public int getLength(BitvectorFormula pNumber) {
    FormulaType<BitvectorFormula> type = getFormulaCreator().getFormulaType(pNumber);
    return ((FormulaType.BitvectorType) type).getSize();
  }

  @Override
  public final BooleanFormula distinct(List<BitvectorFormula> pBits) {
    // optimization
    if (pBits.size() <= 1) {
      return bmgr.makeTrue();
    } else if (pBits.size() > 1L << getLength(pBits.iterator().next())) {
      return bmgr.makeFalse();
    } else {
      return wrapBool(distinctImpl(Lists.transform(pBits, this::extractInfo)));
    }
  }

  protected TFormulaInfo distinctImpl(List<TFormulaInfo> pBits) {
    List<TFormulaInfo> lst = new ArrayList<>();
    for (int i = 0; i < pBits.size(); i++) {
      for (int j = 0; j < i; j++) {
        lst.add(bmgr.not(equal(pBits.get(i), pBits.get(j))));
      }
    }
    return bmgr.andImpl(lst);
  }
}
