package com.horowitz.seaport.ocr;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.horowitz.ocr.OCRB;
import com.horowitz.ocr.OCRe;

import Catalano.Imaging.Filters.Threshold;

public class OCRContract {
	public static void main(String[] args) throws IOException {
		OCRe ocre = new OCRe("goal");
		ocre.learn("ocr/contract", "goal", "ocr/contract/output", false);

		ocre.setOcrb(new OCRB("ocr/contract/output/goal"));

		String res = ocre.scanImage("ocr/contract/test.bmp");
		System.err.println(res);
		ocre.setNegative(true);
		ocre.setThreshold(new Threshold(120));
		ocre.learn("ocr/contract", "cargo", "ocr/contract/output", false);
		ocre.setOcrb(new OCRB("ocr/contract/output/cargo"));
		res = ocre.scanImage("ocr/contract/testCargo1.bmp");
		System.err.println(res);
		res = ocre.scanImage("ocr/contract/testCargo2.bmp");
		System.err.println(res);
		res = ocre.scanImage("ocr/contract/testCargo3.bmp");
		System.err.println(res);
	}

	private OCRe ocrContract;
	private OCRe ocrCargo;

	public OCRContract() throws IOException {
		super();
		ocrContract = new OCRe("goal");

		ocrContract.setOcrb(new OCRB("ocr/contract/output/goal"));

		ocrCargo = new OCRe("cargo");
		ocrCargo.setNegative(true);
		ocrCargo.setThreshold(new Threshold(120));
		ocrCargo.setOcrb(new OCRB("ocr/contract/output/cargo"));
	}

	public String scanProgress(BufferedImage image) {
		return ocrContract.scanImage(image);
	}

	public String scanCargo(BufferedImage image) {
		return ocrCargo.scanImage(image);
	}

	public void learn() throws IOException {
		// TODO Auto-generated method stub
		ocrContract.learn("ocr/contract", "goal", "ocr/contract/output", false);
		ocrCargo.learn("ocr/contract", "cargo", "ocr/contract/output", false);
		ocrContract.setOcrb(new OCRB("ocr/contract/output/goal"));
		ocrCargo.setOcrb(new OCRB("ocr/contract/output/cargo"));

	}
}
