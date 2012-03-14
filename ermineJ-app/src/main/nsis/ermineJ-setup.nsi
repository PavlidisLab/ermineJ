; ermineJ Windows Installer
; Written by Will Braynen, Paul Pavlidis, Kiran Keshav
; Copyright (c) 2008-2012  University of British Columbia


;--------------------------------

; The name of the installer
Name "ermineJ"

!include ..\..\..\target\project.nsh

; The file to write
OutFile "..\..\..\target\ermineJ-${PROJECT_VERSION}-setup.exe"

; The default installation directory
InstallDir "$PROGRAMFILES\ermineJ"

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKEY_LOCAL_MACHINE "Software\ermineJ" "Install_Dir"

LicenseText "You must agree to this license before installing."
LicenseData "LICENSE.txt"

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
  File "LICENSE.txt" 

  ; Jars (ours and third-party)
  SetOutPath "$INSTDIR\lib"
 ; !include includes.deps.nsi

  ; .bat file
  SetOutPath "$INSTDIR\bin"
  File "${PROJECT_BASEDIR}\src\main\nsis\bin\ermineJ.bat"

  ; images
  SetOutPath "$INSTDIR\bin"
  File "${PROJECT_BASEDIR}\src\main\nsis\bin\ermineJ.ico"

  ; If upgrading, might not want to overwrite the old data folder
  IfFileExists "$PROFILE\ermineJ.data" 0 YesOverwrite

    MessageBox MB_YESNO|MB_ICONQUESTION|MB_DEFBUTTON2 "You already have an ermineJ data folder. Would you like to overwrite it?" IDNO NoOverwrite

    YesOverwrite:
	SetOutPath "$PROFILE\ermineJ.data"
    !include includes.data.nsi
    CreateDirectory "$PROFILE\ermineJ.data\genesets"

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
  CreateShortCut "$DESKTOP\ermineJ.lnk" "$INSTDIR\bin\ermineJ.bat" "" "$INSTDIR\bin\ermineJ.ico" 0 SW_SHOWMINIMIZED CONTROL|SHIFT|J
  CreateDirectory "$SMPROGRAMS\ermineJ"
  CreateShortCut "$SMPROGRAMS\ermineJ\ermineJ.lnk" "$INSTDIR\bin\ermineJ.bat" "" "$INSTDIR\bin\ermineJ.ico" 0 SW_SHOWMINIMIZED CONTROL|SHIFT|J

  SetOutPath $INSTDIR  ; reset the working directory
  CreateShortCut "$SMPROGRAMS\ermineJ\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\ermineJ\license.txt" "$INSTDIR\license.txt"

SectionEnd


;--------------------------------

; Uninstaller

Section "Uninstall"

  ; Remove registry keys
  DeleteRegKey HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\ermineJ"
  DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\ermineJ"

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\ermineJ\*.*"
  Delete "$DESKTOP\ermineJ.lnk"

  ; Remove files and uninstaller
  Delete "$INSTDIR\*.*"

  ; Remove other directories used
  RMDir /r "$INSTDIR\bin"
  RMDir /r "$INSTDIR\lib"

  ; Remove data folder
  ;IfFileExists "$INSTDIR\ermineJ.data" 0 YesRemoveDataFolder
  IfFileExists "$PROFILE\ermineJ.data" 0 YesRemoveDataFolder
  MessageBox MB_YESNO|MB_ICONQUESTION|MB_DEFBUTTON2 "Do you want to remove your ermineJ data folder now too?" IDNO NoRemoveDataFolder
  YesRemoveDataFolder:
    RMDir /r "$PROFILE\ermineJ.data"
    Delete "$PROFILE\ermineJ.properties"
    Delete "$PROFILE\ermineJ.log"
  NoRemoveDataFolder:
  RMDir "$SMPROGRAMS\ermineJ"

SectionEnd
