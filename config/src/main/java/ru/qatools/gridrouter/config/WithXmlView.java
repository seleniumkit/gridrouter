package ru.qatools.gridrouter.config;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.bind.Marshaller.JAXB_ENCODING;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 */
public interface WithXmlView {

    default String toXml() {
        try {
            JAXBContext context = JAXBContext.newInstance(getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(JAXB_ENCODING, UTF_8.toString());
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(this, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new GridRouterException("Unable to marshall bean", e);
        }
    }
}
