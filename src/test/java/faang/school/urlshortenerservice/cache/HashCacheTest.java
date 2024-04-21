package faang.school.urlshortenerservice.cache;

import faang.school.urlshortenerservice.hash_generator.HashGenerator;
import faang.school.urlshortenerservice.repository.HashRepository;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HashCacheTest {
    @Mock
    private HashRepository hashRepository;
    @Mock
    private HashGenerator hashGenerator;
    private BlockingQueue<String> cache;
    @Mock
    private Executor hashCacheThreadPool;
    @InjectMocks
    private HashCache hashCache;
    private long cachePollTimeout;
    private int cacheSize;
    private int minFill;
    @Mock
    private Lock lock = new ReentrantLock();
    @BeforeEach
    void setUp(){
        cacheSize = 10;
        minFill = 5;
        cachePollTimeout = 1000L;
        cache = new ArrayBlockingQueue<>(cacheSize);
        hashCache = new HashCache(hashRepository, hashGenerator, hashCacheThreadPool);
        hashCache.setCacheSize(cacheSize);
        hashCache.setMinFill(minFill);
        hashCache.setCachePollTimeout(cachePollTimeout);
        hashCache.setCache(cache);
    }

    @Test
    void getHashMinLevelReachedTest(){
        hashCache.setCacheSize(30);
        cache.add("TestHash");

        String hash = hashCache.getHash();

        assertEquals("TestHash", hash);
        verify(hashCacheThreadPool, times(1)).execute(any(Runnable.class));
    }

    @Test
    void getHashMinLevelNotReachedTest(){
        hashCache.setCacheSize(1);
        cache.add("TestHash");

        String hash = hashCache.getHash();

        assertEquals("TestHash", hash);
        verify(hashCacheThreadPool, times(0)).execute(any(Runnable.class));
    }

    @Test
    void fillCacheLockFalseTest(){
        boolean isLocked = true;
        if (!isLocked) {
            try {
                when(hashRepository.getHashBatch(anyInt())).thenReturn(Arrays.asList("hash1", "hash2", "hash3"));
                hashCache.fillCache();
                verify(hashRepository, times(1)).getHashBatch(anyInt());
                verify(hashGenerator, times(1)).generateBatch();
            } finally {
                verify(lock, times(1)).unlock();
            }
        }
    }

    @Test
    void fillCacheLockTrueTest(){
        boolean isLocked = true;
        if (isLocked) {
           verify(hashRepository,times(0)).getHashBatch(anyInt());
           verify(hashGenerator, times(0)).generateBatch();
           verify(lock, times(0)).unlock();
        }
    }

    @Test
    void addHashToCashTest(){
        String hash = "hash";

        try {
            cache.put(hash);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Assert.assertTrue(cache.contains(hash));
    }
}
