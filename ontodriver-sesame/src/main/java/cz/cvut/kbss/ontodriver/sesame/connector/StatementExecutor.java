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
package cz.cvut.kbss.ontodriver.sesame.connector;

import cz.cvut.kbss.ontodriver.sesame.exceptions.SesameDriverException;
import org.eclipse.rdf4j.query.TupleQueryResult;

public interface StatementExecutor {

    /**
     * Executes the specified query and returns result in form of a Sesame query
     * result.
     *
     * @param query The query to execute
     * @return Tuple query result
     * @throws SesameDriverException When things go wrong with query execution
     */
    TupleQueryResult executeSelectQuery(String query) throws SesameDriverException;

    /**
     * Executes the specified boolean query.
     * <p>
     * This method is intended mostly for SPARQL ASK queries.
     *
     * @param query The query to execute
     * @return Boolean result of the query
     * @throws SesameDriverException When things go wrong with query execution
     */
    boolean executeBooleanQuery(String query) throws SesameDriverException;

    /**
     * Executes the specified update query.
     *
     * @param query The query to execute
     * @throws SesameDriverException When things go wrong with query execution
     */
    void executeUpdate(String query) throws SesameDriverException;
}
