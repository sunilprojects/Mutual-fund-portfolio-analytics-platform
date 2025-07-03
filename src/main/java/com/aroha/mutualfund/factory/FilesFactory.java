package com.aroha.mutualfund.factory;

public class FilesFactory {

	public MutualFundFile getFile(String filename) {
		if (filename == null) {
			return null;
		}
		if (filename.equalsIgnoreCase("Mira Index Mutual Fund")) {
			return new HandlerMiraeAssetFund();
		} else if (filename.equalsIgnoreCase(
				"HDFC Mid-Cap Opportunities Fund")) {
			return new HandlerHDFCOpportunitiesFund();
		} else if (filename.equalsIgnoreCase("DSP Mutual Fund Allocation")) {
			return new HandlerDSPFund();
		}
		return null;
	}
}
