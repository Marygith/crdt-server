package ru.nms.crdt_server;

import org.springframework.web.client.RestTemplate;

import java.util.Random;

public class ClientTest {

    private static Random random = new Random(100);
    private static RestTemplate restTemplate = new RestTemplate();
    public static void main(String[] args) {



    }

    private static char nextChar() {
        return (char)(random.nextInt(26) + 'a');
    }
}
