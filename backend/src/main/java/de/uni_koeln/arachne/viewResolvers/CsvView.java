package de.uni_koeln.arachne.viewResolvers;

import de.uni_koeln.arachne.response.Facet;
import de.uni_koeln.arachne.response.FacetList;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Patrick Jominet
 */
public class CsvView extends AbstractCsvView {

    @Override
    protected void buildCsvDocument(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setHeader("Content-Disposition", "attachment; filename=\"facetsList.csv\"");

        FacetList facetList = new FacetList();

        List<String> headerList = new ArrayList<>();
        for (Facet facet : facetList.getList()) {
            headerList.add(facet.getName());
        }
        String[] header = new String[headerList.size()];
        headerList.toArray(header);

        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);

        csvWriter.writeHeader(header);

        for (Facet facet : facetList.getList()) {
            csvWriter.write(facet, header);
        }
        csvWriter.close();


    }
}
