package com.waz.zclient.settings.account

import androidx.lifecycle.*
import com.waz.zclient.core.exception.Failure
import com.waz.zclient.core.exception.HttpError
import com.waz.zclient.user.domain.model.User
import com.waz.zclient.user.domain.usecase.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class ProfileDetail(val value: String) : ProfileDetailsState()
object ProfileDetailEmpty : ProfileDetailsState()

sealed class ProfileDetailsState

@ExperimentalCoroutinesApi
class SettingsAccountViewModel constructor(private val getUserProfileUseCase: GetUserProfileUseCase,
                                           private val changeNameUseCase: ChangeNameUseCase,
                                           private val changePhoneUseCase: ChangePhoneUseCase,
                                           private val changeEmailUseCase: ChangeEmailUseCase,
                                           private val changeHandleUseCase: ChangeHandleUseCase)
    : ViewModel() {

    private val mutableProfileData = MutableLiveData<User>()
    private val mutableError = MutableLiveData<String>()

    val name: LiveData<String> = Transformations.map(mutableProfileData) {
        it.name
    }

    val handle: LiveData<String> = Transformations.map(mutableProfileData) {
        it.handle
    }

    val email: LiveData<ProfileDetailsState> = Transformations.map(mutableProfileData) {
        if (it.email.isNullOrEmpty()) ProfileDetailEmpty else ProfileDetail(it.email)
    }

    val phone: LiveData<ProfileDetailsState> = Transformations.map(mutableProfileData) {
        if (it.phone.isNullOrEmpty()) ProfileDetailEmpty else ProfileDetail(it.phone)
    }

    val error: LiveData<String>
        get() = mutableError

    fun loadProfileDetails() {
        getUserProfileUseCase(viewModelScope, Unit) {
            it.fold(::handleError, ::handleProfileSuccess)
        }
    }

    fun updateName(name: String) {
        changeNameUseCase(viewModelScope, ChangeNameParams(name)) {
            it.fold(::handleError) {}
        }
    }

    fun updatePhone(phoneNumber: String) {
        changePhoneUseCase(viewModelScope, ChangePhoneParams(phoneNumber)) {
            it.fold(::handleError) {}
        }
    }

    fun updateHandle(handle: String) {
        changeHandleUseCase(viewModelScope, ChangeHandleParams(handle)) {
            it.fold(::handleError) {}
        }
    }

    fun updateEmail(email: String) {
        changeEmailUseCase(viewModelScope, ChangeEmailParams(email)) {
            it.fold(::handleError) {}
        }
    }

    private fun handleProfileSuccess(user: User) {
        mutableProfileData.postValue(user)
    }

    private fun handleError(failure: Failure) {
        if (failure is HttpError) {
            mutableError.postValue("${failure.errorCode} + ${failure.errorMessage}")
        } else {
            mutableError.postValue("Misc error scenario")
        }
    }
}


