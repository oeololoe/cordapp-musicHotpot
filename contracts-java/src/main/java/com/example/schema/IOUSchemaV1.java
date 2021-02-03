package com.example.schema;

import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.UUID;
//4.6 changes
import org.hibernate.annotations.Type;
import javax.annotation.Nullable;

/**
 * An IOUState schema.
 */
public class IOUSchemaV1 extends MappedSchema {
    public IOUSchemaV1() {
        super(IOUSchema.class, 1, Arrays.asList(PersistentIOU.class));
    }

    @Nullable
    @Override
    public String getMigrationResource() {
        return "iou.changelog-master";
    }

    @Entity
    @Table(name = "iou_states")
    public static class PersistentIOU extends PersistentState {
        @Column(name = "creator") private final String creator;
        @Column(name = "follower") private final String follower;
        @Column(name = "key") private final String key;
        @Column(name = "linear_id") @Type (type = "uuid-char") private final UUID linearId;


        public PersistentIOU(String creator, String follower, String key, UUID linearId) {
            this.creator = creator;
            this.follower = follower;
            this.key = key;
            this.linearId = linearId;
        }

        // Default constructor required by hibernate.
        public PersistentIOU() {
            this.creator = null;
            this.follower = null;
            this.key = null;
            this.linearId = null;
        }

        public String getCreator() {
            return creator;
        }

        public String getFollower() {
            return follower;
        }

        public String getKey() {
            return key;
        }

        public UUID getId() {
            return linearId;
        }
    }
}