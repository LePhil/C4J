package de.vksi.c4j.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import javassist.ClassPool;
import javassist.CtClass;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.contracts.ContractRegistry;
import de.vksi.c4j.internal.transformer.affected.AffectedClassTransformer;
import de.vksi.c4j.internal.transformer.contract.ContractClassTransformer;
import de.vksi.c4j.internal.types.ListOrderedSet;

public class RootTransformerTest {

	private RootTransformer transformer;
	private AffectedClassTransformer targetClassTransformer;
	private ContractClassTransformer contractClassTransformer;
	private ClassPool pool;
	private CtClass targetClass;
	private CtClass contractClass;

	@Before
	public void before() throws Exception {
		transformer = RootTransformer.INSTANCE;
		transformer.init();
		targetClassTransformer = mock(AffectedClassTransformer.class);
		transformer.targetClassTransformer = targetClassTransformer;
		contractClassTransformer = mock(ContractClassTransformer.class);
		transformer.contractClassTransformer = contractClassTransformer;
		pool = ClassPool.getDefault();
		targetClass = pool.get(TargetClass.class.getName());
		contractClass = pool.get(ContractClass.class.getName());
	}

	@Test
	public void testTransformClassInterface() throws Exception {
		assertNull(transformer.transformType(pool.get(EmptyInterface.class.getName())));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testTransformClassTargetClass() throws Exception {
		assertNotNull(transformer.transformType(pool.get(TargetClass.class.getName())));
		assertEquals(targetClass, ContractRegistry.INSTANCE.getContractInfo(contractClass).getTargetClass());
		assertEquals(contractClass, ContractRegistry.INSTANCE.getContractInfo(contractClass).getContractClass());
		verify(targetClassTransformer).transform(any(ListOrderedSet.class),
				argThat(new ArgumentMatcher<ListOrderedSet<ContractInfo>>() {
					@Override
					public boolean matches(Object argument) {
						ListOrderedSet<ContractInfo> contractInfos = (ListOrderedSet<ContractInfo>) argument;
						return contractInfos != null && contractInfos.size() == 1
								&& contractInfos.iterator().next().getTargetClass().equals(targetClass)
								&& contractInfos.iterator().next().getContractClass().equals(contractClass);
					}
				}), eq(targetClass));
	}

	@Test
	public void testTransformClassContractClass() throws Exception {
		ContractRegistry.INSTANCE.registerContract(targetClass, contractClass);
		assertNotNull(transformer.transformType(pool.get(ContractClass.class.getName())));
		verify(contractClassTransformer).transform(argThat(new ArgumentMatcher<ContractInfo>() {
			@Override
			public boolean matches(Object argument) {
				ContractInfo contractInfo = (ContractInfo) argument;
				return contractInfo != null && contractInfo.getTargetClass().equals(targetClass)
						&& contractInfo.getContractClass().equals(contractClass);
			}
		}), eq(contractClass));
	}

	@Test
	public void testTransformClassUninvolvedClass() throws Exception {
		assertNotNull(transformer.transformType(pool.get(UninvolvedClass.class.getName())));
	}

	public interface EmptyInterface {
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass {
	}

	private static class ContractClass extends TargetClass {
	}

	private static class UninvolvedClass {
	}

	@ContractReference(SuperClassContract.class)
	private static class SuperClass implements HasContract {
	}

	private static class SuperClassContract extends SuperClass {
	}

	@ContractReference(HasContractContract.class)
	private interface HasContract extends SuperInterface1, SuperInterface2 {
	}

	private static class HasContractContract implements HasContract {
	}

	@ContractReference(SuperInterface1Contract.class)
	private interface SuperInterface1 {
	}

	private static class SuperInterface1Contract implements SuperInterface1 {
	}

	@ContractReference(SuperInterface2Contract.class)
	private interface SuperInterface2 {
	}

	private interface SuperInterface2Contract {
	}

}
