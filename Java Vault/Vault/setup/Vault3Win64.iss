; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

#define MyAppName "Vault 3 for 64-bit Windows (x86)"
#define MyAppVersion "0.81"
#define MyAppPublisher "Eric Bergman-Terrell"
#define MyAppURL "https://www.ericbt.com/vault3"

[Setup]
; NOTE: The value of AppId uniquely identifies this application. Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{47D95F46-607D-4C8A-ACE0-31FF74AD13E6}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
;AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={commonpf64}\Vault 3
DefaultGroupName=Vault 3
AllowNoIcons=yes
; Uncomment the following line to run in non administrative install mode (install for current user only.)
;PrivilegesRequired=lowest
OutputBaseFilename=Vault 3 Setup
Compression=lzma
SolidCompression=yes
WizardStyle=modern
ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "C:\Temp\Vault3\Vault3Win64\custom-runtime\*"; DestDir: "{app}\custom-runtime"; Flags: ignoreversion recursesubdirs
Source: "C:\Temp\Vault3\Vault3Win64\Help\*"; DestDir: "{app}\Help"; Flags: ignoreversion
Source: "C:\Temp\Vault3\Vault3Win64\example.vl3"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\Temp\Vault3\Vault3Win64\LicenseTerms.txt"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\Temp\Vault3\Vault3Win64\vault_win.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\Temp\Vault3\Vault3Win64\vault3.ico"; DestDir: "{app}"; Flags: ignoreversion
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{group}\Vault 3"; WorkingDir: "{app}"; Filename:"{app}\custom-runtime\bin\javaw.exe"; Parameters:"-DConfigPath=""%AppData%\Vault 3"" -jar ""{app}\vault_win.jar"""; IconFilename:"{app}\vault3.ico";
Name: "{commondesktop}\Vault 3"; WorkingDir: "{app}"; Filename:"{app}\custom-runtime\bin\javaw.exe"; Parameters:"-DConfigPath=""%AppData%\Vault 3"" -jar ""{app}\vault_win.jar"""; IconFilename:"{app}\vault3.ico";

[Tasks]
Name: StartAfterInstall; Description: Run Vault 3 after install

[Run]
Filename:"{app}\custom-runtime\bin\javaw.exe"; Parameters:"-DConfigPath=""{userappdata}\Vault 3"" -jar ""{app}\vault_win.jar"""; Flags: nowait skipifsilent; Tasks: StartAfterInstall;
