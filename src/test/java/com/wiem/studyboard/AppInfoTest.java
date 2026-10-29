package com.wiem.studyboard;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class AppInfoTest {

    @Test
    void versionIsFilteredByMaven() {
        assertFalse(AppInfo.version().isBlank());
        assertFalse(AppInfo.version().contains("${"));
    }

    @Test
    void buildNumberIsFilteredByMaven() {
        assertFalse(AppInfo.build().isBlank());
        assertFalse(AppInfo.build().contains("${"));
    }
}
