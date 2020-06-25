DRIVER='CH34x_Install_V1.5.pkg'

# Dialog Title
dialogTitle="Ardublock and CH34x Installer"

# obtain the password from a dialog box
authPass=$(/usr/bin/osascript <<EOT
tell application "System Events"
    activate
    repeat
        display dialog "Please enter your Apple ID password to start software installation." ¬
            default answer "" ¬
            with title "$dialogTitle" ¬
            with hidden answer ¬
            buttons {"Quit", "Continue"} default button 2
        if button returned of the result is "Quit" then
            return 1
            exit repeat
        else if the button returned of the result is "Continue" then
            set pswd to text returned of the result
            set usr to short user name of (system info)
            try
                do shell script "echo test" user name usr password pswd with administrator privileges
                return pswd
                exit repeat
            end try
        end if
        end repeat
        end tell
EOT
)

# Abort if the Quit button was pressed
if [ "$authPass" == 1 ]; then
    /bin/echo "User aborted. Exiting..."
    exit 1
fi

# function that replaces sudo command
sudo () {
    /bin/echo $authPass | /usr/bin/sudo -S "$@"
}



# uninstall any previous drivers
echo "Uninstalling previous versions of CH34x Driver"

sudo kextunload /Library/Extensions/usbserial.kext
sudo kextunload /System/Library/Extensions/usb.kext
sudo rm -rf /System/Library/Extensions/usb.kext
sudo rm -rf /Library/Extensions/usbserial.kext

# "CH34x Driver exists"
echo "Installing V1.5 CH34x Driver"
echo "Please wait. This may take up to 5 min...\n"
sudo installer -verboseR -allowUntrusted -pkg $DRIVER -target /

# load driver so that you don't need to restart the computer
echo "Enabling CH34x driver to avoid reboot"
sudo kextload /Library/Extensions/usbserial.kext

exit 0
