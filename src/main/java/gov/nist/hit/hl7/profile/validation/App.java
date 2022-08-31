package gov.nist.hit.hl7.profile.validation;

import java.io.IOException;
import java.io.InputStream;

import gov.nist.hit.hl7.profile.validation.service.ValidationService;
import gov.nist.hit.hl7.profile.validation.service.impl.ValidationServiceImpl;

/**
 * Hello world!
 *
 */
public class App {
  public static void main(String[] args) throws IOException {
    ValidationService service = new ValidationServiceImpl();
    
    InputStream pio = ClassLoader.class.getResourceAsStream("/xmls/test/Profile.xml");
    InputStream cio = ClassLoader.class.getResourceAsStream("/xmls/test/Constraints.xml");
    InputStream vio = ClassLoader.class.getResourceAsStream("/xmls/test/ValueSets.xml");
    
    System.out.println(service.validationXMLs(pio, cio, vio).generateHTML());
  }
}
