/**
 * Copyright (C) 2022 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.ontodriver.sesame;

import cz.cvut.kbss.ontodriver.descriptor.AxiomDescriptor;
import cz.cvut.kbss.ontodriver.model.Assertion;
import cz.cvut.kbss.ontodriver.model.NamedResource;
import cz.cvut.kbss.ontodriver.sesame.connector.Connector;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EpistemicAxiomRemoverTest {

    private static final NamedResource SUBJECT = NamedResource
            .create("http://krizik.felk.cvut.cz/ontologies/jopa/entityX");

    private static final String PROPERTY = "http://krizik.felk.cvut.cz/ontologies/jopa/propertyOne";

    private AxiomDescriptor descriptor;

    @Mock
    private Connector connectorMock;

    private ValueFactory vf;

    private EpistemicAxiomRemover axiomRemover;

    @BeforeEach
    public void setUp() {
        this.vf = SimpleValueFactory.getInstance();
        this.descriptor = new AxiomDescriptor(SUBJECT);

        this.axiomRemover = new EpistemicAxiomRemover(connectorMock, vf);
    }

    @Test
    public void removeSkipsInferredAssertions() throws Exception {
        final Assertion ass = Assertion.createObjectPropertyAssertion(URI.create(PROPERTY), true);
        descriptor.addAssertion(ass);

        axiomRemover.remove(descriptor);
        verify(connectorMock, never())
                .findStatements(eq(vf.createIRI(SUBJECT.toString())), eq(vf.createIRI(PROPERTY)), any(), anyBoolean(),
                        any());
        verify(connectorMock, never())
                .findStatements(eq(vf.createIRI(SUBJECT.toString())), eq(vf.createIRI(PROPERTY)), any(), anyBoolean(),
                        any());
    }

    @Test
    public void removeWithAssertionContextSearchesInContext() throws Exception {
        final String context = "http://krizik.felk.cvut.cz/ontologies/jopa/contexts#One";
        final Assertion ass = Assertion.createObjectPropertyAssertion(URI.create(PROPERTY), false);
        descriptor.addAssertion(ass);
        descriptor.addAssertionContext(ass, URI.create(context));

        axiomRemover.remove(descriptor);

        verify(connectorMock).findStatements(vf.createIRI(SUBJECT.toString()), vf.createIRI(PROPERTY), null, false,
                Collections.singleton(vf.createIRI(context)));
        verify(connectorMock).removeStatements(anyCollection());
    }

    @Test
    public void removeCallsFindStatementsWithoutContextsWhenItIsNotSpecifiedForAssertion() throws Exception {
        final Assertion ass = Assertion.createObjectPropertyAssertion(URI.create(PROPERTY), false);
        descriptor.addAssertion(ass);

        axiomRemover.remove(descriptor);

        verify(connectorMock)
                .findStatements(vf.createIRI(SUBJECT.toString()), vf.createIRI(PROPERTY), null, false, Collections.emptySet());
        verify(connectorMock).removeStatements(anyCollection());
    }
}
