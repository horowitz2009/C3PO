package com.horowitz.seaport.model;

public interface IBarrelsProtocol {

	void setBlobMax(int blobMax);

	int getBlobMax();

	void setBlobMin(int blobMin);

	int getBlobMin();

	void setCapture(boolean capture);

	boolean isCapture();

}
