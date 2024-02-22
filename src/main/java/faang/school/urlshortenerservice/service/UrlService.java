package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.cache.HashCache;
import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.HashNotFoundException;
import faang.school.urlshortenerservice.mapper.UrlMapper;
import faang.school.urlshortenerservice.repository.UrlRedisCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UrlService {

    private final HashCache hashCache;
    private final UrlRedisCacheRepository urlRedisCacheRepository;
    private final UrlMapper urlMapper;
    private final UrlRepository urlRepository;
    @Value("$(url.shortener.address)")
    private String serverAddress;
    private final String URL_REGEX = "^(https?://)";

    @Transactional
    public UrlDto shortenUrl(UrlDto urlDto){
        String hash = hashCache.getHash();
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

    public Url getOriginalUrl (String hash){
        Url cachedUrl = urlRedisCacheRepository.get(hash).orElse(null);
        if (cachedUrl == null){
            log.info("hash {} not found in cache", hash);
            cachedUrl = urlRepository.findByHash(hash)
                    .orElseThrow(() -> new HashNotFoundException(String.format("hash {} not found in DB", hash)));
        }

        log.info("hash {} found", hash);
        return cachedUrl;
    }

    private String formatUrl(String url){
        String formattedUrl = url.trim()
                .replaceFirst(URL_REGEX, "");
        return formattedUrl.endsWith("/") ? formattedUrl.substring(0, formattedUrl.length() - 1) : formattedUrl;
    }
}