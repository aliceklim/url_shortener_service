package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, String> {
    Optional<Url> findByHash(String hash);

    @Query(nativeQuery = true, value = """
            DELETE FROM url
            WHERE hash IN (
              SELECT hash 
              FROM url 
              WHERE created_at < current_timestamp - INTERVAL '1 year'
              )
            RETURNING *;           
            """)
    List<Url> deleteUrlsOlderThanOneYear();
}