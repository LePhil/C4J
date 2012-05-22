package de.andrena.c4j.acceptancetest.s2;

import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.unchanged;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Target;
import de.andrena.c4j.acceptancetest.floatingwindow.Vector;
import de.andrena.c4j.acceptancetest.floatingwindow.WindowStyle;
import de.andrena.c4j.acceptancetest.point.Color;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class NNUnchangedS2Test {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testUnchangedViolation() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("illegal access on unpure method or field");

		Window window = new Window(new Vector(0, 0));
		window.setStyleColor(Color.BLUE);
	}

	@ContractReference(WindowContract.class)
	public class Window {
		public Vector upperLeftCorner;
		public WindowStyle style;

		public Window(Vector upperLeftCorner) {
			this.upperLeftCorner = upperLeftCorner;
			style = new WindowStyle();
		}

		public void setStyleColor(Color color) {
			style.color = color;
			//postcondition violation: upperLeftCorner changed
			upperLeftCorner.setX(5);
		}
	}

	public class WindowContract extends Window {
		@Target
		private Window target;

		public WindowContract(Vector upperLeftCorner) {
			super(upperLeftCorner);
		}

		@Override
		public void setStyleColor(Color color) {
			if (post()) {
				assert target.style.color.equals(color) : "color set";
				assert unchanged(target.upperLeftCorner) : "upperLeftCorner unchanged";
			}
		}
	}
}