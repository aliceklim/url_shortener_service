package faang.school.urlshortenerservice.scheduler;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class CleanerScheduler {
    private final UrlRepository urlRepository;
    private final HashRepository hashRepository;

    /**
     * Once a day deletes url older than 1yo
     * and adds their hashes to hash repository
     */
    @Scheduled(cron = "${hash.scheduler.deletion.cron}")
    @Transactional
    @Async("hashCacheThreadPool")
    public void deleteOldUrls(){
        List<Url> deletedUrls = urlRepository.deleteUrlsOlderThanOneYear();
        log.info("{} urls older than 1 year deleted from DB", deletedUrls.size());
        List<Hash> freeHashes = deletedUrls.stream().map(url -> {
            Hash hash = new Hash();
            hash.setHash(url.getHash());
            return hash;
        })
                .collect(Collectors.toList());

        hashRepository.saveAll(freeHashes);
        log.info("{} free hashes saved to DB");
    }
}