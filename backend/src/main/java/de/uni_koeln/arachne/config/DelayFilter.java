package de.uni_koeln.arachne.config;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = {"/*"}, description = "Slow Backend down for debug purposes")
public class DelayFilter implements Filter {

    private Integer delayForSeconds;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        final String secondsAsString = WebApplicationContextUtils.getRequiredWebApplicationContext(filterConfig.getServletContext()).getEnvironment().getProperty("delaySeconds");
        delayForSeconds = (secondsAsString != null) ? Integer.parseInt(secondsAsString) : null;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        if (delayForSeconds != null) {
            try {
                Thread.sleep(delayForSeconds * 1000);
            } catch (InterruptedException e) {
                throw new ServletException("Interrupted!");
            }
        }
        HttpServletResponse response = (HttpServletResponse) resp;
        response.setHeader("Cache-Control", "no-cache, must-revalidate");
        chain.doFilter(req, resp);
    }

    @Override
    public void destroy() {}
}