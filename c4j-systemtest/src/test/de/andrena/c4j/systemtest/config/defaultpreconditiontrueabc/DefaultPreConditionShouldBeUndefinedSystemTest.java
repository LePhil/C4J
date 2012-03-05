package de.andrena.c4j.systemtest.config.defaultpreconditiontrueabc;

import static de.andrena.c4j.Condition.pre;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.c4j.Contract;

public class DefaultPreConditionShouldBeUndefinedSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test(expected = AssertionError.class)
	public void testPreConditionUndefined() {
		new TargetClass().method(-1);
	}

	@Contract(ContractClass.class)
	public static class TargetClass extends SuperClass {
	}

	public static class ContractClass extends TargetClass {
		@Override
		public void method(int arg) {
			if (pre()) {
				assert arg > 0;
			}
		}
	}

	public static class SuperClass {
		public void method(int arg) {
		}
	}
}