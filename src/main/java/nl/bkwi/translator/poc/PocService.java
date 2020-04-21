package nl.bkwi.translator.poc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedXMLInputFactory;
import org.codehaus.jettison.mapped.MappedXMLOutputFactory;
import org.springframework.stereotype.Component;

@Component
public class PocService {


    private SOAPMessage buildMessageRequest(String json)
        throws IOException, SOAPException, XMLStreamException, TransformerException {
        Configuration config = new Configuration();
        Map<String, String> xml2JsonNs = new TreeMap<String, String>();
        xml2JsonNs.put("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
        xml2JsonNs
            .put("http://bkwi.nl/SuwiML/Diensten/BRPDossierPersoonLeefsituatieBVV/v0100", "v01");
        config.setPrimitiveArrayKeys(Collections.singleton("Names"));
        MappedXMLInputFactory factory = new MappedXMLInputFactory(xml2JsonNs);
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(json));
        String message = convert(reader);
        InputStream is = new ByteArrayInputStream(message.getBytes());
        SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
        return soapMessage;
    }

    public String getAanvraagPersoon(String json)
        throws XMLStreamException, SOAPException, TransformerException, IOException {
        SOAPMessage request = buildMessageRequest(json);
        return getResponseFromEndpoint(request);

    }

    private String getResponseFromEndpoint(SOAPMessage request) throws SOAPException {
        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection soapConnection = soapConnectionFactory.createConnection();
        try {
            String url = "http://localhost:8081"; // Stub service without security
            SOAPMessage soapResponse = soapConnection.call(request, url);
            String responseSoap = xmlResponse(soapResponse);
            XMLEventReader reader = XMLInputFactory.newInstance()
                .createXMLEventReader(new StringReader(responseSoap));
            Configuration config = new Configuration();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            config.setPrimitiveArrayKeys(Collections.singleton("Names"));
            XMLEventWriter xmlWriter = new MappedXMLOutputFactory(config)
                .createXMLEventWriter(buffer);
            xmlWriter.add(reader);
            xmlWriter.close();
            reader.close();
            String retval = new String(buffer.toByteArray());
            return retval;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private String convert(XMLStreamReader reader)
        throws TransformerFactoryConfigurationError, TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter stringWriter = new StringWriter();
        transformer.transform(new StAXSource(reader), new StreamResult(stringWriter));
        return stringWriter.toString();
    }


    protected String xmlResponse(SOAPMessage soapMessage) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        Source sourceContent = soapMessage.getSOAPPart().getContent();
        StringWriter outWriter = new StringWriter();
        StreamResult result = new StreamResult(outWriter);
        transformer.transform(sourceContent, result);
        StringBuffer sb = outWriter.getBuffer();
        String finalstringEnv = sb.toString();
        return finalstringEnv;
    }

}
