package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.mapper.UrlMapper;
import faang.school.urlshortenerservice.repository.UrlRedisCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static io.lettuce.core.ShutdownArgs.Builder.save;

@Service
@Slf4j
@RequiredArgsConstructor
public class UrlService {

    private final HashCash hashCash;
    private final UrlRedisCacheRepository urlRedisCacheRepository;
    private final UrlMapper urlMapper;
    private final UrlRepository urlRepository;
    @Value("$(url.shortener.address)")
    private String serverAddress;
    private final String URL_REGEX = "^(https?://)";

    @Transactional
    public UrlDto shortenUrl(UrlDto urlDto){
        String hash = hashCash.getHash();
        String url = urlDto.getUrl();

        urlDto.setUrl(formatUrl(url));
        Url entityToSave = urlMapper.toEntity(urlDto);
        entityToSave.setHash(hash);

        Url savedEntity = urlRepository.save(entityToSave);
        log.info("Link {} and its hash have been saved to DB", urlDto.getUrl());

        urlRedisCacheRepository.save(savedEntity);
        log.info("Link {} and its hash have been saved to Redis", urlDto.getUrl());
        return urlMapper.toDto(savedEntity);

    }

    private String formatUrl(String url){
        String formattedUrl = url.trim()
                .replaceFirst(URL_REGEX, "");
        return formattedUrl.endsWith("/") ? formattedUrl.substring(0, formattedUrl.length() - 1) : formattedUrl;
    }
}
