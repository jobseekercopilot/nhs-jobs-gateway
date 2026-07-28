package com.jobseekercopilot.nhsjobsgateway.client;

import com.jobseekercopilot.nhsjobsgateway.model.CanonicalJob;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchResponse;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

final class NhsJobsXmlParser {
    NhsJobsSearchResponse parse(
            byte[] xml,
            int requestedPage,
            int resultsPerPage) {
        try {
            DocumentBuilder builder = secureFactory().newDocumentBuilder();
            builder.setErrorHandler(throwingErrorHandler());
            Document document = builder
                    .parse(new ByteArrayInputStream(xml));
            Element root = document.getDocumentElement();
            if (root == null || !"nhsJobs".equals(root.getNodeName())) {
                throw new NhsJobsProviderException("NHS Jobs response has an unexpected root element");
            }
            int totalResults = integer(root, "totalResults");
            int totalPages = integer(root, "totalPages");
            List<CanonicalJob> jobs = new ArrayList<>();
            NodeList vacancyNodes = root.getElementsByTagName("vacancyDetails");
            for (int index = 0; index < vacancyNodes.getLength(); index++) {
                jobs.add(map((Element) vacancyNodes.item(index)));
            }
            return NhsJobsSearchResponse.available(
                    totalResults,
                    totalPages,
                    requestedPage,
                    resultsPerPage,
                    jobs);
        } catch (NhsJobsProviderException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new NhsJobsProviderException("NHS Jobs response could not be parsed");
        }
    }

    private CanonicalJob map(Element vacancy) {
        String link = SafeNhsJobsLink.live(text(vacancy, "url")).orElse(null);
        return new CanonicalJob(
                text(vacancy, "id"),
                text(vacancy, "reference"),
                text(vacancy, "title"),
                text(vacancy, "employer"),
                text(vacancy, "description"),
                locations(vacancy),
                text(vacancy, "salary"),
                text(vacancy, "type"),
                text(vacancy, "postDate"),
                text(vacancy, "closeDate"),
                "NHS Jobs",
                link,
                link);
    }

    private List<String> locations(Element vacancy) {
        List<String> result = new ArrayList<>();
        NodeList nodes = vacancy.getElementsByTagName("locations");
        for (int index = 0; index < nodes.getLength(); index++) {
            Node node = nodes.item(index);
            if (!hasElementChildren(node)) {
                String value = clean(node.getTextContent());
                if (value != null && !result.contains(value)) {
                    result.add(value);
                }
            }
        }
        return List.copyOf(result);
    }

    private boolean hasElementChildren(Node node) {
        NodeList children = node.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            if (children.item(index).getNodeType() == Node.ELEMENT_NODE) {
                return true;
            }
        }
        return false;
    }

    private int integer(Element parent, String name) {
        String value = text(parent, name);
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new NhsJobsProviderException("NHS Jobs pagination is invalid");
        }
    }

    private String text(Element parent, String name) {
        NodeList children = parent.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            if (child.getNodeType() == Node.ELEMENT_NODE
                    && name.equals(child.getNodeName())) {
                return clean(child.getTextContent());
            }
        }
        return null;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String clean = value.strip();
        return clean.isEmpty() ? null : clean;
    }

    private DocumentBuilderFactory secureFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setNamespaceAware(false);
        return factory;
    }

    private ErrorHandler throwingErrorHandler() {
        return new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                throw exception;
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                throw exception;
            }

            @Override
            public void fatalError(SAXParseException exception)
                    throws SAXException {
                throw exception;
            }
        };
    }
}
