; ermineJ Windows Installer
; Written by Will Braynen
; Copyright (c) 2004  Columbia University


;--------------------------------

; The name of the installer
Name "ermineJ"

; The file to write
OutFile "ermineJ-setup.exe"

; The default installation directory
InstallDir "$PROGRAMFILES\ermineJ"

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKEY_LOCAL_MACHINE "Software\ermineJ" "Install_Dir"

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

; The stuff to install
Section "ermineJ (required)"

  SectionIn RO
  
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR

  ; Put file there

  ; License Agreements
  File "license.txt"
  File "GNU_General_Public_License.txt"

  ; Jars (ours and third-party)
  SetOutPath "$INSTDIR\lib"
  File "lib\baseCode-0.2.jar"
  File "lib\colt.jar"
  File "lib\commons-logging.jar"
  File "lib\ermineJ-2.0b2.jar"
  File "lib\ermineJ-help.jar"
  File "lib\jhbasic.jar"
  File "lib\mysql-connector-java-3.0.14-production-bin.jar"
  File "lib\xercesImpl-2.6.2.jar"

  ; .bat file
  SetOutPath "$INSTDIR\bin"
  File "bin\ermineJ.bat"

  ; images
  SetOutPath "$INSTDIR\bin"
  File "bin\ermineJ.ico"

  ; If upgrading, might not want to overwrite the old data folder
  IfFileExists "$INSTDIR\ermineJ.data" 0 YesOverwrite

    MessageBox MB_YESNO|MB_ICONQUESTION|MB_DEFBUTTON2 "You already have an ermineJ data folder. Would you like to overwrite it?" IDNO NoOverwrite

    YesOverwrite:
    SetOutPath "$INSTDIR\ermineJ.data"
    File "ermineJ.data\go_200406-termdb.xml"
    File "ermineJ.data\HG-U133A.an.txt"
    File "ermineJ.data\RG-U34A.an.txt"
    File "ermineJ.data\RN-U34.an.txt"
    CreateDirectory "$INSTDIR\ermineJ.data\genesets"

  NoOverwrite:
  
  ; Write the installation path into the registry
  WriteRegStr HKEY_LOCAL_MACHINE "SOFTWARE\ermineJ" "Install_Dir" "$INSTDIR"
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\ermineJ" "DisplayName" "ermineJ (remove only)"
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\ermineJ" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\ermineJ" "NoModify" 1
  WriteRegDWORD HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\ermineJ" "NoRepair" 1
  WriteUninstaller "uninstall.exe"

SectionEnd

; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"

  SetOutPath $INSTDIR\bin  ; the working directory should be \bin
  CreateShortCut "$DESKTOP\emrineJ.lnk" "$INSTDIR\bin\ermineJ.bat" "" "$INSTDIR\bin\ermineJ.ico" 0 SW_SHOWMINIMIZED CONTROL|SHIFT|J
  CreateDirectory "$SMPROGRAMS\emrineJ"
  CreateShortCut "$SMPROGRAMS\emrineJ\emrineJ.lnk" "$INSTDIR\bin\ermineJ.bat" "" "$INSTDIR\bin\ermineJ.ico" 0 SW_SHOWMINIMIZED CONTROL|SHIFT|J

  SetOutPath $INSTDIR  ; reset the working directory
  CreateShortCut "$SMPROGRAMS\emrineJ\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\emrineJ\license.txt" "$INSTDIR\license.txt"

SectionEnd


;--------------------------------

; Uninstaller

Section "Uninstall"

  ; Remove registry keys
  DeleteRegKey HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\ermineJ"
  DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\ermineJ"

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\emrineJ\*.*"
  Delete "$DESKTOP\emrineJ.lnk"

  ; Remove files and uninstaller
  Delete "$INSTDIR\*.*"

  ; Remove other directories used
  RMDir /r "$INSTDIR\bin"
  RMDir /r "$INSTDIR\lib"
  RMDir /r "$INSTDIR\jre" ; jre wasn't distributed with this version, but just in case it's left over from another
 
  ; Remove data folder
  IfFileExists "$INSTDIR\ermineJ.data" 0 YesRemoveDataFolder
  MessageBox MB_YESNO|MB_ICONQUESTION|MB_DEFBUTTON2 "Do you want to remove your ermineJ data folder now too?" IDNO NoRemoveDataFolder
  YesRemoveDataFolder:
    RMDir /r "$INSTDIR"  ; the data folder is a subdirectory of INSTDIR
  NoRemoveDataFolder:
  RMDir "$SMPROGRAMS\emrineJ"

SectionEnd