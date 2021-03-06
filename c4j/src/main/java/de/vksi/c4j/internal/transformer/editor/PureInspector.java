package de.vksi.c4j.internal.transformer.editor;

import static de.vksi.c4j.internal.classfile.ClassAnalyzer.getDeclaredMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import de.vksi.c4j.AllowPureAccess;
import de.vksi.c4j.Pure;
import de.vksi.c4j.PureTarget;
import de.vksi.c4j.internal.compiler.ArrayExp;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.compiler.StaticCallExp;
import de.vksi.c4j.internal.configuration.XmlConfigurationManager;
import de.vksi.c4j.internal.contracts.ContractInfo;
import de.vksi.c4j.internal.runtime.PureEvaluator;
import de.vksi.c4j.internal.transformer.util.AffectedBehaviorLocator;
import de.vksi.c4j.internal.types.ListOrderedSet;

public class PureInspector {
	private UnpureBehaviorExpressionEditor unpureBehaviorExpressionEditor = new UnpureBehaviorExpressionEditor();
	private AffectedBehaviorLocator affectedBehaviorLocator = new AffectedBehaviorLocator();
	private ArrayAccessEditor arrayAccessEditor = new ArrayAccessEditor();

	public CtMethod getPureOrigin(ListOrderedSet<CtClass> involvedClasses, ListOrderedSet<ContractInfo> contracts,
			CtMethod method) throws NotFoundException {
		for (CtClass involvedClass : involvedClasses) {
			CtMethod involvedMethod = getInvolvedMethod(method, involvedClass);
			if (involvedMethod != null
					&& (involvedMethod.hasAnnotation(Pure.class) || XmlConfigurationManager.INSTANCE.getConfiguration(
							method.getDeclaringClass()).getWhitelistMethods().contains(method))) {
				return involvedMethod;
			}
		}
		for (ContractInfo contract : contracts) {
			CtMethod contractMethod = affectedBehaviorLocator.getContractMethod(contract, method);
			if (contractMethod != null && contractMethod.hasAnnotation(PureTarget.class)) {
				return contractMethod;
			}
		}
		return null;
	}

	private CtMethod getInvolvedMethod(CtMethod affectedBehavior, CtClass involvedClass) throws NotFoundException {
		return getDeclaredMethod(involvedClass, affectedBehavior.getName(), affectedBehavior.getParameterTypes());
	}

	public void verify(CtMethod affectedBehavior, boolean allowOwnStateChange) throws CannotCompileException,
			NotFoundException {
		PureBehaviorExpressionEditor editor = new PureBehaviorExpressionEditor(affectedBehavior, this,
				allowOwnStateChange);
		affectedBehavior.instrument(editor);
		arrayAccessEditor.instrumentArrayAccesses(affectedBehavior);
		if (editor.getPureError() != null) {
			editor.getPureError().insertBefore(affectedBehavior);
		}
		verifyUnpureObjects(affectedBehavior);
	}

	private void verifyUnpureObjects(CtMethod affectedBehavior) throws NotFoundException, CannotCompileException {
		List<NestedExp> unpureObjects = new ArrayList<NestedExp>();
		boolean methodIsStatic = Modifier.isStatic(affectedBehavior.getModifiers());
		if (!methodIsStatic) {
			unpureObjects.add(NestedExp.THIS);
		}
		addParametersToUnpureObjects(affectedBehavior, unpureObjects);
		addFieldsToUnpureObjects(affectedBehavior, unpureObjects, methodIsStatic);
		registerUnpureObjects(affectedBehavior, unpureObjects);
	}

	private void addFieldsToUnpureObjects(CtMethod affectedBehavior, List<NestedExp> unpureObjects,
			boolean methodIsStatic) throws NotFoundException {
		for (CtField field : getAccessibleFields(affectedBehavior)) {
			if (!field.getType().isPrimitive() && (!methodIsStatic || Modifier.isStatic(field.getModifiers()))
					&& !field.hasAnnotation(AllowPureAccess.class)) {
				unpureObjects.add(NestedExp.field(field));
			}
		}
	}

	private void addParametersToUnpureObjects(CtMethod affectedBehavior, List<NestedExp> unpureObjects)
			throws NotFoundException {
		CtClass[] parameterTypes = affectedBehavior.getParameterTypes();
		for (int parameterIndex = 0; parameterIndex < parameterTypes.length; parameterIndex++) {
			CtClass paramType = parameterTypes[parameterIndex];
			if (!paramType.isPrimitive()) {
				unpureObjects.add(NestedExp.arg(parameterIndex + 1));
			}
		}
	}

	private void registerUnpureObjects(CtBehavior affectedBehavior, List<NestedExp> unpureObjects)
			throws CannotCompileException {
		if (unpureObjects.isEmpty()) {
			return;
		}
		ArrayExp unpureArray = new ArrayExp(Object.class, unpureObjects);
		new StaticCallExp(PureEvaluator.registerUnpure, unpureArray).insertBefore(affectedBehavior);
		new StaticCallExp(PureEvaluator.unregisterUnpure).insertFinally(affectedBehavior);
	}

	private Set<CtField> getAccessibleFields(CtBehavior affectedBehavior) {
		Set<CtField> accessibleFields = new HashSet<CtField>();
		for (CtField field : affectedBehavior.getDeclaringClass().getFields()) {
			if (isAccessibleField(affectedBehavior, field)) {
				accessibleFields.add(field);
			}
		}
		Collections.addAll(accessibleFields, affectedBehavior.getDeclaringClass().getDeclaredFields());
		return accessibleFields;
	}

	private boolean isAccessibleField(CtBehavior affectedBehavior, CtField field) {
		return !Modifier.isPackage(field.getModifiers())
				|| getPackageName(affectedBehavior).equals(getPackageName(field));
	}

	private String getPackageName(CtMember member) {
		return member.getDeclaringClass().getPackageName();
	}

	public void checkUnpureAccess(CtBehavior affectedBehavior) throws CannotCompileException {
		if (!XmlConfigurationManager.INSTANCE.isWithinRootPackages(affectedBehavior.getDeclaringClass())) {
			return;
		}
		affectedBehavior.instrument(unpureBehaviorExpressionEditor);
		if (Modifier.isStatic(affectedBehavior.getModifiers())) {
			return;
		}
		new StaticCallExp(PureEvaluator.checkUnpureAccess, NestedExp.THIS).insertBefore(affectedBehavior);
	}

	public void verifyUnchangeable(CtBehavior affectedBehavior, ListOrderedSet<ContractInfo> contracts)
			throws CannotCompileException {
		boolean containsUnchanged = false;
		for (ContractInfo contract : contracts) {
			if (contract.getMethodsContainingUnchanged().contains(affectedBehavior)) {
				containsUnchanged = true;
				break;
			}
		}
		if (containsUnchanged) {
			arrayAccessEditor.instrumentArrayAccesses(affectedBehavior);
		}
	}
}
