#define AppName "DroidCast Windows Sender"
#define AppVersion "0.1.0"
#define AppPublisher "DroidCast"
#define AppExeName "DroidCastSender.exe"

[Setup]
AppId={{E6E10D5D-5FE4-4A2B-9D82-7E8B7AFA6F23}
AppName={#AppName}
AppVersion={#AppVersion}
AppPublisher={#AppPublisher}
DefaultDirName={pf}\DroidCastWindowsSender
DefaultGroupName={#AppName}
OutputDir=output
OutputBaseFilename=droidcast_sender_setup
Compression=lzma
SolidCompression=yes
SetupIconFile=..\assets\app.ico

[Files]
Source: "..\build\exe.win-amd64-3.13\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\{#AppName}"; Filename: "{app}\{#AppExeName}"
Name: "{commondesktop}\{#AppName}"; Filename: "{app}\{#AppExeName}"

[Run]
Filename: "{app}\{#AppExeName}"; Description: "Launch {#AppName}"; Flags: nowait postinstall skipifsilent
