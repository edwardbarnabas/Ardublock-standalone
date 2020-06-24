DRIVER='CH34x_Install_V1.5.pkg'

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