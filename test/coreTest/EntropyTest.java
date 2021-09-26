package coreTest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import mlcore.Entropy;

/**
 * Tests entropy class.
 * 
 * @author Murat Ali Bayir.
 *
 */
@Testable
class EntropyTest {

	@Test
	@DisplayName("Non Zero Entropy")
	void testEntropyNonZero() {
		var entropy = new Entropy();
		assertEquals(0.6931, entropy.computeEntropy(1, 1), Math.pow(10, -4));
	}

	@Test
	@DisplayName("Zero Entropy")
	void testEntropyZero() {
		var entropy = new Entropy();
		assertEquals(0.0, entropy.computeEntropy(0, 0), Math.pow(10, -6));
	}
}
