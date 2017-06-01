package de.uni_koeln.arachne.viewResolvers;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.Locale;

/**
 * @author Patrick Jominet
 */
public class CsvViewResolver implements ViewResolver {

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        return new CsvView();
    }
}
