package com.hawkins;

import static com.github.cbismuth.fdupes.metrics.MetricRegistrySingleton.getMetricRegistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ComponentScan("com.github.cbismuth.fdupes")
@ComponentScan("com.hawkins")
public class FdupesSpringApplication {

	public static void main(String[] args) {
		
        getMetricRegistry();

		SpringApplication.run(FdupesSpringApplication.class, args);
	
		
	}

}
