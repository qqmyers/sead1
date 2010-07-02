package com.schneertz.medici;

public abstract class ProgressHandler {
	/**
	 * @param k percent progress has been made
	 */
	public abstract void onProgress(int k);

	/** called on successful completion */
	public void onSuccess() {
	}

	/**
	 * @param t has been thrown
	 */
	public void onFailure(Throwable t) {
	}
}
