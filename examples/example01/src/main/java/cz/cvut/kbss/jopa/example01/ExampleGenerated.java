package cz.cvut.kbss.jopa.example01;

import cz.cvut.kbss.jopa.example01.generated.model.ConferencePaper;
import cz.cvut.kbss.jopa.example01.generated.model.Course;
import cz.cvut.kbss.jopa.example01.model.UndergraduateStudent;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This example works with the entity classes generated by OWL2Java.
 */
public class ExampleGenerated {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleGenerated.class);

    private EntityManager em = PersistenceFactory.createEntityManager();

    public static void main(String[] args) {
        new ExampleGenerated().run();
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
        final cz.cvut.kbss.jopa.example01.generated.model.UndergraduateStudent student = initStudent();
        em.persist(student);
        student.getTakesCourse().forEach(em::persist);
        student.getIsAuthorOf().forEach(em::persist);
        em.getTransaction().commit();

        LOG.info("Loading example data...");
        final cz.cvut.kbss.jopa.example01.generated.model.UndergraduateStudent loaded = em.find(
                cz.cvut.kbss.jopa.example01.generated.model.UndergraduateStudent.class, student.getId());
        assert loaded != null;
        LOG.info("Loaded {}", loaded);

        LOG.info("Updating example data...");
        em.getTransaction().begin();
        loaded.setTelephone("CTN 0452-9");
        em.getTransaction().commit();

        final cz.cvut.kbss.jopa.example01.generated.model.UndergraduateStudent result = em.find(
                cz.cvut.kbss.jopa.example01.generated.model.UndergraduateStudent.class, student.getId());
        assert loaded.getTelephone().equals(result.getTelephone());
        LOG.info("Loaded {}", result);

        LOG.info("Deleting example data...");
        em.getTransaction().begin();
        em.remove(result);
        em.getTransaction().commit();

        assert em.find(UndergraduateStudent.class, student.getId()) == null;
    }

    private cz.cvut.kbss.jopa.example01.generated.model.UndergraduateStudent initStudent() {
        final Set<String> types = new HashSet<>();
        types.add("http://www.oni.unsc.org/types#Man");
        types.add("http://www.oni.unsc.org/types#ManSpartanII");
        final Set<Course> courses = new HashSet<>();
        Course course = new Course();
        course.setId("http://www.Department0.University0.edu/Course45");
        course.setName("Hand combat");
        courses.add(course);
        course = new Course();
        course.setId("http://www.Department0.University0.edu/Course41");
        course.setName("Special Weapons");
        courses.add(course);
        course = new Course();
        course.setId("http://www.Department0.University0.edu/Course23");
        course.setName("Combat tactics");
        courses.add(course);
        course = new Course();
        course.setId("http://www.Department0.University0.edu/Course11");
        course.setName("Halo");
        courses.add(course);
        final cz.cvut.kbss.jopa.example01.generated.model.UndergraduateStudent student = new cz.cvut.kbss.jopa.example01.generated.model.UndergraduateStudent();
        student.setId("http://www.oni.unsc.org/spartanII/John117");
        student.setFirstName("Master");
        student.setLastName("Chief");
        student.setEmailAddress("spartan-117@unsc.org");
        student.setTelephone("xxxxxxxxxxxx-xxxx");
        student.setTypes(types);
        final ConferencePaper paper = new ConferencePaper();
        paper.setName1("ConferencePaperP");
        student.setIsAuthorOf(Collections.singleton(paper));
        student.setTakesCourse(courses);
        return student;
    }
}
