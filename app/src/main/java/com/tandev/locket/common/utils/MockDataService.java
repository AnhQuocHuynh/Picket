package com.tandev.locket.common.utils;

import com.tandev.locket.common.database.entities.FriendEntity;
import com.tandev.locket.common.database.entities.MomentEntity;
import com.tandev.locket.common.models.auth.LoginRespone;
import com.tandev.locket.common.models.friend.Friend;
import com.tandev.locket.common.models.friend.Result;
import com.tandev.locket.common.models.friend.UserData;
import com.tandev.locket.common.models.moment.Background;
import com.tandev.locket.common.models.moment.Data;
import com.tandev.locket.common.models.moment.Date;
import com.tandev.locket.common.models.moment.Moment;
import com.tandev.locket.common.models.moment.Overlay;
import com.tandev.locket.common.models.moment.OverlayData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockDataService {

    // Mock Login Response
    public static LoginRespone getMockLoginResponse() {
        LoginRespone loginResponse = new LoginRespone();
        loginResponse.setKind("identitytoolkit#VerifyPasswordResponse");
        loginResponse.setLocalId("mock_user_12345");
        loginResponse.setEmail("test@example.com");
        loginResponse.setDisplayName("Test User");
        loginResponse.setIdToken("mock_id_token_abcdef123456");
        loginResponse.setRegistered(true);
        loginResponse.setProfilePicture("https://example.com/profile.jpg");
        loginResponse.setRefreshToken("mock_refresh_token_789xyz");
        loginResponse.setExpiresIn("3600");
        return loginResponse;
    }

    // Mock Friends Data
    public static List<FriendEntity> getMockFriends() {
        List<FriendEntity> friends = new ArrayList<>();
        
        friends.add(new FriendEntity(
            "friend_001",
            "Nguyễn",
            "Văn A",
            "🌟",
            "https://i.pravatar.cc/300?img=1",
            false,
            "nguyenvana"
        ));
        
        friends.add(new FriendEntity(
            "friend_002", 
            "Trần",
            "Thị B",
            "🎯",
            "https://i.pravatar.cc/300?img=2",
            false,
            "tranthib"
        ));
        
        friends.add(new FriendEntity(
            "friend_003",
            "Lê",
            "Hoàng C",
            "🚀",
            "https://i.pravatar.cc/300?img=3",
            false,
            "lehoangc"
        ));
        
        friends.add(new FriendEntity(
            "friend_004",
            "Phạm",
            "Minh D",
            "💎",
            "https://i.pravatar.cc/300?img=4",
            false,
            "phamminhd"
        ));
        
        friends.add(new FriendEntity(
            "friend_005",
            "Hoàng",
            "Thu E",
            "🌸",
            "https://i.pravatar.cc/300?img=5",
            false,
            "hoangthue"
        ));
        
        return friends;
    }

    // Mock Moments Data
    public static List<MomentEntity> getMockMoments() {
        List<MomentEntity> moments = new ArrayList<>();
        long currentTime = System.currentTimeMillis() / 1000;
        
        // Moment 1
        List<Overlay> overlays1 = new ArrayList<>();
        Overlay overlay1 = new Overlay();
        overlay1.setOverlay_id("caption:standard");
        overlay1.setOverlay_type("caption");
        overlay1.setAlt_text("Good morning!");
        
        OverlayData overlayData1 = new OverlayData();
        overlayData1.setText("Good morning!");
        overlayData1.setText_color("#FFFFFFE6");
        overlayData1.setType("standard");
        overlayData1.setMax_lines(4);
        
        Background background1 = new Background();
        overlay1.setData(overlayData1);
        overlays1.add(overlay1);
        
        moments.add(new MomentEntity(
            "moment_001",
            "friend_001",
            "https://picsum.photos/400/600?random=1",
            currentTime - 3600, // 1 hour ago
            "Good morning!",
            "md5_hash_001",
            overlays1
        ));
        
        // Moment 2
        List<Overlay> overlays2 = new ArrayList<>();
        Overlay overlay2 = new Overlay();
        overlay2.setOverlay_id("caption:standard");
        overlay2.setOverlay_type("caption");
        overlay2.setAlt_text("Having coffee ☕");
        
        OverlayData overlayData2 = new OverlayData();
        overlayData2.setText("Having coffee ☕");
        overlayData2.setText_color("#FFFFFFE6");
        overlayData2.setType("standard");
        overlayData2.setMax_lines(4);
        
        overlay2.setData(overlayData2);
        overlays2.add(overlay2);
        
        moments.add(new MomentEntity(
            "moment_002",
            "friend_002",
            "https://picsum.photos/400/600?random=2",
            currentTime - 7200, // 2 hours ago
            "Having coffee ☕",
            "md5_hash_002",
            overlays2
        ));
        
        // Moment 3
        List<Overlay> overlays3 = new ArrayList<>();
        moments.add(new MomentEntity(
            "moment_003",
            "friend_003",
            "https://picsum.photos/400/600?random=3",
            currentTime - 10800, // 3 hours ago
            "Beautiful sunset 🌅",
            "md5_hash_003",
            overlays3
        ));
        
        // Moment 4
        List<Overlay> overlays4 = new ArrayList<>();
        moments.add(new MomentEntity(
            "moment_004",
            "friend_004",
            "https://picsum.photos/400/600?random=4",
            currentTime - 14400, // 4 hours ago
            "Lunch time! 🍽️",
            "md5_hash_004",
            overlays4
        ));
        
        // Moment 5
        List<Overlay> overlays5 = new ArrayList<>();
        moments.add(new MomentEntity(
            "moment_005",
            "friend_005",
            "https://picsum.photos/400/600?random=5",
            currentTime - 18000, // 5 hours ago
            "Working from home 💻",
            "md5_hash_005",
            overlays5
        ));
        
        // Moment 6
        List<Overlay> overlays6 = new ArrayList<>();
        moments.add(new MomentEntity(
            "moment_006",
            "friend_001",
            "https://picsum.photos/400/600?random=6",
            currentTime - 21600, // 6 hours ago
            "Weekend vibes! 🎉",
            "md5_hash_006",
            overlays6
        ));
        
        return moments;
    }

    // Mock API Response for Moments
    public static String getMockMomentsApiResponse() {
        return "{\n" +
               "  \"result\": {\n" +
               "    \"status\": 200,\n" +
               "    \"data\": [\n" +
               "      {\n" +
               "        \"canonical_uid\": \"moment_001\",\n" +
               "        \"user\": \"friend_001\",\n" +
               "        \"thumbnail_url\": \"https://picsum.photos/400/600?random=1\",\n" +
               "        \"date\": {\n" +
               "          \"_seconds\": " + (System.currentTimeMillis() / 1000 - 3600) + ",\n" +
               "          \"_nanoseconds\": 0\n" +
               "        },\n" +
               "        \"caption\": \"Good morning!\",\n" +
               "        \"md5\": \"md5_hash_001\",\n" +
               "        \"overlays\": [\n" +
               "          {\n" +
               "            \"overlay_id\": \"caption:standard\",\n" +
               "            \"overlay_type\": \"caption\",\n" +
               "            \"alt_text\": \"Good morning!\",\n" +
               "            \"data\": {\n" +
               "              \"text\": \"Good morning!\",\n" +
               "              \"text_color\": \"#FFFFFFE6\",\n" +
               "              \"type\": \"standard\",\n" +
               "              \"max_lines\": 4\n" +
               "            }\n" +
               "          }\n" +
               "        ]\n" +
               "      },\n" +
               "      {\n" +
               "        \"canonical_uid\": \"moment_002\",\n" +
               "        \"user\": \"friend_002\",\n" +
               "        \"thumbnail_url\": \"https://picsum.photos/400/600?random=2\",\n" +
               "        \"date\": {\n" +
               "          \"_seconds\": " + (System.currentTimeMillis() / 1000 - 7200) + ",\n" +
               "          \"_nanoseconds\": 0\n" +
               "        },\n" +
               "        \"caption\": \"Having coffee ☕\",\n" +
               "        \"md5\": \"md5_hash_002\",\n" +
               "        \"overlays\": []\n" +
               "      }\n" +
               "    ]\n" +
               "  }\n" +
               "}";
    }

    // Mock API Response for Friends
    public static String getMockFriendsApiResponse() {
        return "{\n" +
               "  \"result\": {\n" +
               "    \"status\": 200,\n" +
               "    \"data\": [\n" +
               "      {\n" +
               "        \"uid\": \"friend_001\",\n" +
               "        \"first_name\": \"Nguyễn\",\n" +
               "        \"last_name\": \"Văn A\",\n" +
               "        \"badge\": \"🌟\",\n" +
               "        \"profile_picture_url\": \"https://i.pravatar.cc/300?img=1\",\n" +
               "        \"temp\": false,\n" +
               "        \"username\": \"nguyenvana\"\n" +
               "      },\n" +
               "      {\n" +
               "        \"uid\": \"friend_002\",\n" +
               "        \"first_name\": \"Trần\",\n" +
               "        \"last_name\": \"Thị B\",\n" +
               "        \"badge\": \"🎯\",\n" +
               "        \"profile_picture_url\": \"https://i.pravatar.cc/300?img=2\",\n" +
               "        \"temp\": false,\n" +
               "        \"username\": \"tranthib\"\n" +
               "      }\n" +
               "    ]\n" +
               "  }\n" +
               "}";
    }

    // Mock Login API Response
    public static String getMockLoginApiResponse() {
        return "{\n" +
               "  \"kind\": \"identitytoolkit#VerifyPasswordResponse\",\n" +
               "  \"localId\": \"mock_user_12345\",\n" +
               "  \"email\": \"test@example.com\",\n" +
               "  \"displayName\": \"Test User\",\n" +
               "  \"idToken\": \"mock_id_token_abcdef123456\",\n" +
               "  \"registered\": true,\n" +
               "  \"profilePicture\": \"https://i.pravatar.cc/300?img=10\",\n" +
               "  \"refreshToken\": \"mock_refresh_token_789xyz\",\n" +
               "  \"expiresIn\": \"3600\"\n" +
               "}";
    }

    // Mock Check Email Response
    public static String getMockCheckEmailResponse() {
        return "{\n" +
               "  \"status\": 200,\n" +
               "  \"message\": \"Email is valid\",\n" +
               "  \"exists\": true\n" +
               "}";
    }

    // Helper method to populate database with mock data
    public static void populateMockData(android.content.Context context) {
        // Lưu mock login response vào SharedPreferences
        SharedPreferencesUser.saveLoginResponse(context, getMockLoginResponse());
        
        // Có thể thêm logic để insert mock data vào Room database
        // Tuy nhiên cần thực hiện trên background thread
    }
} 