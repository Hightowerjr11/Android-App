/*
 * Copyright (c) 2021 Windscribe Limited.
 */
package com.windscribe.mobile.welcome

import android.text.TextUtils
import android.util.Patterns
import android.view.View
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.windscribe.mobile.BuildConfig
import com.windscribe.mobile.R
import com.windscribe.vpn.ActivityInteractor
import com.windscribe.vpn.api.CreateHashMap.createClaimAccountMap
import com.windscribe.vpn.api.CreateHashMap.createGhostModeMap
import com.windscribe.vpn.api.CreateHashMap.createLoginMap
import com.windscribe.vpn.api.CreateHashMap.createRegistrationMap
import com.windscribe.vpn.api.response.*
import com.windscribe.vpn.constants.NetworkErrorCodes
import com.windscribe.vpn.constants.NetworkKeyConstants
import com.windscribe.vpn.constants.UserStatusConstants.USER_STATUS_PREMIUM
import com.windscribe.vpn.errormodel.SessionErrorHandler
import com.windscribe.vpn.errormodel.WindError
import com.windscribe.vpn.services.ping.PingTestService.Companion.startPingTestService
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

class WelcomePresenterImpl @Inject constructor(
    private val welcomeView: WelcomeView, private val interactor: ActivityInteractor
) : WelcomePresenter {

    private val logger = LoggerFactory.getLogger("login-p")

    override fun onDestroy() {
        interactor.getCompositeDisposable().clear()
    }

    override fun exportLog() {
        try {
            val file = File(interactor.getDebugFilePath())
            welcomeView.launchShareIntent(file)
        } catch (e: Exception) {
            welcomeView.showToast(WindError.instance.rxErrorToString(e))
        }
    }

    override val isUserPro: Boolean
        get() = interactor.getAppPreferenceInterface().userStatus == USER_STATUS_PREMIUM

    override fun onBackPressed() {
        interactor.getCompositeDisposable().clear()
        welcomeView.hideSoftKeyboard()
    }

    override fun startAccountClaim(
        username: String,
        password: String,
        email: String,
        ignoreEmptyEmail: Boolean
    ) {
        welcomeView.hideSoftKeyboard()
        if (validateLoginInputs(username, password, email, false)) {
            if (ignoreEmptyEmail.not() && email.isEmpty()) {
                val proUser = (interactor.getAppPreferenceInterface().userStatus
                        == USER_STATUS_PREMIUM)
                welcomeView.showNoEmailAttentionFragment(username, password, true, proUser)
                return
            }
            logger.info("Trying to claim account with provided credentials...")
            welcomeView.prepareUiForApiCallFinished()
            welcomeView.prepareUiForApiCallStart()
            val loginMap = createClaimAccountMap(username, password)
            if (email.isNotEmpty()) {
                loginMap[NetworkKeyConstants.ADD_EMAIL_KEY] = email
            }
            interactor.getCompositeDisposable().add(
                interactor.getApiCallManager()
                    .claimAccount(loginMap)
                    .doOnSubscribe { welcomeView.updateCurrentProcess("Signing up") }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(
                        object :
                            DisposableSingleObserver<GenericResponseClass<ClaimAccountResponse?, ApiErrorResponse?>>() {
                            override fun onError(e: Throwable) {
                                logger.debug("User SignUp error..." + e.message)
                                onSignUpFailedWithNoError()
                            }

                            override fun onSuccess(
                                genericLoginResponse: GenericResponseClass<ClaimAccountResponse?, ApiErrorResponse?>
                            ) {
                                if (genericLoginResponse.dataClass != null) {
                                    logger.info("Account claimed successfully...")
                                    welcomeView.updateCurrentProcess("SignUp successful...")
                                    onAccountClaimSuccess(username)
                                } else if (genericLoginResponse.errorClass != null) {
                                    logger.info(
                                        "Account claim..." + genericLoginResponse
                                            .errorClass
                                    )
                                    onLoginResponseError(genericLoginResponse.errorClass!!)
                                } else {
                                    onSignUpFailedWithNoError()
                                }
                            }
                        })
            )
        }
    }

    override fun startGhostAccountSetup() {
        welcomeView.prepareUiForApiCallStart()
        welcomeView.updateCurrentProcess("Signing In")
        interactor.getCompositeDisposable().add(
            interactor.getApiCallManager().getReg(null)
                .flatMap(
                    Function<GenericResponseClass<RegToken?, ApiErrorResponse?>, SingleSource<GenericResponseClass<UserRegistrationResponse?, ApiErrorResponse?>>> label@{ regToken: GenericResponseClass<RegToken?, ApiErrorResponse?> ->
                        if (regToken.dataClass != null) {
                            val ghostModeMap = createGhostModeMap(
                                regToken.dataClass!!.token
                            )
                            return@label interactor.getApiCallManager().signUserIn(ghostModeMap)
                        } else if (regToken.errorClass != null) {
                            throw Exception(regToken.errorClass!!.errorMessage)
                        } else {
                            throw Exception("Unknown Error")
                        }
                    }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                    object :
                        DisposableSingleObserver<GenericResponseClass<UserRegistrationResponse?, ApiErrorResponse?>>() {
                        override fun onError(e: Throwable) {
                            welcomeView.prepareUiForApiCallFinished()
                            if (e is IOException) {
                                welcomeView.showError("Unable to reach server. Check your network connection.")
                            } else {
                                logger.debug(e.message)
                                welcomeView.goToSignUp()
                            }
                        }

                        override fun onSuccess(
                            regResponse: GenericResponseClass<UserRegistrationResponse?, ApiErrorResponse?>
                        ) {
                            if (regResponse.errorClass != null) {
                                logger.debug(regResponse.errorClass!!.errorMessage)
                                welcomeView.prepareUiForApiCallFinished()
                                welcomeView.goToSignUp()
                            } else {
                                interactor.getAppPreferenceInterface().sessionHash =
                                    regResponse.dataClass!!.sessionAuthHash
                                setFireBaseDeviceToken
                            }
                        }
                    })
        )
    }

    override fun startLoginProcess(username: String, password: String, twoFa: String) {
        welcomeView.hideSoftKeyboard()
        if (validateLoginInputs(username, password, "", true)) {
            logger.info("Trying to login with provided credentials...")
            welcomeView.prepareUiForApiCallStart()
            val loginMap = createLoginMap(username, password, twoFa)
            interactor.getCompositeDisposable().add(
                interactor.getApiCallManager()
                    .logUserIn(loginMap)
                    .doOnSubscribe { welcomeView.updateCurrentProcess("Signing in...") }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(
                        object :
                            DisposableSingleObserver<GenericResponseClass<UserLoginResponse?, ApiErrorResponse?>>() {
                            override fun onError(e: Throwable) {
                                if (e is Exception) {
                                    logger.debug(
                                        "Login Error: " + WindError.instance.rxErrorToString(
                                            e,
                                        )
                                    )
                                }
                                onLoginFailedWithNoError()
                            }

                            override fun onSuccess(
                                genericLoginResponse: GenericResponseClass<UserLoginResponse?, ApiErrorResponse?>
                            ) {
                                if (genericLoginResponse.dataClass != null) {
                                    logger.info("Logged user in successfully...")
                                    welcomeView.updateCurrentProcess("Login successful...")
                                    interactor.getAppPreferenceInterface().sessionHash =
                                        genericLoginResponse.dataClass!!.sessionAuthHash
                                    setFireBaseDeviceToken
                                } else if (genericLoginResponse.errorClass != null) {
                                    logger.info(
                                        "Login error..." + genericLoginResponse
                                            .errorClass
                                    )
                                    onLoginResponseError(genericLoginResponse.errorClass!!)
                                } else {
                                    onLoginFailedWithNoError()
                                }
                            }
                        })
            )
        }
    }

    override fun startSignUpProcess(
        username: String,
        password: String,
        email: String,
        referralUsername: String,
        ignoreEmptyEmail: Boolean
    ) {
        welcomeView.hideSoftKeyboard()
        if (validateLoginInputs(username, password, email, false)) {
            if (!ignoreEmptyEmail && email.isEmpty()) {
                welcomeView.showNoEmailAttentionFragment(
                    username, password,
                    accountClaim = false,
                    pro = false
                )
                return
            }
            logger.info("Trying to sign up with provided credentials...")
            welcomeView.prepareUiForApiCallStart()
            val registrationMap = createRegistrationMap(username, password).toMutableMap()
            if (email.isNotEmpty()) {
                registrationMap[NetworkKeyConstants.ADD_EMAIL_KEY] = email
            }
            if (referralUsername.isNotEmpty()) {
                registrationMap[NetworkKeyConstants.REFERRING_USERNAME] = referralUsername
            }
            interactor.getCompositeDisposable().add(
                interactor.getApiCallManager()
                    .signUserIn(registrationMap)
                    .doOnSubscribe { welcomeView.updateCurrentProcess("Signing up") }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(
                        object :
                            DisposableSingleObserver<GenericResponseClass<UserRegistrationResponse?, ApiErrorResponse?>>() {
                            override fun onError(e: Throwable) {
                                logger.debug("User SignUp error..." + e.message)
                                onSignUpFailedWithNoError()
                            }

                            override fun onSuccess(
                                genericLoginResponse: GenericResponseClass<UserRegistrationResponse?, ApiErrorResponse?>
                            ) {
                                if (genericLoginResponse.dataClass != null) {
                                    logger.info("Sign up user successfully...")
                                    welcomeView.updateCurrentProcess("SignUp successful...")
                                    interactor.getAppPreferenceInterface().sessionHash =
                                        genericLoginResponse.dataClass!!.sessionAuthHash
                                    setFireBaseDeviceToken
                                } else if (genericLoginResponse.errorClass != null) {
                                    logger
                                        .info("SignUp..." + genericLoginResponse.errorClass)
                                    onLoginResponseError(genericLoginResponse.errorClass!!)
                                } else {
                                    onSignUpFailedWithNoError()
                                }
                            }
                        })
            )
        }
    }

    private fun evaluatePassword(password: String): Boolean {
        val pattern = Regex("(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}")
        return password.matches(pattern)
    }

    private val setFireBaseDeviceToken: Unit
        get() {
            val sessionMap: MutableMap<String, String> = HashMap()
            if (BuildConfig.API_KEY.isEmpty()) {
                prepareLoginRegistrationDashboard(sessionMap)
            } else {
                FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult: InstanceIdResult ->
                    try {
                        val newToken = instanceIdResult.token
                        logger.debug("" + newToken.length)
                        logger.info("Received firebase device token.")
                        logger.info(newToken)
                        sessionMap[NetworkKeyConstants.FIREBASE_DEVICE_ID_KEY] = newToken
                    } catch (e: RuntimeExecutionException) {
                        logger.debug(
                            "No registered account for the selected device! " + WindError.instance
                                .convertErrorToString(e)
                        )
                    }
                    prepareLoginRegistrationDashboard(sessionMap)
                }.addOnFailureListener {
                    prepareLoginRegistrationDashboard(sessionMap)
                }
                    .addOnCanceledListener { prepareLoginRegistrationDashboard(sessionMap) }
            }
        }

    private fun onAccountClaimSuccess(username: String) {
        welcomeView.updateCurrentProcess("Getting session")
        interactor.getCompositeDisposable().add(
            interactor.getApiCallManager().getSessionGeneric(null)
                .flatMapCompletable { sessionResponse: GenericResponseClass<UserSessionResponse?, ApiErrorResponse?> ->
                    Completable.fromSingle(
                        Single.fromCallable {
                            interactor.getUserRepository().reload(sessionResponse.dataClass, null)
                            true
                        })
                }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        welcomeView.gotoHomeActivity(true)
                    }

                    override fun onError(e: Throwable) {
                        welcomeView.prepareUiForApiCallFinished()
                        welcomeView.showError("Unable to auto login. Log in using new credentials.")
                        logger.debug(
                            "Error getting session"
                                    + WindError.instance.convertThrowableToString(e)
                        )
                    }
                })
        )
    }

    private fun onLoginFailedWithNoError() {
        welcomeView.prepareUiForApiCallFinished()
        welcomeView.showFailedAlert(interactor.getResourceString(R.string.failed_network_alert))
    }

    private fun onLoginResponseError(apiErrorResponse: ApiErrorResponse) {
        logger.debug(apiErrorResponse.toString())
        welcomeView.prepareUiForApiCallFinished()
        val errorMessage = SessionErrorHandler.instance.getErrorMessage(apiErrorResponse)
        when (apiErrorResponse.errorCode) {
            NetworkErrorCodes.ERROR_2FA_REQUIRED, NetworkErrorCodes.ERROR_INVALID_2FA -> {
                welcomeView.setFaFieldsVisibility(View.VISIBLE)
                welcomeView.setTwoFaError(errorMessage)
            }
            NetworkErrorCodes.ERROR_USER_NAME_ALREADY_TAKEN, NetworkErrorCodes.ERROR_USER_NAME_ALREADY_IN_USE -> {
                welcomeView.setUsernameError(errorMessage)
            }
            NetworkErrorCodes.ERROR_EMAIL_ALREADY_EXISTS, NetworkErrorCodes.ERROR_DISPOSABLE_EMAIL -> {
                welcomeView.setEmailError(errorMessage)
            }
            else -> {
                welcomeView.setLoginRegistrationError(errorMessage)
            }
        }
    }

    private fun onSignUpFailedWithNoError() {
        welcomeView.prepareUiForApiCallFinished()
        welcomeView.showFailedAlert(interactor.getResourceString(R.string.sign_up_failed_network_alert))
    }

    private fun prepareLoginRegistrationDashboard(sessionMap: Map<String, String>) {
        welcomeView.updateCurrentProcess("Getting session")
        interactor.getCompositeDisposable().add(
            interactor.getApiCallManager().getSessionGeneric(sessionMap)
                .flatMapCompletable { sessionResponse: GenericResponseClass<UserSessionResponse?, ApiErrorResponse?> ->
                    Completable.fromSingle(
                        Single.fromCallable {
                            if (interactor.getAppPreferenceInterface()
                                    .getDeviceUUID(sessionResponse.dataClass!!.userName) == null
                            ) {
                                logger.debug("No device id is found for the current user, generating and saving UUID")
                                interactor.getAppPreferenceInterface().setDeviceUUID(
                                    sessionResponse.dataClass!!.userName,
                                    UUID.randomUUID().toString()
                                )
                            }
                            interactor.getUserRepository().reload(sessionResponse.dataClass, null)
                            true
                        })
                }.andThen(updateStaticIps())
                .doOnComplete { welcomeView.updateCurrentProcess("Getting user credentials") }
                .andThen(interactor.getConnectionDataUpdater().update())
                .doOnComplete { welcomeView.updateCurrentProcess("Getting server list") }
                .andThen(interactor.getServerListUpdater().update())
                .andThen(Completable.fromAction {
                    interactor.getPreferenceChangeObserver().postCityServerChange()
                })
                .andThen(interactor.updateUserData())
                .onErrorResumeNext { throwable: Throwable ->
                    logger.info(
                        "*****Preparing dashboard failed: " + throwable.toString()
                                + " Use reload button in server list in home activity."
                    )
                    Completable.fromAction {
                        interactor.getPreferenceChangeObserver().postCityServerChange()
                    }
                        .andThen(interactor.updateUserData())
                }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        interactor.getWorkManager().onAppStart()
                        interactor.getWorkManager().onAppMovedToForeground()
                        startPingTestService()
                        interactor.getWorkManager().onAppStart()
                        welcomeView.gotoHomeActivity(true)
                    }

                    override fun onError(e: Throwable) {
                        welcomeView.prepareUiForApiCallFinished()
                        logger.debug(
                            "Error while updating server status to local db. StackTrace: "
                                    + WindError.instance.convertThrowableToString(e)
                        )
                    }
                })
        )
    }

    private fun updateStaticIps(): Completable {
        val user = interactor.getUserRepository().user.value
        return if (user != null && user.sipCount > 0) {
            interactor.getStaticListUpdater().update()
        } else {
            Completable.fromAction {}
        }
    }

    private fun validateLoginInputs(
        username: String, password: String, email: String,
        isLogin: Boolean
    ): Boolean {
        logger.info("Validating login credentials")
        welcomeView.clearInputErrors()

        //Empty username
        if (TextUtils.isEmpty(username)) {
            logger.info("[username] is empty, displaying toast to the user...")
            welcomeView.setUsernameError(interactor.getResourceString(R.string.username_empty))
            welcomeView.showToast(interactor.getResourceString(R.string.enter_username))
            return false
        }

        //Invalid username
        if (!validateUsernameCharacters(username)) {
            logger.info("[username] has invalid characters in , displaying toast to the user...")
            welcomeView.setUsernameError(interactor.getResourceString(R.string.login_with_username))
            welcomeView.showToast(interactor.getResourceString(R.string.login_with_username))
            return false
        }

        //Empty Password
        if (TextUtils.isEmpty(password)) {
            logger.info("[password] is empty, displaying toast to the user...")
            welcomeView.setPasswordError(interactor.getResourceString(R.string.password_empty))
            welcomeView.showToast(interactor.getResourceString(R.string.enter_password))
            return false
        }
        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            logger.info("[Email] is invalid, displaying toast to the user...")
            welcomeView.setEmailError(interactor.getResourceString(R.string.invalid_email_format))
            welcomeView.showToast(interactor.getResourceString(R.string.invalid_email_format))
            return false
        }
        if (!isLogin && password.length < 8) {
            logger.info("[Password] is small, displaying toast to the user...")
            welcomeView.setPasswordError(interactor.getResourceString(R.string.small_password))
            welcomeView.showToast(interactor.getResourceString(R.string.small_password))
            return false
        }
        // Sign up and claim account password minimum strength enforce.
        if (!isLogin && !evaluatePassword(password)) {
            logger.info("[Password] is weak, displaying toast to the user...")
            welcomeView.setPasswordError(interactor.getResourceString(R.string.weak_password))
            welcomeView.showToast(interactor.getResourceString(R.string.weak_password))
            return false
        }
        return true
    }

    private fun validateUsernameCharacters(username: String): Boolean {
        return username.matches(Regex("[a-zA-Z0-9_-]*"))
    }
}