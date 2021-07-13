
# <center>SelfUser extends [User]()</center>

> Kotlin
```kotlin
val bot = JDABuilder.createDefault("TOKEN").build()
val user = bot.selfUser
```
> Java
```java
JDA bot = JDABuilder.createDefault("TOKEN").build();
SelfUser user = bot.getSelfUser();
```
## Methods
> isVerified()

**Boolean**
A function for this interface that returns a `boolean` if this client's account is verified.
If `true` you have verified your email. If `false` you have not yet accepted the verification email
> isMfaEnabled()

**Boolean**
A function for this interface. If this client's account has Multi-Factor authorization [See Here](https://support.discord.com/hc/en-us/articles/219576828-Setting-up-Two-Factor-Authentication).
<br>
If `true` then this account has MFA this account is protected by MFA. If `false` this means MFA is not enabled.<br>
***Additionally*** if the current logged in client for the JDA instance is a bot this then returns the person that created this bot MFA status.
> getEmail()

**Deprecation Warning (Since 4.2.0, Client Accounts are no longer supported!)**<br>
**For Removal**<br>
**String** A function for this interface. returns a **string** of a `email` for the logged in account of your JDA instance.<br>
***Additionally***  The currently logged in account for your JDA instance must be the type of `AccountType.CLIENT`
> isMobile()

**Deprecation Warning (Since 4.2.0, Client Accounts are no longer supported!)**<br>
**For Removal**<br>
**Boolean** A function for this interface. If this client has ever been accessed on the mobile app
If `true` this account has been linked with a mobile app. If `false` this account has not been accessed from the mobile app.<br>
***Additionally***  The currently logged in account for your JDA instance must be the type of `AccountType.CLIENT`
> isNitro()

**Deprecation Warning (Since 4.2.0, Client Accounts are no longer supported!)**<br>
**For Removal**

A function for this interface that throws an [`UnsupportedOperationException`]()
> getPhoneNumber()

**Deprecation Warning (Since 4.2.0, Client Accounts are no longer supported!)**<br>
**For Removal**

A function for this interface that throws an [`UnsupportedOperationException`]()

> getAllowedFileSize()

**Long** A function for this interface. Returns the maximum allowed file size (`8388608` bytes, around `8`mb)

> getManager()

**[AccountManager]()** A function for this interface. Returns the [`AccountManager`]() for the currently logged in session of the JDA instance 

# <center>See [`User`]() for additional methods</center>


