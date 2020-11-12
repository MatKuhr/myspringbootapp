package customer.myspringbootapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.sap.cloud.sdk", "customer"})
@ServletComponentScan({"com.sap.cloud.sdk", "customer"})

public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}