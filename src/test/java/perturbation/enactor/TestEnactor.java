package perturbation.enactor;

import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.visitor.filter.NameFilter;
import util.Util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by spirals on 29/03/16.
 */
public class TestEnactor {

    private static Launcher launcher = null;

    private static URLClassLoader classLoaderWithoutOldFile;
    private static CtClass simpleResWithPerturbation;
    private static Class<?> classPerturbator;
    private static Object objectPerturbator;
    private static Method addLocationToPerturb;
    private static Method removeLocationToPerturb;
    private static Method setEnactor;

    private static Class<?> classUnderTest;
    private static Object objectUnderTest;
    private static Method booleanMethodOfClassUnderTest;


    private static void initialisation() throws Exception {
        launcher = Util.createSpoonWithPerturbationProcessors();

        launcher.addInputResource("src/test/resources/SimpleRes.java");

        launcher.run();

        simpleResWithPerturbation = (CtClass) launcher.getFactory().Package().getRootPackage().getElements(new NameFilter("SimpleRes")).get(0);

        Util.addPathToClassPath(launcher.getModelBuilder().getBinaryOutputDirectory().toURL());
        classLoaderWithoutOldFile = Util.removeOldFileFromClassPath((URLClassLoader) ClassLoader.getSystemClassLoader());

        //Perturbation
        classPerturbator = classLoaderWithoutOldFile.loadClass("perturbation.Perturbation");
        objectPerturbator = classPerturbator.newInstance();
        addLocationToPerturb = classPerturbator.getMethod("add", classLoaderWithoutOldFile.loadClass("perturbation.PerturbationLocation"));
        removeLocationToPerturb = classPerturbator.getMethod("remove", classLoaderWithoutOldFile.loadClass("perturbation.PerturbationLocation"));
        setEnactor = classPerturbator.getMethod("setEnactor", classLoaderWithoutOldFile.loadClass("perturbation.enactor.Enactor"));

        classUnderTest = classLoaderWithoutOldFile.loadClass(simpleResWithPerturbation.getQualifiedName());
        objectUnderTest = classUnderTest.newInstance();
        booleanMethodOfClassUnderTest = classUnderTest.getMethod("_pBoolean");

    }


    @Test
    public void testLocationEnactor() throws Exception {

        //test the configuration of the LocationEnactor which is enact the perturbtion if the PerturbationLocation is in his list.

        if (launcher == null)
            initialisation();

        //Setting Enactor Location
        setEnactor.invoke(objectPerturbator, classLoaderWithoutOldFile.loadClass("perturbation.enactor.LocationEnactor").newInstance());

        //shoudln't be perturb because the list of PerturbationLocation is empty
        assertEquals(true, (Boolean)booleanMethodOfClassUnderTest.invoke(objectUnderTest));

        addLocationToPerturb.invoke(objectPerturbator, classUnderTest.getFields()[0].get(null));

        //now it is perturbed
        assertEquals(false, (Boolean)booleanMethodOfClassUnderTest.invoke(objectUnderTest));

        removeLocationToPerturb.invoke(objectPerturbator, classUnderTest.getFields()[0].get(null));
    }

    @Test
    public void testAlwaysEnactor() throws Exception {
        if (launcher == null)
            initialisation();

        //test the always Enactor which always enact perturbation
        setEnactor.invoke(objectPerturbator, classLoaderWithoutOldFile.loadClass("perturbation.enactor.AlwaysEnactor").newInstance());

        //perturb while no PerturbationLocation has been added to the list : 2 perturbations in a row : !!(true) == true
        assertEquals(true,(Boolean)booleanMethodOfClassUnderTest.invoke(objectUnderTest));

        addLocationToPerturb.invoke(objectPerturbator, classUnderTest.getFields()[0].get(null));

        assertEquals(true,(Boolean)booleanMethodOfClassUnderTest.invoke(objectUnderTest));
    }

    @Test
    public void testRandomLocationEnactor() throws Exception {
        if (launcher == null)
            initialisation();

        //test the Random Enactor which means that is enact the perturbation with a probability epsilon

        //Setting Enactor NTime with Location as decorated Enactor
        Constructor constructorOfRandomEnactor = classLoaderWithoutOldFile.loadClass("perturbation.enactor.RandomEnactor").getConstructor(
                classLoaderWithoutOldFile.loadClass("perturbation.enactor.Enactor"), float.class
        );

        Object OneForTwoPerturbationEnactor = constructorOfRandomEnactor.newInstance(
                classLoaderWithoutOldFile.loadClass("perturbation.enactor.LocationEnactor").newInstance(), 0.5f
        );

        setEnactor.invoke(objectPerturbator, OneForTwoPerturbationEnactor);

        //The perturbation will occurs 1 time on two (50%)
        addLocationToPerturb.invoke(objectPerturbator, classUnderTest.getFields()[0].get(null));

        int cptPerturb = 0;
        int cptNotPerturb = 0;

        for (int i = 0 ; i < 10000 ; i++) {
            if ((Boolean)booleanMethodOfClassUnderTest.invoke(objectUnderTest))
                cptNotPerturb++;
            else
                cptPerturb++;
        }

        //5% of error is allowed, because it's random
        assertTrue(10000*0.05 >= Math.abs(cptNotPerturb - cptPerturb));

        removeLocationToPerturb.invoke(objectPerturbator, classUnderTest.getFields()[0].get(null));
    }

    @Test
    public void testNTimeLocationEnactor() throws Exception {

        if (launcher == null)
            initialisation();

        //test the NTime Enactor which means that is enact n time the perturbation at the given location

        //Setting Enactor NTime with Location as decorated Enactor
        Constructor constructorOfNTimeEnactor = classLoaderWithoutOldFile.loadClass("perturbation.enactor.NTimeEnactor").getConstructor(
                classLoaderWithoutOldFile.loadClass("perturbation.enactor.Enactor"), int.class
        );

        Object FiveTimeEnactorWithLocationEnactorDecorated = constructorOfNTimeEnactor.newInstance(
                classLoaderWithoutOldFile.loadClass("perturbation.enactor.LocationEnactor").newInstance(), 5
        );

        setEnactor.invoke(objectPerturbator, FiveTimeEnactorWithLocationEnactorDecorated);

        //The perturbation will occurs 5 times
        addLocationToPerturb.invoke(objectPerturbator, classUnderTest.getFields()[0].get(null));

        assertEquals(false, (Boolean)booleanMethodOfClassUnderTest.invoke(objectUnderTest));
        assertEquals(false, (Boolean)booleanMethodOfClassUnderTest.invoke(objectUnderTest));
        assertEquals(false, (Boolean)booleanMethodOfClassUnderTest.invoke(objectUnderTest));
        assertEquals(false, (Boolean)booleanMethodOfClassUnderTest.invoke(objectUnderTest));
        assertEquals(false, (Boolean)booleanMethodOfClassUnderTest.invoke(objectUnderTest));

        //after that, no more perturbation
        assertEquals(true, (Boolean)booleanMethodOfClassUnderTest.invoke(objectUnderTest));

        removeLocationToPerturb.invoke(objectPerturbator, classUnderTest.getFields()[0].get(null));
    }
}