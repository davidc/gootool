;NSIS install script for GooTool

SetCompressor /SOLID lzma

;--------------------------------
;Include Modern UI

!include "MUI.nsh"

; Include Register File Extension header
!include "installer\registerExtension.nsh"

;--------------------------------
;General

;Name and file
Name "GooTool"
OutFile "dist\GooTool-${gootool.version.full}-WindowsSetup.exe"

;Default installation folder
InstallDir "$PROGRAMFILES\GooTool"

;Get installation folder from registry if available
InstallDirRegKey HKLM "SOFTWARE\GooTool" "Install_Dir"

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING
	!define MUI_HEADERIMAGE
	!define MUI_HEADERIMAGE_BITMAP_NOSTRETCH
	!define MUI_HEADERIMAGE_BITMAP "installer\nsis-header.bmp"
	!define MUI_HEADERIMAGE_RIGHT
;	!define MUI_ICON "C:\temp\SimpleText\installer\setup.ico"
;	!define MUI_UNICON "C:\temp\SimpleText\installer\setup.ico"

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_LICENSE "dist_src\common\doc\LICENSE.txt"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES

  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES

;--------------------------------
;Languages

  !insertmacro MUI_LANGUAGE "English"

;--------------------------------

;Version
  VIProductVersion "${gootool.version}"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductName" "World of Goo Tool"
;  VIAddVersionKey /LANG=${LANG_ENGLISH} "Comments" "A test comment"
;  VIAddVersionKey /LANG=${LANG_ENGLISH} "CompanyName" "Fake company"
;  VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalTrademarks" "Test Application is a trademark of Fake company"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalCopyright" "Copyright 2008, 2009, 2010, 2019 David C. A. Croft"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "FileDescription" "GooTool Installer"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "FileVersion" "${gootool.version}"

!define JRE_VERSION "1.6"
!define JRE_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=35989"


;--------------------------------
;Installer Sections

Section "GooTool (required)" SecDummy

  Call DetectJRE

  SectionIn RO

  ;Files to be installed
  SetOutPath "$INSTDIR"

   File /r "dist\win32\*.*"
;   File "C:\temp\SimpleText\images\SimpleText.ico"
;	File "C:\temp\SimpleText\swt-win32-3138.dll"

;	SetOutPath "$INSTDIR\lib"

;	File "C:\temp\SimpleText\lib\org.eclipse.swt.win32.win32.x86_3.1.0.jar"

;  SetOutPath "$INSTDIR"

    ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\GooTool "Install_Dir" "$INSTDIR"

  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\GooTool" "DisplayName" "GooTool"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\GooTool" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\GooTool" "Publisher" "goofans.com"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\GooTool" "HelpLink" "http://goofans.com/faq/1"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\GooTool" "DisplayVersion" "${gootool.version.full}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\GooTool" "URLUpdateInfo" "http://goofans.com/gootool/download"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\GooTool" "URLInfoAbout" "http://goofans.com/gootool"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\GooTool" "DisplayIcon" '"$INSTDIR\bin\gootool.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\GooTool" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\GooTool" "NoRepair" 1

  ;SetOutPath "$INSTDIR\bin"
  WriteUninstaller "uninstall.exe"

  ${registerExtension} "$INSTDIR\bin\GooTool.exe" ".goomod" "World of Goo Addin" "$INSTDIR\lib\goomod.ico"

SectionEnd

; Optional section (can be disabled by the user)
Section "Start Menu Shortcut"
  SetShellVarContext all
  CreateShortCut "$SMPROGRAMS\GooTool.lnk" "$INSTDIR\bin\gootool.exe" "" "$INSTDIR\bin\gootool.exe"
;  CreateDirectory "$SMPROGRAMS\GooTool"
;  CreateShortCut "$SMPROGRAMS\GooTool\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe"
SectionEnd

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\GooTool"
  DeleteRegKey HKLM SOFTWARE\GooTool
;  DeleteRegKey /ifempty HKCU "Software\GooTool"


  ; Remove shortcuts
  SetShellVarContext all
;  RMDir /r "$SMPROGRAMS\GooTool"
  Delete $SMPROGRAMS\GooTool.lnk

  ; Remove directories used
  RMDir /r "$INSTDIR"

  ${unregisterExtension} ".goomod" "World of Goo Addin"

SectionEnd


Function DetectJRE
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" \
             "CurrentVersion"
  StrCmp $2 ${JRE_VERSION} done

  Call GetJRE

  done:
FunctionEnd

Function GetJRE
        MessageBox MB_OK "GooTool requires Java ${JRE_VERSION}. it will now \
                         be downloaded and installed"

        StrCpy $2 "$TEMP\Java Runtime Environment.exe"
        nsisdl::download /TIMEOUT=30000 ${JRE_URL} $2
        Pop $R0 ;Get the return value
                StrCmp $R0 "success" +3
                MessageBox MB_OK "Download failed: $R0"
                Quit
        ExecWait $2
        Delete $2
FunctionEnd

