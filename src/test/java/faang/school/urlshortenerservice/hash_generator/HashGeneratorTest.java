package faang.school.urlshortenerservice.hash_generator;

import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.service.Base62Encoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class HashGeneratorTest {
    @Mock
    private HashRepository hashRepository;
    @Mock
    private Base62Encoder base62Encoder;
    @InjectMocks
    private HashGenerator hashGenerator;

    @BeforeEach
    void setUp(){
        hashGenerator = new HashGenerator(hashRepository, base62Encoder);
    }

    @Test
    void generateBatchTest(){
        int uniqueNumberRange = 5;
        List<Long> uniqueNumbers = List.of(1L, 2L, 3L, 4L, 5L);

        hashGenerator.generateBatch();

        lenient().when(hashRepository.getUniqueNumbers(uniqueNumberRange)).thenReturn(uniqueNumbers);
        verify(hashRepository, times(1)).saveAll(anyCollection());
    }
}
