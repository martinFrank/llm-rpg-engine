package com.github.martinfrank.elitegames.llmrpgengine.session;

import org.junit.jupiter.api.Test;

import java.util.UUID;

public class UUIDTest {

    @Test
    void UUIDTest() {
        for(int i = 0; i < 10; i ++){
            System.out.println(UUID.randomUUID());
        }
    }
}
