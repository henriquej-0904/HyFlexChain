package pt.unl.fct.di.hyflexchain.planes.data.ledger;

import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import pt.unl.fct.di.hyflexchain.planes.data.block.HyFlexChainBlock;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.crypto.HashedObject;

public class JsonLedgerState implements LedgerState
{
	private List<HashedObject<HyFlexChainBlock>> list;

	/**
	 * 
	 */
	public JsonLedgerState() {
	}



	/**
	 * @param list
	 */
	public JsonLedgerState(List<HashedObject<HyFlexChainBlock>> list) {
		this.list = list;
	}

	@Override
	public void loadFullLedger(byte[] ledger) {
		var json = Utils.json;
		TypeReference<List<HashedObject<HyFlexChainBlock>>> ref =
			new TypeReference<>() { };

		try {
			this.list = json.readValue(ledger, ref);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] getFullLedger() {
		var json = Utils.json;

		try {
			return json.writeValueAsBytes(this.list);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Iterator<HashedObject<HyFlexChainBlock>> iterator() {
		return this.list.iterator();
	}


	@Override
	public String toString() {
		return new String(getFullLedger());
	}
	
}
