; ermineJ Windows Installer
; Written by Will Braynen
; Copyright (c) 2004  Columbia University


;--------------------------------

; The name of the installer
Name "ermineJ"

; The file to write
OutFile "..\..\target\ermineJ-setup-jre.exe"

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
  File "..\..\target\bindist\lib\baseCode.jar"
  File "..\..\target\bindist\lib\colt.jar"
  File "..\..\target\bindist\lib\commons-logging.jar"
  File "..\..\target\bindist\lib\ermineJ.jar"
  File "..\..\target\bindist\lib\ermineJ-help.jar"
  File "..\..\target\bindist\lib\jhelp.jar"
  File "..\..\target\bindist\lib\xercesImpl.jar"

 
  
  ; .bat file
  SetOutPath "$INSTDIR\bin"
  File "..\..\target\bindist\bin\ermineJ-jre.bat"

  ; images
  SetOutPath "$INSTDIR\bin"
  File "..\..\target\bindist\bin\ermineJ.ico"

  ; JRE (Java Runtime Environment)
  !echo  $%JAVA_HOME%
  SetOutPath "$INSTDIR"
  File /r "$%JAVA_HOME%\jre"

  ; If upgrading, might not want to overwrite the old data folder
  IfFileExists "$INSTDIR\ermineJ.data" 0 YesOverwrite

    MessageBox MB_YESNO|MB_ICONQUESTION|MB_DEFBUTTON2 "You already have an ermineJ data folder. Would you like to overwrite it?" IDNO NoOverwrite

    YesOverwrite:
    SetOutPath "$INSTDIR\ermineJ.data"
    File "..\..\target\bindist\data\go_200406-termdb.xml"
  ;  File "..\..\target\bindist\data\HG-U133A.an.txt"
    File "..\..\target\bindist\data\HG-U95A.an.txt"
  ;  File "..\..\target\bindist\data\RG-U34A.an.txt"
  ;  File "..\..\target\bindist\data\RN-U34.an.txt"
    CreateDirectory "$INSTDIR\ermineJ.data\genesets"

  NoOverwrite:
  
  ; Write the installation path into the registry
  WriteRegStr HKEY_LOCAL_MACHINE SOFTWARE\ermineJ "Install_Dir" "$INSTDIR"
  
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
  CreateShortCut "$DESKTOP\emrineJ.lnk" "$INSTDIR\bin\ermineJ-jre.bat" "" "$INSTDIR\bin\ermineJ.ico" 0 SW_SHOWMINIMIZED CONTROL|SHIFT|J
  CreateDirectory "$SMPROGRAMS\emrineJ"
  CreateShortCut "$SMPROGRAMS\emrineJ\emrineJ.lnk" "$INSTDIR\bin\ermineJ-jre.bat" "" "$INSTDIR\bin\ermineJ.ico" 0 SW_SHOWMINIMIZED CONTROL|SHIFT|J

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
  RMDir /r "$INSTDIR\jre"

  ; Remove data folder
  IfFileExists "$INSTDIR\ermineJ.data" 0 YesRemoveDataFolder
  MessageBox MB_YESNO|MB_ICONQUESTION|MB_DEFBUTTON2 "Do you want to remove your ermineJ data folder now too?" IDNO NoRemoveDataFolder
  YesRemoveDataFolder:
    RMDir /r "$INSTDIR"  ; the data folder is a subdirectory of INSTDIR
  NoRemoveDataFolder:
  RMDir "$SMPROGRAMS\emrineJ"

SectionEnd