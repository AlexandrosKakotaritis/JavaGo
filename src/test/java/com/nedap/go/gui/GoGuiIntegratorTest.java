package com.nedap.go.gui;



import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Example test class
 */
public class GoGuiIntegratorTest {

	@Test
	public void boardSizeTest() {
		GoGuiIntegrator goGuiIntegrator = new GoGuiIntegrator(true, true, 10);
		assertEquals(10, goGuiIntegrator.getBoardSize());
	}

}
