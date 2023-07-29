package com.integralblue.demo.jumpstart;

import java.time.ZoneOffset;
import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JumpstartApplication {
    static {
        // Ensure UTC is the default timezone so all date/time handling is done using UTC consistently
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
    }

    @SuppressWarnings("java:S4823") // false positive; see https://jira.sonarsource.com/browse/SONARJAVA-2906
    public static void main(String[] args) {
        SpringApplication.run(JumpstartApplication.class, args);
    }
}
