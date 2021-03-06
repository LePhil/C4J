package de.vksi.c4j.internal.transformer.affected;

import java.util.List;
import java.util.Map;

import javassist.CtBehavior;
import javassist.CtClass;
import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.contracts.ContractMethod;
import de.vksi.c4j.internal.types.ListOrderedSet;

public abstract class AbstractAffectedClassTransformer {
	public abstract void transform(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtClass affectedClass, Map<CtBehavior, List<ContractMethod>> contractMap) throws Exception;
}
