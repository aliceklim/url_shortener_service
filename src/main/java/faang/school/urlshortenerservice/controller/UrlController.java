package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.service.UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import redis.clients.jedis.exceptions.InvalidURIException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

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

    @GetMapping("{hash}")
    @ResponseStatus(HttpStatus.FOUND)
    public RedirectView redirectToOriginalUrl(@PathVariable String hash){
        Url originalUrl = urlService.getOriginalUrl(hash);
        log.info("Endpoint <redirectToOriginalUrl>, uri='api/v1/url/{}' was called", hash);
        return new RedirectView(Objects.requireNonNullElse(originalUrl, "/").toString());
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