package faang.school.urlshortenerservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "url")
public class Url {
    @Id
    @Column(name = "hash")
    private String hash;

    @Column(name = "url")
    private String url;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Url url1 = (Url) o;
        return Objects.equals(hash, url1.hash) && Objects.equals(url, url1.url) && Objects.equals(createdAt, url1.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, url, createdAt);
    }
}