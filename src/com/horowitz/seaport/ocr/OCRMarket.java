package com.horowitz.seaport.ocr;

import java.awt.image.BufferedImage;
import java.io.IOException;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Threshold;

import com.horowitz.commons.DateUtils;
import com.horowitz.commons.ImageComparator;
import com.horowitz.ocr.OCRB;
import com.horowitz.ocr.OCRe;

public class OCRMarket {

  private static final String INPUT_PATH = "ocr/ocrm";
  private final static String OUTPUT_PATH = "ocrm";
  private final static String OCR_PREFIX = "m";

  private OCRB ocrb;

  public OCRMarket(ImageComparator comparator) throws IOException {
    super();
    ocrb = new OCRB(OUTPUT_PATH + "/" + OCR_PREFIX, comparator);
    ocrb.setErrors(2);
  }

  public String scanImage(BufferedImage image) {
    FastBitmap fb = new FastBitmap(image);
    if (fb.isRGB())
      fb.toGrayscale();
    Threshold t = new Threshold(200);
    t.applyInPlace(fb);
    //fb.saveAsBMP("energy_" + DateUtils.formatDateForFile(System.currentTimeMillis()) + ".bmp");
    return ocrb.scanImage(fb.toBufferedImage());
  }

  // ////////////////////////////

  public static void main(String[] args) {
    OCRe ocr = new OCRe();
    Threshold t = new Threshold(200);
    ocr.setThreshold(t);
    if (args.length < 3)
      ocr.learn(INPUT_PATH, OCR_PREFIX, "images/" + OUTPUT_PATH, false);
    else {
      ocr.learn(args[0], args[1], args[2], false);
    }
  }
 

}
