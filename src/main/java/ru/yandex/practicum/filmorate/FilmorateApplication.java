package ru.yandex.practicum.filmorate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class FilmorateApplication {
    public static void main(String[] args) {
        log.info("Start Application");
        SpringApplication.run(FilmorateApplication.class, args);
    }
}
