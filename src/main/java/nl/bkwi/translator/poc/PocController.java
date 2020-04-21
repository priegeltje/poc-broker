package nl.bkwi.translator.poc;

import java.io.IOException;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PocController {

    private final PocService dalService;

    public PocController(PocService dalService) {
        this.dalService = dalService;
    }

    @GetMapping(value = "/aanvraagPersoon", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String aanvraagPersoon(@RequestBody String aanvraagPersoon)
        throws IOException, TransformerException, SOAPException, XMLStreamException {
        return dalService.getAanvraagPersoon(aanvraagPersoon);
    }

}


