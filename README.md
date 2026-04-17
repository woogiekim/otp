# otp
> 외부 OTP 라이브러리 없이 RFC 표준을 직접 구현한 Kotlin OTP 라이브러리

[![](https://jitpack.io/v/woogiekim/otp.svg)](https://jitpack.io/#woogiekim/otp)

## 📌 목차
- [프로젝트 소개](#-프로젝트-소개)
- [핵심 기능](#-핵심-기능)
- [설치 방법](#-설치-방법)
- [사용법](#-사용법)
- [라이선스](#-라이선스)

---

## 💡 프로젝트 소개

- **문제 인식:** OTP 인증 구현 시 외부 라이브러리에 의존하면 내부 동작을 이해하기 어렵고, 라이브러리 변경에 취약합니다.
- **해결 방안:** TOTP(RFC 6238)와 HOTP(RFC 4226)를 `javax.crypto.Mac`으로 직접 구현하여 외부 OTP 라이브러리 의존성을 제거했습니다.
- **핵심 목적:** OTP 생성·검증·QR 코드를 하나의 라이브러리로 제공하며, `SecretKeyGenerator` 인터페이스를 통해 키 생성 전략을 교체할 수 있습니다.

## ✨ 핵심 기능

- **TOTP (RFC 6238):** 30초마다 갱신되는 시간 기반 OTP. Google Authenticator 등 표준 OTP 앱과 호환
- **HOTP (RFC 4226):** 카운터 기반 OTP. look-ahead 등 동기화 정책은 사용처에서 직접 구현
- **QR 코드 생성:** `otpauth://` URL을 QR 코드(BitMatrix)로 변환
- **키 생성 커스터마이징:** `SecretKeyGenerator` 인터페이스로 키 생성 방식 교체 가능
- **무인증 배포:** JitPack을 통해 토큰 없이 Maven/Gradle에서 바로 사용 가능

## 🛠️ 설치 방법

### Gradle (build.gradle.kts)

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.woogiekim:otp:v0.0.8")
}
```

### Maven (pom.xml)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.woogiekim</groupId>
    <artifactId>otp</artifactId>
    <version>v0.0.8</version>
</dependency>
```

## 📖 사용법

### TOTP (시간 기반, RFC 6238)

```kotlin
// 생성
val totp = TOtp(issuer = "MyApp", account = "user@example.com")

// QR 코드 등록용 URL (otpauth://totp/...)
val url = totp.generateUrl()

// 현재 시간 기반 6자리 OTP 생성
val otp = totp.generateOtp()

// 검증 (클럭 스큐 대응을 위해 ±30초 윈도우 허용)
val isValid = TOtpChecker(totp).check(otp) // true

// QR 코드 생성 (BitMatrix → 이미지 변환은 사용처에서 처리)
val bitMatrix = TOtpBarcodeGenerator(totp).generate(width = 300, height = 300)
```

### HOTP (카운터 기반, RFC 4226)

```kotlin
// 생성 (counter=0 에서 시작)
val hotp = HOtp(issuer = "MyApp", account = "user@example.com")

// QR 코드 등록용 URL (otpauth://hotp/...?counter=0)
val url = hotp.generateUrl()

// OTP 생성 후 카운터 자동 증가 (0 → 1)
val otp = hotp.generateOtp()

// 검증 (hotp.counter 기준 단일 값 검증)
val isValid = HOtpChecker(hotp).check(otp)

// QR 코드 생성
val bitMatrix = HOtpBarcodeGenerator(hotp).generate(width = 300, height = 300)
```

#### look-ahead 구현 예시

카운터 동기화 오차 허용이 필요한 경우 사용처에서 직접 구현합니다.

```kotlin
fun verifyWithLookAhead(hotp: HOtp, otp: String, window: Int = 5): Boolean {
    for (i in 0 until window) {
        if (hotp.computeHotp(hotp.counter + i) == otp) {
            hotp.counter += i + 1
            return true
        }
    }
    return false
}
```

### 시크릿 키 커스터마이징

```kotlin
class CustomSecretKeyGenerator : SecretKeyGenerator {
    override fun generate(): ByteArray {
        // 커스텀 키 생성 로직
    }
}

val totp = TOtp("MyApp", "user@example.com", CustomSecretKeyGenerator())
```

### 인터페이스 및 구현체

| 인터페이스 | 구현체 | 설명 |
|-----------|--------|------|
| `Otp` | `TOtp`, `HOtp` | OTP URL 및 코드 생성 |
| `OtpChecker` | `TOtpChecker`, `HOtpChecker` | OTP 검증 |
| `OtpBarcodeGenerator` | `TOtpBarcodeGenerator`, `HOtpBarcodeGenerator` | QR 코드 생성 |
| `SecretKeyGenerator` | `DefaultSecretKeyGenerator` | 시크릿 키 생성 |

## 📄 라이선스

This project is licensed under the MIT License.
