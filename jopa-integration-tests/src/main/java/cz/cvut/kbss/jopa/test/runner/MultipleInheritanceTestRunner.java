package cz.cvut.kbss.jopa.test.runner;

import cz.cvut.kbss.jopa.model.IRI;
import cz.cvut.kbss.jopa.model.metamodel.EntityType;
import cz.cvut.kbss.jopa.test.*;
import cz.cvut.kbss.jopa.test.environment.DataAccessor;
import cz.cvut.kbss.jopa.test.environment.Generators;
import cz.cvut.kbss.jopa.test.environment.PersistenceFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.net.URI;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class MultipleInheritanceTestRunner extends BaseRunner {
    public MultipleInheritanceTestRunner(Logger logger, PersistenceFactory persistenceFactory, DataAccessor dataAccessor) {
        super(logger, persistenceFactory, dataAccessor);
    }

    @Test
    void entityCanBeFoundByBothParentTypes() {
        this.em = getEntityManager("entityCanBeFoundByBothParentTypes", false);

        URI id = Generators.generateUri();
        final OWLChildClassA child = new OWLChildClassA();
        child.setId(id);
        child.setStringAttribute("AttRVal");
        child.setPluralAnnotationProperty(Collections.singleton("seeet"));

        em.persist(child);
        em.clear();
        final OWLChildClassA found = findRequired(OWLChildClassA.class, id);
        em.clear();
        final OWLParentB parentBFound = findRequired(OWLParentB.class, id);
        em.clear();
        final OWLParentA parentAFound = findRequired(OWLParentA.class, id);

        assertEquals(child.getId(), found.getId());
        assertEquals(child.getStringAttribute(), parentBFound.getStringAttribute());
        assertEquals(child.getPluralAnnotationProperty(), parentAFound.getPluralAnnotationProperty());
    }

    @Test
    void annotatedMethodPassesDownAnnotationValuesFromSingleParent() {
        this.em = getEntityManager("annotatedMethodPassesDownAnnotationValues", false);
        URI id = Generators.generateUri();
        final OWLClassWithUnProperties subject = new OWLClassWithUnProperties();

        subject.setId(id);
        subject.setName("NAME_VALUE");

        em.persist(subject);
        em.clear();

        OWLClassWithUnProperties found = em.find(OWLClassWithUnProperties.class, id);

        IRI namePropertyIRI = em.getMetamodel()
                .entity(OWLClassWithUnProperties.class)
                .getDeclaredAttribute("name")
                .getIRI();

        assertNotNull(found);
        assertEquals(subject.getName(), found.getName());
        assertEquals(subject.getId(), found.getId());
        assertEquals(Vocabulary.p_m_unannotated_name, namePropertyIRI.toString());
    }

    @Test
    void annotatedMethodPassesDownAnnotationValuesFromMultipleParents() {
        this.em = getEntityManager("annotatedMethodPassesDownAnnotationValuesFromMultipleParents", false);

        URI id = Generators.generateUri();
        final OWLChildClassB child = new OWLChildClassB();
        child.setId(id);
        child.setAttributeA("Value");
        child.setAttributeB(Boolean.FALSE);
        em.persist(child);
        em.clear();
        final OWLChildClassB found = findRequired(OWLChildClassB.class, id);
        em.clear();
        final OWLInterfaceA parentAFound = findRequired(OWLInterfaceA.class, id);
        em.clear();
        final OWLInterfaceB parentBFound = findRequired(OWLInterfaceB.class, id);

        assertEquals(child.getId(), found.getId());
        assertEquals(child.getAttributeA(), parentAFound.getAttributeA());
        assertEquals(child.getAttributeB(), parentBFound.getAttributeB());

        EntityType<OWLChildClassB> childEt = em.getMetamodel().entity(OWLChildClassB.class);

        assertEquals(Vocabulary.p_m_attributeA, childEt.getDeclaredAttribute("attributeA").getIRI().toString());
        assertEquals(Vocabulary.p_m_attributeB, childEt.getDeclaredAttribute("attributeB").getIRI().toString());
    }

    @Test
    void annotationInheritedThroughTwoWaysIsHandledProperly() {
        this.em = getEntityManager("annotationInheritedThroughTwoWaysIsHandledProperly", false);
        URI id = Generators.generateUri();

        final OWLChildClassC child = new OWLChildClassC();
        child.setId(id);
        child.setName("Name");
        child.setAttributeB(Generators.randomBoolean());

        em.persist(child);
        em.clear();
        final OWLChildClassC found = findRequired(OWLChildClassC.class, id);
        em.clear();
        em.clear();
        final OWLInterfaceAnMethods commonParentFound = findRequired(OWLInterfaceAnMethods.class, id);

        assertEquals(child.getId(), found.getId());
        assertEquals(child.getName(), found.getName());
        assertEquals(child.getAttributeB(), found.getAttributeB());

        assertEquals(child.getName(), commonParentFound.getName());
    }
}
