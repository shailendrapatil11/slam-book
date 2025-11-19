package com.slambook.api;

import com.slambook.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class SlamBookApiApplicationTests {

	@MockBean
	private FileStorageService fileStorageService;

	@Test
	void contextLoads() {
	}

}
