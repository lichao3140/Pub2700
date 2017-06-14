package com.dpower.domain;

public class BindAccountInfo {

	public int mDB_id = 0;
	public String accountname;
	public String accountpwd;
	public int isonline;
	public String token;
	public String phonetype;
	
	public BindAccountInfo(){
		
	}
	
	public BindAccountInfo(int mDB_id, String accountname, String accountpwd,
			int isonline, String token, String phonetype) {
		super();
		this.mDB_id = mDB_id;
		this.accountname = accountname;
		this.accountpwd = accountpwd;
		this.isonline = isonline;
		this.token = token;
		this.phonetype = phonetype;
	}
	
	public BindAccountInfo(String accountname, String accountpwd,
			int isonline, String token, String phonetype) {
		super();
		this.accountname = accountname;
		this.accountpwd = accountpwd;
		this.isonline = isonline;
		this.token = token;
		this.phonetype = phonetype;
	}

	public int getmDB_id() {
		return mDB_id;
	}
	
	public String getAccountname() {
		return accountname;
	}
	
	public void setAccountname(String accountname) {
		this.accountname = accountname;
	}
	
	public String getAccountpwd() {
		return accountpwd;
	}
	public void setAccountpwd(String accountpwd) {
		this.accountpwd = accountpwd;
	}
	
	public int getIsonline() {
		return isonline;
	}
	
	public void setIsonline(int isonline) {
		this.isonline = isonline;
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public String getPhonetype() {
		return phonetype;
	}
	
	public void setPhonetype(String phonetype) {
		this.phonetype = phonetype;
	}

	@Override
	public String toString() {
		return "BindAccountInfo [mDB_id=" + mDB_id + ", accountname="
				+ accountname + ", accountpwd=" + accountpwd + ", isonline="
				+ isonline + ", token=" + token + ", phonetype=" + phonetype
				+ "]";
	}
	
}
