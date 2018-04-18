/**
 * This software was developed at the National Institute of Standards and Technology by employees of
 * the Federal Government in the course of their official duties. Pursuant to title 17 Section 105
 * of the United States Code this software is not subject to copyright protection and is in the
 * public domain. This is an experimental system. NIST assumes no responsibility whatsoever for its
 * use by other parties, and makes no guarantees, expressed or implied, about its quality,
 * reliability, or any other characteristic. We would appreciate acknowledgement if the software is
 * used. This software can be redistributed and/or modified freely provided that any derivative
 * works bear some notice that they are derived from it, and any modified versions bear some notice
 * that they have been modified.
 */
package gov.nist.hit.hl7.profile.validation.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jungyubw
 *
 */
public class ProfileValidationReport {

  private boolean success;

  private XSDVerificationResult profileXSDValidationResult;
  private XSDVerificationResult valueSetXSDValidationResult;
  private XSDVerificationResult constraintsXSDValidationResult;

  List<CustomProfileError> profileErrors;

  public ProfileValidationReport() {
    this.success = true;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public XSDVerificationResult getProfileXSDValidationResult() {
    return profileXSDValidationResult;
  }

  public void setProfileXSDValidationResult(XSDVerificationResult profileXSDValidationResult) {
    this.profileXSDValidationResult = profileXSDValidationResult;
    if (!this.profileXSDValidationResult.isSuccess())
      this.success = false;
  }

  public XSDVerificationResult getValueSetXSDValidationResult() {
    return valueSetXSDValidationResult;
  }

  public void setValueSetXSDValidationResult(XSDVerificationResult valueSetXSDValidationResult) {
    this.valueSetXSDValidationResult = valueSetXSDValidationResult;
    if (!this.valueSetXSDValidationResult.isSuccess())
      this.success = false;
  }

  public XSDVerificationResult getConstraintsXSDValidationResult() {
    return constraintsXSDValidationResult;
  }

  public void setConstraintsXSDValidationResult(
      XSDVerificationResult constraintsXSDValidationResult) {
    this.constraintsXSDValidationResult = constraintsXSDValidationResult;
    if (!this.constraintsXSDValidationResult.isSuccess())
      this.success = false;
  }

  public List<CustomProfileError> getProfileErrors() {
    return profileErrors;
  }

  public void setProfileErrors(List<CustomProfileError> profileErrors) {
    this.profileErrors = profileErrors;
  }


  public void addProfileError(CustomProfileError error) {
    if (profileErrors == null)
      profileErrors = new ArrayList<CustomProfileError>();
    profileErrors.add(error);
    this.success = false;
  }

  @Override
  public String toString() {
    return "ProfileValidationReport [success=" + success + ", profileXSDValidationResult="
        + profileXSDValidationResult + ", valueSetXSDValidationResult="
        + valueSetXSDValidationResult + ", constraintsXSDValidationResult="
        + constraintsXSDValidationResult + ", profileErrors=" + profileErrors + "]";
  }


}
