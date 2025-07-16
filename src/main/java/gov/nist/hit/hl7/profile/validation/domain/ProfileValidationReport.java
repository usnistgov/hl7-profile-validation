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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * @author jungyubw
 *
 */
public class ProfileValidationReport {

	private boolean success;

	private XSDVerificationResult profileXSDValidationResult;
	private XSDVerificationResult valueSetXSDValidationResult;
	private List<XSDVerificationResult> constraintsXSDValidationResults = new ArrayList<XSDVerificationResult>();
	private XSDVerificationResult coconstraintsXSDValidationResult;
	private XSDVerificationResult slicingXSDValidationResult;
	private XSDVerificationResult bindingXSDValidationResult;
	
	private String testName = "";

	private List<CustomProfileError> profileErrors;
	
	private List<GenericError> genericErrors;
	

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

	public List<XSDVerificationResult> getConstraintsXSDValidationResults() {
		return constraintsXSDValidationResults;
	}

	public void setConstraintsXSDValidationResults(List<XSDVerificationResult> constraintsXSDValidationResults) {
		this.constraintsXSDValidationResults = constraintsXSDValidationResults;
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public XSDVerificationResult getValueSetXSDValidationResult() {
		return valueSetXSDValidationResult;
	}

	public void setValueSetXSDValidationResult(XSDVerificationResult valueSetXSDValidationResult) {
		this.valueSetXSDValidationResult = valueSetXSDValidationResult;
		if (!this.valueSetXSDValidationResult.isSuccess())
			this.success = false;
	}

	public List<XSDVerificationResult> getConstraintsXSDValidationResult() {
		return constraintsXSDValidationResults;
	}

	public void setConstraintsXSDValidationResult(XSDVerificationResult constraintsXSDValidationResult) {
		this.constraintsXSDValidationResults.add(constraintsXSDValidationResult);
		if (!constraintsXSDValidationResult.isSuccess())
			this.success = false;
	}
	
	public String getConstraintsXSDValidationResultToString() {
		String res = "";
		for (XSDVerificationResult constraintResult : constraintsXSDValidationResults ) {
			res+= constraintResult.toString() + " ";
		}
		return res.substring(0, res.length() - 1);
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
	

	public List<GenericError> getGenericErrors() {
		return genericErrors;
	}

	public void setGenericErrors(List<GenericError> genericErrors) {
		this.genericErrors = genericErrors;
	}
	
	public void addGenericError(GenericError error) {
		if (genericErrors == null)
			genericErrors = new ArrayList<GenericError>();
		genericErrors.add(error);
		this.success = false;
	}

	@Override
	public String toString() {
		return "ProfileValidationReport [success=" + success + ", profileXSDValidationResult="
				+ profileXSDValidationResult + ", valueSetXSDValidationResult=" + valueSetXSDValidationResult +				
				 ", constraintsXSDValidationResults=" + getConstraintsXSDValidationResultToString() +
				", profileErrors="+ profileErrors + "]";
	}

	public String generateHTML() {
		String reportHTML = "";

		ClassLoader classLoader = getClass().getClassLoader();
		try {
			reportHTML = IOUtils.toString(classLoader.getResourceAsStream("report.html"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (this.success) {
			reportHTML = reportHTML.replace("$Overall-Result$", "No errors found.");
		} else {
			reportHTML = reportHTML.replace("$Overall-Result$", "Invalid");
		}
		
		reportHTML = reportHTML.replace("$TestName$", this.getTestName());
		if(this.profileXSDValidationResult != null) {
			if (this.profileXSDValidationResult.isSuccess()) {
				String profileResult = "<div id=\"mvrTestingToolBox\" style=\"display:block;\">" + "<br/>"
						+ "<table width=\"100%\" cellpadding=\"2\" cellspacing=\"1\" border=\"0\" class=\"forumline\">"
						+ "<tr>" + "<td class=\"row1\" valign=\"top\">Profile XSD Validation</td>"
						+ "<td class=\"row2\">Valid Profile XML</td>" + "</tr>" + "</table>" + "</div>";

				reportHTML = reportHTML.replace("$Profile-Result$", profileResult);
			} else {
				String profileResult = "<div id=\"mvrTestingToolBox\" style=\"display:block;\">" + "<br/>"
						+ "<table width=\"100%\" cellpadding=\"2\" cellspacing=\"1\" border=\"0\" class=\"forumline\">"
						+ "<tr>" + "<td class=\"row1\" rowspan=\"2\" valign=\"top\">Profile XSD Validation</td>"
						+ "<td class=\"row2\">Invalid Profile XML</td>" + "</tr>" + "<tr>" + "<td class=\"row3\">"
						+ this.profileXSDValidationResult.getE().getMessage() + "</td>" + "</tr>" + "</table>" + "</div>";

				reportHTML = reportHTML.replace("$Profile-Result$", profileResult);
			}
		}
		
		
		String constraintseResultString = "";
		for (XSDVerificationResult constraintResult : constraintsXSDValidationResults ) {
			if (constraintResult.isSuccess()) {
				constraintseResultString += "<div id=\"mvrTestingToolBox\" style=\"display:block;\">" + "<br/>"
						+ "<table width=\"100%\" cellpadding=\"2\" cellspacing=\"1\" border=\"0\" class=\"forumline\">"
						+ "<tr>" + "<td class=\"row1\" valign=\"top\">Constraints XSD Validation</td>"
						+ "<td class=\"row2\">Valid Constraint XML</td>" + "</tr>" + "</table>" + "</div>";

			} else {
				constraintseResultString += "<div id=\"mvrTestingToolBox\" style=\"display:block;\">" + "<br/>"
						+ "<table width=\"100%\" cellpadding=\"2\" cellspacing=\"1\" border=\"0\" class=\"forumline\">"
						+ "<tr>" + "<td class=\"row1\" rowspan=\"2\" valign=\"top\">Constraints XSD Validation</td>"
						+ "<td class=\"row2\">Invalid Constraint XML</td>" + "</tr>" + "<tr>" + "<td class=\"row3\">"
						+ constraintResult.getE().getMessage() + "</td>" + "</tr>" + "</table>"
						+ "</div>";
			}
		}
		reportHTML = reportHTML.replace("$Constraints-Result$", constraintseResultString);

		
		if (this.getValueSetXSDValidationResult()!= null) {
			if (this.getValueSetXSDValidationResult().isSuccess()) {
				String valueSetsResult = "<div id=\"mvrTestingToolBox\" style=\"display:block;\">" + "<br/>"
						+ "<table width=\"100%\" cellpadding=\"2\" cellspacing=\"1\" border=\"0\" class=\"forumline\">"
						+ "<tr>" + "<td class=\"row1\" valign=\"top\">ValueSets XSD Validation</td>"
						+ "<td class=\"row2\">Valid ValueSets XML</td>" + "</tr>" + "</table>" + "</div>";
	
				reportHTML = reportHTML.replace("$ValueSets-Result$", valueSetsResult);
			} else {
				String valueSetsResult = "<div id=\"mvrTestingToolBox\" style=\"display:block;\">" + "<br/>"
						+ "<table width=\"100%\" cellpadding=\"2\" cellspacing=\"1\" border=\"0\" class=\"forumline\">"
						+ "<tr>" + "<td class=\"row1\" rowspan=\"2\" valign=\"top\">ValueSets XSD Validation</td>"
						+ "<td class=\"row2\">Invalid ValueSets XML</td>" + "</tr>" + "<tr>" + "<td class=\"row3\">"
						+ this.getValueSetXSDValidationResult().getE().getMessage() + "</td>" + "</tr>" + "</table>"
						+ "</div>";
	
				reportHTML = reportHTML.replace("$ValueSets-Result$", valueSetsResult);
			}
		}
		if(this.getCoconstraintsXSDValidationResult() != null) {
			if (this.getCoconstraintsXSDValidationResult().isSuccess()) {
				String coConstraintsResult = "<div id=\"mvrTestingToolBox\" style=\"display:block;\">" + "<br/>"
						+ "<table width=\"100%\" cellpadding=\"2\" cellspacing=\"1\" border=\"0\" class=\"forumline\">"
						+ "<tr>" + "<td class=\"row1\" valign=\"top\">CoConstraints XSD Validation</td>"
						+ "<td class=\"row2\">Valid CoConstraints XML</td>" + "</tr>" + "</table>" + "</div>";

				reportHTML = reportHTML.replace("$CoConstraints-Result$", coConstraintsResult);
			} else {
				String coConstraintsResult = "<div id=\"mvrTestingToolBox\" style=\"display:block;\">" + "<br/>"
						+ "<table width=\"100%\" cellpadding=\"2\" cellspacing=\"1\" border=\"0\" class=\"forumline\">"
						+ "<tr>" + "<td class=\"row1\" rowspan=\"2\" valign=\"top\">CoConstraints XSD Validation</td>"
						+ "<td class=\"row2\">Invalid CoConstraints XML</td>" + "</tr>" + "<tr>" + "<td class=\"row3\">"
						+ this.getCoconstraintsXSDValidationResult().getE().getMessage() + "</td>" + "</tr>" + "</table>"
						+ "</div>";

				reportHTML = reportHTML.replace("$CoConstraints-Result$", coConstraintsResult);
			}
		}else {
			String coConstraintsResult = "<div id=\"mvrTestingToolBox\" style=\"display:block;\">" + "<br/>"
					+ "<table width=\"100%\" cellpadding=\"2\" cellspacing=\"1\" border=\"0\" class=\"forumline\">"
					+ "<tr>" + "<td class=\"row1\" valign=\"top\">CoConstraints XSD Validation</td>"
					+ "<td class=\"row2\">No CoConstraints XML</td>" + "</tr>" + "</table>" + "</div>";
			reportHTML = reportHTML.replace("$CoConstraints-Result$", coConstraintsResult);
		}
		
		if (this.getSlicingXSDValidationResult() != null) {
			if (this.getSlicingXSDValidationResult().isSuccess()) {
				String slicingResult = "<div id=\"mvrTestingToolBox\" style=\"display:block;\">" + "<br/>"
						+ "<table width=\"100%\" cellpadding=\"2\" cellspacing=\"1\" border=\"0\" class=\"forumline\">"
						+ "<tr>" + "<td class=\"row1\" valign=\"top\">Slicing XSD Validation</td>"
						+ "<td class=\"row2\">Valid Slicing XML</td>" + "</tr>" + "</table>" + "</div>";
	
				reportHTML = reportHTML.replace("$Slicings-Result$", slicingResult);
			} else {
				String slicingResult = "<div id=\"mvrTestingToolBox\" style=\"display:block;\">" + "<br/>"
						+ "<table width=\"100%\" cellpadding=\"2\" cellspacing=\"1\" border=\"0\" class=\"forumline\">"
						+ "<tr>" + "<td class=\"row1\" rowspan=\"2\" valign=\"top\">Slicing XSD Validation</td>"
						+ "<td class=\"row2\">Invalid Slicing XML</td>" + "</tr>" + "<tr>" + "<td class=\"row3\">"
						+ this.getSlicingXSDValidationResult().getE().getMessage() + "</td>" + "</tr>" + "</table>"
						+ "</div>";
	
				reportHTML = reportHTML.replace("$Slicings-Result$", slicingResult);
			}
		}else {
			String slicingResult = "<div id=\"mvrTestingToolBox\" style=\"display:block;\">" + "<br/>"
					+ "<table width=\"100%\" cellpadding=\"2\" cellspacing=\"1\" border=\"0\" class=\"forumline\">"
					+ "<tr>" + "<td class=\"row1\" valign=\"top\">Slicing XSD Validation</td>"
					+ "<td class=\"row2\">No Slicing XML</td>" + "</tr>" + "</table>" + "</div>";
			reportHTML = reportHTML.replace("$Slicings-Result$", slicingResult);
		}
		
		
		if (this.getBindingXSDValidationResult() != null) {
			if (this.getBindingXSDValidationResult().isSuccess()) {
				String bindingResult = "<div id=\"mvrTestingToolBox\" style=\"display:block;\">" + "<br/>"
						+ "<table width=\"100%\" cellpadding=\"2\" cellspacing=\"1\" border=\"0\" class=\"forumline\">"
						+ "<tr>" + "<td class=\"row1\" valign=\"top\">ValusetBinding XSD Validation</td>"
						+ "<td class=\"row2\">Valid ValusetBinding XML</td>" + "</tr>" + "</table>" + "</div>";

				reportHTML = reportHTML.replace("$Bindings-Result$", bindingResult);
			} else {
				String bindingResult = "<div id=\"mvrTestingToolBox\" style=\"display:block;\">" + "<br/>"
						+ "<table width=\"100%\" cellpadding=\"2\" cellspacing=\"1\" border=\"0\" class=\"forumline\">"
						+ "<tr>" + "<td class=\"row1\" rowspan=\"2\" valign=\"top\">ValusetBinding XSD Validation</td>"
						+ "<td class=\"row2\">Invalid ValusetBinding XML</td>" + "</tr>" + "<tr>" + "<td class=\"row3\">"
						+ this.getBindingXSDValidationResult().getE().getMessage() + "</td>" + "</tr>" + "</table>"
						+ "</div>";

				reportHTML = reportHTML.replace("$Bindings-Result$", bindingResult);
			}
		}else {
			String bindingResult = "<div id=\"mvrTestingToolBox\" style=\"display:block;\">" + "<br/>"
					+ "<table width=\"100%\" cellpadding=\"2\" cellspacing=\"1\" border=\"0\" class=\"forumline\">"
					+ "<tr>" + "<td class=\"row1\" valign=\"top\">ValusetBinding XSD Validation</td>"
					+ "<td class=\"row2\">No ValusetBinding XML</td>" + "</tr>" + "</table>" + "</div>";
			reportHTML = reportHTML.replace("$Bindings-Result$", bindingResult);
		}
		

		if (this.getProfileErrors() == null || this.getProfileErrors().size() == 0) {
			reportHTML = reportHTML.replace("$Custom-Result$", "");
		} else {
			String customResult = "<div id=\"mvrTestingToolBox\" style=\"display:block;\">" + "<br />"
					+ "<table width=\"100%\" cellpadding=\"2\" cellspacing=\"1\" border=\"0\" class=\"forumline\">"
					+ "<tr>" + "<td class=\"row1\" rowspan=\"" + (this.getProfileErrors().size() + 1)
					+ "\" valign=\"top\">Custom Verifications</td>" + "<td class=\"row2\">Target</td>"
					+ "<td class=\"row2\">Error Type</td>" + "<td class=\"row2\">Location</td>"
					+ "<td class=\"row2\">Error Message</td>" + "</tr>";

			for (CustomProfileError e : this.getProfileErrors()) {
				String part = "<tr>" + "<td class=\"row6\">" + e.getTarget() + "</td>" + "<td class=\"row6\">"
						+ e.getErrorType() + "</td>" + "<td class=\"row4\">" + e.getLocation() + "</td>"
						+ "<td class=\"row5\">" + e.getErrorMessage() + "</td>" + "</tr>";
				customResult = customResult + part;
			}
			customResult = customResult + "</table></div>";
			reportHTML = reportHTML.replace("$Custom-Result$", customResult);
		}
		
		if (this.getGenericErrors() == null || this.getGenericErrors().size() == 0) {
			reportHTML = reportHTML.replace("$Generic-Result$", "");
		} else {
			String genericResult = "<div id=\"mvrTestingToolBox\" style=\"display:block;\">" + "<br />"
					+ "<table width=\"100%\" cellpadding=\"2\" cellspacing=\"1\" border=\"0\" class=\"forumline\">"
					+ "<tr>" + "<td class=\"row1\" rowspan=\"" + (this.getGenericErrors().size() + 1)
					+ "\" valign=\"top\">Other Verifications</td>" + "<td class=\"row2\">Target</td>"
					+ "<td class=\"row2\">Error Type</td>" 
					+ "<td class=\"row2\">Error Message</td>" + "</tr>";

			for (GenericError e : this.getGenericErrors()) {
				String part = "<tr>" + "<td class=\"row6\">" + e.getTarget() + "</td>" + "<td class=\"row6\">"
						+ e.getErrorType() + "</td>" + "</td>"
						+ "<td class=\"row5\">" + e.getErrorMessage() + "</td>" + "</tr>";
				genericResult = genericResult + part;
			}
			genericResult = genericResult + "</table></div>";
			reportHTML = reportHTML.replace("$Generic-Result$", genericResult);
		}

		return reportHTML;
	}

	public XSDVerificationResult getCoconstraintsXSDValidationResult() {
		return coconstraintsXSDValidationResult;
	}

	public void setCoconstraintsXSDValidationResult(XSDVerificationResult coconstraintsXSDValidationResult) {
		this.coconstraintsXSDValidationResult = coconstraintsXSDValidationResult;
		if (!this.coconstraintsXSDValidationResult.isSuccess())
			this.success = false;
	}

	public XSDVerificationResult getSlicingXSDValidationResult() {
		return slicingXSDValidationResult;
	}

	public void setSlicingXSDValidationResult(XSDVerificationResult slicingXSDValidationResult) {
		this.slicingXSDValidationResult = slicingXSDValidationResult;
		if (!this.slicingXSDValidationResult.isSuccess())
			this.success = false;
	}

	public XSDVerificationResult getBindingXSDValidationResult() {
		return bindingXSDValidationResult;
	}

	public void setBindingXSDValidationResult(XSDVerificationResult bindingXSDValidationResult) {
		this.bindingXSDValidationResult = bindingXSDValidationResult;
		if (!this.bindingXSDValidationResult.isSuccess())
			this.success = false;
	}

	public enum DocumentTarget {

		PROFILE, MESSAGE, SEGMENT, DATATYPE, VALUESET, CONSTRAINT
	}

	public enum ErrorType {

		FiveLevelComponent, MissingValueSet, DuplicatedDynamicMapping, Unknown, CoreParsingError, MissingProfileFile, MissingConstraintFile, MissingValueSetFile
	}

}
