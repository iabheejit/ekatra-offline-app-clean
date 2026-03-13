package org.ekatra.alfred

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import org.ekatra.alfred.viewmodel.AuthStep
import org.ekatra.alfred.viewmodel.ProfileViewModel

@AndroidEntryPoint
class ProfileActivity : ComponentActivity() {

    private var pendingGoogleToken: String? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { token ->
                pendingGoogleToken = token
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_EDIT_PROFILE = "edit_profile"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val isEditMode = intent.getBooleanExtra(EXTRA_EDIT_PROFILE, false)

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF2563EB),
                    background = Color(0xFFF6F6F8)
                )
            ) {
                val viewModel: ProfileViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()

                // If opened from Settings, load existing profile for editing
                LaunchedEffect(isEditMode) {
                    if (isEditMode) {
                        viewModel.loadExistingProfile()
                    }
                }

                // Handle pending Google token
                LaunchedEffect(pendingGoogleToken) {
                    pendingGoogleToken?.let {
                        viewModel.signInWithGoogle(it)
                        pendingGoogleToken = null
                    }
                }

                // Navigate when authenticated (only for login flow, not edit mode)
                LaunchedEffect(uiState.authStep) {
                    if (uiState.authStep == AuthStep.AUTHENTICATED && !isEditMode) {
                        startActivity(Intent(this@ProfileActivity, ModelDownloadActivity::class.java))
                        finish()
                    }
                }

                when (uiState.authStep) {
                    AuthStep.EMAIL_INPUT -> EmailInputScreen(
                        email = uiState.email,
                        password = uiState.password,
                        isLoading = uiState.isLoading,
                        error = uiState.error,
                        onEmailChange = viewModel::updateEmail,
                        onPasswordChange = viewModel::updatePassword,
                        onSignIn = viewModel::signInWithEmail,
                        onCreateAccount = viewModel::createAccountWithEmail,
                        onGoogleSignIn = { launchGoogleSignIn() }
                    )
                    AuthStep.PROFILE_FORM -> ProfileFormScreen(
                        name = uiState.name,
                        country = uiState.country,
                        grade = uiState.grade,
                        isLoading = uiState.isLoading,
                        error = uiState.error,
                        onNameChange = viewModel::updateName,
                        onCountryChange = viewModel::updateCountry,
                        onGradeChange = viewModel::updateGrade,
                        onSave = viewModel::saveProfile
                    )
                    AuthStep.EDIT_PROFILE -> EditProfileScreen(
                        name = uiState.name,
                        email = uiState.email,
                        country = uiState.country,
                        grade = uiState.grade,
                        isLoading = uiState.isLoading,
                        error = uiState.error,
                        onNameChange = viewModel::updateName,
                        onCountryChange = viewModel::updateCountry,
                        onGradeChange = viewModel::updateGrade,
                        onSave = {
                            viewModel.saveProfile()
                            // After saving in edit mode, go back to settings
                        },
                        onBack = { finish() },
                        onSaved = uiState.authStep == AuthStep.AUTHENTICATED
                    )
                    AuthStep.AUTHENTICATED -> {
                        if (isEditMode) {
                            // Profile saved successfully in edit mode — go back
                            LaunchedEffect(Unit) { finish() }
                        } else {
                            // Will navigate away via LaunchedEffect
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(this, gso)
        googleSignInLauncher.launch(client.signInIntent)
    }
}

// ==================== Email Input Screen ====================

@Composable
fun EmailInputScreen(
    email: String,
    password: String,
    isLoading: Boolean,
    error: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onCreateAccount: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    var isCreatingAccount by remember { mutableStateOf(false) }
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val horizontalPadding = if (screenWidth < 360) 16.dp else 24.dp
    val emojiSize = if (screenWidth < 360) 36.sp else 48.sp
    val titleSize = if (screenWidth < 360) 20.sp else 24.sp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F8))
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = horizontalPadding, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(1f))

        Text("📘", fontSize = emojiSize)
        Spacer(Modifier.height(12.dp))
        Text("Welcome to Maya AI", fontSize = titleSize, fontWeight = FontWeight.Bold)
        Text("Your personal AI tutor", fontSize = 14.sp, color = Color(0xFF6B7280))

        Spacer(Modifier.height(24.dp))

        // Login / Sign Up header
        Text(
            text = if (isCreatingAccount) "Create Account" else "Sign In",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111318),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email address") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        error?.let {
            Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = if (isCreatingAccount) onCreateAccount else onSignIn,
            enabled = !isLoading && email.isNotBlank() && password.length >= 6,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            else Text(if (isCreatingAccount) "Create Account" else "Sign In")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = { isCreatingAccount = !isCreatingAccount }) {
            Text(
                if (isCreatingAccount) "Already have an account? Sign In" else "Don't have an account? Create One",
                color = Color(0xFF2563EB)
            )
        }

        // Google Sign-In (OAuth clients now configured)
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text("  or  ", color = Color(0xFF9CA3AF), fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onGoogleSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sign in with Google")
        }

        Spacer(Modifier.weight(1f))
    }
}

// ==================== Phone Input Screen (DISABLED - Enable when Firebase Phone Auth is available) ====================

/*
@Composable
fun PhoneInputScreen(
    phone: String,
    isLoading: Boolean,
    error: String?,
    onPhoneChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F8))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📘", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text("Welcome to Maya AI", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Your personal AI tutor", fontSize = 14.sp, color = Color(0xFF6B7280))

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Phone number") },
            prefix = { Text("+91 ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        error?.let {
            Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onSendOtp,
            enabled = !isLoading && phone.length >= 10,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            else Text("Send OTP")
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text("  or  ", color = Color(0xFF9CA3AF), fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onGoogleSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sign in with Google")
        }
    }
}
*/

// ==================== OTP Verification Screen (DISABLED - Enable when Firebase Phone Auth is available) ====================

/*
@Composable
fun OtpScreen(
    otp: String,
    phone: String,
    isLoading: Boolean,
    error: String?,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F8))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔐", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text("Verify your number", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Code sent to $phone", fontSize = 14.sp, color = Color(0xFF6B7280))

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 6) onOtpChange(it) },
            label = { Text("6-digit code") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        error?.let {
            Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onVerify,
            enabled = !isLoading && otp.length == 6,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            else Text("Verify")
        }
    }
}
*/

// ==================== Profile Form Screen ====================

@Composable
fun ProfileFormScreen(
    name: String,
    country: String,
    grade: String,
    isLoading: Boolean,
    error: String?,
    onNameChange: (String) -> Unit,
    onCountryChange: (String) -> Unit,
    onGradeChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val horizontalPadding = if (screenWidth < 360) 16.dp else 24.dp
    val emojiSize = if (screenWidth < 360) 36.sp else 48.sp
    val titleSize = if (screenWidth < 360) 20.sp else 24.sp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F8))
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = horizontalPadding, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(1f))

        Text("👋", fontSize = emojiSize)
        Spacer(Modifier.height(12.dp))
        Text("Tell us about yourself", fontSize = titleSize, fontWeight = FontWeight.Bold)
        Text("This helps Maya personalize your learning", fontSize = 14.sp, color = Color(0xFF6B7280))

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Your name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = country,
            onValueChange = onCountryChange,
            label = { Text("Country") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = grade,
            onValueChange = onGradeChange,
            label = { Text("Grade / Education level (optional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        error?.let {
            Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onSave,
            enabled = !isLoading && name.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            else Text("Get Started →")
        }

        Spacer(Modifier.weight(1f))
    }
}

// ==================== Edit Profile Screen (from Settings) ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    name: String,
    email: String,
    country: String,
    grade: String,
    isLoading: Boolean,
    error: String?,
    onNameChange: (String) -> Unit,
    onCountryChange: (String) -> Unit,
    onGradeChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onSaved: Boolean = false
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val horizontalPadding = if (screenWidth < 360) 16.dp else 24.dp

    // Auto-close after save
    LaunchedEffect(onSaved) {
        if (onSaved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF111318)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(Color(0xFF2563EB)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    name.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            // Email (read-only)
            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = false
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Your name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = country,
                onValueChange = onCountryChange,
                label = { Text("Country") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = grade,
                onValueChange = onGradeChange,
                label = { Text("Grade / Education level") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            error?.let {
                Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onSave,
                enabled = !isLoading && name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Save Changes")
            }
        }
    }
}
