;NSIS install script for GooTool

SetCompressor /SOLID lzma

;--------------------------------
;Include Modern UI

  !include "MUI.nsh"

;--------------------------------
;General

;Name and file
Name "GooTool"
OutFile "GooToolInstaller.exe"

;Default installation folder
InstallDir "$PROGRAMFILES\GooTool"

;Get installation folder from registry if available
InstallDirRegKey HKCU "Software\GooTool" ""

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING
	!define MUI_HEADERIMAGE
	!define MUI_HEADERIMAGE_BITMAP_NOSTRETCH
	!define MUI_HEADERIMAGE_BITMAP "nsis-header.bmp"
	!define MUI_HEADERIMAGE_RIGHT
;	!define MUI_ICON "C:\temp\SimpleText\installer\setup.ico"
;	!define MUI_UNICON "C:\temp\SimpleText\installer\setup.ico"

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_LICENSE "..\dist_src\common\doc\LICENSE.txt"
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
;  VIProductVersion "1.2.3.4"
;  VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductName" "Test Application"
;  VIAddVersionKey /LANG=${LANG_ENGLISH} "Comments" "A test comment"
;  VIAddVersionKey /LANG=${LANG_ENGLISH} "CompanyName" "Fake company"
;  VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalTrademarks" "Test Application is a trademark of Fake company"
;  VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalCopyright" "© Fake company"
;  VIAddVersionKey /LANG=${LANG_ENGLISH} "FileDescription" "Test Application"
;  VIAddVersionKey /LANG=${LANG_ENGLISH} "FileVersion" "1.2.3"

!define JRE_VERSION "1.6"
!define JRE_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=26223&/jre-6u11-windows-i586-p.exe"


;--------------------------------
;Installer Sections

Section "GooTool (required)" SecDummy

  Call DetectJRE

  SectionIn RO

  ;Files to be installed
  SetOutPath "$INSTDIR"

   File /r "..\dist\win32\*.*"
;   File "C:\temp\SimpleText\images\SimpleText.ico"
;	File "C:\temp\SimpleText\swt-win32-3138.dll"

;	SetOutPath "$INSTDIR\lib"

;	File "C:\temp\SimpleText\lib\org.eclipse.swt.win32.win32.x86_3.1.0.jar"

;  SetOutPath "$INSTDIR"

    ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\SimpleText "Install_Dir" "$INSTDIR"

  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\GooTool" "DisplayName" "GooTool"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\GooTool" "UninstallString" '"$INSTDIR\bin\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\GooTool" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\GooTool" "NoRepair" 1

  ;SetOutPath "$INSTDIR\bin"
  WriteUninstaller "uninstall.exe"

SectionEnd

; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"
  CreateDirectory "$SMPROGRAMS\GooTool"
  CreateShortCut "$SMPROGRAMS\GooTool\GooTool.lnk" "$INSTDIR\bin\gootool.exe" "" "$INSTDIR\bin\gootool.exe"
  CreateShortCut "$SMPROGRAMS\GooTool\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe"
SectionEnd

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\GooTool"
  DeleteRegKey HKLM SOFTWARE\GooTool
  DeleteRegKey /ifempty HKCU "Software\GooTool"

	; Remove shortcuts
  RMDir /r "$SMPROGRAMS\GooTool"

  ; Remove directories used
  RMDir /r "$INSTDIR"

SectionEnd


Function DetectJRE
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" \
             "CurrentVersion"
  StrCmp $2 ${JRE_VERSION} done

  Call GetJRE

  done:
FunctionEnd

Function GetJRE
        MessageBox MB_OK "${PRODUCT_NAME} uses Java ${JRE_VERSION}, it will now \
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

