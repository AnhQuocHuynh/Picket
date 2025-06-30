package com.example.locket.auth.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.locket.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class RegisterEmailFragment extends Fragment {
    private static final String TAG = "RegisterEmailFragment";
    
    // Interface để nhận backend validation errors
    public interface ValidationErrorCallback {
        void onEmailError(String errorMessage);
        void onPasswordError(String errorMessage);
        void onGeneralError(String errorMessage);
    }
    
    private ImageView img_back;
    private EditText edt_email, edt_password, edt_confirm_password;
    private TextView txt_already_have_account;
    private LinearLayout linear_continue;
    private TextView txt_continue;
    private ImageView img_continue;
    
    // Error TextViews
    private TextView txt_email_error, txt_password_error, txt_confirm_password_error;
    
    private String email, password, confirmPassword;
    
    // Configuration flags
    private static final boolean ENABLE_PASSWORD_COMPLEXITY = true; // Set to false if backend doesn't require complexity

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_email, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        configViews();
        onClick();
    }

    private void initViews(View view) {
        img_back = view.findViewById(R.id.img_back);
        edt_email = view.findViewById(R.id.edt_email);
        edt_password = view.findViewById(R.id.edt_password);
        edt_confirm_password = view.findViewById(R.id.edt_confirm_password);
        txt_already_have_account = view.findViewById(R.id.txt_already_have_account);
        linear_continue = view.findViewById(R.id.linear_continue);
        txt_continue = view.findViewById(R.id.txt_continue);
        img_continue = view.findViewById(R.id.img_continue);
        
        // Error TextViews
        txt_email_error = view.findViewById(R.id.txt_email_error);
        txt_password_error = view.findViewById(R.id.txt_password_error);
        txt_confirm_password_error = view.findViewById(R.id.txt_confirm_password_error);
    }

    private void configViews() {
        edt_email.requestFocus();
        
        // Mở bàn phím
        requireActivity().getWindow().getDecorView().post(() -> {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edt_email, InputMethodManager.SHOW_IMPLICIT);
        });

        // TextWatcher để validate input
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
            }
        };

        edt_email.addTextChangedListener(textWatcher);
        edt_password.addTextChangedListener(textWatcher);
        edt_confirm_password.addTextChangedListener(textWatcher);
    }

    private void validateInputs() {
        email = edt_email.getText().toString().trim();
        password = edt_password.getText().toString().trim();
        confirmPassword = edt_confirm_password.getText().toString().trim();

        // Clear all previous errors
        clearAllErrors();

        boolean isValidEmail = validateEmail();
        boolean isValidPassword = validatePassword();
        boolean isValidConfirmPassword = validateConfirmPassword();

        boolean isValid = isValidEmail && isValidPassword && isValidConfirmPassword;

        if (isValid) {
            // Enable button
            linear_continue.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_btn_continue_check));
            txt_continue.setTextColor(getResources().getColor(R.color.bg));
            img_continue.setColorFilter(ContextCompat.getColor(requireContext(), R.color.bg));
            linear_continue.setEnabled(true);
        } else {
            // Disable button
            linear_continue.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_btn_continue_un_check));
            txt_continue.setTextColor(ContextCompat.getColor(requireContext(), R.color.hint));
            img_continue.setColorFilter(ContextCompat.getColor(requireContext(), R.color.hint));
            linear_continue.setEnabled(false);
        }
    }

    /**
     * 📧 Validate email field
     */
    private boolean validateEmail() {
        if (email.isEmpty()) {
            // Don't show error for empty email initially
            return false;
        }
        
        if (!isValidEmail(email)) {
            showFieldError(edt_email, txt_email_error, "Email không đúng định dạng");
            return false;
        }
        
        clearFieldError(edt_email, txt_email_error);
        return true;
    }

    /**
     * 🔐 Validate password field
     */
    private boolean validatePassword() {
        if (password.isEmpty()) {
            // Don't show error for empty password initially
            return false;
        }
        
        if (password.length() < 6) {
            showFieldError(edt_password, txt_password_error, "Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }
        
        if (password.length() > 50) {
            showFieldError(edt_password, txt_password_error, "Mật khẩu không được quá 50 ký tự");
            return false;
        }
        
        // Check password complexity (optional - only if backend requires it)
        if (ENABLE_PASSWORD_COMPLEXITY && !isPasswordComplex(password)) {
            showFieldError(edt_password, txt_password_error, "Mật khẩu phải chứa chữ hoa, chữ thường và số");
            return false;
        }
        
        clearFieldError(edt_password, txt_password_error);
        return true;
    }

    /**
     * 🔍 Check password complexity
     */
    private boolean isPasswordComplex(String password) {
        // Check for at least one uppercase letter
        boolean hasUppercase = password.matches(".*[A-Z].*");
        
        // Check for at least one lowercase letter  
        boolean hasLowercase = password.matches(".*[a-z].*");
        
        // Check for at least one digit
        boolean hasDigit = password.matches(".*\\d.*");
        
        return hasUppercase && hasLowercase && hasDigit;
    }

    /**
     * 🔍 Check if error message is about password complexity
     */
    private boolean isPasswordComplexityError(String errorMessage) {
        String lowerMessage = errorMessage.toLowerCase();
        return lowerMessage.contains("password must contain") ||
               lowerMessage.contains("uppercase") ||
               lowerMessage.contains("lowercase") ||
               lowerMessage.contains("number") ||
               lowerMessage.contains("digit") ||
               lowerMessage.contains("at least one") ||
               (lowerMessage.contains("password") && 
                (lowerMessage.contains("complex") || lowerMessage.contains("strength")));
    }

    /**
     * 🔒 Validate confirm password field
     */
    private boolean validateConfirmPassword() {
        if (confirmPassword.isEmpty()) {
            // Don't show error for empty confirm password initially
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            showFieldError(edt_confirm_password, txt_confirm_password_error, "Mật khẩu xác nhận không khớp");
            return false;
        }
        
        clearFieldError(edt_confirm_password, txt_confirm_password_error);
        return true;
    }

    /**
     * ❌ Show error for a specific field
     */
    private void showFieldError(EditText editText, TextView errorTextView, String errorMessage) {
        editText.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_edit_text_error));
        errorTextView.setText(errorMessage);
        errorTextView.setVisibility(View.VISIBLE);
    }

    /**
     * ✅ Clear error for a specific field
     */
    private void clearFieldError(EditText editText, TextView errorTextView) {
        editText.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_edit_text));
        errorTextView.setVisibility(View.GONE);
    }

    /**
     * 🧹 Clear all error states
     */
    private void clearAllErrors() {
        clearFieldError(edt_email, txt_email_error);
        clearFieldError(edt_password, txt_password_error);
        clearFieldError(edt_confirm_password, txt_confirm_password_error);
    }

    /**
     * 🚨 Handle backend validation errors
     */
    public void showBackendValidationErrors(String errorMessage, int errorCode) {
        // Clear all previous errors first
        clearAllErrors();
        
        switch (errorCode) {
            case 400:
                // Bad request - usually validation errors
                parseValidationErrors(errorMessage);
                break;
            case 409:
                // Conflict - email already exists
                showFieldError(edt_email, txt_email_error, "Email này đã được sử dụng");
                break;
            case 422:
                // Unprocessable Entity - validation failed
                parseValidationErrors(errorMessage);
                break;
            default:
                // General error
                showErrorDialog("Lỗi đăng ký", errorMessage);
                break;
        }
    }

    /**
     * 🔍 Parse backend validation errors and show on specific fields
     */
    private void parseValidationErrors(String errorMessage) {
        // First try to parse JSON error response
        String parsedError = parseJsonErrorResponse(errorMessage);
        if (parsedError != null) {
            errorMessage = parsedError;
        }
        
        String lowerErrorMessage = errorMessage.toLowerCase();
        
        // Password complexity validation errors
        if (isPasswordComplexityError(errorMessage)) {
            showFieldError(edt_password, txt_password_error, "Mật khẩu phải chứa chữ hoa, chữ thường và số");
            return;
        }
        
        // Email validation errors
        if (lowerErrorMessage.contains("email")) {
            if (lowerErrorMessage.contains("invalid") || lowerErrorMessage.contains("không hợp lệ")) {
                showFieldError(edt_email, txt_email_error, "Email không đúng định dạng");
            } else if (lowerErrorMessage.contains("required") || lowerErrorMessage.contains("bắt buộc")) {
                showFieldError(edt_email, txt_email_error, "Email là bắt buộc");
            } else if (lowerErrorMessage.contains("exists") || lowerErrorMessage.contains("đã tồn tại")) {
                showFieldError(edt_email, txt_email_error, "Email này đã được sử dụng");
            } else {
                showFieldError(edt_email, txt_email_error, "Email: " + getSimplifiedErrorMessage(errorMessage));
            }
            return;
        }
        
        // Password validation errors
        if (lowerErrorMessage.contains("password") || lowerErrorMessage.contains("mật khẩu")) {
            if (lowerErrorMessage.contains("short") || lowerErrorMessage.contains("ngắn")) {
                showFieldError(edt_password, txt_password_error, "Mật khẩu quá ngắn (tối thiểu 6 ký tự)");
            } else if (lowerErrorMessage.contains("weak") || lowerErrorMessage.contains("yếu")) {
                showFieldError(edt_password, txt_password_error, "Mật khẩu quá yếu");
            } else if (lowerErrorMessage.contains("required") || lowerErrorMessage.contains("bắt buộc")) {
                showFieldError(edt_password, txt_password_error, "Mật khẩu là bắt buộc");
            } else {
                showFieldError(edt_password, txt_password_error, getSimplifiedErrorMessage(errorMessage));
            }
            return;
        }
        
        // Username validation errors (nếu có username field ở màn hình này)
        if (lowerErrorMessage.contains("username") || lowerErrorMessage.contains("tên người dùng")) {
            // Không có username field trong RegisterEmailFragment, hiển thị general error
            showErrorDialog("Lỗi Username", getSimplifiedErrorMessage(errorMessage));
            return;
        }
        
        // General validation error
        showErrorDialog("Lỗi xác thực", getSimplifiedErrorMessage(errorMessage));
    }

    /**
     * 🔧 Parse JSON error response from backend
     */
    private String parseJsonErrorResponse(String errorMessage) {
        try {
            // Check if message contains JSON (look for Body: {...})
            if (errorMessage.contains("Body: {")) {
                int jsonStart = errorMessage.indexOf("Body: {");
                String jsonPart = errorMessage.substring(jsonStart + 6); // Remove "Body: "
                
                // Clean up the JSON part - remove trailing characters
                if (jsonPart.endsWith("}]")) {
                    jsonPart = jsonPart.substring(0, jsonPart.length() - 1); // Remove "]"
                }
                if (jsonPart.endsWith("}]")) {
                    jsonPart = jsonPart.substring(0, jsonPart.length() - 1); // Remove another "]" if exists
                }
                
                Log.d(TAG, "Parsing JSON: " + jsonPart);
                JSONObject jsonObject = new JSONObject(jsonPart);
                
                // Try to get errors array - this is the main error messages
                if (jsonObject.has("errors")) {
                    Object errorsObj = jsonObject.get("errors");
                    
                    if (errorsObj instanceof JSONArray) {
                        JSONArray errorsArray = (JSONArray) errorsObj;
                        StringBuilder errorBuilder = new StringBuilder();
                        
                        for (int i = 0; i < errorsArray.length(); i++) {
                            Object errorItem = errorsArray.get(i);
                            
                            if (errorItem instanceof String) {
                                String error = (String) errorItem;
                                // Only include actual error messages, skip metadata
                                if (!error.contains("path") && !error.contains("location") && 
                                    !error.equals("password") && !error.equals("body") &&
                                    error.length() > 5) { // Skip very short non-descriptive items
                                    
                                    if (errorBuilder.length() > 0) {
                                        errorBuilder.append(". ");
                                    }
                                    errorBuilder.append(error);
                                }
                            }
                        }
                        
                        if (errorBuilder.length() > 0) {
                            return errorBuilder.toString();
                        }
                    }
                }
                
                // Fallback to message field
                if (jsonObject.has("message")) {
                    String message = jsonObject.getString("message");
                    if (!message.equals("Validation failed")) {
                        return message;
                    }
                }
            }
            
            // Also try to extract JSON if it's embedded differently
            if (errorMessage.contains("{") && errorMessage.contains("}")) {
                int start = errorMessage.indexOf("{");
                int end = errorMessage.lastIndexOf("}") + 1;
                
                if (start >= 0 && end > start) {
                    String jsonPart = errorMessage.substring(start, end);
                    // Remove trailing characters that might break JSON
                    jsonPart = jsonPart.replaceAll("\\]$", "");
                    
                    try {
                        JSONObject jsonObject = new JSONObject(jsonPart);
                        return parseErrorsFromJson(jsonObject);
                    } catch (Exception e) {
                        Log.d(TAG, "Failed to parse alternative JSON format: " + e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse JSON error response: " + e.getMessage());
            Log.e(TAG, "Original message: " + errorMessage);
        }
        
        return null; // Return null if parsing failed
    }

    /**
     * 🔧 Helper method to parse errors from JSON object
     */
    private String parseErrorsFromJson(JSONObject jsonObject) throws Exception {
        if (jsonObject.has("errors")) {
            JSONArray errorsArray = jsonObject.getJSONArray("errors");
            StringBuilder errorBuilder = new StringBuilder();
            
            for (int i = 0; i < errorsArray.length(); i++) {
                String error = errorsArray.getString(i);
                // Filter out metadata and keep only actual error messages
                if (error.length() > 10 && 
                    !error.contains("path") && 
                    !error.contains("location") &&
                    !error.equals("password") && 
                    !error.equals("body")) {
                    
                    if (errorBuilder.length() > 0) {
                        errorBuilder.append(". ");
                    }
                    errorBuilder.append(error);
                }
            }
            
            if (errorBuilder.length() > 0) {
                return errorBuilder.toString();
            }
        }
        
        if (jsonObject.has("message")) {
            return jsonObject.getString("message");
        }
        
        return null;
    }

    /**
     * 📝 Get simplified error message for display
     */
    private String getSimplifiedErrorMessage(String originalMessage) {
        // If message is too long or contains technical details, simplify it
        if (originalMessage.length() > 100 || originalMessage.contains("Body:") || originalMessage.contains("{")) {
            // Extract the first part before any technical details
            String simplified = originalMessage.split("\\|")[0].trim();
            simplified = simplified.split("Body:")[0].trim();
            
            if (simplified.length() > 80) {
                simplified = simplified.substring(0, 80) + "...";
            }
            
            return simplified;
        }
        
        return originalMessage;
    }

    /**
     * 💡 Pre-validate before sending to backend (public method)
     */
    public boolean preValidateForBackend() {
        email = edt_email.getText().toString().trim();
        password = edt_password.getText().toString().trim();
        confirmPassword = edt_confirm_password.getText().toString().trim();

        clearAllErrors();

        boolean hasErrors = false;

        // Email validation
        if (email.isEmpty()) {
            showFieldError(edt_email, txt_email_error, "Email là bắt buộc");
            hasErrors = true;
        } else if (!isValidEmail(email)) {
            showFieldError(edt_email, txt_email_error, "Email không đúng định dạng");
            hasErrors = true;
        }

        // Password validation
        if (password.isEmpty()) {
            showFieldError(edt_password, txt_password_error, "Mật khẩu là bắt buộc");
            hasErrors = true;
        } else if (password.length() < 6) {
            showFieldError(edt_password, txt_password_error, "Mật khẩu phải có ít nhất 6 ký tự");
            hasErrors = true;
        } else if (password.length() > 50) {
            showFieldError(edt_password, txt_password_error, "Mật khẩu không được quá 50 ký tự");
            hasErrors = true;
        } else if (ENABLE_PASSWORD_COMPLEXITY && !isPasswordComplex(password)) {
            showFieldError(edt_password, txt_password_error, "Mật khẩu phải chứa chữ hoa, chữ thường và số");
            hasErrors = true;
        }

        // Confirm password validation
        if (confirmPassword.isEmpty()) {
            showFieldError(edt_confirm_password, txt_confirm_password_error, "Xác nhận mật khẩu là bắt buộc");
            hasErrors = true;
        } else if (!password.equals(confirmPassword)) {
            showFieldError(edt_confirm_password, txt_confirm_password_error, "Mật khẩu xác nhận không khớp");
            hasErrors = true;
        }

        return !hasErrors;
    }

    /**
     * 🧪 Demo method to test validation scenarios (for development)
     */
    @SuppressWarnings("unused")
    private void demoValidationScenarios() {
        // Demo different backend error responses
        
        // Scenario 1: Password complexity error (like in the screenshot)
        String complexityError = "Mật khẩu invalid request data | Body: {\"success\":false,\"message\":\"Validation failed\",\"errors\":[\"Password must contain at least one uppercase letter, one lowercase letter, and one number\",\"path\":\"password\",\"location\":\"body\"]}]";
        showBackendValidationErrors(complexityError, 422);
        
        // Scenario 2: Email already exists
        String emailExistsError = "Email này đã được sử dụng";
        showBackendValidationErrors(emailExistsError, 409);
        
        // Scenario 3: Invalid email format
        String invalidEmailError = "Email invalid format | Body: {\"success\":false,\"message\":\"Email is not valid\",\"errors\":[\"Invalid email format\"]}";
        showBackendValidationErrors(invalidEmailError, 400);
    }

    private void onClick() {
        img_back.setOnClickListener(view -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        txt_already_have_account.setOnClickListener(view -> {
            // Chuyển đến màn hình đăng nhập
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(
                    R.anim.enter_from_left,
                    R.anim.exit_to_right,
                    R.anim.enter_from_right,
                    R.anim.exit_to_left
            );
            transaction.replace(R.id.frame_layout, new LoginEmailFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        linear_continue.setOnClickListener(view -> {
            if (validateForm()) {
                hideKeyboard();
                performRegistration();
            }
        });
    }

    private boolean validateForm() {
        if (!isValidEmail(email)) {
            showErrorDialog("Email không hợp lệ", "Vui lòng nhập email đúng định dạng.");
            return false;
        }

        if (password.length() < 6) {
            showErrorDialog("Mật khẩu quá ngắn", "Mật khẩu phải có ít nhất 6 ký tự.");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showErrorDialog("Mật khẩu không khớp", "Mật khẩu xác nhận không giống với mật khẩu đã nhập.");
            return false;
        }

        return true;
    }

    private void performRegistration() {
        Log.d(TAG, "🚀 Starting registration flow for email: " + email);
        
        // Không gọi register API ở đây, chỉ lưu data và chuyển đến username screen
        // Register API sẽ được gọi từ RegisterUserNameFragment với đầy đủ thông tin
        
        // Chuyển đến RegisterUserNameFragment để nhập username
        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        bundle.putString("password", password);
        
        RegisterUserNameFragment registerUserNameFragment = new RegisterUserNameFragment();
        registerUserNameFragment.setArguments(bundle);
        
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
        );
        transaction.replace(R.id.frame_layout, registerUserNameFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getView();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private boolean isValidEmail(CharSequence email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
} 