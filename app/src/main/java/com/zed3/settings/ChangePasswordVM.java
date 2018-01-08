package com.zed3.settings;

import com.zed3.network.HttpPasswordChange;
import com.zed3.network.NetworkAbstract;
import com.zed3.network.NetworkAbstract.ResponseListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChangePasswordVM {
	private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
	public String confirmPassword = "";
	private ChangePasswordListener mChangePasswordListener = null;
	public String newPassword = "";
	public String oldInPhone = "";
	public String oldPassword = "";
	public String serverIP = "";
	public int serverPort = 0;
	public String username = "";

	public interface ChangePasswordListener {
		void oldPasswordWrong();

		void passwordIncomplete();

		void passwordTooLong();

		void saveFailure(String str);

		void saveSuccess(String str);

		void saveTimeout();

		void twoPasswordNotEqual();
	}

	class C19321 implements ResponseListener {
		C19321() {
		}

		public void onSuccess(String res) {
			if (ChangePasswordVM.this.mChangePasswordListener != null) {
				ChangePasswordVM.this.mChangePasswordListener.saveSuccess(res);
			}
		}

		public void onError(String err) {
			if (ChangePasswordVM.this.mChangePasswordListener != null) {
				ChangePasswordVM.this.mChangePasswordListener.saveFailure(err);
			}
		}

		public void onTimeOut() {
			if (ChangePasswordVM.this.mChangePasswordListener != null) {
				ChangePasswordVM.this.mChangePasswordListener.saveTimeout();
			}
		}
	}

	public ChangePasswordVM(ChangePasswordListener mChangePasswordListener) {
		this.mChangePasswordListener = mChangePasswordListener;
	}

	public ChangePasswordVM(String oldInPhone, String username, ChangePasswordListener mChangePasswordListener) {
		this.oldInPhone = oldInPhone;
		this.username = username;
		this.mChangePasswordListener = mChangePasswordListener;
	}

	public ChangePasswordVM(String username, String oldInPhone, String serverIP, int serverPort, ChangePasswordListener mChangePasswordListener) {
		this.username = username;
		this.oldInPhone = oldInPhone;
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		this.mChangePasswordListener = mChangePasswordListener;
	}

	public ChangePasswordVM(String oldPassword, String newPassword, String confirmPassword, String oldInPhone, String username, ChangePasswordListener mChangePasswordListener) {
		this.oldPassword = oldPassword;
		this.newPassword = newPassword;
		this.confirmPassword = confirmPassword;
		this.oldInPhone = oldInPhone;
		this.username = username;
		this.mChangePasswordListener = mChangePasswordListener;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public void setOldInPhone(String oldInPhone) {
		this.oldInPhone = oldInPhone;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setmChangePasswordListener(ChangePasswordListener mChangePasswordListener) {
		this.mChangePasswordListener = mChangePasswordListener;
	}

	public boolean check() {
		if (isPassworIntegrity()) {
			if (isOldWrong()) {
				if (this.mChangePasswordListener == null) {
					return false;
				}
				this.mChangePasswordListener.oldPasswordWrong();
				return false;
			} else if (isTwoPwdEqual()) {
				return true;
			} else {
				if (this.mChangePasswordListener == null) {
					return false;
				}
				this.mChangePasswordListener.twoPasswordNotEqual();
				return false;
			}
		} else if (this.mChangePasswordListener == null) {
			return false;
		} else {
			this.mChangePasswordListener.passwordIncomplete();
			return false;
		}
	}

	public void save() {
		final NetworkAbstract networkAbstract = new HttpPasswordChange(this.serverIP, this.serverPort, new C19321());
		this.cachedThreadPool.execute(new Runnable() {
			public void run() {
				networkAbstract.send(ChangePasswordVM.this.username, ChangePasswordVM.this.confirmPassword);
			}
		});
	}

	public boolean isPassworIntegrity() {
		if (this.oldPassword.length() < 1 || this.newPassword.length() < 1 || this.confirmPassword.length() < 1) {
			return false;
		}
		return true;
	}

	public boolean isTwoPwdEqual() {
		if (this.newPassword.equals(this.confirmPassword)) {
			return true;
		}
		return false;
	}

	public boolean isOldWrong() {
		if (this.oldInPhone == null || this.oldInPhone.equals(this.oldPassword)) {
			return false;
		}
		return true;
	}
}
