package com.game.repository;

import com.game.entity.Player;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;


@Repository(value = "db")
@NamedQueries({
        @NamedQuery(name = "all_count", query = "select count(*) from Player")
})
public class PlayerRepositoryDB implements IPlayerRepository {
    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Configuration configuration = new Configuration().addAnnotatedClass(Player.class)
                    .setProperty(Environment.HBM2DDL_AUTO,"update");
            this.sessionFactory = configuration.buildSessionFactory();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No class found");
        }
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        try(Session session = sessionFactory.openSession()){
            NativeQuery<Player> nativeQuery = session
                    .createNativeQuery("select * from player", Player.class);
            nativeQuery.setMaxResults(pageSize);
            nativeQuery.setFirstResult(pageSize*pageNumber);
            return nativeQuery.getResultList();
        }
    }

    @Override
    public int getAllCount() {
        try(Session session = sessionFactory.openSession()){
            Query<Integer> allCount = session.createNamedQuery("all_count", Integer.class);
            return allCount.uniqueResult();
        }
    }

    @Override
    public Player save(Player player) {
        try(Session session = sessionFactory.openSession()){
            session.beginTransaction();
            session.persist(player);
            session.getTransaction().commit();
            return player;
        }
    }

    @Override
    public Player update(Player player) {
        try(Session session = sessionFactory.openSession()){
            session.beginTransaction();
            Player updated = (Player) session.merge(player);
            session.getTransaction().commit();
            return updated;
        }
    }

    @Override
    public Optional<Player> findById(long id) {
        try(Session session = sessionFactory.openSession()){
            return Optional.of(session.get(Player.class, id));
        }
    }

    @Override
    public void delete(Player player) {
        try(Session session = sessionFactory.openSession()){
            session.beginTransaction();
            session.delete(player);
            session.getTransaction().commit();
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}