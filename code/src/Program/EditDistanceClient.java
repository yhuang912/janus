// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.math.*;

import YaoGC.*;
import Utils.*;

public class EditDistanceClient extends ProgClient {
	private BigInteger cdna;
	private BigInteger ssecmask;
	private BigInteger csecmask;
	private int sSecBitLen, cSecBitLen;
	private BigInteger[] sdnalbs, cdnalbs;

	private State outputState;

	public EditDistanceClient(BigInteger dna, BigInteger secMask, int length) {
		cdna = dna;
		csecmask = secMask;
		EditDistanceCommon.cdnaLen = length;

		EditDistanceCommon.strCdna = EditDistanceServer.biToString(cdna,
				secMask, EditDistanceCommon.sigma, EditDistanceCommon.cdnaLen);
	}

	protected void init() throws Exception {
		EditDistanceCommon.oos.writeInt(EditDistanceCommon.cdnaLen);
		EditDistanceCommon.oos.writeObject(EditDistanceCommon.strCdna);
		EditDistanceCommon.oos.writeObject(csecmask);
		EditDistanceCommon.oos.flush();
		EditDistanceCommon.sdnaLen = EditDistanceCommon.ois.readInt();
		EditDistanceCommon.strSdna = (String) EditDistanceCommon.ois.readObject();
		ssecmask= (BigInteger) EditDistanceCommon.ois.readObject();

		EditDistanceCommon.initCircuits();

		otNumOfPairs = EditDistanceCommon.sigma * EditDistanceCommon.cdnaLen;

		super.init();
	}

	protected void execTransfer() throws Exception {
		int bytelength = (Wire.labelBitLength - 1) / 8 + 1;

		sdnalbs = new BigInteger[EditDistanceCommon.sigma
				* EditDistanceCommon.sdnaLen];
		for (int i = 0; i < sdnalbs.length; i++) {
			if (ssecmask.testBit(i))
				sdnalbs[i] = Utils.readBigInteger(bytelength,
						EditDistanceCommon.ois);
			else
				sdnalbs[i] = null;
		}
		StopWatch.taskTimeStamp("receiving labels for peer's inputs");

		cdnalbs = new BigInteger[EditDistanceCommon.sigma
				* EditDistanceCommon.cdnaLen];
		BigInteger tmpchoice = BigInteger.ZERO;
		for (int i = 0, j = 0; i < cdnalbs.length; i++)
			if (csecmask.testBit(i)) {
				if (cdna.testBit(i))
					tmpchoice.setBit(j);
				j++;
			}

		rcver.execProtocol(tmpchoice);
		BigInteger[] temp = rcver.getData();
		for (int i = 0, j = 0; i < cdnalbs.length; i++)
			if (csecmask.testBit(i))
				cdnalbs[i] = temp[j++];

		StopWatch.taskTimeStamp("receiving labels for self's inputs");
	}

	protected void execCircuit() throws Exception {
		outputState = EditDistanceCommon.execCircuit(sdnalbs, cdnalbs);
	}

	protected void interpretResult() throws Exception {
		EditDistanceCommon.oos.writeObject(outputState.wires);
		EditDistanceCommon.oos.flush();
	}

	protected void verify_result() throws Exception {
		EditDistanceCommon.oos.writeObject(cdna);
		EditDistanceCommon.oos.flush();
	}
}