; ermineJ Windows Installer
; Written by Will Braynen
; Copyright (c) 2004  Columbia University


;--------------------------------

; The name of the installer
Name "ermineJ"

; The file to write
OutFile "install.exe"

; The default installation directory
InstallDir "$PROGRAMFILES\ermineJ"

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\ermineJ" "Install_Dir"

LicenseText "You must agree to this license before installing."
LicenseData "license.txt"

;--------------------------------

; Pages

Page license
Page components
Page directory
Page instfiles

UninstPage uninstConfirm
UninstPage instfiles

;--------------------------------
; Declaration of user variables (Var command), allowed charaters for variables names : [a-z][A-Z][0-9] and '_'

Var "DataFolderOutPath"

;--------------------------------

; The stuff to install
Section "ermineJ (required)"

  SectionIn RO
  
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR

  ; Set the output path for the data folder
  StrCpy "$DataFolderOutPath" "$PROGRAMFILES\ermineJ.data"
  
  ; Put file there

  ; License Agreements
  File "license.txt"
  File "GNU_General_Public_License.txt"

  ; Jars (ours and third-party)
  File /r "lib"

  ; JRE (Java Runtime Environment)
  File /r "bin"

  ; If upgrading, might not want to overwrite the old data folder
  IfFileExists "$DataFolderOutPath" 0 YesOverwrite

    MessageBox MB_YESNO|MB_ICONQUESTION "You already have a data folder; would you like to keep it?" IDYES NoOverwrite

    YesOverwrite:
    File /r "ermineJ.data"

  NoOverwrite:
  
  ; Write the installation path into the registry
  WriteRegStr HKLM "SOFTWARE\ermineJ" "Install_Dir" "$INSTDIR"
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\ermineJ" "DisplayName" "ermineJ"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\ermineJ" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\ermineJ" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\ermineJ" "NoRepair" 1
  WriteUninstaller "uninstall.exe"

SectionEnd

; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"

  CreateShortCut "$DESKTOP\emrineJ.lnk" "$INSTDIR\ermineJ.exe"
  CreateDirectory "$SMPROGRAMS\emrineJ"
  CreateShortCut "$SMPROGRAMS\emrineJ\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\emrineJ\emrineJ.lnk" "$INSTDIR\ermineJ.exe" "" "$INSTDIR\ermineJ.exe" 0
  CreateShortCut "$SMPROGRAMS\emrineJ\license.txt" "$INSTDIR\license.txt"

SectionEnd

; Optional section (can be disabled by the user)
Section "Java 2 Runtime Environment, SE v1.4.2_05"

  Exec "bin\j2re-1_4_2_05-windows-i586-p.exe"

SectionEnd

;--------------------------------

; Uninstaller

Section "Uninstall"
  
  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\emrineJ"
  DeleteRegKey HKLM "SOFTWARE\emrineJ"

  ; Remove files and uninstaller
  Delete "$INSTDIR\*.*"

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\emrineJ\*.*"
  Delete "$DESKTOP\emrineJ.lnk"

  ; Remove directories used
  RMDir "$SMPROGRAMS\emrineJ"
  RMDir "$INSTDIR"

SectionEnd