package com.example.state;

import com.example.contract.IOUContract;
import com.example.schema.IOUSchemaV1;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;

import java.util.Arrays;
import java.util.List;

/**
 * The state object recording IOU agreements between two parties.
 *
 * A state must implement [ContractState] or one of its descendants.
 */
@BelongsToContract(IOUContract.class)
public class IOUState implements LinearState, QueryableState {
    private final String key;
    private final Party creator;
    private final Party follower;
    private final UniqueIdentifier linearId;

    /**
     * @param key the encrypted key of the IOU.
     * @param creator the party issuing the IOU.
     * @param follower the party receiving and approving the IOU.
     */
    public IOUState(String key,
                    Party creator,
                    Party follower,
                    UniqueIdentifier linearId)
    {
        this.key = key;
        this.creator = creator;
        this.follower = follower;
        this.linearId = linearId;
    }

    public String getKey() { return key; }
    public Party getCreator() { return creator; }
    public Party getFollower() { return follower; }
    @Override public UniqueIdentifier getLinearId() { return linearId; }
    @Override public List<AbstractParty> getParticipants() {
        return Arrays.asList(creator, follower);
    }

    @Override public PersistentState generateMappedObject(MappedSchema schema) {
        if (schema instanceof IOUSchemaV1) {
            return new IOUSchemaV1.PersistentIOU(
                    this.creator.getName().toString(),
                    this.follower.getName().toString(),
                    this.key,
                    this.linearId.getId());
        } else {
            throw new IllegalArgumentException("Unrecognised schema $schema");
        }
    }

    @Override public Iterable<MappedSchema> supportedSchemas() {
        return Arrays.asList(new IOUSchemaV1());
    }

    @Override
    public String toString() {
        return String.format("IOUState(key=%s, creator=%s, follower=%s, linearId=%s)", key, creator, follower, linearId);
    }
}