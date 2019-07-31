package main.java.io.mosip.ivv.auth.kyc;

import java.util.ArrayList;
import java.util.List;

public class Biometrics {
	
		//private ArrayList<Data> data=new ArrayList<Data>();
		private String hash;
		private String sessionKey;
		private String signature;
		private Data data;
		
		
		/*public List<Data> getData() {
			return data;
		}
		
		public void setData(ArrayList<Data> data) {
			this.data = data;
		}*/
		
		public Data getData() {
			return data;
		}

		public void setData(Data data) {
			this.data = data;
		}

		public String getHash() {
			return hash;
		}
		
		public void setHash(String hash) {
			this.hash = hash;
		}
		
		public String getSessionKey() {
			return sessionKey;
		}
		
		public void setSessionKey(String sessionKey) {
			this.sessionKey = sessionKey;
		}
		
		public String getSignature() {
			return signature;
		}
		
		public void setSignature(String signature) {
			this.signature = signature;
		}
		
		public static class Data{
			
			private String   mosipProcess;    
			private String   environment;    
			private String   version;    
			private String   deviceCode;    
			private String   deviceProviderID;    
			private String   deviceServiceID;    
			private String   deviceServiceVersion;    
			private String   bioType;
			private String   bioSubType;
			private String   bioValue;
			private String   transactionID;
			private String   timestamp;
			private String   requestedScore;
			private String   qualityScore;
			
			public String getMosipProcess() {
				return mosipProcess;
			}
			public void setMosipProcess(String mosipProcess) {
				this.mosipProcess = mosipProcess;
			}
			public String getEnvironment() {
				return environment;
			}
			public void setEnvironment(String environment) {
				this.environment = environment;
			}
			public String getVersion() {
				return version;
			}
			public void setVersion(String version) {
				this.version = version;
			}
			public String getDeviceCode() {
				return deviceCode;
			}
			public void setDeviceCode(String deviceCode) {
				this.deviceCode = deviceCode;
			}
			public String getDeviceProviderID() {
				return deviceProviderID;
			}
			public void setDeviceProviderID(String deviceProviderID) {
				this.deviceProviderID = deviceProviderID;
			}
			public String getDeviceServiceID() {
				return deviceServiceID;
			}
			public void setDeviceServiceID(String deviceServiceID) {
				this.deviceServiceID = deviceServiceID;
			}
			public String getDeviceServiceVersion() {
				return deviceServiceVersion;
			}
			public void setDeviceServiceVersion(String deviceServiceVersion) {
				this.deviceServiceVersion = deviceServiceVersion;
			}
			public String getBioType() {
				return bioType;
			}
			public void setBioType(String bioType) {
				this.bioType = bioType;
			}
			public String getBioSubType() {
				return bioSubType;
			}
			public void setBioSubType(String bioSubType) {
				this.bioSubType = bioSubType;
			}
			public String getBioValue() {
				return bioValue;
			}
			public void setBioValue(String bioValue) {
				this.bioValue = bioValue;
			}
			public String getTransactionID() {
				return transactionID;
			}
			public void setTransactionID(String transactionID) {
				this.transactionID = transactionID;
			}
			public String getTimestamp() {
				return timestamp;
			}
			public void setTimestamp(String timestamp) {
				this.timestamp = timestamp;
			}
			public String getRequestedScore() {
				return requestedScore;
			}
			public void setRequestedScore(String requestedScore) {
				this.requestedScore = requestedScore;
			}
			public String getQualityScore() {
				return qualityScore;
			}
			public void setQualityScore(String qualityScore) {
				this.qualityScore = qualityScore;
			}
			
		}
		
}
