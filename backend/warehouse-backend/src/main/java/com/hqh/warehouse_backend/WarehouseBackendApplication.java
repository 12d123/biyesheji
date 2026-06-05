package com.hqh.warehouse_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"com.hqh.warehouse_backend"})
@EnableScheduling
public class WarehouseBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(WarehouseBackendApplication.class, args);
	}

}
