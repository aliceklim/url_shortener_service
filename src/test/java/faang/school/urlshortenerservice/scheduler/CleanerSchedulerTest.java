package faang.school.urlshortenerservice.scheduler;

import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class CleanerSchedulerTest {
    @Mock
    private UrlRepository urlRepository;
    @Mock
    private HashRepository hashRepository;
    @InjectMocks
    private CleanerScheduler cleanerScheduler;

    @BeforeEach
    void setUp(){
        cleanerScheduler = new CleanerScheduler(urlRepository, hashRepository);
    }

    @Test
    void deleteOldUrls(){
        Url url = Url.builder().url("url.com").build();
        List<Url> deletedUrls = List.of(url);
        when(urlRepository.deleteUrlsOlderThanOneYear()).thenReturn(deletedUrls);

        cleanerScheduler.deleteOldUrls();

        verify(hashRepository).saveAll(anyList());
    }
}