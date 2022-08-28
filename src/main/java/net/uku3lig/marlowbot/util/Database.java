package net.uku3lig.marlowbot.util;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.uku3lig.marlowbot.Main;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.io.Serializable;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Slf4j
public class Database {
    @Getter
    private static final Database instance = new Database();
    private SessionFactory factory;

    private Database() {
        try {
            if (!Files.exists(Main.DB_PATH)) Files.createFile(Main.DB_PATH);

            Configuration cfg = new Configuration().configure("hibernate.cfg.xml");

            ClassScanner.findAnnotated(Entity.class).forEach(cfg::addAnnotatedClass);

            final ServiceRegistry registry = new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml").build();
            factory = cfg.buildSessionFactory(registry);
        } catch (Exception e) {
            log.error("Execption encountered while trying to read start db connection");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Tests if an object is a Hibernate entity.
     * @param o The object.
     * @return <code>true</code> if the <code>o</code> is an entity
     */
    private static boolean isEntity(Object o) {
        return isEntity(o.getClass());
    }

    /**
     * @see #isEntity(Object)
     */
    private static boolean isEntity(Class<?> klass) {
        return klass.isAnnotationPresent(Entity.class);
    }

    /**
     * Saves (or updates) entities in the database.
     * @param entities The entities to save or update.
     * @param <T> The type of the entity, to ensure they all are entities with only one check.
     * @return <code>true</code> if nothing was wrong.
     */
    @SafeVarargs
    public final <T> boolean saveOrUpdate(T... entities) {
        if (entities.length == 0 || !isEntity(entities[0])) return false;
        try (var s = factory.openSession()) {
            s.beginTransaction();
            Arrays.stream(entities).forEach(s::merge);
            s.getTransaction().commit();
            return true;
        } catch (Exception e) {
            log.error("An error happened while trying to saveOrUpdate a {}: {}", entities[0].getClass().getSimpleName(), e.getClass().getSimpleName());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes entities in the database (if they are in there).
     * @param entities The entities to delete.
     * @param <T> The type of the entity, to ensure they all are entities with only one check.
     * @return <code>true</code> if nothing was wrong.
     */
    @SafeVarargs
    public final <T> boolean delete(T... entities) {
        if (entities.length == 0 || !isEntity(entities[0])) return false;
        try (var s = factory.openSession()) {
            s.beginTransaction();
            Arrays.stream(entities).forEach(s::remove);
            s.getTransaction().commit();
            return true;
        } catch (Exception e) {
            log.error("An error happened while trying to delete a {}: {}", entities[0].getClass().getSimpleName(), e.getClass().getSimpleName());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Finds an entity in the database with its type and id.
     * @param klass The class of the entity.
     * @param id The id of the entity.
     * @param <T> The type of the entity.
     * @return An optional containing the object found in database.
     */
    public <T> Optional<T> getById(Class<T> klass, Serializable id) {
        if (!isEntity(klass)) return Optional.empty();
        try (var s = factory.openSession()) {
            s.beginTransaction();
            Optional<T> o = Optional.ofNullable(s.get(klass, id));
            s.getTransaction().commit();
            return o;
        } catch (Exception e) {
            log.error("An error happened while trying to get a {}: {}", klass.getSimpleName(), e.getClass().getSimpleName());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Gets all the entities in a table. <br>
     * You should use {@link #getById(Class, Serializable)} to get a specific entity, instead of using streams.
     * @param klass The class linked to the table in the database.
     * @param <T> The type of the class.
     * @return A collection of the found entities, can be empty.
     */
    public <T> Collection<T> getAll(Class<T> klass) {
        if (!isEntity(klass)) return Collections.emptySet();
        try (var s = factory.openSession()) {
            return s.createQuery("SELECT a FROM " + klass.getSimpleName() + " a", klass).getResultList();
        }
    }
}
