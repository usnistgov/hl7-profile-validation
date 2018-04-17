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
    System.out.println("Hello World!");
    ValidationService service = new ValidationServiceImpl();
    
    InputStream pio = ClassLoader.class.getResourceAsStream("/xmls/Profile.xml");
    InputStream cio = ClassLoader.class.getResourceAsStream("/xmls/Constraints.xml");
    InputStream vio = ClassLoader.class.getResourceAsStream("/xmls/ValueSets.xml");
    
    service.validationXMLs(pio, cio, vio);
  }
}
