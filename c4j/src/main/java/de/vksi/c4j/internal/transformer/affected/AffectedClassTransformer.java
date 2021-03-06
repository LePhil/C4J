package de.vksi.c4j.internal.transformer.affected;

import static de.vksi.c4j.internal.transformer.util.TransformationHelper.addClassAnnotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import de.vksi.c4j.internal.classfile.ClassFilePool;
import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.contracts.ContractMethod;
import de.vksi.c4j.internal.contracts.Transformed;
import de.vksi.c4j.internal.transformer.util.AffectedBehaviorLocator;
import de.vksi.c4j.internal.types.ListOrderedSet;

public class AffectedClassTransformer {
	private static final Logger LOGGER = Logger.getLogger(AffectedClassTransformer.class);

	private AffectedBehaviorLocator affectedBehaviorLocator = new AffectedBehaviorLocator();
	private AbstractAffectedClassTransformer[] transformers = new AbstractAffectedClassTransformer[] {
			// beware: PureTransformer has to run first!
			// InvariantTransformer has to run after DynamicConditionTransformer
			new PureTransformer(), new DynamicConditionTransformer(), new InvariantTransformer(),
			new StaticConditionTransformer() };

	public void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws Exception {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("transforming class " + affectedClass.getName());
		}
		Map<CtBehavior, List<ContractMethod>> contractMap = getContractMap(contracts, affectedClass);
		for (AbstractAffectedClassTransformer transformer : transformers) {
			transformer.transform(involvedClasses, contracts, affectedClass, contractMap);
		}
		addClassAnnotation(affectedClass, ClassFilePool.INSTANCE.getClass(Transformed.class));
	}

	protected Map<CtBehavior, List<ContractMethod>> getContractMap(ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass) throws NotFoundException, CannotCompileException {
		Map<CtBehavior, List<ContractMethod>> contractMap = new HashMap<CtBehavior, List<ContractMethod>>();
		for (ContractInfo contractInfo : contracts) {
			for (ContractMethod contractMethod : contractInfo.getMethods()) {
				CtBehavior affectedBehavior = affectedBehaviorLocator.getAffectedBehavior(contractInfo, affectedClass,
						contractMethod.getMethod());
				if (affectedBehavior != null) {
					if (!contractMap.containsKey(affectedBehavior)) {
						contractMap.put(affectedBehavior, new ArrayList<ContractMethod>());
					}
					contractMap.get(affectedBehavior).add(contractMethod);
				}
			}
		}
		return contractMap;
	}
}
