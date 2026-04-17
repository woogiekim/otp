# otp

TOTP(RFC 6238)와 HOTP(RFC 4226)를 직접 구현한 Kotlin OTP 라이브러리입니다.
QR 코드 생성을 포함하며, 추가 인증 없이 JitPack을 통해 Maven/Gradle 프로젝트에서 사용할 수 있습니다.

## 설치

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

## 사용법

### TOTP (시간 기반, RFC 6238)

30초마다 갱신되는 6자리 OTP입니다. Google Authenticator 등 대부분의 OTP 앱과 호환됩니다.

```kotlin
// OTP 생성
val totp = TOtp(issuer = "MyApp", account = "user@example.com")

// QR 코드 등록용 URL (otpauth://totp/...)
val url = totp.generateUrl()

// 현재 시간 기반 6자리 OTP 생성
val otp = totp.generateOtp()

// OTP 검증 (클럭 스큐 대응을 위해 ±30초 윈도우 허용)
val checker = TOtpChecker(totp)
val isValid = checker.check(otp) // true

// QR 코드 생성 (BitMatrix → 이미지 변환은 사용처에서 처리)
val barcodeGenerator = TOtpBarcodeGenerator(totp)
val bitMatrix = barcodeGenerator.generate(width = 300, height = 300)
```

### HOTP (카운터 기반, RFC 4226)

OTP를 생성할 때마다 카운터가 증가하는 방식입니다.
look-ahead, 카운터 동기화 등의 정책은 사용처에서 직접 구현합니다.

```kotlin
// OTP 생성 (counter=0 에서 시작)
val hotp = HOtp(issuer = "MyApp", account = "user@example.com")

// QR 코드 등록용 URL (otpauth://hotp/...?counter=0)
val url = hotp.generateUrl()

// 현재 카운터로 OTP 생성 후 카운터 자동 증가
val otp = hotp.generateOtp() // counter: 0 → 1

// OTP 검증 (hotp.counter 기준으로 단일 값 검증)
val checker = HOtpChecker(hotp)
val isValid = checker.check(otp)

// QR 코드 생성
val barcodeGenerator = HOtpBarcodeGenerator(hotp)
val bitMatrix = barcodeGenerator.generate(width = 300, height = 300)
```

#### HOTP look-ahead 구현 예시

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

`SecretKeyGenerator` 인터페이스를 구현하면 키 생성 방식을 교체할 수 있습니다.

```kotlin
class CustomSecretKeyGenerator : SecretKeyGenerator {
    override fun generate(): ByteArray {
        // 커스텀 키 생성 로직
    }
}

val totp = TOtp("MyApp", "user@example.com", CustomSecretKeyGenerator())
```

## 인터페이스

| 인터페이스 | 설명 |
|-----------|------|
| `Otp` | `generateUrl()`, `generateOtp()` |
| `OtpChecker` | `check(otp: String): Boolean` |
| `OtpBarcodeGenerator` | `generate(format, width, height): BitMatrix` |
| `SecretKeyGenerator` | `generate(): ByteArray` |

## 구현체

| 클래스 | 설명 |
|--------|------|
| `TOtp` | TOTP 생성 (RFC 6238, HMAC-SHA1, 30초 윈도우) |
| `TOtpChecker` | TOTP 검증 (±1 시간 스텝 허용) |
| `TOtpBarcodeGenerator` | TOTP QR 코드 생성 |
| `HOtp` | HOTP 생성 (RFC 4226, HMAC-SHA1, 카운터 기반) |
| `HOtpChecker` | HOTP 검증 (현재 카운터 단일 검증) |
| `HOtpBarcodeGenerator` | HOTP QR 코드 생성 |
| `DefaultSecretKeyGenerator` | `SecureRandom` 기반 20바이트 키 생성 |

## 의존성

- `commons-codec` — Base32 인코딩
- `com.google.zxing` — QR 코드 생성
