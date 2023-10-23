# Vault 3 (Desktop)

Vault 3 stores your notes and photographs as an outline. Vault 3 organizes your information into categories and sub-categories that you specify. Vault 3's outline is easily and rapidly searchable. Vault 3 uses strong encryption to ensure the privacy of your personal data. Vault 3 can even run slideshows of your photographs.

# Screenshots

![`Vault 3 Screenshot`](https://ericbt.com/images/Vault3/vault3.png "Vault 3 Screenshot")

![`Vault 3 Screenshot`](https://ericbt.com/images/Vault3/photos.jpg "Vault 3 Screenshot")

# Copyright

Vault 3 &#169; Copyright 2023, [`Eric Bergman-Terrell`](https://www.ericbt.com)

# Links

* [`website`](https://ericbt.com/vault3)
* [`GitHub repo (Desktop)`](https://github.com/EricTerrell/Vault3.Desktop)
* [`GitHub repo (Android)`](https://github.com/EricTerrell/Vault3.Android)

# Android Version

A version of [`Vault 3 for Android`](https://play.google.com/store/apps/details?id=com.ericbt.Vault3Paid) is also available. All versions of Vault 3 (Android, Windows, Linux, OSX) use the same file format. Any .vl3 file can be read and updated by any version.

# How to Build

## Development Build

Vault 3 relies on many of the same libraries used by the [`Eclipse`](https://www.eclipse.org/) project. It is currently using version 4.29 of the libraries.

1. These instructions assume you're using [`Intellij IDEA`](https://www.jetbrains.com/idea/). The procedure for other development tools will be similar to the following.
2. Make sure you have a Java Development Kit (JDK) with a version of **17 or later**. You can download a JDK [`here`](https://www.oracle.com/java/technologies/downloads/).
3. Download the latest version of the [`Eclipse libraries`](https://download.eclipse.org/eclipse/downloads/).
4. Download additional libraries:
    1. [`JavaMail`](https://www.oracle.com/java/technologies/javamail-api.html)
    1. [`sqlite-jdbc`](https://github.com/xerial/sqlite-jdbc)
    1. [`itextpdf`](https://github.com/itext/itextpdf/releases)
    1. [`perf4j`](https://mvnrepository.com/artifact/org.perf4j/perf4j)
    1. [`imgscalr`](https://github.com/downloads/thebuzzmedia/imgscalr/imgscalr-lib-4.2.zip)
5. Unpack the library files completely. If you're on Windows, [`7-Zip`](https://www.7-zip.org/) is ideal for this purpose. 
6. Update the project root level .classpath to point to the libraries you downloaded.
7. Update the ant build scripts to point to the libraries.   
8. Update the .classpath file to point to the Eclipse libraries on your machine. Make sure each path is correct. Note: you may have to change version numbers of the .jar files.
9. Run!

## Deployment Build

If you intend to distribute Vault 3 to other users, use the scripts in the "Vault/ant build scripts" folder. 
These scripts will create the folders that you can distribute.

1. Download the libraries mentioned in the Development Build section, above.
2. Update the ant build scripts to point to the downloaded libraries
3. Run the appropriate ant build scripts
4. The scripts create the deployment folders, and automatically compress them as .zip files.
5. Distribute the deployment .zip files.

# Encryption

Password-protected Vault 3 documents are encrypted with the AES (Rijndael) algorithm in CBC mode, using 256-bit keys.

# License

[`GPL3`](https://www.gnu.org/licenses/gpl-3.0.en.html)

# Feedback

Please submit your feedback to [Vault3@EricBT.com](mailto:Vault3@EricBT.com).