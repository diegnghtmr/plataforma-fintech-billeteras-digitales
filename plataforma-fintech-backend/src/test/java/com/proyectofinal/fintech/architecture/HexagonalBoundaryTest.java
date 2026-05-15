package com.proyectofinal.fintech.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces hexagonal architecture layer boundaries.
 *
 * Rules:
 * 1. Domain must not depend on Spring, Jakarta, or infrastructure.
 * 2. Domain must not depend on the application layer.
 * 3. Application must not depend on Spring, Jakarta, or infrastructure.
 * 4. Domain ports must not use forbidden JDK collection types in signatures.
 */
@AnalyzeClasses(packages = "com.proyectofinal.fintech", importOptions = ImportOption.DoNotIncludeTests.class)
public class HexagonalBoundaryTest {

    // ── Rule 1: Domain must not depend on Spring ──────────────────────────────

    @ArchTest
    static final ArchRule domain_must_not_depend_on_spring =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                    .because("Domain layer must be framework-agnostic (hexagonal architecture)");

    // ── Rule 2: Domain must not depend on Jakarta ─────────────────────────────

    @ArchTest
    static final ArchRule domain_must_not_depend_on_jakarta =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("jakarta..")
                    .because("Domain layer must not use Jakarta EE annotations or APIs");

    // ── Rule 3: Domain must not depend on infrastructure ─────────────────────

    @ArchTest
    static final ArchRule domain_must_not_depend_on_infrastructure =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                    .because("Domain must not know about infrastructure adapters");

    // ── Rule 4: Domain must not depend on application ─────────────────────────

    @ArchTest
    static final ArchRule domain_must_not_depend_on_application =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..application..")
                    .because("Domain is the innermost layer and must not depend on application use cases");

    // ── Rule 5: Application must not depend on Spring ─────────────────────────

    @ArchTest
    static final ArchRule application_must_not_depend_on_spring =
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                    .because("Application use cases must be framework-agnostic");

    // ── Rule 6: Application must not depend on Jakarta ────────────────────────

    @ArchTest
    static final ArchRule application_must_not_depend_on_jakarta =
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAPackage("jakarta..")
                    .because("Application use cases must not use Jakarta EE APIs");

    // ── Rule 7: Application must not depend on infrastructure ─────────────────

    @ArchTest
    static final ArchRule application_must_not_depend_on_infrastructure =
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                    .because("Application layer must not directly reference infrastructure adapters");

    // ── Rules 8–15: Domain ports must not use forbidden JDK collection types ──

    @ArchTest
    static final ArchRule domain_ports_must_not_use_java_util_List =
            noClasses()
                    .that().resideInAPackage("..domain.port..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.List")
                    .because("Domain ports must use domain-owned collection types (e.g. MiLista), not java.util.List");

    @ArchTest
    static final ArchRule domain_ports_must_not_use_java_util_Map =
            noClasses()
                    .that().resideInAPackage("..domain.port..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.Map")
                    .because("Domain ports must use domain-owned collection types, not java.util.Map");

    @ArchTest
    static final ArchRule domain_ports_must_not_use_java_util_Set =
            noClasses()
                    .that().resideInAPackage("..domain.port..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.Set")
                    .because("Domain ports must use domain-owned collection types, not java.util.Set");

    @ArchTest
    static final ArchRule domain_ports_must_not_use_java_util_HashMap =
            noClasses()
                    .that().resideInAPackage("..domain.port..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.HashMap")
                    .because("Domain ports must use domain-owned collection types, not java.util.HashMap");

    @ArchTest
    static final ArchRule domain_ports_must_not_use_java_util_HashSet =
            noClasses()
                    .that().resideInAPackage("..domain.port..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.HashSet")
                    .because("Domain ports must use domain-owned collection types, not java.util.HashSet");

    @ArchTest
    static final ArchRule domain_ports_must_not_use_java_util_ArrayList =
            noClasses()
                    .that().resideInAPackage("..domain.port..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.ArrayList")
                    .because("Domain ports must use domain-owned collection types, not java.util.ArrayList");

    @ArchTest
    static final ArchRule domain_ports_must_not_use_java_util_LinkedList =
            noClasses()
                    .that().resideInAPackage("..domain.port..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.LinkedList")
                    .because("Domain ports must use domain-owned collection types, not java.util.LinkedList");

    @ArchTest
    static final ArchRule domain_ports_must_not_use_java_util_TreeMap =
            noClasses()
                    .that().resideInAPackage("..domain.port..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.TreeMap")
                    .because("Domain ports must use domain-owned collection types, not java.util.TreeMap");

    @ArchTest
    static final ArchRule domain_ports_must_not_use_java_util_TreeSet =
            noClasses()
                    .that().resideInAPackage("..domain.port..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.TreeSet")
                    .because("Domain ports must use domain-owned collection types, not java.util.TreeSet");

    // ── Rules 16–22: forbidden JDK collection types banned across domain.model + domain.service ──
    // Scope excludes domain.structures (where ArrayList is used only in boundary helpers like toList())
    // and domain.port (already covered by rules 8-15). HashMap/HashSet/TreeMap/TreeSet/LinkedList are
    // NEVER acceptable in domain — must use TablaHash / Conjunto / ArbolBST / MiLista.

    @ArchTest
    static final ArchRule domain_model_and_service_must_not_use_java_util_HashMap =
            noClasses()
                    .that().resideInAnyPackage("..domain.model..", "..domain.service..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.HashMap")
                    .because("domain.model and domain.service must use TablaHash, not java.util.HashMap");

    @ArchTest
    static final ArchRule domain_model_and_service_must_not_use_java_util_HashSet =
            noClasses()
                    .that().resideInAnyPackage("..domain.model..", "..domain.service..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.HashSet")
                    .because("domain.model and domain.service must use Conjunto, not java.util.HashSet");

    @ArchTest
    static final ArchRule domain_model_and_service_must_not_use_java_util_TreeMap =
            noClasses()
                    .that().resideInAnyPackage("..domain.model..", "..domain.service..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.TreeMap")
                    .because("domain.model and domain.service must use TablaHash/ArbolBST, not java.util.TreeMap");

    @ArchTest
    static final ArchRule domain_model_and_service_must_not_use_java_util_TreeSet =
            noClasses()
                    .that().resideInAnyPackage("..domain.model..", "..domain.service..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.TreeSet")
                    .because("domain.model and domain.service must use Conjunto/ArbolBST, not java.util.TreeSet");

    @ArchTest
    static final ArchRule domain_model_and_service_must_not_use_java_util_LinkedList =
            noClasses()
                    .that().resideInAnyPackage("..domain.model..", "..domain.service..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.LinkedList")
                    .because("domain.model and domain.service must use MiLista, not java.util.LinkedList");

    @ArchTest
    static final ArchRule domain_model_and_service_must_not_use_java_util_List =
            noClasses()
                    .that().resideInAnyPackage("..domain.model..", "..domain.service..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.List")
                    .because("domain.model and domain.service must use MiLista, not java.util.List");

    @ArchTest
    static final ArchRule domain_model_and_service_must_not_use_java_util_ArrayList =
            noClasses()
                    .that().resideInAnyPackage("..domain.model..", "..domain.service..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.ArrayList")
                    .because("domain.model and domain.service must use MiLista, not java.util.ArrayList");

    // ── Rules 23–25: forbidden JDK *internal-storage* types in application.usecase ──
    // ADR-9.1 still allows java.util.List/ArrayList at REST boundary (Jackson serialization).
    // But HashMap/HashSet/TreeMap/TreeSet/LinkedList are never an acceptable boundary type —
    // application use cases must use TablaHash / Conjunto when accumulating internally.

    @ArchTest
    static final ArchRule application_usecase_must_not_use_java_util_HashMap =
            noClasses()
                    .that().resideInAPackage("..application.usecase..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.HashMap")
                    .because("Application use cases must accumulate with TablaHash, not java.util.HashMap");

    @ArchTest
    static final ArchRule application_usecase_must_not_use_java_util_HashSet =
            noClasses()
                    .that().resideInAPackage("..application.usecase..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.HashSet")
                    .because("Application use cases must use Conjunto, not java.util.HashSet");

    @ArchTest
    static final ArchRule application_usecase_must_not_use_java_util_TreeMap =
            noClasses()
                    .that().resideInAPackage("..application.usecase..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.TreeMap")
                    .because("Application use cases must use TablaHash/ArbolBST, not java.util.TreeMap");

    @ArchTest
    static final ArchRule application_usecase_must_not_use_java_util_TreeSet =
            noClasses()
                    .that().resideInAPackage("..application.usecase..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.TreeSet")
                    .because("Application use cases must use Conjunto/ArbolBST, not java.util.TreeSet");

    @ArchTest
    static final ArchRule application_usecase_must_not_use_java_util_LinkedList =
            noClasses()
                    .that().resideInAPackage("..application.usecase..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("java.util.LinkedList")
                    .because("Application use cases must use MiLista, not java.util.LinkedList");
}
