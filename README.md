LoyaltyKeyring
==============

A simple Android application to store and (later) display barcodes.

This was designed with loyalty cards in mind, but could be used with things like
coupons as well.

Note that not all scanners will be able to read barcodes from phone screens. In
particular, laser scanners seem to have trouble, while optical scanners should
work.

This makes heavy use of the ZXing library, and their Barcode Scanner application
in particular. If Barcode Scanner is not installed, you will be prompted to
install it the first time it would be used.

See also:
  http://code.google.com/p/zxing/
  https://play.google.com/store/apps/details?id=com.google.zxing.client.android
