package roadmap.aiagent.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.http.MediaType.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 올바른_로그인_정보로_토큰_발급() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(APPLICATION_JSON_VALUE)
                .content("""
                        {"username":"user","password":"password"}
                        """))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());
    }

    @Test
    void 잘못된_로그인_정보는_401_반환() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content("""
                        {"username":"wrong","password":"wrong"}
                        """))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}
