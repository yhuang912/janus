// Copyright (C) 2011 by Yan Huang <yhuang@virginia.edu>

package YaoGC;

import java.math.*;
import Utils.*;

public class EQUAL_2L_1 extends CompositeCircuit {
	private final int L;
	static final int XOR = 0;
	static final int OR = 1;

	public EQUAL_2L_1(int l) {
		super(2 * l, 1, 2, "EQUAL");

		L = l;
	}

	protected void createSubCircuits() throws Exception {
		subCircuits[XOR] = new XOR_2L_L(L);
		subCircuits[OR] = new OR_L_1(L);

		super.createSubCircuits();
	}

	protected void connectWires() {
		for (int i = 0; i < L; i++) {
			inputWires[i].connectTo(subCircuits[XOR].inputWires, i);
			inputWires[i + L].connectTo(subCircuits[XOR].inputWires, i + L);

			subCircuits[XOR].outputWires[i].connectTo(
					subCircuits[OR].inputWires, i);
		}
	}

	protected void defineOutputWires() {
		outputWires[0] = subCircuits[OR].outputWires[0];
	}

	public boolean isEqual(State n1, State n2) {
		State s = State.concatenate(n1, n2);
		s = startExecuting(s);

		BigInteger out;
		boolean res = false;
		if (Circuit.isForGarbling) {
			int bytelength = (Wire.labelBitLength - 1) / 8 + 1;
			out = Utils.readBigInteger(bytelength, ois);

			if (out.equals(s.wires[0].lbl))
				res = ((s.wires[0].invd) ? false : true);
			else if (out.equals(Wire.conjugate(s.wires[0].lbl)))
				res = ((s.wires[0].invd) ? true : false);
			else {
				System.err.println("Error: Unrecognized label.");
				(new Exception()).printStackTrace();
				System.exit(1);
			}

			try {
				oos.writeBoolean(res);
				oos.flush();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

			return res;
		} else {
			try {
				int bytelength = (Wire.labelBitLength - 1) / 8 + 1;
				Utils.writeBigInteger(s.wires[0].lbl, bytelength, oos);
				oos.flush();

				res = ois.readBoolean();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

			return res;
		}
	}

	public State testEqual(State n1, State n2) {
		State s = State.concatenate(n1, n2);
		s = startExecuting(s);
		return s;
	}
}