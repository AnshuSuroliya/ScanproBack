package com.mavericks.scanpro;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ScanproApplicationTests {

	@Test
	void contextLoads() {
		assertTrue(true);
	}

	@Test
	void mainMethodRunsSuccessfully() {
		ScanproApplication.main(new String[]{});
		assertTrue(true);
	}

}
