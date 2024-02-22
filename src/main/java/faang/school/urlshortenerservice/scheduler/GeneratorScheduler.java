package faang.school.urlshortenerservice.scheduler;

import faang.school.urlshortenerservice.hash_generator.HashGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneratorScheduler {
    private HashGenerator hashGenerator;

    @Scheduled(cron = "${hash.generation.scheduler.cron}")
    public void generateUniqueNumbers(){
        log.info("GeneratorScheduler started at {}", LocalDateTime.now());
        hashGenerator.generateBatch();
    }
}