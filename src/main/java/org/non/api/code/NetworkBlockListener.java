package org.non.api.code;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.protos.ledger.rwset.kvrwset.KvRwset;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockListener;
import org.hyperledger.fabric.sdk.TxReadWriteSetInfo;
import org.hyperledger.fabric.sdk.exception.InvalidProtocolBufferRuntimeException;

import com.google.protobuf.InvalidProtocolBufferException;

import org.hyperledger.fabric.sdk.BlockInfo;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import static java.lang.String.format;
import static org.hyperledger.fabric.sdk.BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE;
import static org.junit.Assert.assertEquals;


public class NetworkBlockListener implements BlockListener {
	private static Logger logger = LogManager.getLogger(HyperledgerAPI.class);

//	 private static final Map<String, String> TX_EXPECTED;
//
//	    static {
//	        TX_EXPECTED = new HashMap<>();
//	        TX_EXPECTED.put("readset1", "Missing readset for channel bar block 1");
//	        TX_EXPECTED.put("writeset1", "Missing writeset for channel bar block 1");
//	    }
	    
	@Override
	public void received(BlockEvent blockEvent) {
		// TODO Auto-generated method stub
		System.out.println("TEST: NetworkBlockListener got blockEvent!");
		try {
			final long blockNumber = blockEvent.getBlockNumber();
            final int envelopCount = blockEvent.getEnvelopCount();//One envelop for one transaction 

            out("current block number %d has %d envelope count:", blockNumber, envelopCount);
            int i = 0;
            
            //Iterate through the transactions in this block
            for (BlockInfo.EnvelopeInfo envelopeInfo : blockEvent.getEnvelopeInfos()) {
                ++i;

                out("  Transaction number %d has transaction id: %s", i, envelopeInfo.getTransactionID());
                final String channelId = envelopeInfo.getChannelId();
                out("  Transaction number %d has channel id: %s", i, channelId);
                out("  Transaction number %d has transaction timestamp: %tB %<te,  %<tY  %<tT %<Tp", i, envelopeInfo.getTimestamp());
                out("  Transaction number %d has type id: %s", i, "" + envelopeInfo.getType());

                if (envelopeInfo.getType() == TRANSACTION_ENVELOPE) {
                    BlockInfo.TransactionEnvelopeInfo transactionEnvelopeInfo = (BlockInfo.TransactionEnvelopeInfo) envelopeInfo;

                    out("  Transaction number %d has %d actions", i, transactionEnvelopeInfo.getTransactionActionInfoCount());
                    out("  Transaction number %d isValid %b", i, transactionEnvelopeInfo.isValid());

                    int j = 0;
                    for (BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo transactionActionInfo : transactionEnvelopeInfo.getTransactionActionInfos()) {
                        ++j;
                        out("   Transaction action %d has response status %d", j, transactionActionInfo.getResponseStatus());
                        assertEquals(200, transactionActionInfo.getResponseStatus());
                        out("   Transaction action %d has response message bytes as string: %s", j,
                                printableString(new String(transactionActionInfo.getResponseMessageBytes(), "UTF-8")));
                        out("   Transaction action %d has %d endorsements", j, transactionActionInfo.getEndorsementsCount());

//                        for (int n = 0; n < transactionActionInfo.getEndorsementsCount(); ++n) {
//                            BlockInfo.EndorserInfo endorserInfo = transactionActionInfo.getEndorsementInfo(n);
//                            out("Endorser %d signature: %s", n, Hex.encodeHexString(endorserInfo.getSignature()));
//                            out("Endorser %d endorser: %s", n, new String(endorserInfo.getEndorser(), "UTF-8"));
//                        }
                        out("   Transaction action %d has %d chaincode input arguments", j, transactionActionInfo.getChaincodeInputArgsCount());
                        for (int z = 0; z < transactionActionInfo.getChaincodeInputArgsCount(); ++z) {
                            out("     Transaction action %d has chaincode input argument %d is: %s", j, z,
                                    printableString(new String(transactionActionInfo.getChaincodeInputArgs(z), "UTF-8")));
                        }

                        out("   Transaction action %d proposal response status: %d", j,
                                transactionActionInfo.getProposalResponseStatus());
                        out("   Transaction action %d proposal response payload: %s", j,
                                printableString(new String(transactionActionInfo.getProposalResponsePayload())));

                        TxReadWriteSetInfo rwsetInfo = transactionActionInfo.getTxReadWriteSet();
                        if (null != rwsetInfo) {
                            out("   Transaction action %d has %d name space read write sets", j, rwsetInfo.getNsRwsetCount());

                            for (TxReadWriteSetInfo.NsRwsetInfo nsRwsetInfo : rwsetInfo.getNsRwsetInfos()) {
                                final String namespace = nsRwsetInfo.getNaamespace();
                                KvRwset.KVRWSet rws = nsRwsetInfo.getRwset();

                                int rs = -1;
                                for (KvRwset.KVRead readList : rws.getReadsList()) {
                                    rs++;
                                    out("     Namespace %s read set %d key %s  version [%d:%d]", namespace, rs, readList.getKey(),
                                            readList.getVersion().getBlockNum(), readList.getVersion().getTxNum());
                                }

                                rs = -1;
                                for (KvRwset.KVWrite writeList : rws.getWritesList()) {
                                    rs++;
                                    String valAsString = printableString(new String(writeList.getValue().toByteArray(), "UTF-8"));
                                    out("     Namespace %s write set %d key %s has value '%s' ", namespace, rs,
                                            writeList.getKey(),
                                            valAsString);
                                }
                            }
                        }
                    }
                }
            }

    } catch (InvalidProtocolBufferRuntimeException e) {
        try {
			throw e.getCause();
		} catch (InvalidProtocolBufferException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}


	}
	
	 static String printableString(final String string) {
	        int maxLogStringLength = 64;
	        if (string == null || string.length() == 0) {
	            return string;
	        }

	        String ret = string.replaceAll("[^\\p{Print}]", "?");

	        ret = ret.substring(0, Math.min(ret.length(), maxLogStringLength)) + (ret.length() > maxLogStringLength ? "..." : "");

	        return ret;

	    }

	    static void out(String format, Object... args) {

	        System.err.flush();
	        System.out.flush();

	        System.out.println(format(format, args));
	        System.err.flush();
	        System.out.flush();

	    }


}
