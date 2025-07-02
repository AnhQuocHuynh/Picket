package com.example.locket.common.repository.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.locket.common.database.entities.FriendEntity;
import com.example.locket.common.models.common.ApiResponse;
import com.example.locket.common.models.friendship.FriendshipResponse;
import com.example.locket.common.models.friendship.FriendsListResponse;
import com.example.locket.common.models.friendship.GenerateLinkResponse;
import com.example.locket.common.models.user.UserSearchResponse;
import com.example.locket.common.repository.FriendRepository;
import com.example.locket.common.repository.FriendshipRepository;

import java.util.List;

public class FriendViewModel extends AndroidViewModel {
    private final FriendRepository friendRepository;
    private final FriendshipRepository friendshipRepository;

    // This LiveData is for the local DB, which seems unused in UI.
    private final LiveData<List<FriendEntity>> friendEntities;

    // Renaming for clarity and to fix errors in BottomSheetFriend
    private final MutableLiveData<FriendsListResponse> _myFriends = new MutableLiveData<>();
    public final LiveData<FriendsListResponse> myFriends = _myFriends;

    // LiveData for API operations
    private final MutableLiveData<UserSearchResponse> _userSearchResults = new MutableLiveData<>();
    public final LiveData<UserSearchResponse> userSearchResults = _userSearchResults;

    private final MutableLiveData<FriendsListResponse> _receivedFriendRequests = new MutableLiveData<>();
    public final LiveData<FriendsListResponse> receivedFriendRequests = _receivedFriendRequests;

    private final MutableLiveData<FriendsListResponse> _sentFriendRequests = new MutableLiveData<>();
    public final LiveData<FriendsListResponse> sentFriendRequests = _sentFriendRequests;

    private final MutableLiveData<ApiResponse> _actionResponse = new MutableLiveData<>();
    public final LiveData<ApiResponse> actionResponse = _actionResponse;

    private final MutableLiveData<FriendshipResponse> _friendshipResponse = new MutableLiveData<>();
    public final LiveData<FriendshipResponse> friendshipResponse = _friendshipResponse;

    private final MutableLiveData<GenerateLinkResponse> _generateLinkResponse = new MutableLiveData<>();
    public final LiveData<GenerateLinkResponse> generateLinkResponse = _generateLinkResponse;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;


    public FriendViewModel(@NonNull Application application) {
        super(application);
        friendRepository = new FriendRepository(application);
        friendshipRepository = new FriendshipRepository(application);
        friendEntities = friendRepository.getAllFriends();
        fetchMyFriends(); // Initial fetch
    }

    public LiveData<List<FriendEntity>> getFriendEntities() {
        return friendEntities;
    }

    // --- API Calls ---

    public void fetchMyFriends() {
        friendshipRepository.getFriendsList(new FriendshipRepository.FriendsListCallback() {
            @Override
            public void onSuccess(FriendsListResponse friendsListResponse) {
                _myFriends.postValue(friendsListResponse);
            }

            @Override
            public void onError(String message, int code) {
                _errorMessage.postValue(message);
            }

            @Override
            public void onLoading(boolean isLoading) {
                _isLoading.postValue(isLoading);
            }
        });
    }

    public void searchUsers(String query) {
        friendRepository.searchUsers(query, new FriendRepository.UserSearchCallback() {
            @Override
            public void onSuccess(UserSearchResponse response) {
                _userSearchResults.postValue(response);
            }

            @Override
            public void onError(String message, int code) {
                _errorMessage.postValue(message);
            }

            @Override
            public void onLoading(boolean isLoading) {
                _isLoading.postValue(isLoading);
            }
        });
    }

    // Overloaded method for BottomSheetFriend
    public void sendFriendRequest(String recipientId) {
        sendFriendRequest(recipientId, "Hi, let's be friends!");
    }

    public void sendFriendRequest(String recipientId, String message) {
        friendshipRepository.sendFriendRequest(recipientId, message, new FriendshipRepository.FriendshipCallback() {
            @Override
            public void onSuccess(FriendshipResponse friendshipResponse) {
                _friendshipResponse.postValue(friendshipResponse);
            }

            @Override
            public void onError(String message, int code) {
                _errorMessage.postValue(message);
            }

            @Override
            public void onLoading(boolean isLoading) {
                _isLoading.postValue(isLoading);
            }
        });
    }

    public void acceptFriendRequest(String friendshipId) {
        friendshipRepository.acceptFriendRequest(friendshipId, new FriendshipRepository.FriendshipCallback() {
            @Override
            public void onSuccess(FriendshipResponse friendshipResponse) {
                 _friendshipResponse.postValue(friendshipResponse);
                 fetchMyFriends(); // Refresh friends list
            }

            @Override
            public void onError(String message, int code) {
                _errorMessage.postValue(message);
            }

            @Override
            public void onLoading(boolean isLoading) {
                _isLoading.postValue(isLoading);
            }
        });
    }

    public void declineFriendRequest(String friendshipId) {
        friendshipRepository.declineFriendRequest(friendshipId, new FriendshipRepository.DeleteCallback() {
            @Override
            public void onSuccess(String message) {
                _actionResponse.postValue(new ApiResponse(true, message));
                fetchReceivedFriendRequests(); // Refresh pending list
            }

            @Override
            public void onError(String message, int code) {
                _errorMessage.postValue(message);
            }
        });
    }

    public void cancelFriendRequest(String friendshipId) {
        friendshipRepository.cancelFriendRequest(friendshipId, new FriendshipRepository.DeleteCallback() {
            @Override
            public void onSuccess(String message) {
                _actionResponse.postValue(new ApiResponse(true, message));
                fetchSentFriendRequests(); // Refresh sent list
            }

            @Override
            public void onError(String message, int code) {
                _errorMessage.postValue(message);
            }
        });
    }

    public void removeFriend(String friendUserId) {
        friendshipRepository.removeFriend(friendUserId, new FriendshipRepository.DeleteCallback() {
            @Override
            public void onSuccess(String message) {
                _actionResponse.postValue(new ApiResponse(true, message));
                fetchMyFriends(); // Refresh friends list
            }

            @Override
            public void onError(String message, int code) {
                _errorMessage.postValue(message);
            }
        });
    }

    public void fetchReceivedFriendRequests() {
        friendshipRepository.getReceivedFriendRequests(new FriendshipRepository.FriendsListCallback() {
            @Override
            public void onSuccess(FriendsListResponse friendsListResponse) {
                _receivedFriendRequests.postValue(friendsListResponse);
            }

            @Override
            public void onError(String message, int code) {
                _errorMessage.postValue(message);
            }

            @Override
            public void onLoading(boolean isLoading) {
                _isLoading.postValue(isLoading);
            }
        });
    }

    public void generateFriendLink() {
        friendshipRepository.generateFriendLink(new FriendshipRepository.LinkCallback() {
            @Override
            public void onSuccess(GenerateLinkResponse linkResponse) {
                _generateLinkResponse.postValue(linkResponse);
            }

            @Override
            public void onError(String message, int code) {
                _errorMessage.postValue(message);
            }
        });
    }

    public void acceptFriendViaLink(String token) {
        friendshipRepository.acceptFriendViaLink(token, new FriendshipRepository.FriendshipCallback() {
            @Override
            public void onSuccess(FriendshipResponse friendshipResponse) {
                _friendshipResponse.postValue(friendshipResponse);
                fetchMyFriends(); // Refresh friends list
            }

            @Override
            public void onError(String message, int code) {
                _errorMessage.postValue(message);
            }

            @Override
            public void onLoading(boolean isLoading) {
                _isLoading.postValue(isLoading);
            }
        });
    }

    public void fetchSentFriendRequests() {
        friendshipRepository.getSentFriendRequests(new FriendshipRepository.FriendsListCallback() {
            @Override
            public void onSuccess(FriendsListResponse friendsListResponse) {
                _sentFriendRequests.postValue(friendsListResponse);
            }

            @Override
            public void onError(String message, int code) {
                _errorMessage.postValue(message);
            }

            @Override
            public void onLoading(boolean isLoading) {
                _isLoading.postValue(isLoading);
            }
        });
    }
}

