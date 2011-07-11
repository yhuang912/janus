// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package test;

import java.util.*;
import java.math.*;

import jargs.gnu.CmdLineParser;

import Utils.*;
import Program.*;

class TestEDClient {
	static BigInteger dna;
	static BigInteger secMask;
	static boolean autogen;
	static int n;

	static Random rnd = new Random();

	private static void printUsage() {
		System.out
				.println("Usage: java TestEDClient [{-d, --dna} dna] [{-L, --EDBitLength} L] [{-a, --autogen}] [{-n, --DNALength} length] [{-s, --server} servername]");
	}

	private static void process_cmdline_args(String[] args) {
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option optionServerIPname = parser.addStringOption('s',
				"server");
		CmdLineParser.Option optionAuto = parser.addBooleanOption('a',
				"autogen");
		CmdLineParser.Option optionDNALength = parser.addIntegerOption('n',
				"DNALength");
		CmdLineParser.Option optionIterCount = parser.addIntegerOption('r',
				"iteration");
		CmdLineParser.Option optionSigma = parser
				.addIntegerOption('g', "sigma");

		try {
			parser.parse(args);
		} catch (CmdLineParser.OptionException e) {
			System.err.println(e.getMessage());
			printUsage();
			System.exit(2);
		}

		autogen = (Boolean) parser.getOptionValue(optionAuto, false);
		n = ((Integer) parser.getOptionValue(optionDNALength, new Integer(100)))
				.intValue();
		ProgClient.serverIPname = (String) parser.getOptionValue(
				optionServerIPname, new String("localhost"));
		Program.iterCount = ((Integer) parser.getOptionValue(optionIterCount,
				new Integer(10))).intValue();
		EditDistanceCommon.sigma = ((Integer) parser.getOptionValue(
				optionSigma, new Integer(2))).intValue();
	}

	private static void generateData() throws Exception {
		dna = new BigInteger(EditDistanceCommon.sigma * n, rnd);

		BigInteger temp = new BigInteger(n, rnd);
		secMask = BigInteger.ZERO;
		BigInteger ones = BigInteger.valueOf((1 << EditDistanceCommon.sigma) - 1);
		for (int i = 0; i < n; i++) {
			secMask = secMask.shiftLeft(EditDistanceCommon.sigma);
			if (temp.testBit(i))
				secMask = secMask.or(ones);
		}
	}

	public static void main(String[] args) throws Exception {
		StopWatch.pointTimeStamp("Starting program");
		process_cmdline_args(args);

		if (autogen)
			generateData();

		EditDistanceClient edclient = new EditDistanceClient(dna, secMask, n);
		edclient.run();
	}
}