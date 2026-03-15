package com.taskflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class TaskflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskflowApplication.class, args);
	}

}
