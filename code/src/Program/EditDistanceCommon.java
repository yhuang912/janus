// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.math.*;

import ast.AstGCExecutor;

import YaoGC.State;

public class EditDistanceCommon extends ProgCommon {
	static int sdnaLen;
	static int cdnaLen;
	static String strSdna, strCdna; 
	public static int sigma;

	static int bitLength(int x) {
		return BigInteger.valueOf(x).bitLength();
	}

	protected static void initCircuits() {
		// in new computation framework, no need to initialize circuits here
	}

	public static State execCircuit(BigInteger[] sdnalbs, BigInteger[] cdnalbs)
			throws Exception {
		EditDistance ed = new EditDistance(strSdna, strCdna);
		return AstGCExecutor.execute(ed.getRoot()).state;
	}
}