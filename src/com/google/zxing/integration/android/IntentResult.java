/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.integration.android;

/**
 * <p>
 * Encapsulates the result of a barcode scan invoked through
 * {@link IntentIntegrator}.
 * </p>
 * 
 * @author Sean Owen
 */
public final class IntentResult {

    /**
     * Default {@link StringBuilder} capacity to use when making the result
     * human-readable.
     */
    private static final int DEFAULT_TOSTRING_CAPACITY = 100;

    /**
     * Raw content of barcode.
     */
    private final String contents;
    /**
     * Name of format, like "QR_CODE", "UPC_A". See {@code BarcodeFormat} for
     * more format names.
     */
    private final String formatName;
    /**
     * Raw bytes of the barcode content, if applicable, or null otherwise.
     */
    private final byte[] rawBytes;
    /**
     * Rotation of the image, in degrees, which resulted in a successful scan.
     * May be null.
     */
    private final Integer orientation;
    /**
     * Name of the error correction level used in the barcode, if applicable.
     */
    private final String errorCorrectionLevel;

    /**
     * Simple null constructor; contains no real data.
     */
    IntentResult() {
        this(null, null, null, null, null);
    }

    /**
     * Create an immutable result from a scan.
     * 
     * @param barcodeContents
     *            raw content of barcode
     * @param barcodeFormatName
     *            name of format, like "QR_CODE", "UPC_A"
     * @param barcodeRawBytes
     *            raw bytes of the barcode content, if applicable
     * @param barcodeOrientation
     *            rotation of the image, in degrees, which resulted in a
     *            successful scan
     * @param barcodeErrorCorrectionLevel
     *            name of the error correction level used in the barcode, if
     *            applicable
     */
    IntentResult(final String barcodeContents, final String barcodeFormatName,
            final byte[] barcodeRawBytes, final Integer barcodeOrientation,
            final String barcodeErrorCorrectionLevel) {
        contents = barcodeContents;
        formatName = barcodeFormatName;
        rawBytes = barcodeRawBytes;
        orientation = barcodeOrientation;
        errorCorrectionLevel = barcodeErrorCorrectionLevel;
    }

    /**
     * @return raw content of barcode
     */
    public String getContents() {
        return contents;
    }

    /**
     * @return name of format, like "QR_CODE", "UPC_A". See
     *         {@code BarcodeFormat} for more format names.
     */
    public String getFormatName() {
        return formatName;
    }

    /**
     * @return raw bytes of the barcode content, if applicable, or null
     *         otherwise
     */
    public byte[] getRawBytes() {
        return rawBytes;
    }

    /**
     * @return rotation of the image, in degrees, which resulted in a successful
     *         scan. May be null.
     */
    public Integer getOrientation() {
        return orientation;
    }

    /**
     * @return name of the error correction level used in the barcode, if
     *         applicable
     */
    public String getErrorCorrectionLevel() {
        return errorCorrectionLevel;
    }

    @Override
    public String toString() {
        StringBuilder dialogText = new StringBuilder(DEFAULT_TOSTRING_CAPACITY);
        dialogText.append("Format: ").append(formatName).append('\n');
        dialogText.append("Contents: ").append(contents).append('\n');
        int rawBytesLength;
        if (rawBytes == null) {
            rawBytesLength = 0;
        } else {
            rawBytesLength = rawBytes.length;
        }
        dialogText.append("Raw bytes: (").append(rawBytesLength)
                .append(" bytes)\n");
        dialogText.append("Orientation: ").append(orientation).append('\n');
        dialogText.append("EC level: ").append(errorCorrectionLevel)
                .append('\n');
        return dialogText.toString();
    }

}
