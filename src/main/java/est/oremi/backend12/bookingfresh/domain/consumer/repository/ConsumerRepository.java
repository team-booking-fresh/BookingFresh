package est.oremi.backend12.bookingfresh.domain.consumer.repository;

import est.oremi.backend12.bookingfresh.domain.consumer.entity.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsumerRepository extends JpaRepository<Consumer, Long> {
    Optional<Consumer> findByEmail(String email); // 로그인
    boolean existsByEmail(String email); // 중복 이메일 감지
    boolean existsByNickname(String nickname); // 중복 닉네임 감지
}