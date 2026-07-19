# VaultPass 🛡️

**VaultPass** is a modern, secure and intuitive password manager for Android. It allows you to generate, store and synchronize your identifiers in complete security.

![Build Status](https://github.com/DilanneVadex/ByPassPublic/actions/workflows/android.yml/badge.svg )
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/license-MIT-blue.svg )](LICENSE)

## ✨ Features

- **Robust Generator**: Create complex passwords with customizable options (length, symbols, capital letters).
- **Strength Analysis**: Real-time evaluation of the robustness of your passwords via the `nbvcxz` algorithm.
- **Encrypted Local Safe**: Your data is stored locally with military-level encryption via 'Jetpack Security'.
- **Cloud Synchronization**: Backup and restore your data on all your devices thanks to Firebase Firestore.
- **Secure Authentication**: Support for Google Sign-In and email authentication.
- **Modern Interface**: Design based on Material Components for a fluid user experience.

## 🛡️ Security

Safety is at the heart of ByPass :
- **AES-256 Encryption**: Use of 'EncryptedSharedPreferences` and 'Room' with SQLCipher (via Jetpack Security).
- **Zero Log**: We never store your master password in the clear.
- **Strict validation**: Security linting activated to prevent data leaks and bad storage practices.

## 🚀 Installation

### Prerequisites
- Android 11.0+ (API 30+)
- Android Studio Ladybug (or newer)

### Clone the project
bash git clone https://github.com/DilanneVadex/ByPassPublic.git

### Firebase Configuration
1. Create a project on the [Firebase Console](https://console.firebase.google.com /).
2. Add an Android application with the package 'com.dilanne.bypass`.
3. Download the file 'google-services.json` and place it in the `app/' folder.

## 🛠️ Technical Stack

- **Language** : Java 
- **Architecture**: MVVM (ViewModel, LiveData)
- **Database** : Room Persistence Library
- **Network** : Retrofit & OkHttp
- **Security** : 'androidx.security:security-crypto`, 'nbvcxz`
- **Backend** : Firebase Auth & Firestore
- **CI/CD** : GitHub Actions

## 📦 Release

The signed APKs are available in the [Releases] section(https://github.com/DilanneVadex/ByPassPublic/releases ).

## 📄 License

This project is licensed under the MIT license. See the [LICENSE](LICENSE) file for more details.

---
Developed by [Dilanne]
