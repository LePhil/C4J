package de.vksi.c4j.systemtest.pure;

import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.vksi.c4j.Pure;
import de.vksi.c4j.internal.runtime.PureEvaluator;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class PurePassingParametersToUnpureMethodsSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testPurePassingParametersToUnpureMethods() {
		expectedException.expect(AssertionError.class);
		new TargetClass().move(3);
		assertTrue(PureEvaluator.isUnpureCacheEmpty());
	}

	@Test
	public void testStaticPurePassingParametersToUnpureMethods() {
		expectedException.expect(AssertionError.class);
		TargetClass.moveStatic(3);
		assertTrue(PureEvaluator.isUnpureCacheEmpty());
	}

	@Test
	public void testDoubleRegistration() {
		expectedException.expect(AssertionError.class);
		new TargetClass().doubleRegistration(3);
		assertTrue(PureEvaluator.isUnpureCacheEmpty());
	}

	@SuppressWarnings("unused")
	private static class TargetClass {
		private Position position = new Position();
		private static Position positionStatic = new Position();

		public int getPosition() {
			return position.getValue();
		}

		@Pure
		public void doubleRegistration(int value) {
			doNothing();
			PositionChanger changer = new PositionChanger(position);
			changer.updatePosition(value);
		}

		@Pure
		public void doNothing() {
		}

		@Pure
		public void move(int value) {
			PositionChanger changer = new PositionChanger(position);
			changer.updatePosition(value);
		}

		@Pure
		public static void moveStatic(int value) {
			PositionChanger changer = new PositionChanger(positionStatic);
			changer.updatePosition(value);
		}
	}

	private static class Position {
		private int value;

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}
	}

	private static class PositionChanger {
		private Position position;

		public PositionChanger(Position position) {
			this.position = position;
		}

		public void updatePosition(int value) {
			position.setValue(value);
		}
	}
}
