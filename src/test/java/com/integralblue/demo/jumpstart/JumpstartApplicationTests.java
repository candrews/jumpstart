package com.integralblue.demo.jumpstart;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
/* default */ class JumpstartApplicationTests {
	@Autowired
	private JumpstartApplication application;

	@Test
	/* default */ void contextLoads() {
		assertThat(application).isNotNull();
		// do nothing; this test just ensure that the Spring context loads
	}
}
