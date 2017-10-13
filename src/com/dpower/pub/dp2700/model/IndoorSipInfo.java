package com.dpower.pub.dp2700.model;

public class IndoorSipInfo extends BaseMod {

	private static final long serialVersionUID = 1L;

	private int mDB_id = 1;
	
	private String deviceNo;

	private String areaNo;

	private String regionNo;

	private String buildingNo;

	private String unitNo;

	private String houseNo;

	private String mac;

	private String ip;

	private String deviceName = null;

	private String deviceType;

	private String devicePassword;

	private String position;
	
	private String version;

	private String ipState;

	private String houseState;

	private String sipId;

	private String sipPwd;

	public IndoorSipInfo(int mDB_id, String deviceNo, String areaNo,
			String regionNo, String buildingNo, String unitNo, String houseNo,
			String mac, String ip, String deviceName, String deviceType,
			String devicePassword, String position, String version,
			String ipState, String houseState, String sipId, String sipPwd) {
		super();
		this.mDB_id = mDB_id;
		this.deviceNo = deviceNo;
		this.areaNo = areaNo;
		this.regionNo = regionNo;
		this.buildingNo = buildingNo;
		this.unitNo = unitNo;
		this.houseNo = houseNo;
		this.mac = mac;
		this.ip = ip;
		this.deviceName = deviceName;
		this.deviceType = deviceType;
		this.devicePassword = devicePassword;
		this.position = position;
		this.version = version;
		this.ipState = ipState;
		this.houseState = houseState;
		this.sipId = sipId;
		this.sipPwd = sipPwd;
	}

	public IndoorSipInfo() {
		
	}

	public int getDb_id() {
		return this.mDB_id;
	}
	
	public void setDb_id(int db_id) {
		this.mDB_id = db_id;
	}

	public void setDeviceNo(String deviceNo) {
		this.deviceNo = deviceNo;
	}

	public String getDeviceNo() {
		return this.deviceNo;
	}

	public void setAreaNo(String areaNo) {
		this.areaNo = areaNo;
	}

	public String getAreaNo() {
		return this.areaNo;
	}

	public void setRegionNo(String regionNo) {
		this.regionNo = regionNo;
	}

	public String getRegionNo() {
		return this.regionNo;
	}

	public void setBuildingNo(String buildingNo) {
		this.buildingNo = buildingNo;
	}

	public String getBuildingNo() {
		return this.buildingNo;
	}

	public void setUnitNo(String unitNo) {
		this.unitNo = unitNo;
	}

	public String getUnitNo() {
		return this.unitNo;
	}

	public void setHouseNo(String houseNo) {
		this.houseNo = houseNo;
	}

	public String getHouseNo() {
		return this.houseNo;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getMac() {
		return this.mac;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getIp() {
		return this.ip;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getDeviceName() {
		return this.deviceName;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getDeviceType() {
		return this.deviceType;
	}

	public void setDevicePassword(String devicePassword) {
		this.devicePassword = devicePassword;
	}

	public String getDevicePassword() {
		return this.devicePassword;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getPosition() {
		return this.position;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return this.version;
	}

	public void setIpState(String ipState) {
		this.ipState = ipState;
	}

	public String getIpState() {
		return this.ipState;
	}

	public void setHouseState(String houseState) {
		this.houseState = houseState;
	}

	public String getHouseState() {
		return this.houseState;
	}

	public void setSipId(String sipId) {
		this.sipId = sipId;
	}

	public String getSipId() {
		return this.sipId;
	}

	public void setSipPwd(String sipPwd) {
		this.sipPwd = sipPwd;
	}

	public String getSipPwd() {
		return this.sipPwd;
	}

	
	@Override
	public String toString() {
		return "IndoorSipInfo [deviceNo=" + deviceNo
				+ ", areaNo=" + areaNo + ", regionNo=" + regionNo
				+ ", buildingNo=" + buildingNo + ", unitNo=" + unitNo
				+ ", houseNo=" + houseNo + ", mac=" + mac + ", ip=" + ip
				+ ", deviceName=" + deviceName + ", deviceType=" + deviceType
				+ ", devicePassword=" + devicePassword + ", position="
				+ position + ", version=" + version + ", ipState=" + ipState
				+ ", houseState=" + houseState + ", sipId=" + sipId
				+ ", sipPwd=" + sipPwd + "]";
	}
	
}
