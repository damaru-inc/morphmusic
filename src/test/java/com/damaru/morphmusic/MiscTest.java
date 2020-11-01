package com.damaru.morphmusic;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MiscTest {

    @Test
    public void randomizeTest() {
        int numNums = 10;
        List<Integer> ret = IntStream.rangeClosed(1, numNums).boxed().collect(Collectors.toList());
        Collections.shuffle(ret);
        System.out.println(ret);
    }
}
