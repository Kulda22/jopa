/**
 * Copyright (C) 2022 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.ontodriver.sesame;

import cz.cvut.kbss.ontodriver.descriptor.AbstractAxiomDescriptor;
import cz.cvut.kbss.ontodriver.model.Assertion;
import cz.cvut.kbss.ontodriver.model.NamedResource;
import cz.cvut.kbss.ontodriver.model.Value;
import cz.cvut.kbss.ontodriver.sesame.connector.Connector;
import cz.cvut.kbss.ontodriver.sesame.exceptions.SesameDriverException;
import cz.cvut.kbss.ontodriver.sesame.util.SesameUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class performs an epistemic remove of axioms described by the axiom descriptor.
 * <p/>
 * Epistemic remove means that only information known to the application is deleted. The assertions in the descriptor
 * represent this information. Thus, only these assertions are removed from the ontology. Note that if the descriptor
 * contains an unspecified property assertion, all property assertions related to the subject individual are removed
 * from the property's context.
 */
class EpistemicAxiomRemover {

    private final Connector connector;
    private final ValueFactory valueFactory;

    EpistemicAxiomRemover(Connector connector, ValueFactory valueFactory) {
        this.connector = connector;
        this.valueFactory = valueFactory;
    }

    void remove(AbstractAxiomDescriptor axiomDescriptor) throws SesameDriverException {
        final Resource individual = SesameUtils.toSesameIri(axiomDescriptor.getSubject().getIdentifier(), valueFactory);
        final Collection<Statement> toRemove = new HashSet<>();
        for (Assertion a : axiomDescriptor.getAssertions()) {
            if (a.isInferred()) {
                continue;
            }
            final Set<IRI> contexts = axiomDescriptor.getAssertionContexts(a).stream()
                                                     .map(uri -> SesameUtils.toSesameIri(uri, valueFactory))
                                                     .collect(Collectors.toSet());
            toRemove.addAll(connector.findStatements(individual,
                    SesameUtils.toSesameIri(a.getIdentifier(), valueFactory), null, a.isInferred(), contexts));
        }
        connector.removeStatements(toRemove);
    }

    void remove(NamedResource individual, Map<Assertion, Set<Value<?>>> values, java.net.URI context)
            throws SesameDriverException {
        final IRI sesameContext = SesameUtils.toSesameIri(context, valueFactory);
        final Resource subject = SesameUtils.toSesameIri(individual.getIdentifier(), valueFactory);
        final Collection<Statement> toRemove = new ArrayList<>();
        final SesameValueConverter valueConverter = new SesameValueConverter(valueFactory);
        for (Map.Entry<Assertion, Set<Value<?>>> entry : values.entrySet()) {
            final IRI property = SesameUtils.toSesameIri(entry.getKey().getIdentifier(), valueFactory);
            for (Value<?> val : entry.getValue()) {
                final org.eclipse.rdf4j.model.Value sesameValue = valueConverter.toSesameValue(entry.getKey(), val);
                if (sesameContext != null) {
                    toRemove.add(valueFactory.createStatement(subject, property, sesameValue, sesameContext));
                } else {
                    toRemove.add(valueFactory.createStatement(subject, property, sesameValue));
                }
            }
        }
        connector.removeStatements(toRemove);
    }
}
