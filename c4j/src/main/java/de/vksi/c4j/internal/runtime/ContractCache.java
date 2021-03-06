package de.vksi.c4j.internal.runtime;

import de.vksi.c4j.internal.types.ObjectMapper;
import de.vksi.c4j.internal.types.Pair;

public class ContractCache {
	private static final ObjectMapper<ContractCacheEntry, Object> contractCache = new ObjectMapper<ContractCacheEntry, Object>();

	private static class ContractCacheEntry extends Pair<Class<?>, Class<?>> {
		public ContractCacheEntry(Class<?> contractClass, Class<?> callingClass) {
			super(contractClass, callingClass);
		}
	}

	public static Object getContractFromCache(Object target, Class<?> contractClass, Class<?> callingClass)
			throws InstantiationException, IllegalAccessException {
		if (target == null) {
			return null;
		}
		ContractCacheEntry classPair = new ContractCacheEntry(contractClass, callingClass);
		if (contractCache.contains(target, classPair)) {
			return contractCache.get(target, classPair);
		}
		Object contract = contractClass.newInstance();
		contractCache.put(target, classPair, contract);
		return contract;
	}
}
