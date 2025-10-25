# Vault 3 (Desktop)

Vault 3 stores your notes and photographs as an outline. Vault 3 organizes your information into 
categories and sub-categories that you specify. Vault 3's outline is easily and rapidly searchable. 
Vault 3 uses strong encryption to ensure the privacy of your personal data. Vault 3 can even run 
slideshows of your photographs. Vault 3 runs on Windows (Intel/AMD 64-bit) and 
Linux (Intel/AMD 64-bit, ARM 64-bit).

# Screenshots

![`Vault 3 Screenshot`](https://ericbt.com/images/Vault3/vault3.png "Vault 3 Screenshot")

![`Vault 3 Screenshot`](https://ericbt.com/images/Vault3/photos.jpg "Vault 3 Screenshot")

# Copyright

Vault 3 &#169; Copyright 2025, [`Eric Bergman-Terrell`](https://www.ericbt.com)

# Links

* [`website`](https://ericbt.com/vault3)
* [`GitHub repo (Desktop)`](https://github.com/EricTerrell/Vault3.Desktop)
* [`GitHub repo (Android)`](https://github.com/EricTerrell/Vault3.Android)

# Android Version

A version of [`Vault 3 for Android`](https://play.google.com/store/apps/details?id=com.ericbt.Vault3Paid) is also available. All versions of Vault 3 (Android, Windows, and Linux) use 
the same file format. Any .vl3 file can be read and updated by any version.

# How to Build

## Development Build

Vault 3 relies on many of the same libraries used by the [`Eclipse`](https://www.eclipse.org/) project.

1. Make sure you have a Java Development Kit (JDK) with a version of **17 or later**. I am using version 24 at the moment. You can download a JDK [`here`](https://www.oracle.com/java/technologies/downloads/).
2. Clone the [`Vault3.CommonCode`](https://github.com/EricTerrell/Vault3.CommonCode) repo.
3. Install Vault3.CommonCode into your local Maven repository (mvn install)
4. Clone this repo.
5. Run. Use a command like: `mvn clean compile exec:java -Dexec.mainClass=mainPackage.MainApplicationWindow`.

## Deployment Build

If you intend to distribute Vault 3 to other users, use the scripts in the "Vault/ant build scripts" folder. 
These scripts will create the folders that you can distribute.

# Encryption

Password-protected Vault 3 documents are encrypted with the AES (Rijndael) algorithm in CBC mode, using 256-bit keys.

# License

[`GPL3`](https://www.gnu.org/licenses/gpl-3.0.en.html)

# Feedback

Please submit your feedback to [Vault3@EricBT.com](mailto:Vault3@EricBT.com).