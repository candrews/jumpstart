package com.integralblue.demo.jumpstart.controller;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
/* default */ class HomeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	/* default */ void testHomeAuthenticationRequired() throws Exception {
		mockMvc.perform(
				get("/")
				.with(anonymous())
				.secure(true))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser
	/* default */ void testHome() throws Exception {
		mockMvc.perform(
				get("/")
				.with(user("user"))
				.secure(true))
			.andExpect(status().isOk());
	}
}
