/**
 * Copyright (C) 2016 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jopa.query.mapper;

import cz.cvut.kbss.jopa.environment.OWLClassA;
import cz.cvut.kbss.jopa.exception.SparqlResultMappingException;
import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jopa.model.metamodel.EntityTypeImpl;
import cz.cvut.kbss.jopa.model.metamodel.FieldSpecification;
import cz.cvut.kbss.jopa.model.metamodel.Identifier;
import cz.cvut.kbss.jopa.model.metamodel.MetamodelBuilder;
import cz.cvut.kbss.jopa.query.ResultSetMappingManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResultSetMappingProcessorTest {

    private static final String MAPPING_NAME = "testMapping";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private MetamodelBuilder builderMock;

    private ResultSetMappingProcessor processor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.processor = new ResultSetMappingProcessor(builderMock);
    }

    @Test
    public void buildMapperCreatesRowMapperWithVariableMappersConfiguredInMappingAnnotation() throws Exception {
        processor.buildMapper(getMapping(WithVariableMapping.class));
        final ResultSetMappingManager manager = processor.getManager();
        final SparqlResultMapper mapper = manager.getMapper(MAPPING_NAME);
        assertNotNull(mapper);
        final ResultRowMapper rowMapper = (ResultRowMapper) mapper;
        assertEquals(MAPPING_NAME, rowMapper.getName());
        final List<SparqlResultMapper> rowMappers = rowMapper.getRowMappers();
        assertEquals(2, rowMappers.size());
        assertTrue(rowMappers.get(0) instanceof VariableResultMapper);
        assertEquals("x", ((VariableResultMapper) rowMappers.get(0)).getName());
        assertEquals(void.class, ((VariableResultMapper) rowMappers.get(0)).getTargetType());
        assertTrue(rowMappers.get(1) instanceof VariableResultMapper);
        assertEquals("y", ((VariableResultMapper) rowMappers.get(1)).getName());
        assertEquals(URI.class, ((VariableResultMapper) rowMappers.get(1)).getTargetType());
    }

    @SparqlResultSetMapping(name = MAPPING_NAME, variables = {
            @VariableResult(name = "x"),
            @VariableResult(name = "y", type = URI.class)
    })
    private static final class WithVariableMapping {
    }

    private static SparqlResultSetMapping getMapping(Class<?> cls) {
        return cls.getDeclaredAnnotation(SparqlResultSetMapping.class);
    }

    @Test
    public void buildMapperBuildsEmptyMapperFromEmptyMappingConfiguration() {
        processor.buildMapper(getMapping(EmptyMapping.class));
        final ResultSetMappingManager manager = processor.getManager();
        final SparqlResultMapper mapper = manager.getMapper(MAPPING_NAME);
        assertNotNull(mapper);
        final ResultRowMapper rowMapper = (ResultRowMapper) mapper;
        assertEquals(MAPPING_NAME, rowMapper.getName());
        assertTrue(rowMapper.getRowMappers().isEmpty());
    }

    @SparqlResultSetMapping(name = MAPPING_NAME)
    private static final class EmptyMapping {
    }

    @Test
    public void buildMapperCreatesConstructorResultMapperWithCorrespondingVariableMappersForParameters() {
        processor.buildMapper(getMapping(WithConstructorMapping.class));
        final ResultSetMappingManager manager = processor.getManager();
        final SparqlResultMapper mapper = manager.getMapper(MAPPING_NAME);
        final ResultRowMapper rowMapper = (ResultRowMapper) mapper;
        assertEquals(1, rowMapper.getRowMappers().size());
        final ConstructorResultMapper ctorMapper = (ConstructorResultMapper) rowMapper.getRowMappers().get(0);
        assertNotNull(ctorMapper);
        assertEquals(OWLClassA.class, ctorMapper.getTargetType());
        assertEquals(3, ctorMapper.getParamMappers().size());
        assertEquals("uri", ctorMapper.getParamMappers().get(0).getName());
        assertEquals(String.class, ctorMapper.getParamMappers().get(0).getTargetType());
        assertEquals("label", ctorMapper.getParamMappers().get(1).getName());
        assertEquals(void.class, ctorMapper.getParamMappers().get(1).getTargetType());
        assertEquals("comment", ctorMapper.getParamMappers().get(2).getName());
        assertEquals(void.class, ctorMapper.getParamMappers().get(2).getTargetType());
    }

    @SparqlResultSetMapping(name = MAPPING_NAME, classes = {
            @ConstructorResult(targetClass = OWLClassA.class, variables = {
                    @VariableResult(name = "uri", type = String.class),
                    @VariableResult(name = "label"),
                    @VariableResult(name = "comment")
            })
    })
    private static final class WithConstructorMapping {
    }

    @Test
    public void buildMapperCreatesEntityResultMapperWhenAllFieldMappingsAreSpecified() throws Exception {
        final EntityTypeImpl<OWLClassA> etA = mock(EntityTypeImpl.class);
        when(builderMock.entity(OWLClassA.class)).thenReturn(etA);
        final FieldSpecification idField = mock(Identifier.class);
        when(etA.getFieldSpecification("uri")).thenReturn(idField);
        final FieldSpecification stringField = mock(FieldSpecification.class);
        when(etA.getFieldSpecification(OWLClassA.getStrAttField().getName())).thenReturn(stringField);
        final SparqlResultSetMapping mapping = getMapping(WithEntityMapping.class);
        processor.buildMapper(mapping);
        final ResultSetMappingManager manager = processor.getManager();
        final SparqlResultMapper mapper = manager.getMapper(MAPPING_NAME);
        final ResultRowMapper rowMapper = (ResultRowMapper) mapper;
        assertEquals(1, rowMapper.getRowMappers().size());
        assertTrue(rowMapper.getRowMappers().get(0) instanceof EntityResultMapper);
        final EntityResultMapper<OWLClassA> etMapper = (EntityResultMapper<OWLClassA>) rowMapper.getRowMappers().get(0);
        assertEquals(2, etMapper.getFieldMappers().size());
        assertEquals(etA, etMapper.getEntityType());
        final FieldResultMapper uriMapper = etMapper.getFieldMappers().get(0);
        assertEquals("id", uriMapper.getVariableName());
        assertEquals(idField, uriMapper.getFieldSpecification());
        final FieldResultMapper stringMapper = etMapper.getFieldMappers().get(1);
        assertEquals(stringField, stringMapper.getFieldSpecification());
        assertEquals("label", stringMapper.getVariableName());
    }

    @SparqlResultSetMapping(name = MAPPING_NAME, entities = {
            @EntityResult(entityClass = OWLClassA.class, fields = {
                    @FieldResult(name = "uri", variable = "id"),
                    @FieldResult(name = "stringAttribute", variable = "label")
            })
    })
    private static final class WithEntityMapping {
    }

    @Test
    public void buildMapperThrowsMappingExceptionWhenMappedFieldDoesNotExist() {
        final EntityTypeImpl<OWLClassA> etA = mock(EntityTypeImpl.class);
        when(builderMock.entity(OWLClassA.class)).thenReturn(etA);
        final FieldSpecification idField = mock(Identifier.class);
        when(etA.getFieldSpecification("uri")).thenReturn(idField);
        final String msg = "Unknown field stringAttribute.";
        when(etA.getFieldSpecification("stringAttribute")).thenThrow(new IllegalArgumentException(msg));
        thrown.expect(SparqlResultMappingException.class);
        thrown.expectMessage(msg);
        final SparqlResultSetMapping mapping = getMapping(WithEntityMapping.class);

        processor.buildMapper(mapping);
    }

    @Test
    public void buildMapperThrowsMappingExceptionWhenEntityMappingTargetClassIsNotEntity() {
        when(builderMock.entity(OWLClassA.class)).thenReturn(null);
        thrown.expect(SparqlResultMappingException.class);
        thrown.expectMessage("Type " + OWLClassA.class +
                " is not a known entity type and cannot be used as @EntityResult target class.");
        processor.buildMapper(getMapping(WithEntityMapping.class));
    }
}