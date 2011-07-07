// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package YaoGC;

import java.math.*;

public class State {

	public static class StaticWire {
		public int value = Wire.UNKNOWN_SIG;
		public BigInteger lbl = null;
		public boolean invd = false;

		StaticWire() {
		}

		public StaticWire(int v) {
			value = v;
		}

		public StaticWire(BigInteger label) {
			lbl = label;
		}
	}

	public StaticWire wires[];
	public BigInteger plainValue = null; // The non-negative integer the state
											// object represents.

	public State(StaticWire[] ws) {
		wires = ws;

		for (int i = 0; i < ws.length; i++) {
			if (ws[i].value == Wire.UNKNOWN_SIG) {
				plainValue = null;
				return;
			}
		}

		plainValue = BigInteger.ZERO;
		for (int i = 0; i < ws.length; i++) {
			if (ws[i].value == 1)
				plainValue = plainValue.setBit(i);
		}
	}

	public State(BigInteger v, int length) {
		wires = new StaticWire[length];
		for (int i = 0; i < length; i++) {
			wires[i] = new StaticWire();
			wires[i].value = v.testBit(i) ? 1 : 0;
		}

		plainValue = v;
	}

	private State(int length) {
		wires = new StaticWire[length];
		plainValue = null;
	}

	public static State flattenStateArray(State[] as) {
		State res = new State(as.length * as[0].wires.length);
		for (int i = 0; i < as.length; i++)
			for (int j = 0; j < as[0].wires.length; j++) {
				res.wires[i * 8 + j] = new StaticWire();
				res.wires[i * 8 + j].value = as[i].wires[j].value;
				res.wires[i * 8 + j].lbl = as[i].wires[j].lbl;
				res.wires[i * 8 + j].invd = as[i].wires[j].invd;
			}

		return res;
	}

	public static State extractState(State s, int start, int end) {
		State res = new State(end - start);
		for (int i = 0; i < end - start; i++) {
			res.wires[i] = s.wires[i + start];
		}

		return res;
	}

	public static State fromWires(Wire[] ws) {
		State.StaticWire[] swires = new State.StaticWire[ws.length];
		for (int i = 0; i < ws.length; i++) {
			swires[i] = new StaticWire();
			swires[i].value = ws[i].value;
			swires[i].lbl = ws[i].lbl;
			swires[i].invd = ws[i].invd;
		}

		return new State(swires);
	}

	public static State fromLabels(BigInteger[] lbs) {
		State res = new State(lbs.length);
		for (int i = 0; i < lbs.length; i++)
			res.wires[i] = new StaticWire(lbs[i]);

		return res;
	}

	public static State fromLabels(BigInteger[] lbs, int begin, int end) {
		State res = new State(end - begin);
		for (int i = begin; i < end; i++)
			res.wires[i - begin] = new StaticWire(lbs[i]);

		return res;
	}

	public static State fromConcatenation(State s1, State s2) {
		State res = new State(s1.getWidth() + s2.getWidth());

		for (int i = 0; i < s1.getWidth(); i++)
			res.wires[i] = s1.wires[i];

		for (int i = 0; i < s2.getWidth(); i++)
			res.wires[i + s1.getWidth()] = s2.wires[i];

		return res;
	}

	public static BigInteger toBigInteger(State s) {
		BigInteger res = BigInteger.ZERO;
		for (int i = 0; i < s.wires.length; i++) {
			if (s.wires[i].value == Wire.UNKNOWN_SIG) {
				return null;
			} else if (s.wires[i].value == 1)
				res = res.setBit(i);
		}

		return res;
	}

	public int getWidth() {
		return wires.length;
	}

	public static State signExtend(State s, int width) {
		if (s.getWidth() > width) {
			(new Exception("s is already wider than width.")).printStackTrace();
			System.exit(1);
		} else if (s.getWidth() == width)
			return s;

		State res = new State(width);
		for (int i = 0; i < width; i++)
			if (i < s.wires.length)
				res.wires[i] = s.wires[i];
			else
				res.wires[i] = s.wires[s.wires.length - 1];

		res.plainValue = s.plainValue;
		return res;
	}

	public static State concatenate(State s1, State s2) {
		int width = s1.getWidth() + s2.getWidth();
		State res = new State(width);
		for (int i = 0; i < width; i++)
			if (i < s2.wires.length)
				res.wires[i] = s2.wires[i];
			else
				res.wires[i] = s1.wires[i - s2.wires.length];

		if (s1.plainValue == null || s2.plainValue == null)
			res.plainValue = null;
		else
			res.plainValue = s1.plainValue.shiftLeft(s2.getWidth()).xor(
					s2.plainValue);
		return res;
	}

	public BigInteger[] toLabels() {
		BigInteger[] res = new BigInteger[getWidth()];
		for (int i = 0; i < res.length; i++)
			res[i] = wires[i].lbl;
		return res;
	}

	/*
	 * Return the highest bit that is potentially "1".
	 */
	private int highestBit() {
		if (plainValue != null)
			return plainValue.bitCount();
		else {
			for (int i = wires.length - 1; i >= 0; i--) {
				if (wires[i].value == 0)
					continue;
				else
					return i;
			}

			Exception e = new Exception("Should never run here.");
			e.printStackTrace();
			System.exit(1);
			return -1;
		}
	}

	public void debugPrint(String msg) {
		System.out.println("[" + msg + "] STATE:" + plainValue + "\t");

		BigInteger[] lbs = new BigInteger[wires.length];
		for (int i = 0; i < lbs.length; i++) {
			if (wires[i].value != Wire.UNKNOWN_SIG)
				lbs[i] = BigInteger.valueOf(wires[i].value);
			else if (wires[i].invd == false)
				lbs[i] = wires[i].lbl;
			else
				lbs[i] = Wire.conjugate(wires[i].lbl);
		}

		Wire.printLabels(msg, lbs);
		if (Circuit.isForGarbling)
			Wire.printConjuLabels(msg, lbs);

		System.out.println();
	}

	public void debugPrint(String msg, int start, int end) {
		System.out.println("[" + msg + "] STATE:" + plainValue + "\t");

		BigInteger[] lbs = new BigInteger[end - start];
		for (int i = 0; i < end - start; i++) {
			if (wires[i + start].value != Wire.UNKNOWN_SIG)
				lbs[i] = BigInteger.valueOf(wires[i + start].value);
			else if (wires[i + start].invd == false)
				lbs[i] = wires[i + start].lbl;
			else
				lbs[i] = Wire.conjugate(wires[i + start].lbl);
		}

		Wire.printLabels(msg, lbs);
		if (Circuit.isForGarbling)
			Wire.printConjuLabels(msg, lbs);

		System.out.println();
	}

}
