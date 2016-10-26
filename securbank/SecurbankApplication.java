package securbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


/**
 * @author Ayush Gupta
 *
 */
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = "securbank")
public class SecurbankApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurbankApplication.class, args);
	}
	
}
