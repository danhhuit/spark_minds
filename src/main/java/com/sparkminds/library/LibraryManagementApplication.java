package com.sparkminds.library;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibraryManagementApplication {

  private static final String APPLICATION_TIME_ZONE = "Asia/Ho_Chi_Minh";

  public static void main(String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone(APPLICATION_TIME_ZONE));

    SpringApplication.run(LibraryManagementApplication.class, args);
  }
}
