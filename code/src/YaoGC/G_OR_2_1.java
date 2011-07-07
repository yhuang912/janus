// Copyright (C) 2010 by Yan Huang <yh8h@virginia.edu>

package YaoGC;

class G_OR_2_1 extends OR_2_1 {
    public G_OR_2_1() {
	super();
    }

    protected void execYao() {
	fillTruthTable();
	encryptTruthTable();
	// permuteTruthTable();
	sendGTT();
	gtt = null;
    }
}
