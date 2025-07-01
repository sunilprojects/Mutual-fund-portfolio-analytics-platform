package com.aroha.mutualfund.factory;

public class FilesFactory {

	public MutualFundFile getFile(String filename) {
		if (filename == null) {
			return null;
		}
		if (filename.equalsIgnoreCase("Mirae Asset Large Cap Fund")) {
			return new handlerMiraeAssetFund();
		} else if (filename.equalsIgnoreCase(
				"HDFC Mid-Cap Opportunities Fund (An open ended equity scheme predominantly investing in mid cap stocks)")) {
			return new handlerHDFCOpportunitiesFund();
		} else if (filename.equalsIgnoreCase("DSP Mid Cap Fund")) {
			return new handlerDSPFund();
		}
		return null;
	}
}
