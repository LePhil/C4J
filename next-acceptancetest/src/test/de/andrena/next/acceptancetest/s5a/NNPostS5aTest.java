package de.andrena.next.acceptancetest.s5a;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.next.acceptancetest.subinterfaces.VeryBottom;
import de.andrena.next.systemtest.TransformerAwareRule;

public class NNPostS5aTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void returnsValueWhenPostConditionIsSatisfied() {
		assertThat(new VeryBottom(0).post(""), is(0));
	}
	
	@Test
	public void failsWhenPostConditionIsNotMet() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("result >= 0");
		new VeryBottom(-2).post("");
	}
}
