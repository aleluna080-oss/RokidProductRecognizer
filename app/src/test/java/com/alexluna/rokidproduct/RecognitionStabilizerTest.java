package com.alexluna.rokidproduct;

import org.junit.Test;
import static org.junit.Assert.*;

public class RecognitionStabilizerTest {
    @Test public void requiresConsecutiveHitsAndExpires() {
        RecognitionStabilizer filter = new RecognitionStabilizer();
        assertFalse(filter.accept("Botella", 0));
        assertFalse(filter.accept("Botella", 450));
        assertTrue(filter.accept("Botella", 900));
        assertFalse(filter.expire(3399));
        assertTrue(filter.expire(3400));
        assertFalse(filter.accept("Botella", 3500));
    }

    @Test public void missingOrChangedLabelBreaksConsecutiveMatches() {
        RecognitionStabilizer filter = new RecognitionStabilizer();
        assertFalse(filter.accept("Botella", 0));
        assertFalse(filter.accept("Botella", 450));
        assertFalse(filter.accept(null, 900));
        assertFalse(filter.accept("Botella", 1350));
        assertFalse(filter.accept("Caja", 1800));
        assertFalse(filter.accept("Caja", 2250));
        assertTrue(filter.accept("Caja", 2700));
        filter.reset();
        assertFalse(filter.accept("Caja", 3150));
    }

    @Test public void mapperDoesNotConfuseCanWithOtherWords() {
        assertEquals("Lata", ProductNameMapper.toDisplayName("Can"));
        assertEquals("Candle", ProductNameMapper.toDisplayName("Candle"));
        assertEquals("Botella", ProductNameMapper.toDisplayName(" Bottle "));
        assertEquals("Objeto", ProductNameMapper.toDisplayName(null));
    }
}
