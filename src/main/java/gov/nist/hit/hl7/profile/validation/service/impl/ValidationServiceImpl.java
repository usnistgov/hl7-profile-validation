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
package gov.nist.hit.hl7.profile.validation.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import gov.nist.hit.hl7.profile.validation.domain.XSDVerificationResult;
import gov.nist.hit.hl7.profile.validation.service.ValidationService;
import hl7.v2.profile.XMLDeserializer;

/**
 * @author jungyubw
 *
 */


public class ValidationServiceImpl implements ValidationService {
  private static String profileXSDurl =
      "https://raw.githubusercontent.com/Jungyubw/NIST_healthcare_hl7_v2_profile_schema/master/Schema/NIST%20Validation%20Schema/Profile.xsd";
  private static String valueSetXSDurl =
      "https://raw.githubusercontent.com/Jungyubw/NIST_healthcare_hl7_v2_profile_schema/master/Schema/NIST%20Validation%20Schema/ValueSets.xsd";
  private static String constraintXSDurl =
      "https://raw.githubusercontent.com/Jungyubw/NIST_healthcare_hl7_v2_profile_schema/master/Schema/NIST%20Validation%20Schema/ConformanceContext.xsd";

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nist.hit.hl7.profile.validation.service.ValidationService#validationXMLs(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  public void validationXMLs(String profileXMLStr, String constraintXMLStr, String valuesetXMLStr) {
    // 1. XML Validation by XSD
    XSDVerificationResult profileXSDVerificationResult =
        this.verifyXMLByXSD(profileXSDurl, profileXMLStr);
    XSDVerificationResult valuesetXSDVerificationResult =
        this.verifyXMLByXSD(valueSetXSDurl, valuesetXMLStr);
    XSDVerificationResult constraintXSDVerificationResult =
        this.verifyXMLByXSD(constraintXSDurl, constraintXMLStr);

    System.out.println(profileXSDVerificationResult);
    System.out.println(valuesetXSDVerificationResult);
    System.out.println(constraintXSDVerificationResult);


    // 2. Pasing

    /*
    if(profileXSDVerificationResult.isSuccess()){
      InputStream profileXMLIO = IOUtils.toInputStream(profileXMLStr, StandardCharsets.UTF_8);
      try {
        XMLDeserializer.deserialize(profileXMLIO).get();
        ConstraintsParserImpl constraintsParser = new ConstraintsParserImpl();
        
        Constraints conformanceStatements = constraintsParser.confStatements(c1Xml);
        Constraints predicates = constraintsParser.predicates(c1Xml);
        
        
      } catch (NoSuchElementException nsee) {
        System.out.println(nsee);
      } catch (Exception e) {
        System.out.println(e);
      }      
    }
*/


  }

  public void validationXMLs(InputStream profileXMLIO, InputStream constraintXMLIO,
      InputStream valuesetXMLIO) throws IOException {
    String profileXMLStr = IOUtils.toString(profileXMLIO, StandardCharsets.UTF_8);
    String constraintXMLStr = IOUtils.toString(constraintXMLIO, StandardCharsets.UTF_8);
    String valuesetXMLStr = IOUtils.toString(valuesetXMLIO, StandardCharsets.UTF_8);

    this.validationXMLs(profileXMLStr, constraintXMLStr, valuesetXMLStr);
  }



  private XSDVerificationResult verifyXMLByXSD(String xsdURL, String xml) {
    try {
      URL schemaFile = new URL(xsdURL);
      Source xmlFile = new StreamSource(new StringReader(xml));
      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema schema = schemaFactory.newSchema(schemaFile);
      Validator validator = schema.newValidator();
      validator.validate(xmlFile);
      return new XSDVerificationResult(true, null);
    } catch (SAXException e) {
      return new XSDVerificationResult(false, e);
    } catch (IOException e) {
      return new XSDVerificationResult(false, e);
    } catch (Exception e) {
      return new XSDVerificationResult(false, e);
    }
  }

}

