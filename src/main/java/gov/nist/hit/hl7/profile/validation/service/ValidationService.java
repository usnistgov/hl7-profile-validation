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
package gov.nist.hit.hl7.profile.validation.service;

import java.io.IOException;
import java.io.InputStream;

import gov.nist.hit.hl7.profile.validation.domain.ProfileValidationReport;

/**
 * @author jungyubw
 *
 */
public interface ValidationService {


  public ProfileValidationReport validationXMLs(String profileXMLStr, String constraintXMLStr, String valuesetXMLStr);
  
  public ProfileValidationReport validationXMLs(InputStream profileXMLIO, InputStream constraintXMLIO, InputStream valuesetXMLIO) throws IOException;
  
  public String validationXMLsHTML(String profileXMLStr, String constraintXMLStr, String valuesetXMLStr);
  
  public String validationXMLsHTML(InputStream profileXMLIO, InputStream constraintXMLIO, InputStream valuesetXMLIO) throws IOException;
  
  public ProfileValidationReport validationXMLs(String profileXMLStr, String constraintXMLStr, String valuesetXMLStr, String coconstraintXML, String pofileSlicingXML, String bindingXML);
  
  public ProfileValidationReport validationXMLs(InputStream profileXMLIO, InputStream constraintXMLIO, InputStream valuesetXMLIO, InputStream coconstraintXMLIO, InputStream pofileSlicingXMLIO, InputStream bindingXMLIO) throws IOException;
  
  public String validationXMLsHTML(String profileXMLStr, String constraintXMLStr, String valuesetXMLStr, String coconstraintXML, String pofileSlicingXML, String bindingXML);
  
  public String validationXMLsHTML(InputStream profileXMLIO, InputStream constraintXMLIO, InputStream valuesetXMLIO, InputStream coconstraintXMLIO, InputStream pofileSlicingXMLIO, InputStream bindingXMLIO) throws IOException;

}
