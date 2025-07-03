package com.example.locket.feed.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locket.R;
import com.example.locket.common.database.entities.MomentEntity;
import com.example.locket.common.models.auth.LoginResponse;
import com.example.locket.common.models.friend.Friend;
import com.example.locket.common.network.FriendApiService;
import com.example.locket.common.network.client.LoginApiClient;
import com.example.locket.common.utils.SharedPreferencesUser;
import com.google.android.material.imageview.ShapeableImageView;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

// ItemAdapter.java
public class ViewMomentAdapter extends RecyclerView.Adapter<ViewMomentAdapter.ItemViewHolder> {

    private List<MomentEntity> itemList; // Đổi từ Moment sang MomentEntity

    private final Context context;
    private final LoginResponse loginResponse;
    private final FriendApiService friendApiService;

    public ViewMomentAdapter(Context context, List<MomentEntity> itemList) {
        this.context = context;
        this.itemList = itemList;
        loginResponse = SharedPreferencesUser.getLoginResponse(context);
        friendApiService = LoginApiClient.getCheckEmailClient().create(FriendApiService.class);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFilterList(List<MomentEntity> filterList) {
        this.itemList = filterList;
        notifyDataSetChanged();
    }

    public interface FetchUserCallback {
        void onSuccess(Friend friend);

        void onFailure(String errorMessage);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_moment, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(itemList.get(position));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // ViewHolder cho mỗi item
    public class ItemViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView shapeable_imageview;
        TextView txt_content;
        RoundedImageView rounded_imageview;
        TextView txt_name;
        TextView txt_time;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            shapeable_imageview = itemView.findViewById(R.id.shapeable_imageview);
            txt_content = itemView.findViewById(R.id.txt_content);
            rounded_imageview = itemView.findViewById(R.id.rounded_imageview);
            txt_name = itemView.findViewById(R.id.txt_name);
            txt_time = itemView.findViewById(R.id.txt_time);
        }

        public void bind(MomentEntity moment) {
            // 🔧 FIX: Sử dụng CloudinaryImageLoader để tối ưu và giữ đúng tỷ lệ ảnh
            com.example.locket.common.utils.CloudinaryImageLoader.loadMomentImage(
                    context,
                    moment.getImageUrl(), // Sử dụng imageUrl thay vì thumbnailUrl
                    shapeable_imageview
            );

            // 📝 Hiển thị caption từ overlays hoặc caption field
            if (moment.getOverlays() != null && !moment.getOverlays().isEmpty()) {
                txt_content.setText(checkOverlayId(moment.getOverlays().get(0).getOverlay_id(), moment.getOverlays().get(0).getAlt_text(), txt_content));
                txt_content.setVisibility(View.VISIBLE);
            } else if (moment.getCaption() != null && !moment.getCaption().trim().isEmpty()) {
                txt_content.setText(moment.getCaption());
                txt_content.setVisibility(View.VISIBLE);
            } else {
                txt_content.setVisibility(View.GONE);
            }

            // 👤 Hiển thị thông tin user
            if (moment.getUser() != null && !moment.getUser().isEmpty()) {
                txt_name.setText(moment.getUser());
                txt_name.setVisibility(View.VISIBLE);

                // 🔧 Load default avatar - có thể mở rộng để load real avatar sau
                rounded_imageview.setImageResource(R.drawable.default_avatar);
                rounded_imageview.setVisibility(View.VISIBLE);
            } else {
                txt_name.setVisibility(View.GONE);
                rounded_imageview.setVisibility(View.GONE);
            }

            // ⏰ Hiển thị thời gian
            txt_time.setText(formatDate(moment.getDateSeconds()));
        }
    }

    @SuppressLint("ResourceAsColor")
    private String checkOverlayId(String overlay_id, String alt_text, TextView txt_content) {
        if (overlay_id.equals("caption:time")) {
            alt_text = "🕘 " + alt_text;
        } else if (overlay_id.equals("caption:party_time")) {
            Drawable backgroundDrawable = txt_content.getBackground();
            if (backgroundDrawable instanceof GradientDrawable) {
                txt_content.setBackgroundResource(R.drawable.gradient_party_time);
            }
            txt_content.setTextColor(ContextCompat.getColor(context, R.color.black));
            alt_text = "\uD83E\uDEA9 " + alt_text;
        } else if (overlay_id.equals("caption:goodnight")) {
            Drawable backgroundDrawable = txt_content.getBackground();
            if (backgroundDrawable instanceof GradientDrawable) {
                txt_content.setBackgroundResource(R.drawable.gradient_good_night);
            }
            txt_content.setTextColor(ContextCompat.getColor(context, R.color.white));
            alt_text = "\uD83C\uDF19 " + alt_text;
        } else if (overlay_id.equals("caption:miss_you")) {
            Drawable backgroundDrawable = txt_content.getBackground();
            if (backgroundDrawable instanceof GradientDrawable) {
                txt_content.setBackgroundResource(R.drawable.gradient_miss_you);
            }
            txt_content.setTextColor(ContextCompat.getColor(context, R.color.white));
            alt_text = "\ud83e\udd70 " + alt_text;
        } else if (overlay_id.equals("caption:text")) {
            // 📝 Default text caption - no special formatting
            // Keep original text and styling
        }
        return alt_text;
    }

    private String formatDate(long seconds) {
        long currentTimeMillis = System.currentTimeMillis();
        long timeInMillis = seconds * 1000;
        long diff = currentTimeMillis - timeInMillis;

        if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return minutes + "ph";
        }

        if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            return hours + "g";
        }

        long days = TimeUnit.MILLISECONDS.toDays(diff);
        if (days < 7) { // Nếu dưới 7 ngày thì hiển thị số ngày
            return days + "d";
        }

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        return dateFormat.format(new Date(timeInMillis));
    }

    @SuppressLint("DefaultLocale")
    private String createGetFriendsJson(String user_id) {
        return String.format(
                "{\"data\":{\"user_uid\":\"%s\"}}",
                user_id
        );
    }

    // 🚫 Disable friend API calls để tránh 404 errors - sẽ được enable sau khi có endpoint
    private void handleFriendAction(String userId) {
        // ❌ Backend không có friends endpoints - Disable để tránh 404
        Log.w("ViewMomentAdapter", "Friends endpoint not available, action disabled");
        return;

        /* OLD CODE - Endpoint không tồn tại
        String token = "Bearer " + loginResponse.getIdToken();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), createFriendJson(userId));
        Call<ResponseBody> responseBodyCall = friendApiService.FETCH_USER_RESPONSE_CALL(token, requestBody);

        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Handle friend response
                } else {
                    Log.e("ViewMomentAdapter", "Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                Log.e("ViewMomentAdapter", "Network error: " + throwable.getMessage());
            }
        });
        */
    }
}

