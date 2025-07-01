package com.aroha.mutualfund.factory;

public class FilesFactory {

	public MutualFundFile getFile(String filename) {
		if (filename == null) {
			return null;
		}
		if (filename.equalsIgnoreCase("Mira Index Mutual Fund")) {
			return new handlerMiraeAssetFund();
		} else if (filename.equalsIgnoreCase(
				"HDFC Mid-Cap Opportunities Fund")) {
			return new handlerHDFCOpportunitiesFund();
		} else if (filename.equalsIgnoreCase("DSP Mutual Fund Allocation")) {
			return new handlerDSPFund();
		}
		return null;
	}
}
