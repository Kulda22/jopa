package cz.cvut.kbss.jopa.test;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;

import java.net.URI;

@OWLClass(iri = Vocabulary.C_OWL_INTERFACE_A)
public class OWLInterfaceAChild implements OWLInterfaceA{
    @Id
    private URI id;
    String attributeA;

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    @Override
    public String getAttributeA() {
        return attributeA;
    }

    @Override
    public void setAttributeA(String attributeA) {
        this.attributeA = attributeA;
    }
}
