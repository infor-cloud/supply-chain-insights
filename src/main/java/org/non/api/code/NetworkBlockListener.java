package org.non.api.code;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockListener;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;

public class NetworkBlockListener implements BlockListener {

	@Override
	public void received(BlockEvent blockEvent) {
		// TODO Auto-generated method stub
		System.out.println("TEST: NetworkBlockListener got blockEvent!");
		System.out.println(blockEvent.toString());
		String str = "";
		try {
			str = new String(blockEvent.getTransActionsMetaData(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(blockEvent.getTransActionsMetaData());
		System.out.println(str);

	}

}
