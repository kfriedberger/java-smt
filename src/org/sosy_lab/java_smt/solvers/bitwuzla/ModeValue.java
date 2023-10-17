/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.sosy_lab.java_smt.solvers.bitwuzla;

public class ModeValue {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected ModeValue(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ModeValue obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        BitwuzlaJNI.delete_ModeValue(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setCur(String value) {
    BitwuzlaJNI.ModeValue_cur_set(swigCPtr, this, value);
  }

  public String getCur() {
    return BitwuzlaJNI.ModeValue_cur_get(swigCPtr, this);
  }

  public void setDflt(String value) {
    BitwuzlaJNI.ModeValue_dflt_set(swigCPtr, this, value);
  }

  public String getDflt() {
    return BitwuzlaJNI.ModeValue_dflt_get(swigCPtr, this);
  }

  public void setNum_modes(long value) {
    BitwuzlaJNI.ModeValue_num_modes_set(swigCPtr, this, value);
  }

  public long getNum_modes() {
    return BitwuzlaJNI.ModeValue_num_modes_get(swigCPtr, this);
  }

  public ModeValue() {
    this(BitwuzlaJNI.new_ModeValue(), true);
  }
}
