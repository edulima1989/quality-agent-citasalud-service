package org.ups.citasalud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRetry
@EnableAsync
@EnableScheduling
public class CitasaludServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(CitasaludServiceApplication.class, args);
  }

}
