package est.oremi.backend12.bookingfresh.domain.consumer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/protected")
    public ResponseEntity<String> getProtectedData() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return ResponseEntity.ok("안녕하세요, " + username + "님! 보호된 데이터에 접근 성공하셨습니다.");
    }
}