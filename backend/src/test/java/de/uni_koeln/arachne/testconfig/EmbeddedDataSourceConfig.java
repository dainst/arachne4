package de.uni_koeln.arachne.testconfig;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@ComponentScan("de.uni_koeln.arachne")
@Configuration
@EnableWebMvc
@EnableAsync
@EnableSpringConfigured
@PropertySource("classpath:config/application.properties")
public class EmbeddedDataSourceConfig extends WebMvcConfigurerAdapter {

    @Bean(destroyMethod="shutdown")
    public DataSource dataSource() {
        return new EmbeddedMysqlDatabaseBuilder().build();
    }
}