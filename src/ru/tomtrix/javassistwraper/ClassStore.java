package ru.tomtrix.javassistwraper;

import javassist.*;

import java.util.*;

/** This class is a simple wrapper for a powerful Java ASM/Bytecode Library "Javassist"
 * @author tom-trix */
@SuppressWarnings("unused")
public class ClassStore
{
	/** Singleton instance */
	private static ClassStore	_instance	= new ClassStore();

	/** Private singleton constructor */
	private ClassStore()
	{}

	/** @return a singleton instance */
	public static ClassStore getInstance()
	{
		return _instance;
	}

	private final static String	NO_SUCH_CLASS	= "There is no such a class";
	private final static String	CLASS_IS_FROZEN	= "Class is already loaded into JVM. The only thing you can still do is to add a new class on the base of the existing class\nP.S. Also you can use Javassist outright";

	/** Adds fields and methods to a specific CtClass (only for a code reuse, nothing special)
	 * @param clazz - CtClass to which new members are to be added
	 * @param fields - collection of fields' code
	 * @param methods - collection of methods' code
	 * @throws Exception */
	private void setFieldsAndMethods(CtClass clazz, Collection<String> fields, Collection<String> methods) throws Exception
	{
		if (fields != null) for (String s : fields)
			clazz.addField(CtField.make(s, clazz));
		if (methods != null) for (String s : methods)
			clazz.addMethod(CtMethod.make(s, clazz));
	}

	/** Adds a new class containing the specific fields and methods
	 * @param classname - name of the class (e.g. "ru.tomtrix.Animal")
	 * @param fields - collection of fields' code
	 * @param methods - collection of methods' code
	 * @throws Exception if something goes wrong */
	public void addClass(String classname, Collection<String> fields, Collection<String> methods) throws Exception
	{
		if (classname == null || classname.trim().isEmpty()) throw new IllegalArgumentException("Classname can't be NULL");
		setFieldsAndMethods(ClassPool.getDefault().makeClass(classname), fields, methods);
	}

	/** Adds a new class ON THE BASE OF THE EXISTING ONE</br>it also adds a new pack of fields and methods
	 * @param newClassname - name of the new class (e.g. "ru.tomtrix.Animal")
	 * @param oldClassname - name of the existing class (e.g. "String")
	 * @param fields - collection of fields' code
	 * @param methods - collection of methods' code
	 * @throws Exception if something goes wrong */
	public void addClass(String newClassname, String oldClassname, Collection<String> fields, Collection<String> methods) throws Exception
	{
		if (newClassname == null || newClassname.trim().isEmpty() || newClassname.equals(oldClassname)) throw new IllegalArgumentException(String.format("Wrong newClassname parameter: \"%s\"", newClassname));
		CtClass clazz = ClassPool.getDefault().getOrNull(oldClassname);
		if (clazz == null) throw new NullPointerException(NO_SUCH_CLASS);
		if (clazz.isFrozen()) clazz.defrost();
		clazz.setName(newClassname);
		setFieldsAndMethods(clazz, fields, methods);
	}

	/** Adds a field to a specific class
	 * @param classname - name of the class
	 * @param code - code (e.g. "<i>public int x = 8;</i>")
	 * @throws Exception if something goes wrong */
	public void addField(String classname, String code) throws Exception
	{
		CtClass clazz = ClassPool.getDefault().getOrNull(classname);
		if (clazz == null) throw new NullPointerException(NO_SUCH_CLASS);
		if (clazz.isFrozen()) throw new IllegalAccessException(CLASS_IS_FROZEN);
		clazz.addField(CtField.make(code, clazz));
	}

	/** Adds a method to a specific class
	 * @param classname - name of the class
	 * @param code - code (e.g. "<i>public void go() {System.out.println(\"Fuck me, baby!\");}</i>")
	 * @throws Exception if something goes wrong */
	public void addMethod(String classname, String code) throws Exception
	{
		CtClass clazz = ClassPool.getDefault().getOrNull(classname);
		if (clazz == null) throw new NullPointerException(NO_SUCH_CLASS);
		if (clazz.isFrozen()) throw new IllegalAccessException(CLASS_IS_FROZEN);
		clazz.addMethod(CtMethod.make(code, clazz));
	}

	/** Adds a classpath to the class loader (use this if your external class loader doesn't know about a class)
	 * @param clazz - reference to your class (e.g. <i>Dog.<b>class</b></i>) */
	public void addClassPath(Class<?> clazz)
	{
		ClassPool.getDefault().insertClassPath(new ClassClassPath(clazz));
	}

	/** Adds an import directive
	 * @param packageName - string of an import clause (e.g. "<i>java.utils</i>") */
	public void addImport(String packageName)
	{
		if (packageName.toLowerCase().trim().equals("java.lang")) return;
		ClassPool.getDefault().importPackage(packageName.trim());
	}

	/** Remove a field with a specific name
	 * @param classname - name of the class
	 * @param fieldname - name of a field to be deleted
	 * @throws NotFoundException if there is no such a field */
	public void removeField(String classname, String fieldname) throws NotFoundException
	{
		CtClass clazz = ClassPool.getDefault().getOrNull(classname);
		if (clazz == null) throw new NullPointerException(NO_SUCH_CLASS);
		clazz.removeField(clazz.getField(fieldname));
	}

	/** Remove methods with a specific name</br><b>Be careful!</b> It removes all the overloaded methods that match the methodname
	 * @param classname - name of the class
	 * @param methodname - name of methods to be deleted
	 * @throws NotFoundException if there are no such methods */
	public void removeMethods(String classname, String methodname) throws NotFoundException
	{
		CtClass clazz = ClassPool.getDefault().getOrNull(classname);
		if (clazz == null) throw new NullPointerException(NO_SUCH_CLASS);
		boolean f = false;
		for (CtMethod m : clazz.getMethods())
			if (m.getName().equals(methodname))
			{
				clazz.removeMethod(m);
				f = true;
			}
		if (!f) throw new NotFoundException("There are no such methods");
	}

	/** Compiles the CtClass, load it into the running JVM and retrieves a reference to the new instance.</br><b>REMEMBER!</b> The subsequent alterations are not permitted! The only thing you can do afterwards is to add a new class on the base of the
	 * existing class (or use Javassist outright)
	 * @param classname - name of the class
	 * @return a new instance of the compiled class
	 * @throws Exception if something goes wrong */
	public Object compile(String classname) throws Exception
	{
		CtClass clazz = ClassPool.getDefault().getOrNull(classname);
		if (clazz == null) throw new NullPointerException(NO_SUCH_CLASS);
		return clazz.toClass().newInstance();
	}

	/** @param classname - name of the class
	 * @return list of all fields names */
	public List<String> getFields(String classname)
	{
		CtClass clazz = ClassPool.getDefault().getOrNull(classname);
		if (clazz == null) throw new NullPointerException(NO_SUCH_CLASS);
		List<String> result = new ArrayList<>();
		for (CtField c : clazz.getDeclaredFields())
			result.add(c.getName());
		return result;
	}

	/** @param classname - name of the class
	 * @return list of all methods names */
	public List<String> getMethods(String classname)
	{
		CtClass clazz = ClassPool.getDefault().getOrNull(classname);
		if (clazz == null) throw new NullPointerException(NO_SUCH_CLASS);
		List<String> result = new ArrayList<>();
		for (CtMethod c : clazz.getDeclaredMethods())
			result.add(c.getName());
		return result;
	}
}
