package ru.tomtrix.javassistwraper.test;

import java.util.Arrays;
import ru.tomtrix.javassistwraper.ClassStore;

/** Tester for a ClassStore class
 * @author tom-trix */
public class Tester
{
	/** Look through this simple example to undestand how perfect the Javassist works
	 * @param args command line arguments
	 * @throws Exception */
	public static void main(String[] args) throws Exception
	{
		// Let's create a new class that contains fields x and y (and also method getSum())
		ClassStore.getInstance().addClass("Point", Arrays.asList("int x=1;", "int y=2;"), Arrays.asList("public int getSum() {return x+y;}"));

		// Of course, you can add new fields and methods afterwards
		ClassStore.getInstance().addField("Point", "int dif=0;");
		ClassStore.getInstance().addMethod("Point", "public int getDifference() {return x-y;}");

		// Now compile the class and see what's going on
		Object r = ClassStore.getInstance().compile("Point");
		System.out.println("Summa = " + r.getClass().getMethod("getSum").invoke(r));
		System.out.println("Difference = " + r.getClass().getMethod("getDifference").invoke(r));

		// Important! It's impossible to change the class after it was loaded into JVM!!!
		try
		{
			ClassStore.getInstance().addField("Point", "int z=5;");
		}
		catch (Exception e)
		{
			System.out.println("Error! You can't change an already loaded class!");
		}

		// However, we could create a new class that enhances the previous one with new fields and methods
		ClassStore.getInstance().addClass("3DPoint", "Point", Arrays.asList("int z=5;"), Arrays.asList("public int getFullSum() {return x+y+z;}"));

		// Now compile the new class and see what we have eventually got
		r = ClassStore.getInstance().compile("3DPoint");
		System.out.println("Summa = " + r.getClass().getMethod("getSum").invoke(r));
		System.out.println("Difference = " + r.getClass().getMethod("getDifference").invoke(r));
		System.out.println("Full summa = " + r.getClass().getMethod("getFullSum").invoke(r));
	}
}
