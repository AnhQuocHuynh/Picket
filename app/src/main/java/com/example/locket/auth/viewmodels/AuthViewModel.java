package com.example.locket.auth.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.locket.common.utils.AuthManager;

public class AuthViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<String> _successMessage = new MutableLiveData<>();
    public LiveData<String> successMessage = _successMessage;

    public AuthViewModel(@NonNull Application application) {
        super(application);
    }

    public void forgotPassword(String email) {
        AuthManager.forgotPassword(email, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                _successMessage.postValue(message);
            }

            @Override
            public void onError(String errorMessage, int errorCode) {
                _errorMessage.postValue(errorMessage);
            }

            @Override
            public void onLoading(boolean isLoading) {
                _isLoading.postValue(isLoading);
            }
        });
    }

    public void resetPassword(String code, String newPassword, String confirmPassword) {
        AuthManager.resetPassword(code, newPassword, confirmPassword, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                _successMessage.postValue(message);
            }

            @Override
            public void onError(String errorMessage, int errorCode) {
                _errorMessage.postValue(errorMessage);
            }

            @Override
            public void onLoading(boolean isLoading) {
                _isLoading.postValue(isLoading);
            }
        });
    }

    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        AuthManager.changePassword(getApplication(), currentPassword, newPassword, confirmPassword, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                _successMessage.postValue(message);
            }

            @Override
            public void onError(String errorMessage, int errorCode) {
                _errorMessage.postValue(errorMessage);
            }

            @Override
            public void onLoading(boolean isLoading) {
                _isLoading.postValue(isLoading);
            }
        });
    }
}
