/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xyz.redtorch.api.jctp;

public class CThostFtdcVerifyFuturePasswordAndCustInfoField {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CThostFtdcVerifyFuturePasswordAndCustInfoField(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CThostFtdcVerifyFuturePasswordAndCustInfoField obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        jctpmdapiv6v3v11x64JNI.delete_CThostFtdcVerifyFuturePasswordAndCustInfoField(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setCustomerName(String value) {
    jctpmdapiv6v3v11x64JNI.CThostFtdcVerifyFuturePasswordAndCustInfoField_CustomerName_set(swigCPtr, this, value);
  }

  public String getCustomerName() {
    return jctpmdapiv6v3v11x64JNI.CThostFtdcVerifyFuturePasswordAndCustInfoField_CustomerName_get(swigCPtr, this);
  }

  public void setIdCardType(char value) {
    jctpmdapiv6v3v11x64JNI.CThostFtdcVerifyFuturePasswordAndCustInfoField_IdCardType_set(swigCPtr, this, value);
  }

  public char getIdCardType() {
    return jctpmdapiv6v3v11x64JNI.CThostFtdcVerifyFuturePasswordAndCustInfoField_IdCardType_get(swigCPtr, this);
  }

  public void setIdentifiedCardNo(String value) {
    jctpmdapiv6v3v11x64JNI.CThostFtdcVerifyFuturePasswordAndCustInfoField_IdentifiedCardNo_set(swigCPtr, this, value);
  }

  public String getIdentifiedCardNo() {
    return jctpmdapiv6v3v11x64JNI.CThostFtdcVerifyFuturePasswordAndCustInfoField_IdentifiedCardNo_get(swigCPtr, this);
  }

  public void setCustType(char value) {
    jctpmdapiv6v3v11x64JNI.CThostFtdcVerifyFuturePasswordAndCustInfoField_CustType_set(swigCPtr, this, value);
  }

  public char getCustType() {
    return jctpmdapiv6v3v11x64JNI.CThostFtdcVerifyFuturePasswordAndCustInfoField_CustType_get(swigCPtr, this);
  }

  public void setAccountID(String value) {
    jctpmdapiv6v3v11x64JNI.CThostFtdcVerifyFuturePasswordAndCustInfoField_AccountID_set(swigCPtr, this, value);
  }

  public String getAccountID() {
    return jctpmdapiv6v3v11x64JNI.CThostFtdcVerifyFuturePasswordAndCustInfoField_AccountID_get(swigCPtr, this);
  }

  public void setPassword(String value) {
    jctpmdapiv6v3v11x64JNI.CThostFtdcVerifyFuturePasswordAndCustInfoField_Password_set(swigCPtr, this, value);
  }

  public String getPassword() {
    return jctpmdapiv6v3v11x64JNI.CThostFtdcVerifyFuturePasswordAndCustInfoField_Password_get(swigCPtr, this);
  }

  public void setCurrencyID(String value) {
    jctpmdapiv6v3v11x64JNI.CThostFtdcVerifyFuturePasswordAndCustInfoField_CurrencyID_set(swigCPtr, this, value);
  }

  public String getCurrencyID() {
    return jctpmdapiv6v3v11x64JNI.CThostFtdcVerifyFuturePasswordAndCustInfoField_CurrencyID_get(swigCPtr, this);
  }

  public void setLongCustomerName(String value) {
    jctpmdapiv6v3v11x64JNI.CThostFtdcVerifyFuturePasswordAndCustInfoField_LongCustomerName_set(swigCPtr, this, value);
  }

  public String getLongCustomerName() {
    return jctpmdapiv6v3v11x64JNI.CThostFtdcVerifyFuturePasswordAndCustInfoField_LongCustomerName_get(swigCPtr, this);
  }

  public CThostFtdcVerifyFuturePasswordAndCustInfoField() {
    this(jctpmdapiv6v3v11x64JNI.new_CThostFtdcVerifyFuturePasswordAndCustInfoField(), true);
  }

}
