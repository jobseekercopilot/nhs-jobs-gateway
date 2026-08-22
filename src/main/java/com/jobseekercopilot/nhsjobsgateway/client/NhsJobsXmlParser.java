package com.jobseekercopilot.nhsjobsgateway.client;

import com.jobseekercopilot.nhsjobsgateway.model.NhsJob;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchResponse;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

@Component
public class NhsJobsXmlParser {
    private static final Pattern MONEY = Pattern.compile("(?:£|GBP\\s*)?([0-9]+(?:[,.][0-9]{1,3})*(?:\\.[0-9]{1,2})?)");

    public NhsJobsSearchResponse parse(String xml, int page, int pageSize) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            var document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            List<NhsJob> jobs = new ArrayList<>();
            var nodes = document.getElementsByTagName("vacancyDetails");
            for (int index = 0; index < nodes.getLength(); index++) {
                Element vacancy = (Element) nodes.item(index);
                List<String> locations = new ArrayList<>();
                var locationNodes = vacancy.getElementsByTagName("location");
                for (int locationIndex = 0; locationIndex < locationNodes.getLength(); locationIndex++) {
                    String value = locationNodes.item(locationIndex).getTextContent();
                    if (value != null && !value.isBlank()) locations.add(value.trim());
                }
                String salaryText = text(vacancy, "salary");
                List<BigDecimal> amounts = amounts(salaryText);
                jobs.add(new NhsJob(
                        text(vacancy, "id"), text(vacancy, "reference"), text(vacancy, "title"),
                        text(vacancy, "employer"), text(vacancy, "description"), List.copyOf(locations),
                        salaryText, amounts.isEmpty() ? null : amounts.get(0), amounts.size() < 2 ? null : amounts.get(1),
                        salaryText == null || !salaryText.contains("£") ? null : "GBP", salaryPeriod(salaryText),
                        text(vacancy, "type"), text(vacancy, "postDate"), text(vacancy, "closeDate"), text(vacancy, "url")));
            }
            return new NhsJobsSearchResponse(integer(document.getDocumentElement(), "totalResults"),
                    integer(document.getDocumentElement(), "totalPages"), page, pageSize, List.copyOf(jobs));
        } catch (Exception exception) {
            throw new NhsJobsProviderException("NHS Jobs returned malformed XML", exception);
        }
    }

    private String text(Element parent, String name) {
        var nodes = parent.getElementsByTagName(name);
        if (nodes.getLength() == 0) return null;
        String value = nodes.item(0).getTextContent();
        return value == null || value.isBlank() ? null : value.trim();
    }
    private int integer(Element parent, String name) {
        try { return Integer.parseInt(text(parent, name)); } catch (RuntimeException exception) { return 0; }
    }
    private List<BigDecimal> amounts(String value) {
        if (value == null) return List.of();
        List<BigDecimal> values = new ArrayList<>();
        Matcher matcher = MONEY.matcher(value);
        while (matcher.find() && values.size() < 2) {
            try { values.add(new BigDecimal(matcher.group(1).replace(",", ""))); } catch (NumberFormatException ignored) { }
        }
        return values;
    }
    private String salaryPeriod(String value) {
        if (value == null) return null;
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.contains("hour")) return "HOUR";
        if (lower.contains("week")) return "WEEK";
        if (lower.contains("month")) return "MONTH";
        if (lower.contains("day")) return "DAY";
        if (lower.contains("year") || lower.contains("annum") || lower.contains("annual")) return "YEAR";
        return null;
    }
}
