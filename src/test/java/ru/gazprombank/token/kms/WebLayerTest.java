package ru.gazprombank.token.kms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import ru.gazprombank.token.kms.entity.Dto.KeyDataDto;
import ru.gazprombank.token.kms.entity.KeyStatus;
import ru.gazprombank.token.kms.entity.KeyType;
import ru.gazprombank.token.kms.entity.PurposeType;
import ru.gazprombank.token.kms.service.KeyDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class WebLayerTest {

    @Autowired
    private MockMvc mockMvc;

    @LocalServerPort
    private int port;

    // ObjectMapper mapper = new ObjectMapper();
    ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();

    private static Random r = new Random();

    // заглушки бинов
    @MockBean
    private KeyDataService keyDataService;

    // @MockBean
    // private UserValidator userValidator;

    @Test
    @WithUserDetails(value = "user")
    public void shouldReturnKeyDataList() throws Exception {
        // given

        // when
        when(keyDataService.listAll()).thenReturn(keyDataList());  // задачи для сервиса
        // doNothing().when(userValidator).validate(Object.class, Errors.class.newInstance()); // задачи для сервиса

        // then
        this.mockMvc.perform(get("/keys")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(jsonKeyDataList()));
    }

    private List<KeyDataDto> keyDataList() {
        List<KeyDataDto> keys = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            // keys.add(new KeyData("alias" + r.nextInt(1000), "AES", KeyType.PRIVATE, PurposeType.KEK, KeyStatus.ENABLED));
            keys.add(new KeyDataDto());
        }
        return keys;
    }

    private String jsonKeyDataList() throws JsonProcessingException {
        return mapper.writeValueAsString(keyDataList());
    }

    @Test
    @WithUserDetails(value = "master")
    public void shouldReturnMasterKey() throws Exception {
        // given

        // when
        when(keyDataService.generateMasterKey(null, null, null, null,
                "password1".toCharArray(), null)).thenReturn(
                // new KeyData("alias" + r.nextInt(1000), "RSA", KeyType.PRIVATE, PurposeType.KEK, KeyStatus.ENABLED)
                new KeyDataDto().setAlias("alias" + r.nextInt(1000)).setKey("RSA").setKeyType(KeyType.PRIVATE)
                        .setPurposeType(PurposeType.KEK).setStatus(KeyStatus.ENABLED)
        );  // задачи для сервиса
        // doNothing().when(userValidator).validate(Object.class, Errors.class.newInstance()); // задачи для сервиса

        // then
        this.mockMvc.perform(post("/keys")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(jsonKeyDataList()));
    }
}
