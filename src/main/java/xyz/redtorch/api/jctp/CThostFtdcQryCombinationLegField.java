/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xyz.redtorch.api.jctp;

public class CThostFtdcQryCombinationLegField {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CThostFtdcQryCombinationLegField(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CThostFtdcQryCombinationLegField obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        jctpmdapiv6v3v11x64JNI.delete_CThostFtdcQryCombinationLegField(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setCombInstrumentID(String value) {
    jctpmdapiv6v3v11x64JNI.CThostFtdcQryCombinationLegField_CombInstrumentID_set(swigCPtr, this, value);
  }

  public String getCombInstrumentID() {
    return jctpmdapiv6v3v11x64JNI.CThostFtdcQryCombinationLegField_CombInstrumentID_get(swigCPtr, this);
  }

  public void setLegID(int value) {
    jctpmdapiv6v3v11x64JNI.CThostFtdcQryCombinationLegField_LegID_set(swigCPtr, this, value);
  }

  public int getLegID() {
    return jctpmdapiv6v3v11x64JNI.CThostFtdcQryCombinationLegField_LegID_get(swigCPtr, this);
  }

  public void setLegInstrumentID(String value) {
    jctpmdapiv6v3v11x64JNI.CThostFtdcQryCombinationLegField_LegInstrumentID_set(swigCPtr, this, value);
  }

  public String getLegInstrumentID() {
    return jctpmdapiv6v3v11x64JNI.CThostFtdcQryCombinationLegField_LegInstrumentID_get(swigCPtr, this);
  }

  public CThostFtdcQryCombinationLegField() {
    this(jctpmdapiv6v3v11x64JNI.new_CThostFtdcQryCombinationLegField(), true);
  }

}
