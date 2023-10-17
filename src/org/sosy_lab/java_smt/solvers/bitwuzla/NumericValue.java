/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.sosy_lab.java_smt.solvers.bitwuzla;

public class NumericValue {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected NumericValue(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(NumericValue obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        BitwuzlaJNI.delete_NumericValue(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setCur(long value) {
    BitwuzlaJNI.NumericValue_cur_set(swigCPtr, this, value);
  }

  public long getCur() {
    return BitwuzlaJNI.NumericValue_cur_get(swigCPtr, this);
  }

  public void setDflt(long value) {
    BitwuzlaJNI.NumericValue_dflt_set(swigCPtr, this, value);
  }

  public long getDflt() {
    return BitwuzlaJNI.NumericValue_dflt_get(swigCPtr, this);
  }

  public void setMin(long value) {
    BitwuzlaJNI.NumericValue_min_set(swigCPtr, this, value);
  }

  public long getMin() {
    return BitwuzlaJNI.NumericValue_min_get(swigCPtr, this);
  }

  public void setMax(long value) {
    BitwuzlaJNI.NumericValue_max_set(swigCPtr, this, value);
  }

  public long getMax() {
    return BitwuzlaJNI.NumericValue_max_get(swigCPtr, this);
  }

  public NumericValue() {
    this(BitwuzlaJNI.new_NumericValue(), true);
  }
}
