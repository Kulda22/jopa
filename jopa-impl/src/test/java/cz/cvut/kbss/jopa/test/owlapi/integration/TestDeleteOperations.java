package cz.cvut.kbss.jopa.test.owlapi.integration;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.owlapi.OWLAPIPersistenceProperties;
import cz.cvut.kbss.jopa.test.TestEnvironment;
import cz.cvut.kbss.jopa.test.integration.runners.DeleteOperationsRunner;
import cz.cvut.kbss.jopa.test.utils.OwlapiStorageConfig;
import cz.cvut.kbss.jopa.test.utils.StorageConfig;
import cz.cvut.kbss.ontodriver.OntoDriverProperties;

public class TestDeleteOperations {

	private static final Logger LOG = Logger.getLogger(TestDeleteOperations.class.getName());

	private static final StorageConfig storage = initStorage();
	private static final Map<String, String> properties = initProperties();

	private static DeleteOperationsRunner runner;

	private EntityManager em;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		runner = new DeleteOperationsRunner();
	}

	@After
	public void tearDown() throws Exception {
		if (em.isOpen()) {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
			em.getEntityManagerFactory().close();
		}
		runner.initBeforeTest();
	}

	@Test
	public void testRemoveReference() {
		LOG.config("Test: remove entity referenced by another entity.");
		em = TestEnvironment.getPersistenceConnector("OwlapiRemoveReference", storage, true,
				properties);
		runner.removeReference(em, context());
	}

	@Test
	public void testRemoveCascade() {
		LOG.config("Test: remove cascade.");
		em = TestEnvironment.getPersistenceConnector("OwlapiRemoveCascade", storage, true,
				properties);
		runner.removeCascade(em, context());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveDetached() {
		LOG.config("Test: try removing detached entity.");
		em = TestEnvironment.getPersistenceConnector("OwlapiRemoveDetached", storage, true,
				properties);
		runner.removeDetached(em, context());
	}

	@Test
	public void testRemoveFromSimpleList() {
		LOG.config("Test: remove entity from simple list.");
		em = TestEnvironment.getPersistenceConnector("OwlapiRemoveFromSimpleList", storage, true,
				properties);
		runner.removeFromSimpleList(em, context());
	}

	@Test
	public void testRemoveFromReferencedList() {
		LOG.config("Test: remove entity from referenced list.");
		em = TestEnvironment.getPersistenceConnector("OwlapiRemoveFromReferencedList", storage,
				true, properties);
		runner.removeFromReferencedList(em, context());
	}

	@Test
	public void testRemoveListOwner() {
		LOG.config("Test: remove owner of simple and referenced list.");
		em = TestEnvironment.getPersistenceConnector("OwlapiRemoveListOwner", storage, true,
				properties);
		runner.removeListOwner(em, context());
	}

	private URI context() {
		return null;
	}

	private static StorageConfig initStorage() {
		return new OwlapiStorageConfig();
	}

	private static Map<String, String> initProperties() {
		final Map<String, String> map = new HashMap<>();
		map.put(OntoDriverProperties.USE_TRANSACTIONAL_ONTOLOGY, Boolean.TRUE.toString());
		map.put(OWLAPIPersistenceProperties.LANG, "en");
		return map;
	}
}
