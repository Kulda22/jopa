/**
 * Copyright (C) 2016 Czech Technical University in Prague
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
package cz.cvut.kbss.jopa.owl2java;

import cz.cvut.kbss.jopa.owl2java.exception.OWL2JavaException;
import cz.cvut.kbss.jopa.util.MappingFileParser;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.URI;
import java.util.*;

public class OWL2JavaTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(OWL2JavaTransformer.class);

    private static final ContextDefinition DEFAULT_CONTEXT = new ContextDefinition();

    private final ValidContextAnnotationValueVisitor v = new ValidContextAnnotationValueVisitor();
    private OWLOntology ontology;
    private Map<String, ContextDefinition> contexts = new HashMap<>();
    private boolean ignoreMissingImports;
    private final OptionSet configuration;

    public OWL2JavaTransformer() {
        this.configuration = new OptionParser().parse("");
    }

    OWL2JavaTransformer(OptionSet configuration) {
        this.configuration = configuration;
    }

    public Collection<String> listContexts() {
        return contexts.keySet();
    }

    public void ignoreMissingImports(boolean ignore) {
        this.ignoreMissingImports = ignore;
    }

    private OWLOntology getWholeOntology(final String owlOntologyName, final String mappingFile) {
        // reader
        final OWLOntologyManager m = OWLManager.createOWLOntologyManager();

        if (mappingFile != null) {
            LOG.info("Using mapping file '{}'.", mappingFile);

            final Map<URI, URI> map = MappingFileParser.getMappings(new File(mappingFile));
            m.getIRIMappers().add(ontologyIRI -> {
                final URI value = map.get(ontologyIRI.toURI());

                if (value == null) {
                    return null;
                } else {
                    return IRI.create(value);
                }
            });
            LOG.info("Mapping file successfully parsed.");
        }

        LOG.info("Loading ontology {}... ", owlOntologyName);
        if (ignoreMissingImports) {
            m.getOntologyConfigurator().setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
            m.addMissingImportListener(e -> LOG.warn("Unable to import ontology {}.", e.getImportedOntologyURI()));
        }

        try {
            m.loadOntology(IRI.create(owlOntologyName));
            return new OWLOntologyMerger(m).createMergedOntology(m, IRI.create(owlOntologyName + "-generated"));
        } catch (OWLException | OWLRuntimeException e) {
            LOG.error(e.getMessage(), e);
            throw new OWL2JavaException("Unable to load ontology " + owlOntologyName, e);
        }
    }

    public void setOntology(final String owlOntologyName,
                            final String mappingFile, boolean includeImports) {
        ontology = getWholeOntology(owlOntologyName, mappingFile);

//        this.imports = ontology.getOWLOntologyManager().getOntologies();

        LOG.info("Parsing integrity constraints");

        ontology.axioms().forEach(a -> {
            DEFAULT_CONTEXT.addAxiom(a);
            for (final String icContextName : getContexts(a)) {
                ContextDefinition ctx = getContextDefinition(icContextName);
                ctx.addAxiom(a);
            }
        });

        DEFAULT_CONTEXT.parse();
        for (final ContextDefinition ctx : contexts.values()) {
            ctx.parse();
        }

        LOG.info("Integrity constraints successfully parsed.");
    }

    private ContextDefinition getContextDefinition(String icContextName) {
        return contexts.computeIfAbsent(icContextName, name -> new ContextDefinition());
    }

    private List<String> getContexts(final OWLAxiom a) {
        final List<String> icContexts = new ArrayList<>();
        a.annotations().filter(p -> p.getProperty().getIRI().toString().equals(Constants.P_IS_INTEGRITY_CONSTRAINT_FOR))
         .forEach(p -> {
             LOG.info("Processing annotation : " + p);
             p.getValue().accept(v);
             final String icContextName = v.getName();
             LOG.info("CONTEXT:" + icContextName);
             if (icContextName == null) {
                 return;
             }
             LOG.debug("Found IC {} for context {}", a, icContextName);
             icContexts.add(icContextName);
         });
        return icContexts;
    }

    private void verifyContextExistence(String context) {
        if (!contexts.containsKey(context)) {
            throw new IllegalArgumentException(
                    "Context " + context + " not found. Existing contexts: " + listContexts());
        }
    }

    public void transform(TransformationConfiguration transformConfig) {
        final ContextDefinition def = getValidContext(transformConfig);
        LOG.info("Transforming context ...");
        new JavaTransformer(configuration).generateModel(ontology, def, transformConfig);
        LOG.info("Transformation SUCCESSFUL.");
    }

    private ContextDefinition getValidContext(TransformationConfiguration configuration) {
        if (configuration.areAllAxiomsIntegrityConstraints()) {
            LOG.info(" - for all axioms");
            return DEFAULT_CONTEXT;
        }
        final String context = configuration.getContext();
        verifyContextExistence(context);
        LOG.info(" - for context '{}'.", context);
        return contexts.get(context);
    }

    /**
     * Generates only vocabulary of the loaded ontology.
     *
     * @param transformConfig Configuration of the generation process
     */
    public void generateVocabulary(TransformationConfiguration transformConfig) {
        LOG.info("Generating vocabulary ...");

        ContextDefinition def = getValidContext(transformConfig);
        new JavaTransformer(configuration).generateVocabulary(ontology, def, transformConfig);
    }

    private class ValidContextAnnotationValueVisitor implements OWLAnnotationValueVisitor {
        private String name = null;

        String getName() {
            return name;
        }

        @Override
        public void visit(@Nonnull IRI iri) {
        }

        @Override
        public void visit(@Nonnull OWLAnonymousIndividual individual) {
        }

        @Override
        public void visit(@Nonnull OWLLiteral literal) {
            name = literal.getLiteral();
        }
    }
}
