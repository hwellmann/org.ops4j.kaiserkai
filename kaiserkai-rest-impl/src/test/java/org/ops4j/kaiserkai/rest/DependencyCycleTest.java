package org.ops4j.kaiserkai.rest;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.dependencies.SliceRule;

/**
 * Checks packages of this project for dependency cycles.
 * <p>
 * IMPORTANT: Only classes from classpath <em>directories</em> are imported, classes from archives
 * are ignored.
 * <p>
 * Thus, this test only produces meaningful results from Eclipse or when running {@code mvn test}
 * from the project root.
 * <p>
 * Most classes will not be imported when running {@code mvn package} or any other goal where
 * reactor dependencies are already packaged.
 *
 * @author Harald Wellmann
 *
 */
public class DependencyCycleTest {

    @Test
    public void shouldNotContainPackageDependencyCycles() {
        JavaClasses classes = new ClassFileImporter().importClasspath();

        SliceRule rule = slices().matching("org.ops4j.kaiserkai.(**)")
            .should()
            .beFreeOfCycles();
        rule.check(classes);
    }
}
