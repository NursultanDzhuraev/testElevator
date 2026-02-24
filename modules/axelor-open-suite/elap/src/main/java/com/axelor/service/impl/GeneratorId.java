package com.axelor.service.impl;

import com.axelor.db.JPA;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.Query;

@Slf4j
@Singleton
public class GeneratorId {

    private static final long INITIAL_VALUE = 100L;

    public synchronized String generate(String key) {
        try {
            EntityManager em = JPA.em();

            String sequenceName = buildSequenceName(key);
            createSequenceIfNotExists(em, sequenceName);

            Query query =
                    em.createNativeQuery("SELECT nextval('" + sequenceName + "')");

            long nextVal = ((Number) query.getSingleResult()).longValue();

            return formatId(key, nextVal);

        } catch (Exception e) {
            log.error("ID generation failed for key={}", key, e);
            throw new RuntimeException("ID generation failed for key=" + key, e);
        }
    }

    private String buildSequenceName(String key) {
        return "acs_" + key.toLowerCase() + "_id_seq";
    }

    private void createSequenceIfNotExists(EntityManager em, String sequenceName) {
        Query checkQuery =
                em.createNativeQuery(
                        "SELECT EXISTS (SELECT 1 FROM pg_sequences WHERE schemaname = 'public' AND sequencename = ?)");
        checkQuery.setParameter(1, sequenceName);

        boolean exists = (Boolean) checkQuery.getSingleResult();

        if (!exists) {
            Query createQuery =
                    em.createNativeQuery(
                            "CREATE SEQUENCE IF NOT EXISTS "
                                    + sequenceName
                                    + " START WITH "
                                    + INITIAL_VALUE
                                    + " INCREMENT BY 1");
            createQuery.executeUpdate();
        }
    }

    private String formatId(String key, long value) {
        String prefix = key.substring(0, Math.min(3, key.length())).toUpperCase();
        return prefix + "-" + String.format("%06d", value);
    }
}

