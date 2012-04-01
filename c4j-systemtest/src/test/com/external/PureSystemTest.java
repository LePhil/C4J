package com.external;

import static de.andrena.c4j.Condition.ignored;
import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.pre;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.Condition;
import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;
import de.andrena.c4j.Target;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class PureSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testMA() {
		A a = new A();
		a.mA(4, 6);
	}

	@ContractReference(AContract.class)
	public static class A {
		private int z;

		public int mA(int x, int y) {
			z = x + y;
			return z;
		}

		@Pure
		public int getZ() {
			return z;
		}

	}

	public static class AContract extends A {

		@Target
		private A target;

		@Override
		public int mA(int x, int y) {
			if (pre()) {
				// System.out.println("pre z = " + target.getZ());
				assert x > 3 : "x > 3";
				assert y > 4 : "y > 4";
			}
			if (post()) {
				// System.out.println("post z = " + target.getZ());
				Integer result = Condition.result(Integer.class);
				assert result.intValue() == (x + y) : "result == x + y";
			}
			return ignored();
		}
	}
}