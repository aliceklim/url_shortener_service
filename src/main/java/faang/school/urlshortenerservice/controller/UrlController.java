package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.service.UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.exceptions.InvalidURIException;
import java.net.MalformedURLException;
import java.net.URL;

@Service
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/url/")
public class UrlController {

    private final UrlService urlService;

    @PostMapping
    public UrlDto shortenLink(@RequestBody @Validated UrlDto urlDto){
        log.info("Endpoint <shortenLink>, uri='api/v1/url/{}' was called", urlDto.getUrl());
        validateUrl(urlDto.getUrl());

        return urlService.shortenUrl(urlDto);
    }

    private void validateUrl(String url){
        try{
            new URL(url);
            log.info("URL {} is validated", url);
        } catch (MalformedURLException e){
            throw new InvalidURIException("Invalid url format");
        }
    }

}
