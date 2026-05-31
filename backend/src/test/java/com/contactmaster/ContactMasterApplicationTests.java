package com.contactmaster;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ContactMasterApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void contactLifecycleSupportsDuplicateCheckRecycleBinAndDashboardStats() throws Exception {
        String username = "user" + System.nanoTime();
        String registerBody = mapper.writeValueAsString(Map.of(
                "username", username,
                "password", "123456",
                "email", username + "@example.com"
        ));

        JsonNode register = mapper.readTree(mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username", is(username)))
                .andReturn().getResponse().getContentAsString());
        String token = register.at("/data/token").asText();

        JsonNode groups = mapper.readTree(mockMvc.perform(get("/api/groups")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", is(4)))
                .andReturn().getResponse().getContentAsString());
        long groupId = groups.at("/data/0/id").asLong();

        String contactBody = mapper.writeValueAsString(Map.of(
                "name", "Test User",
                "phone", "13800138000",
                "email", "test@example.com",
                "groupId", groupId,
                "company", "Demo Company",
                "position", "Engineer",
                "address", "Shanghai",
                "birthday", "2000-06-03",
                "remark", "Smoke test",
                "favorite", true
        ));

        JsonNode created = mapper.readTree(mockMvc.perform(post("/api/contacts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contactBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorite", is(true)))
                .andReturn().getResponse().getContentAsString());
        long contactId = created.at("/data/id").asLong();

        mockMvc.perform(post("/api/contacts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contactBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("该手机号已存在")));

        mockMvc.perform(delete("/api/contacts/" + contactId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/contacts/recycle-bin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total", is(1)));

        mockMvc.perform(put("/api/contacts/" + contactId + "/restore")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/dashboard/statistics")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalContacts", is(1)))
                .andExpect(jsonPath("$.data.favoriteContacts", is(1)))
                .andExpect(jsonPath("$.data.recycleBinCount", is(0)));
    }
}
