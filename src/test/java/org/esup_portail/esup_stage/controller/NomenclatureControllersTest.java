package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Contenu;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Harnais générique des contrôleurs de nomenclature (CRUD homogène :
 * recherche paginée, exports Excel/CSV, création avec contrôle de doublon,
 * mise à jour, suppression refusée si des conventions utilisent l'élément).
 *
 * Les dépendances sont des mocks à réponse « intelligente » : exists/count
 * pilotés par scénario, saveAndFlush renvoie son argument, findById renvoie
 * une entité neuve.
 */
class NomenclatureControllersTest {

    private static final List<Class<?>> CONTROLLERS = List.of(
            ContratOffreController.class,
            DeviseController.class,
            EffectifController.class,
            ModeValidationStageController.class,
            ModeVersGratificationController.class,
            NatureTravailController.class,
            NiveauFormationController.class,
            OrigineStageController.class,
            StatutJuridiqueController.class,
            TempsTravailController.class,
            ThemeController.class,
            TypeOffreController.class,
            TypeStructureController.class,
            UniteDureeController.class,
            UniteGratificationController.class
    );

    @TestFactory
    List<DynamicTest> controleursDeNomenclature() {
        List<DynamicTest> tests = new ArrayList<>();
        for (Class<?> controllerClass : CONTROLLERS) {
            String name = controllerClass.getSimpleName();
            tests.add(DynamicTest.dynamicTest(name + " : recherche et exports", () -> rechercheEtExports(controllerClass)));
            tests.add(DynamicTest.dynamicTest(name + " : création", () -> creation(controllerClass)));
            tests.add(DynamicTest.dynamicTest(name + " : création refusée si doublon", () -> creationDoublon(controllerClass)));
            tests.add(DynamicTest.dynamicTest(name + " : mise à jour", () -> miseAJour(controllerClass)));
            tests.add(DynamicTest.dynamicTest(name + " : suppression", () -> suppression(controllerClass)));
            tests.add(DynamicTest.dynamicTest(name + " : suppression refusée si utilisé", () -> suppressionRefusee(controllerClass)));
        }
        return tests;
    }

    // ------------------------------------------------------------------
    // scénarios
    // ------------------------------------------------------------------

    private void rechercheEtExports(Class<?> controllerClass) throws Exception {
        Object controller = wire(controllerClass, false, 0L);

        Object paginated = invoke(controller, "search");
        assertThat(paginated).isInstanceOf(PaginatedResponse.class);
        assertThat(((PaginatedResponse<?>) paginated).getTotal()).isZero();
        assertThat(((PaginatedResponse<?>) paginated).getData()).isEmpty();

        Object excel = invoke(controller, "exportExcel");
        assertThat(((ResponseEntity<?>) excel).getStatusCode()).isEqualTo(HttpStatus.OK);

        Object csv = invoke(controller, "exportCsv");
        assertThat(((ResponseEntity<?>) csv).getBody()).isEqualTo("export-csv");
    }

    private void creation(Class<?> controllerClass) throws Exception {
        Object controller = wire(controllerClass, false, 0L);
        Object entity = filledEntity(entityType(controllerClass));

        Object created = invoke(controller, "create", entity);

        assertThat(created).isNotNull();
        Object temEnServ = getIfPresent(created, "getTemEnServ");
        if (temEnServ != null) {
            assertThat(temEnServ).as("%s : l'élément créé doit être en service", controllerClass.getSimpleName()).isEqualTo("O");
        }
    }

    private void creationDoublon(Class<?> controllerClass) throws Exception {
        Object controller = wire(controllerClass, true, 0L);
        Object entity = filledEntity(entityType(controllerClass));

        try {
            invoke(controller, "create", entity);
            fail("%s : la création d'un doublon doit être refusée", controllerClass.getSimpleName());
        } catch (AppException e) {
            assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    private void miseAJour(Class<?> controllerClass) throws Exception {
        Object controller = wire(controllerClass, false, 0L);
        Method update = findMethod(controllerClass, "update");
        Object request = filledEntity(update.getParameterTypes()[1]);

        Object updated = update.invoke(controller, 7, request);

        assertThat(updated).isNotNull();
        Object libelle = getIfPresent(updated, "getLibelle");
        if (libelle != null) {
            assertThat(libelle).isEqualTo("Libellé test");
        }
    }

    private void suppression(Class<?> controllerClass) throws Exception {
        Object controller = wire(controllerClass, false, 0L);

        invoke(controller, "delete", 7);
    }

    private void suppressionRefusee(Class<?> controllerClass) throws Exception {
        Object controller = wire(controllerClass, false, 5L);

        try {
            invoke(controller, "delete", 7);
            fail("%s : la suppression d'un élément utilisé doit être refusée", controllerClass.getSimpleName());
        } catch (AppException e) {
            assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // ------------------------------------------------------------------
    // moteur du harnais
    // ------------------------------------------------------------------

    /** Instancie le contrôleur et injecte dans chaque champ un mock à réponse intelligente. */
    private Object wire(Class<?> controllerClass, boolean exists, long count) throws Exception {
        Object controller = controllerClass.getDeclaredConstructor().newInstance();
        Class<?> entityType = entityType(controllerClass);
        for (Field field : controllerClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()
                    || field.getType() == String.class) {
                continue;
            }
            Object mock = Mockito.mock(field.getType(), Mockito.withSettings()
                    .defaultAnswer(invocation -> smartAnswer(invocation, exists, count, entityType)));
            field.setAccessible(true);
            field.set(controller, mock);
        }
        return controller;
    }

    private Object smartAnswer(InvocationOnMock invocation, boolean exists, long count, Class<?> entityType) throws Throwable {
        Method method = invocation.getMethod();
        String name = method.getName();
        Class<?> returnType = method.getReturnType();
        if (name.equals("exists")) {
            return exists;
        }
        if (name.startsWith("count")) {
            return returnType == long.class ? count : Long.valueOf(count);
        }
        if (name.equals("saveAndFlush") || name.equals("save")) {
            return invocation.getArgument(0);
        }
        if (name.equals("findById")) {
            Object entity = filledEntity(entityType);
            return returnType == Optional.class ? Optional.of(entity) : entity;
        }
        if (name.equals("findByCode") && returnType == Contenu.class) {
            Contenu contenu = new Contenu();
            contenu.setTexte("Code déjà existant");
            return contenu;
        }
        if (name.equals("findPaginated")) {
            return new ArrayList<>();
        }
        if (name.equals("exportExcel")) {
            return "export-excel".getBytes();
        }
        if (name.equals("exportCsv")) {
            return new StringBuilder("export-csv");
        }
        return Mockito.RETURNS_DEFAULTS.answer(invocation);
    }

    /** Le type d'entité géré par le contrôleur = type du paramètre de create(). */
    private Class<?> entityType(Class<?> controllerClass) {
        return findMethod(controllerClass, "create").getParameterTypes()[0];
    }

    private Method findMethod(Class<?> clazz, String name) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(name) && Modifier.isPublic(method.getModifiers())) {
                return method;
            }
        }
        throw new IllegalStateException(clazz.getSimpleName() + " : méthode " + name + " absente");
    }

    /** Invoque un endpoint en fabriquant des arguments plausibles selon les types. */
    private Object invoke(Object controller, String methodName, Object... explicitArgs) throws Exception {
        Method method = findMethod(controller.getClass(), methodName);
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        int explicitIndex = 0;
        boolean firstInt = true;
        for (int i = 0; i < parameters.length; i++) {
            Class<?> type = parameters[i].getType();
            if (explicitIndex < explicitArgs.length && type.isInstance(explicitArgs[explicitIndex])) {
                args[i] = explicitArgs[explicitIndex++];
            } else if (type == int.class || type == Integer.class) {
                if (explicitIndex < explicitArgs.length && explicitArgs[explicitIndex] instanceof Integer valeur) {
                    args[i] = valeur;
                    explicitIndex++;
                } else {
                    args[i] = firstInt ? 1 : 50;
                    firstInt = false;
                }
            } else if (type == String.class) {
                args[i] = "{}";
            } else if (type == HttpServletResponse.class) {
                args[i] = new MockHttpServletResponse();
            } else {
                args[i] = null;
            }
        }
        try {
            return method.invoke(controller, args);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof AppException appException) {
                throw appException;
            }
            if (e.getCause() instanceof Exception cause) {
                throw cause;
            }
            throw e;
        }
    }

    /** Instancie l'entité (ou le DTO) et remplit libelle/codeCtrl si présents. */
    private Object filledEntity(Class<?> type) throws Exception {
        Object entity = type.getDeclaredConstructor().newInstance();
        setIfPresent(entity, "setLibelle", "Libellé test");
        setIfPresent(entity, "setCodeCtrl", "CODE_TEST");
        setIfPresent(entity, "setTemEnServ", "O");
        return entity;
    }

    private void setIfPresent(Object target, String setter, String value) {
        try {
            target.getClass().getMethod(setter, String.class).invoke(target, value);
        } catch (ReflectiveOperationException ignored) {
            // accesseur absent sur ce type : sans objet
        }
    }

    private Object getIfPresent(Object target, String getter) {
        try {
            return target.getClass().getMethod(getter).invoke(target);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
