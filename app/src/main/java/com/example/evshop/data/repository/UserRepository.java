package com.example.evshop.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.evshop.domain.models.UserData;

import javax.inject.Inject;
import javax.inject.Singleton;

// @Singleton đảm bảo chỉ có một UserRepository duy nhất trong toàn bộ ứng dụng
@Singleton
public class UserRepository {

    // Dùng LiveData để chứa thông tin người dùng hiện tại
    private final MutableLiveData<UserData> _currentUser = new MutableLiveData<>(null);

    // Đây là LiveData công khai để các ViewModel khác có thể quan sát
    public LiveData<UserData> getCurrentUser() {
        return _currentUser;
    }

    @Inject
    public UserRepository() {
        // Constructor rỗng cho Hilt
    }

    // Phương thức để cập nhật người dùng sau khi đăng nhập thành công
    public void setCurrentUser(UserData user) {
        _currentUser.postValue(user);
    }

    // Phương thức để xóa thông tin người dùng khi đăng xuất
    public void clearCurrentUser() {
        _currentUser.postValue(null);
    }
}
