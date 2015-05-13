package cz.cvut.kbss.jopa.example01;

import cz.cvut.kbss.jopa.example01.model.ConferencePaper;
import cz.cvut.kbss.jopa.example01.model.Course;
import cz.cvut.kbss.jopa.example01.model.UndergraduateStudent;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This example uses classes corresponding to those generated, but written by hand with nicer structure.
 * <p/>
 * The hand-written classes also don't have the rdfs:label, description, types and properties fields.
 */
public class Example {

    private static final Logger LOG = LoggerFactory.getLogger(Example.class);

    private EntityManager em = PersistenceFactory.createEntityManager();

    public static void main(String[] args) {
        new Example().run();
    }

    private void run() {
        try {
            runImpl();
        } finally {
            em.close();
            PersistenceFactory.close();
        }
    }

    private void runImpl() {
        LOG.info("Persisting example data...");
        em.getTransaction().begin();
        final UndergraduateStudent student = initStudent();
        em.persist(student);
        student.getCourses().forEach(em::persist);
        em.getTransaction().commit();

        LOG.info("Loading example data...");
        final UndergraduateStudent loaded = em.find(UndergraduateStudent.class, student.getUri());
        assert loaded != null;
        LOG.info("Loaded {}", loaded);

        LOG.info("Updating example data...");
        em.getTransaction().begin();
        loaded.setTelephone("CTN 0452-9");
        em.getTransaction().commit();

        final UndergraduateStudent result = em.find(UndergraduateStudent.class, student.getUri());
        assert loaded.getTelephone().equals(result.getTelephone());
        LOG.info("Loaded {}", result);

        LOG.info("Deleting example data...");
        em.getTransaction().begin();
        em.remove(result);
        em.getTransaction().commit();

        assert em.find(UndergraduateStudent.class, student.getUri()) == null;
    }

    private UndergraduateStudent initStudent() {
        final Set<String> types = new HashSet<>();
        types.add("http://www.oni.unsc.org/types#Man");
        types.add("http://www.oni.unsc.org/types#ManSpartanII");
        final Set<Course> courses = new HashSet<>();
        courses.add(new Course(URI.create("http://www.Department0.University0.edu/Course45"), "Hand combat"));
        courses.add(new Course(URI.create("http://www.Department0.University0.edu/Course41"), "Special Weapons"));
        courses.add(new Course(URI.create("http://www.Department0.University0.edu/Course23"), "Combat tactics"));
        courses.add(new Course(URI.create("http://www.Department0.University0.edu/Course11"), "Halo"));
        final UndergraduateStudent student = new UndergraduateStudent();
        student.setUri(URI.create("http://www.oni.unsc.org/spartanII/John117"));
        student.setFirstName("Master");
        student.setLastName("Chief");
        student.setEmail("spartan-117@unsc.org");
        student.setTelephone("xxxxxxxxxxxx-xxxx");
        student.setTypes(types);
        final ConferencePaper paper = new ConferencePaper();
        paper.setName("ConferencePaperP");
        student.setPapers(Collections.singleton(paper));
        student.setCourses(courses);
        return student;
    }
}
