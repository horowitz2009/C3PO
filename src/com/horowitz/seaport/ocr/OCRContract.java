package com.horowitz.seaport.ocr;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.horowitz.ocr.OCRB;
import com.horowitz.ocr.OCRe;

import Catalano.Imaging.Filters.Threshold;

public class OCRContract {
	
	public static void main(String[] args) throws IOException {
		
		OCRContract ocrContract = new OCRContract();
		ocrContract.learn();
		
		//OCRe ocre = new OCRe("goal");
		//ocre.learn("ocr/contract", "goal", "ocr/contract/output", false);

		//ocre.setOcrb(new OCRB("ocr/contract/output/goal"));

		String res = ocrContract.scanGoal("ocr/contract/test.bmp");
		System.err.println(res);
		res = ocrContract.scanCargo("ocr/contract/testCargo1.bmp");
		System.err.println(res);
		res = ocrContract.scanCargo("ocr/contract/testCargo2.bmp");
		System.err.println(res);
		res = ocrContract.scanCargo("ocr/contract/testCargo3.bmp");
		System.err.println(res);
		res = ocrContract.scanRemaining("ocr/contract/testRem1.png");
		System.err.println(res);
		
		
	}

	private OCRe ocrGoal;
	private OCRe ocrRemaining;
	private OCRe ocrCargo;

	public OCRContract() throws IOException {
		super();
		ocrGoal = new OCRe("goal");
		ocrRemaining = new OCRe("rem");

		ocrGoal.setOcrb(new OCRB("ocr/contract/output/goal"));
		ocrRemaining.setOcrb(new OCRB("ocr/contract/output/rem"));

		ocrCargo = new OCRe("cargo");
		ocrCargo.setNegative(true);
		ocrCargo.setThreshold(new Threshold(120));
		ocrCargo.setOcrb(new OCRB("ocr/contract/output/cargo"));
	}

	public String scanProgress(BufferedImage image) {
		return ocrGoal.scanImage(image);
	}

	public String scanRemaining(BufferedImage image) {
		return ocrRemaining.scanImage(image);
	}

	public String scanCargo(BufferedImage image) {
		return ocrCargo.scanImage(image);
	}

	public void learn() throws IOException {
		
		ocrGoal.learn("ocr/contract", "goal", "ocr/contract/output", false);
		ocrRemaining.learn("ocr/contract", "rem", "ocr/contract/output", false);
		ocrCargo.learn("ocr/contract", "cargo", "ocr/contract/output", false);
		
		ocrGoal.setOcrb(new OCRB("ocr/contract/output/goal"));
		ocrRemaining.setOcrb(new OCRB("ocr/contract/output/rem"));
		ocrCargo.setOcrb(new OCRB("ocr/contract/output/cargo"));

	}

	public String scanCargo(String filename) throws IOException {
		return ocrCargo.scanImage(filename);
	}

	public String scanGoal(String filename) throws IOException {
		return ocrGoal.scanImage(filename);
	}

	public String scanRemaining(String filename) throws IOException {
		return ocrRemaining.scanImage(filename);
	}
	
	
}
