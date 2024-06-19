package ru.nms.crdt_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class CrdtServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrdtServerApplication.class, args);
	}

}
