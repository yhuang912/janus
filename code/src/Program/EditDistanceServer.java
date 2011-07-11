// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.math.*;

import YaoGC.*;
import Utils.*;

public class EditDistanceServer extends ProgServer {
	private BigInteger sdna;
	private BigInteger ssecmask;
	private BigInteger csecmask;
	private int cSecBitLen;
	private BigInteger[][] sdnalps, cdnalps;

	private State outputState;

	public EditDistanceServer(BigInteger dna, BigInteger secMask, int length) {
		sdna = dna;
		ssecmask = secMask;

		// length of dna sequence. Effective bit length of variable dna is sigma
		// times longer
		EditDistanceCommon.sdnaLen = length;

		EditDistanceCommon.strSdna = EditDistanceServer.biToString(sdna,
				secMask, EditDistanceCommon.sigma, EditDistanceCommon.sdnaLen);
	}

	protected void init() throws Exception {
		EditDistanceCommon.cdnaLen = EditDistanceCommon.ois.readInt();
		EditDistanceCommon.strCdna = (String) EditDistanceCommon.ois.readObject();
		csecmask= (BigInteger) EditDistanceCommon.ois.readObject();
		EditDistanceCommon.oos.writeInt(EditDistanceCommon.sdnaLen);
		EditDistanceCommon.oos.writeObject(EditDistanceCommon.strSdna);
		EditDistanceCommon.oos.writeObject(ssecmask);
		EditDistanceCommon.oos.flush();

		EditDistanceCommon.initCircuits();

		generateLabelPairsForDNAs();

		super.init();
	}

	private void generateLabelPairsForDNAs() {
		sdnalps = new BigInteger[EditDistanceCommon.sigma
				* EditDistanceCommon.sdnaLen][2];
		cdnalps = new BigInteger[EditDistanceCommon.sigma
				* EditDistanceCommon.cdnaLen][2];

		for (int i = 0; i < EditDistanceCommon.sdnaLen; i++) {
			if (ssecmask.testBit(i))
				for (int j = 0; j < EditDistanceCommon.sigma; j++)
					sdnalps[EditDistanceCommon.sigma * i + j] = Wire
							.newLabelPair();
			else
				for (int j = 0; j < EditDistanceCommon.sigma; j++)
					sdnalps[EditDistanceCommon.sigma * i + j] = null;
		}

		for (int i = 0; i < EditDistanceCommon.cdnaLen; i++) {
			if (csecmask.testBit(i))
				for (int j = 0; j < EditDistanceCommon.sigma; j++)
					cdnalps[EditDistanceCommon.sigma * i + j] = Wire
							.newLabelPair();
			else
				for (int j = 0; j < EditDistanceCommon.sigma; j++)
					cdnalps[EditDistanceCommon.sigma * i + j] = null;
		}
	}

	protected void execTransfer() throws Exception {
		int bytelength = (Wire.labelBitLength - 1) / 8 + 1;

		for (int i = 0; i < sdnalps.length; i++) {
			if (ssecmask.testBit(i)) {
				int idx = sdna.testBit(i) ? 1 : 0;
				Utils.writeBigInteger(sdnalps[i][idx], bytelength,
						EditDistanceCommon.oos);
			}
		}
		EditDistanceCommon.oos.flush();
		StopWatch.taskTimeStamp("sending labels for selfs inputs");

		BigInteger[][] temp = new BigInteger[cSecBitLen][2];
		for (int i = 0, j = 0; i < cdnalps.length; i++)
			if (csecmask.testBit(i))
				temp[j++] = cdnalps[i];
		snder.execProtocol(temp);
		StopWatch.taskTimeStamp("sending labels for peers inputs");
	}

	protected void execCircuit() throws Exception {
		BigInteger[] sdnalbs = new BigInteger[EditDistanceCommon.sigma
				* EditDistanceCommon.sdnaLen];
		BigInteger[] cdnalbs = new BigInteger[EditDistanceCommon.sigma
				* EditDistanceCommon.cdnaLen];

		for (int i = 0; i < sdnalps.length; i++)
			sdnalbs[i] = sdnalps[i][0];

		for (int i = 0; i < cdnalps.length; i++)
			cdnalbs[i] = cdnalps[i][0];

		outputState = EditDistanceCommon.execCircuit(sdnalbs, cdnalbs);
	}

	protected void interpretResult() throws Exception {
		BigInteger[] outLabels = (BigInteger[]) EditDistanceCommon.ois
				.readObject();

		BigInteger output = BigInteger.ZERO;
		for (int i = 0; i < outLabels.length; i++) {
			if (outputState.wires[i].value != Wire.UNKNOWN_SIG) {
				if (outputState.wires[i].value == 1)
					output = output.setBit(i);
				continue;
			} else if (outLabels[i]
					.equals(outputState.wires[i].invd ? outputState.wires[i].lbl
							: outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1)
									.setBit(0)))) {
				output = output.setBit(i);
			} else if (!outLabels[i]
					.equals(outputState.wires[i].invd ? outputState.wires[i].lbl
							.xor(Wire.R.shiftLeft(1).setBit(0))
							: outputState.wires[i].lbl))
				throw new Exception("Bad label encountered: i = "
						+ i
						+ "\t"
						+ outLabels[i]
						+ " != ("
						+ outputState.wires[i].lbl
						+ ", "
						+ outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1)
								.setBit(0)) + ")");
		}

		System.out.println("output (pp): " + output);
		StopWatch.taskTimeStamp("output labels received and interpreted");
	}

	static String biToString(BigInteger encoding, int sigma, int n) {
		StringBuilder res = new StringBuilder("");
		BigInteger mask = BigInteger.ONE.shiftLeft(sigma).subtract(
				BigInteger.ONE);

		for (int i = 0; i < n; i++) {
			res.append((char) encoding.shiftRight(i * sigma).and(mask)
					.intValue() + 36); // offset by 36 because '#' is used as a
										// special character that stands for a
										// symbolic value
		}
		return res.toString();
	}

	static String biToString(BigInteger encoding, BigInteger secMask,
			int sigma, int n) {
		StringBuilder res = new StringBuilder("");
		BigInteger mask = BigInteger.ONE.shiftLeft(sigma).subtract(
				BigInteger.ONE);

		for (int i = 0; i < n; i++) {
			if (secMask.testBit(sigma * i)) // '1' implies secret
				res.append('#');
			else
				res.append((char) encoding.shiftRight(i * sigma).and(mask)
						.intValue() + 36); // offset by 36 because '#' is used
			// as a special character that
			// stands for a symbolic value
		}
		return res.toString();
	}

	protected void verify_result() throws Exception {
		BigInteger cdna = (BigInteger) EditDistanceCommon.ois.readObject();

		String sdnaStr = biToString(sdna, EditDistanceCommon.sigma,
				EditDistanceCommon.sdnaLen);
		String cdnaStr = biToString(cdna, EditDistanceCommon.sigma,
				EditDistanceCommon.cdnaLen);

		int[][] D = new int[EditDistanceCommon.sdnaLen + 1][EditDistanceCommon.cdnaLen + 1];

		for (int i = 0; i < EditDistanceCommon.sdnaLen + 1; i++)
			D[i][0] = i;

		for (int j = 0; j < EditDistanceCommon.cdnaLen + 1; j++)
			D[0][j] = j;

		for (int i = 1; i < EditDistanceCommon.sdnaLen + 1; i++)
			for (int j = 1; j < EditDistanceCommon.cdnaLen + 1; j++) {
				int t = (sdnaStr.charAt(i - 1) == cdnaStr.charAt(j - 1)) ? 0
						: 1;
				D[i][j] = Math.min(Math.min(D[i - 1][j] + 1, D[i][j - 1] + 1),
						D[i - 1][j - 1] + t);
			}

		System.out.println("output (verify): "
				+ D[EditDistanceCommon.sdnaLen][EditDistanceCommon.cdnaLen]);
	}
}