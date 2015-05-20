package de.uni_koeln.arachne.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@Import(ContextConfiguration.class)
@ImportResource({
	"classpath:META-INF/security-context.xml"
})
public class ApplicationConfiguration {

}
