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
package cz.cvut.kbss.jopa.query.soql;

import cz.cvut.kbss.jopa.environment.Vocabulary;
import cz.cvut.kbss.jopa.environment.utils.MetamodelMocks;
import cz.cvut.kbss.jopa.model.MetamodelImpl;
import cz.cvut.kbss.jopa.query.QueryHolder;
import cz.cvut.kbss.jopa.query.QueryParser;
import cz.cvut.kbss.jopa.query.parameter.ParameterValueFactory;
import cz.cvut.kbss.jopa.query.sparql.SparqlQueryParser;
import cz.cvut.kbss.jopa.sessions.MetamodelProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SoqlQueryParserTest {

    @Mock
    private MetamodelImpl metamodel;

    private QueryParser sut;

    @BeforeEach
    void setUp() throws Exception {
        MetamodelMocks mocks = new MetamodelMocks();
        mocks.setMocks(metamodel);
        final MetamodelProvider mpp = mock(MetamodelProvider.class);
        when(mpp.getMetamodel()).thenReturn(metamodel);
        when(mpp.isEntityType(any())).thenAnswer(inv -> metamodel.isEntityType(inv.getArgument(0)));
        final SparqlQueryParser qp = new SparqlQueryParser(new ParameterValueFactory(mpp));
        this.sut = new SoqlQueryParser(qp, metamodel);
    }

    @Test
    public void testParseFindAllQuery() {
        final String soqlQuery = "SELECT p FROM Person p";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(1, holder.getParameters().size());
    }

    @Test
    public void testParseDistinctFindAllQuery() {
        final String soqlQuery = "SELECT DISTINCT p FROM Person p";
        final String expectedSparqlQuery = "SELECT DISTINCT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(1, holder.getParameters().size());
    }

    @Test
    public void testParseCountQuery() {
        final String soqlQuery = "SELECT COUNT(p) FROM Person p";
        final String expectedSparqlQuery = "SELECT (COUNT(?x) AS ?count) WHERE { ?x a <" + Vocabulary.c_Person + "> . }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(2, holder.getParameters().size());
    }

    @Test
    public void testParseDistinctCountQuery() {
        final String soqlQuery = "SELECT DISTINCT COUNT(p) FROM Person p";
        final String expectedSparqlQuery = "SELECT (COUNT(DISTINCT ?x) AS ?count) WHERE { ?x a <" + Vocabulary.c_Person + "> . }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(2, holder.getParameters().size());
    }

    @Test
    public void testParseFindAllOWLClassAQuery() {
        final String soqlQuery = "SELECT a FROM OWLClassA a";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_OwlClassA + "> . }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(1, holder.getParameters().size());
    }

    @Test
    public void testParseFindOneQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.username = :username";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_username + "> ?username . }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(2, holder.getParameters().size());
    }

    @Test
    public void testParseFindOneLikeQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.username LIKE :username";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_username + "> ?pUsername . FILTER (regex(?pUsername, ?username) ) }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(3, holder.getParameters().size());
    }

    @Test
    public void testParseFindOneJoinedQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.phone.number = :phoneNumber";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_hasPhone + "> ?phone . ?phone <" + Vocabulary.p_p_phoneNumber + "> ?phoneNumber . }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(3, holder.getParameters().size());
    }

    @Test
    public void testParseFindMultipleJoinedQuery() {
        final String soqlQuery = "SELECT g FROM OWLClassG g WHERE g.owlClassH.owlClassA.stringAttribute = :d";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_OwlClassG + "> . ?x <" + Vocabulary.p_g_hasH + "> ?owlClassH . ?owlClassH <" + Vocabulary.p_h_hasA + "> ?owlClassA . ?owlClassA <" + Vocabulary.p_a_stringAttribute + "> ?d . }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(4, holder.getParameters().size());
    }

    @Test
    public void testParseFindMultipleJoinedQueryFilter() {
        final String soqlQuery = "SELECT g FROM OWLClassG g WHERE g.owlClassH.owlClassA.stringAttribute > :str";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_OwlClassG + "> . ?x <" + Vocabulary.p_g_hasH + "> ?owlClassH . ?owlClassH <" + Vocabulary.p_h_hasA + "> ?owlClassA . ?owlClassA <" + Vocabulary.p_a_stringAttribute + "> ?gOwlClassHOwlClassAStringAttribute . FILTER (?gOwlClassHOwlClassAStringAttribute > ?str) }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindOneOrderByQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.age > :age ORDER BY p.age DESC";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } ORDER BY DESC(?pAge) ";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(3, holder.getParameters().size());
    }

    @Test
    public void testParseFindOneOrderByNotInWhereQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.age > :age ORDER BY p.username DESC";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_username + "> ?username . ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } ORDER BY DESC(?username) ";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(4, holder.getParameters().size());
    }

    @Test
    public void testParseFindOneGroupByQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.age > :age GROUP BY p.age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } GROUP BY ?pAge ";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(3, holder.getParameters().size());
    }

    @Test
    public void testParseFindOneGroupByNotInWhereQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.age > :age GROUP BY p.gender";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_gender + "> ?gender . ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } GROUP BY ?gender ";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(4, holder.getParameters().size());
    }

    @Test
    public void testParseFindByOneNotQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE NOT p.username = :username";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_username + "> ?username . } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(2, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleAndQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.username = :username AND p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_username + "> ?username . ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(4, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleNotAndQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE NOT p.username = :username AND p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_username + "> ?username . } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(4, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleAndNotQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.username = :username AND NOT p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_username + "> ?username . FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(4, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleNotAndNotQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE NOT p.username = :username AND NOT p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_username + "> ?username . ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(4, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleOrQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.phone.number = :phoneNumber OR p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { ?x <" + Vocabulary.p_p_hasPhone + "> ?phone . ?phone <" + Vocabulary.p_p_phoneNumber + "> ?phoneNumber . } UNION { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleNotOrQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE NOT p.username = :username OR p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_username + "> ?username . } } UNION { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(4, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleOrNotQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.username = :username OR NOT p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { ?x <" + Vocabulary.p_p_username + "> ?username . } UNION { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(4, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleNotOrNotQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE NOT p.username = :username OR NOT p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_username + "> ?username . } } UNION { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(4, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleOrOrderByNotInWhereQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.age > :age OR p.gender = :gender ORDER BY p.username DESC";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_username + "> ?username . { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } UNION { ?x <" + Vocabulary.p_p_gender + "> ?gender . } } ORDER BY DESC(?username) ";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleOrGroupByNotInWhereQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.age > :age OR p.gender = :gender GROUP BY p.username DESC";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_username + "> ?username . { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } UNION { ?x <" + Vocabulary.p_p_gender + "> ?gender . } } GROUP BY ?username ";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleAndOrQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.phone.number = :phoneNumber AND p.gender = :gender OR p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { ?x <" + Vocabulary.p_p_hasPhone + "> ?phone . ?phone <" + Vocabulary.p_p_phoneNumber + "> ?phoneNumber . ?x <" + Vocabulary.p_p_gender + "> ?gender . } UNION { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(6, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleNotAndOrQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE NOT p.phone.number = :phoneNumber AND p.gender = :gender OR p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { ?x <" + Vocabulary.p_p_gender + "> ?gender . FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_hasPhone + "> ?phone . ?phone <" + Vocabulary.p_p_phoneNumber + "> ?phoneNumber . } } UNION { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(6, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleAndNotOrQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.phone.number = :phoneNumber AND NOT p.gender = :gender OR p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { ?x <" + Vocabulary.p_p_hasPhone + "> ?phone . ?phone <" + Vocabulary.p_p_phoneNumber + "> ?phoneNumber . FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_gender + "> ?gender . } } UNION { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(6, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleAndOrNotQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.phone.number = :phoneNumber AND p.gender = :gender OR NOT p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { ?x <" + Vocabulary.p_p_hasPhone + "> ?phone . ?phone <" + Vocabulary.p_p_phoneNumber + "> ?phoneNumber . ?x <" + Vocabulary.p_p_gender + "> ?gender . } UNION { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(6, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleNotAndNotOrQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE NOT p.username = :username AND NOT p.gender = :gender OR p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_username + "> ?username . ?x <" + Vocabulary.p_p_gender + "> ?gender . } } UNION { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleAndNotOrNotQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.username = :username AND NOT p.gender = :gender OR NOT p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { ?x <" + Vocabulary.p_p_username + "> ?username . FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_gender + "> ?gender . } } UNION { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleNotAndOrNotQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE NOT p.username = :username AND p.gender = :gender OR NOT p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { ?x <" + Vocabulary.p_p_gender + "> ?gender . FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_username + "> ?username . } } UNION { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleAndAndQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.username = :username AND p.gender = :gender AND p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_username + "> ?username . ?x <" + Vocabulary.p_p_gender + "> ?gender . ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleNotAndAndQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE NOT p.username = :username AND p.gender = :gender AND p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_gender + "> ?gender . ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_username + "> ?username . } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleAndNotAndQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.username = :username AND NOT p.gender = :gender AND p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_username + "> ?username . ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_gender + "> ?gender . } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleAndAndNotQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.username = :username AND p.gender = :gender AND NOT p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_username + "> ?username . ?x <" + Vocabulary.p_p_gender + "> ?gender . FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleNotAndNotAndQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE NOT p.username = :username AND NOT p.gender = :gender AND p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_username + "> ?username . ?x <" + Vocabulary.p_p_gender + "> ?gender . } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleAndNotAndNotQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.username = :username AND NOT p.gender = :gender AND NOT p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_username + "> ?username . FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_gender + "> ?gender . ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleNotAndAndNotQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE NOT p.username = :username AND p.gender = :gender AND NOT p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_gender + "> ?gender . FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_username + "> ?username . ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleNotAndNotAndNotQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE NOT p.username = :username AND NOT p.gender = :gender AND NOT p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_username + "> ?username . ?x <" + Vocabulary.p_p_gender + "> ?gender . ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleOrOrQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.username = :username OR p.gender = :gender OR p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { ?x <" + Vocabulary.p_p_username + "> ?username . } UNION { ?x <" + Vocabulary.p_p_gender + "> ?gender . } UNION { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleNotOrOrQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE NOT p.username = :username OR p.gender = :gender OR p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_username + "> ?username . } } UNION { ?x <" + Vocabulary.p_p_gender + "> ?gender . } UNION { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleOrNotOrQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.username = :username OR NOT p.gender = :gender OR p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { ?x <" + Vocabulary.p_p_username + "> ?username . } UNION { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_gender + "> ?gender . } } UNION { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleOrOrNotQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.username = :username OR p.gender = :gender OR NOT p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { ?x <" + Vocabulary.p_p_username + "> ?username . } UNION { ?x <" + Vocabulary.p_p_gender + "> ?gender . } UNION { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleNotOrNotOrQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE NOT p.username = :username OR NOT p.gender = :gender OR p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_username + "> ?username . } } UNION { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_gender + "> ?gender . } } UNION { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleOrNotOrNotQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.username = :username OR NOT p.gender = :gender OR NOT p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { ?x <" + Vocabulary.p_p_username + "> ?username . } UNION { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_gender + "> ?gender . } } UNION { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleNotOrOrNotQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE NOT p.username = :username OR p.gender = :gender OR NOT p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_username + "> ?username . } } UNION { ?x <" + Vocabulary.p_p_gender + "> ?gender . } UNION { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    public void testParseFindByMultipleNotOrNotOrNotQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE NOT p.username = :username OR NOT p.gender = :gender OR NOT p.age > :age";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_username + "> ?username . } } UNION { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_gender + "> ?gender . } } UNION { FILTER NOT EXISTS { ?x <" + Vocabulary.p_p_age + "> ?pAge . FILTER (?pAge > ?age) } } }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(5, holder.getParameters().size());
    }

    @Test
    void testParseFindByAttributeValueInVariable() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.username IN :authorizedUsers";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_username + "> ?pUsername . FILTER (?pUsername IN (?authorizedUsers)) }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertNotNull(holder.getParameter("authorizedUsers"));
    }

    @Test
    void testParseFindByAttributeValueNotInVariable() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.username NOT IN :authorizedUsers";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_username + "> ?pUsername . FILTER (?pUsername NOT IN (?authorizedUsers)) }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertNotNull(holder.getParameter("authorizedUsers"));
    }

    @Test
    public void testParseFindOneNotLikeQuery() {
        final String soqlQuery = "SELECT p FROM Person p WHERE p.username NOT LIKE :username";
        final String expectedSparqlQuery = "SELECT ?x WHERE { ?x a <" + Vocabulary.c_Person + "> . ?x <" + Vocabulary.p_p_username + "> ?pUsername . FILTER (!regex(?pUsername, ?username) ) }";
        final QueryHolder holder = sut.parseQuery(soqlQuery);
        assertEquals(expectedSparqlQuery, holder.getQuery());
        assertEquals(3, holder.getParameters().size());
        assertNotNull(holder.getParameter("username"));
    }
}
