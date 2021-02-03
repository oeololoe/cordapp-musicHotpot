package com.example.test.contract;

import com.example.contract.IOUContract;
import com.example.state.IOUState;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import static java.util.Arrays.asList;
import static net.corda.testing.node.NodeTestUtils.ledger;

public class IOUContractTests {
    static private final MockServices ledgerServices = new MockServices(asList("com.example.contract", "com.example.flow"));
    static private final TestIdentity megaCorp = new TestIdentity(new CordaX500Name("MegaCorp", "London", "GB"));
    static private final TestIdentity miniCorp = new TestIdentity(new CordaX500Name("MiniCorp", "London", "GB"));
    static private final String iouValue = "1";

    @Test
    public void transactionMustIncludeCreateCommand() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {
                tx.output(IOUContract.ID, new IOUState(iouValue, miniCorp.getParty(), megaCorp.getParty(), new UniqueIdentifier()));
                tx.fails();
                tx.command(ImmutableList.of(megaCorp.getPublicKey(), miniCorp.getPublicKey()), new IOUContract.Commands.Create());
                tx.verifies();
                return null;
            });
            return null;
        }));
    }

    @Test
    public void transactionMustHaveNoInputs() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {
                tx.input(IOUContract.ID, new IOUState(iouValue, miniCorp.getParty(), megaCorp.getParty(), new UniqueIdentifier()));
                tx.output(IOUContract.ID, new IOUState(iouValue, miniCorp.getParty(), megaCorp.getParty(), new UniqueIdentifier()));
                tx.command(ImmutableList.of(megaCorp.getPublicKey(), miniCorp.getPublicKey()), new IOUContract.Commands.Create());
                tx.failsWith("No inputs should be consumed when issuing an IOU.");
                return null;
            });
            return null;
        }));
    }

    @Test
    public void transactionMustHaveOneOutput() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {
                tx.output(IOUContract.ID, new IOUState(iouValue, miniCorp.getParty(), megaCorp.getParty(), new UniqueIdentifier()));
                tx.output(IOUContract.ID, new IOUState(iouValue, miniCorp.getParty(), megaCorp.getParty(), new UniqueIdentifier()));
                tx.command(ImmutableList.of(megaCorp.getPublicKey(), miniCorp.getPublicKey()), new IOUContract.Commands.Create());
                tx.failsWith("Only one output state should be created.");
                return null;
            });
            return null;
        }));
    }

    @Test
    public void lenderMustSignTransaction() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {
                tx.output(IOUContract.ID, new IOUState(iouValue, miniCorp.getParty(), megaCorp.getParty(), new UniqueIdentifier()));
                tx.command(miniCorp.getPublicKey(), new IOUContract.Commands.Create());
                tx.failsWith("All of the participants must be signers.");
                return null;
            });
            return null;
        }));
    }

    @Test
    public void borrowerMustSignTransaction() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {
                tx.output(IOUContract.ID, new IOUState(iouValue, miniCorp.getParty(), megaCorp.getParty(), new UniqueIdentifier()));
                tx.command(megaCorp.getPublicKey(), new IOUContract.Commands.Create());
                tx.failsWith("All of the participants must be signers.");
                return null;
            });
            return null;
        }));
    }

    @Test
    public void lenderIsNotBorrower() {
        final TestIdentity megaCorpDupe = new TestIdentity(megaCorp.getName(), megaCorp.getKeyPair());
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {
                tx.output(IOUContract.ID, new IOUState(iouValue, megaCorp.getParty(), megaCorpDupe.getParty(), new UniqueIdentifier()));
                tx.command(ImmutableList.of(megaCorp.getPublicKey(), miniCorp.getPublicKey()), new IOUContract.Commands.Create());
                tx.failsWith("The creator and the follower cannot be the same entity.");
                return null;
            });
            return null;
        }));
    }

    @Test
    public void cannotCreateEmptyIOUs() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {
                tx.output(IOUContract.ID, new IOUState("", miniCorp.getParty(), megaCorp.getParty(), new UniqueIdentifier()));
                tx.command(ImmutableList.of(megaCorp.getPublicKey(), miniCorp.getPublicKey()), new IOUContract.Commands.Create());
                tx.failsWith("The IOU's key must be non-empty.");
                return null;
            });
            return null;
        }));
    }
}