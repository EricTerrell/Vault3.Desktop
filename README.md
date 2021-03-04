# Vault 3 (Desktop)

Vault 3 stores your notes and photographs as an outline. Vault 3 organizes your information into categories and sub-categories that you specify. Vault 3's outline is easily and rapidly searchable. Vault 3 uses strong encryption to ensure the privacy of your personal data. Vault 3 can even run slideshows of your photographs.

# Screenshots

![`EBTCalc Screenshot`](https://ericbt.com/images/Vault3/vault3.png "Vault 3 Screenshot")

![`EBTCalc Screenshot`](https://ericbt.com/images/Vault3/photos.jpg "Vault 3 Screenshot")

# Copyright

Vault 3 &#169; Copyright 2021, [`Eric Bergman-Terrell`](https://www.ericbt.com)

# Links

* [`website`](https://ericbt.com/vault3)
* [`github repo`](https://github.com/EricTerrell/Vault3.Desktop)

# Android Version

A version of [`Vault 3 for Android`](https://play.google.com/store/apps/details?id=com.ericbt.Vault3Paid) is also available.

# How to Build

## Development Build

Vault 3 relies on many of the same libraries used by the [`Eclipse`](https://www.eclipse.org/) project. It is currently using version 4.18 of the libraries.

1. These instructions assume you're using [`Intellij IDEA`](https://www.jetbrains.com/idea/). The procedure for other development tools will be similar to the following.
1. If you're developing on Linux, copy the .classpath file from Java Vault/Vault/classpaths/linux_gtk_64 to the .classpath at the project's root level
1. Download the latest version of the [`Eclipse libraries`](https://download.eclipse.org/eclipse/downloads/).
1. Download additional libraries:
    1. [`JavaMail`](https://www.oracle.com/java/technologies/javamail-api.html)
    1. [`sqlite4java`](https://bitbucket.org/almworks/sqlite4java/downloads/)
    1. [`itextpdf`](https://github.com/itext/itextpdf/releases)
    1. [`perf4j`](https://mvnrepository.com/artifact/org.perf4j/perf4j)
1. Unpack the library files completely. If you're on Windows, [`7-Zip`](https://www.7-zip.org/) is ideal for this purpose. 
1. Store the library files in a single folder. For example, C:\Eclipse\4.18.
1. Update the project root level .classpath to point to the libraries you downloaded.
1. Run!

## Deployment Build

If you intend to distribute Vault 3 to other users, run the scripts in the "Vault/ant build scripts" folder. 
These scripts will create the operating-system specific folders that you can distribute.

# License

[`GPL3`](https://www.gnu.org/licenses/gpl-3.0.en.html)

# Feedback

Please submit your feedback to [Vault3@EricBT.com](mailto:Vault3@EricBT.com).