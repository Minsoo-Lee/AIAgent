package roadmap.aiagent.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String USER = "testUser";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(
                "test-secret-key-for-jwt-signing-must-be-long-enough",
                86_400_000L
        );
    }

    @Test
    void 토큰_생성_후_사용자명_추출() {
        String token = jwtUtil.generateToken(USER);
        String username = jwtUtil.extractUsername(token);

        assertThat(username).isEqualTo(USER);
    }

    @Test
    void 유효한_토큰_검증_성공() {
        String token = jwtUtil.generateToken(USER);
        boolean isValid = jwtUtil.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    void 잘못된_토큰_검증_실패() {
        String inValidToken = "invalid.token.value";
        boolean isValid = jwtUtil.validateToken(inValidToken);

        assertThat(isValid).isFalse();
    }
}
