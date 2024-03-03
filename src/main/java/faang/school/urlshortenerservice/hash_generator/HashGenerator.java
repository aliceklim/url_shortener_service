package faang.school.urlshortenerservice.hash_generator;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.service.Base62Encoder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class HashGenerator {
    private final HashRepository hashRepository;
    private final Base62Encoder base62Encoder;
    @Value("${hash.unique-number.range}")
    private int uniqueNumberRange;

    /**
     * Retrieves a list of unique numbers from DB
     * Converts them into hashes using the base62Encoder
     * Saves new hashes to DB
     */
    @Transactional
    @Async("threadPoolForGenerateBatch")
    public void generateBatch() {
        List<Long> uniqueNumbers = hashRepository.getUniqueNumbers(uniqueNumberRange);
        log.info("{} unique numbers retrieved from DB", uniqueNumberRange);

        List<String> hashes = base62Encoder.encode(uniqueNumbers);
        List<Hash> urls = hashes.stream()
                .map(hashToMap -> {
                    Hash hash = new Hash();
                    hash.setHash(hashToMap);
                    return hash;
                })
                .collect(Collectors.toList());

        hashRepository.saveAll(urls);
        log.info("{} new hashes saved to DB", uniqueNumberRange);
    }
}