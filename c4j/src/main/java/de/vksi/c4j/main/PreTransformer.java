package de.vksi.c4j.main;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javassist.CtClass;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.internal.RootTransformer;
import de.vksi.c4j.internal.classfile.ClassFilePool;

/**
 * EXPERIMENTAL!
 * <p>
 * Allows transforming all .class-files in a source directory to a destination directory, issuing the transformation C4J
 * would otherwise do during runtime. Use this class with the usual C4J JVM Arguments, where the configuration to be
 * used can also be specified. After transformation, the generated .class-files should be able to be used without the
 * -javaagent JVM argument. The -ea argument will still be necessary in order to enable the assert statement.
 */
public class PreTransformer {
	private static final Logger LOGGER = Logger.getLogger(PreTransformer.class);
	private RootTransformer rootTransformer = RootTransformer.INSTANCE;

	private final File sourceDir;
	private final File destinationDir;

	public PreTransformer(File sourceDir, File destinationDir) throws Exception {
		this.sourceDir = sourceDir;
		this.destinationDir = destinationDir;
	}

	public void transformAllClassFiles() throws Exception {
		Set<CtClass> classFiles = searchClassFiles(sourceDir);
		Set<CtClass> contractClassFiles = getContractClasses(classFiles);
		classFiles.removeAll(contractClassFiles);
		LOGGER.info("transforming non-contracts");
		transformClasses(classFiles);
		LOGGER.info("transforming contracts");
		transformClasses(contractClassFiles);
	}

	private Set<CtClass> getContractClasses(Set<CtClass> classFiles) throws NotFoundException, ClassNotFoundException {
		Set<CtClass> contractClassFiles = new HashSet<CtClass>();
		for (CtClass clazz : classFiles) {
			if (clazz.hasAnnotation(ContractReference.class)) {
				contractClassFiles.add(ClassFilePool.INSTANCE.getClassFromAnnotationValue(clazz,
						ContractReference.class, "value"));
			}
		}
		return contractClassFiles;
	}

	private Set<CtClass> searchClassFiles(File directory) throws Exception {
		Set<CtClass> classes = new HashSet<CtClass>();
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				classes.addAll(searchClassFiles(file));
			} else if (file.getName().endsWith(".class")) {
				classes.add(ClassFilePool.INSTANCE.createClass(file));
			}
		}
		return classes;
	}

	private void transformClasses(Set<CtClass> classes) throws Exception {
		for (CtClass clazz : classes) {
			transformClass(clazz);
		}
	}

	private void transformClass(CtClass clazz) throws Exception {
		try {
			rootTransformer.transformType(clazz);
			clazz.writeFile(destinationDir.getAbsolutePath());
			LOGGER.info("transformed " + clazz.getName() + " to " + destinationDir.getAbsolutePath());
		} catch (Exception e) {
			LOGGER.fatal("failed to transform " + clazz.getName(), e);
		}
	}
}
