package faang.school.urlshortenerservice.cache;

import faang.school.urlshortenerservice.hash_generator.HashGenerator;
import faang.school.urlshortenerservice.repository.HashRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@RequiredArgsConstructor
@Getter
@Setter
@Slf4j
public class HashCache {

    private final HashRepository hashRepository;
    private final HashGenerator hashGenerator;
    private BlockingQueue<String> cache;
    private final Executor hashCacheThreadPool;
    @Value("${hash.cache.poll-timeout}")
    private long cachePollTimeout;
    @Value("${hash.cache.size}")
    private int cacheSize;
    @Value("${hash.cache.min-fill}")
    private int minFill;
    private Lock lock = new ReentrantLock();

    @PostConstruct
    private void cacheInit() {
        cache = new ArrayBlockingQueue<>(cacheSize);
        fillCache();
    }

    /**
     * Retrieves a hash from the cache
     * If the cache is filled to less than min %, a new batch of hashes is fetched asynchronously
     */
    public String getHash() {
        int result = cache.size() * 100 / cacheSize;
        if (cache.size() * 100 / cacheSize < minFill) {
            log.info("HashCache getting new hashes for cache");
            hashCacheThreadPool.execute(this::fillCache);
        }

        String hash;
        try {
            hash = cache.poll(cachePollTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Cache has interrupted while waiting method poll() ", e);
            throw new RuntimeException(e);
        }

        return hash;
    }

    /**
     * If the lock is acquired successfully, retrieves a batch of hashes from the repository
     * to fill the cache if the cache size is less than required.
     * Then each hash is added to cache.
     * Generates a new batch of hashes using generator.generateBatch().
     */
    public void fillCache(){
        boolean isLocked = lock.tryLock();
        if (!isLocked){
            log.info("Cache is locked by {}", Thread.currentThread().getName());
            try{
                hashRepository.getHashBatch(cacheSize - cache.size()).forEach(this::addHashToCache);
                hashGenerator.generateBatch();
                log.info("Cache filled");
            } finally {
                lock.unlock();
                log.info("Cache is unlocked by {}", Thread.currentThread().getName());
            }
        }
    }

    public void addHashToCache(String hash){
        try {
            cache.put(hash);
        } catch (InterruptedException e) {
            log.error("Cache interrupted while waiting method put() ", e);
            throw new RuntimeException(e);
        }
    }
}
