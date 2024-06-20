package ru.nms.crdt_server.benchmark;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import ru.nms.crdt_server.dto.InsertDto;
import ru.nms.crdt_server.dto.ReadyDto;
import ru.nms.crdt_server.dto.TextDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class CrdtClientTest {

    private final Random random = new Random(20);
    private static final RestTemplate restTemplate = new RestTemplate();

    private final int[] ports = {8081, 8082, 8083, 8084};
    private static HttpHeaders headers;

    @BeforeAll
    static void initHeaders() {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

    }

    @BeforeEach
    void clear() {
        for (int port : ports) {
            restTemplate.postForObject(createUrl(port, "/clear"), null, Void.class);
        }
    }

    @Test
    void insertRandomTextTest() throws InterruptedException {
        int textLength = random.nextInt(10, 15);
        for (int i = 0; i < textLength; i++) {
            var dto = new InsertDto(random.nextInt(textLength), nextChar());
            var port = ports[random.nextInt(ports.length)];
            HttpEntity<InsertDto> request = new HttpEntity<>(dto, headers);
            restTemplate.postForObject(createUrl(port, "/insert"), request, Void.class);
            Thread.sleep(random.nextInt(0, 3000));
        }
        gatherResults();
    }

    @Test
    void insertAlwaysAsFirstSymbol() throws InterruptedException {
        int textLength = random.nextInt(10, 20);
        for (int i = 0; i < textLength; i++) {
            var dto = new InsertDto(0, nextChar());
            var port = ports[random.nextInt(ports.length)];
            HttpEntity<InsertDto> request = new HttpEntity<>(dto, headers);
            restTemplate.postForObject(createUrl(port, "/insert"), request, Void.class);
            Thread.sleep(random.nextInt(0, 3000));
        }
        gatherResults();

    }


    @Test
    void insertAlwaysAsLastSymbol() throws InterruptedException {
        int textLength = random.nextInt(100, 200);

        for (int i = 0; i < textLength; i++) {
            var dto = new InsertDto(textLength, nextChar());
            var port = ports[random.nextInt(ports.length)];
            HttpEntity<InsertDto> request = new HttpEntity<>(dto, headers);
            restTemplate.postForObject(createUrl(port, "/insert"), request, Void.class);
            Thread.sleep(random.nextInt(0, 3000));
        }
        gatherResults();
    }

    private void gatherResults() throws InterruptedException {
        while (!nodesAreReady(ports)) {
            Thread.sleep(5000);
        }
        List<String> textsFromNodes = new ArrayList<>();
        for (int port : ports) {
            var dto = restTemplate.getForObject(createUrl(port, "/state"), TextDto.class);
            if (dto != null) {
                textsFromNodes.add(dto.getText());
                log.info("Test from {}: {}", port, dto.getText());
            }
        }

        assertEquals(ports.length, textsFromNodes.size());
        assertTrue(textsFromNodes.stream().allMatch(text -> text.equals(textsFromNodes.getFirst())));
    }


    private char nextChar() {
        return (char) (random.nextInt(26) + 'a');
    }

    private String createUrl(int port, String endpoint) {
        return "http://localhost:" + port + endpoint;
    }

    private boolean nodesAreReady(int[] ports) {
        for (int port : ports) {
            var dto = restTemplate.getForObject(createUrl(port, "/ready"), ReadyDto.class);
            if (dto == null || !dto.isReady()) {
                return false;
            }
        }
        return true;
    }

}
