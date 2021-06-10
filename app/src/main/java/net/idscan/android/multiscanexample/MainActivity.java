/*
 * Copyright (c) 2017 IDScan.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Support: support@idscan.net
 */

package net.idscan.android.multiscanexample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import net.idscan.components.android.multiscan.MultiScanActivity;
import net.idscan.components.android.multiscan.Version;
import net.idscan.components.android.multiscan.common.DocumentData;
import net.idscan.components.android.multiscan.components.mrz.MRZComponent;
import net.idscan.components.android.multiscan.components.mrz.MRZData;
import net.idscan.components.android.multiscan.components.pdf417.PDF417Component;
import net.idscan.components.android.multiscan.components.pdf417.PDF417Data;
import net.idscan.components.android.multiscan.components.zxing.ZXingComponent;
import net.idscan.components.android.multiscan.components.zxing.ZXingData;

public class MainActivity extends AppCompatActivity {
    private final static int SCAN_ACTIVITY_CODE = 0x001;
    private final static int REQUEST_CAMERA_PERMISSIONS_DEFAULT = 0x100;
    private final static int REQUEST_CAMERA_PERMISSIONS_CUSTOM = 0x101;

    // Valid key.
    private final static String LIC_KEY_PDF417 = "WJEJW0aqr9vyq06nbUWIQ7b7X5CoFvZuLKwfH2+auGCNLLJlx8sdCuUKgQvI66DvNQ7gwVUGF0hHxypq1u7qd7qJtM/4cmWX1G9+cUmgijr9qy6sEC2nLXoDRjzc8UjYi4Al2dbpjdb1WFbH5AjbUF3u3U1fCntoBAafMGyA0/4=";
    private final static String LIC_KEY_MRZ = "RBuW/cWlecJE2Uqlbpdt03hN9Swu68NowojB+sutwdAl5IJDwyjyfRgPKCsgU3t4si+ZbxPvlpU2ncUp4ZAI6rilQSUwsd/9lBtu1CItiptpaTy3tRcFhA1X9mPemooH5m23HMaY/nJ+uD6L/WKmjwYbfXRiQClhA3k/Feq1owc=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((TextView) findViewById(R.id.tv_result)).setMovementMethod(new ScrollingMovementMethod());

        ((TextView) findViewById(R.id.tv_version)).setText("Version: " + Version.getVersion());

        findViewById(R.id.btn_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDefaultScanView();
            }
        });

        findViewById(R.id.btn_scan_custom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomScanView();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSIONS_DEFAULT:
                if (checkCameraPermissions()) {
                    showDefaultScanView();
                }
                break;

            case REQUEST_CAMERA_PERMISSIONS_CUSTOM:
                if (checkCameraPermissions()) {
                    showCustomScanView();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        TextView tv_result = findViewById(R.id.tv_result);
        if (requestCode == SCAN_ACTIVITY_CODE) {
            switch (resultCode) {
                case MultiScanActivity.RESULT_OK:
                    if (data != null) {
                        DocumentData document = (DocumentData) data.getSerializableExtra(MultiScanActivity.DOCUMENT_DATA);
                        if (document != null) {
                            MRZData mrzData = MRZComponent.extractDataFromDocument(document);
                            PDF417Data pdf417Data = PDF417Component.extractDataFromDocument(document);
                            ZXingData zxingData = ZXingComponent.extractDataFromDocument(document);

                            if (mrzData != null) {
                                showDocument(mrzData);
                            }

                            if (pdf417Data != null) {
                                showDocument(pdf417Data);
                            }

                            if (zxingData != null) {
                                showDocument(zxingData);
                            }
                        }
                    }
                    break;

                case MultiScanActivity.ERROR_RECOGNITION:
                    tv_result.setText(data.getStringExtra(MultiScanActivity.ERROR_DESCRIPTION));
                    break;

                case MultiScanActivity.ERROR_INVALID_CAMERA_NUMBER:
                    tv_result.setText("Invalid camera number.");
                    break;

                case MultiScanActivity.ERROR_CAMERA_NOT_AVAILABLE:
                    tv_result.setText("Camera not available.");
                    break;

                case MultiScanActivity.ERROR_INVALID_CAMERA_ACCESS:
                    tv_result.setText("Invalid camera access.");
                    break;

                case MultiScanActivity.RESULT_CANCELED:
                    break;

                default:
                    tv_result.setText("Undefined error.");
                    break;
            }
        }
    }

    private void showDocument(@NonNull MRZData data) {
        TextView tv_result = findViewById(R.id.tv_result);
        StringBuilder text = new StringBuilder();
        for (MRZData.MRZField f : data.fields.values()) {
            switch (f.type) {
                case DocumentType:
                    text.append("DocumentType: ").append(f.value).append("\n");
                    break;
                case FullName:
                    text.append("FullName: ").append(f.value).append("\n");
                    break;
                case LastName:
                    text.append("LastName: ").append(f.value).append("\n");
                    break;
                case FirstName:
                    text.append("FirstName: ").append(f.value).append("\n");
                    break;
                case Dob:
                    text.append("Dob: ").append(f.value).append("\n");
                    break;
                case Exp:
                    text.append("Exp: ").append(f.value).append("\n");
                    break;
                case DocumentNumber:
                    text.append("DocumentNumber: ").append(f.value).append("\n");
                    break;
                case Gender:
                    text.append("Gender: ").append(f.value).append("\n");
                    break;
                case IssuingState:
                    text.append("IssuingState: ").append(f.value).append("\n");
                    break;
                case Nationality:
                    text.append("Nationality: ").append(f.value).append("\n");
                    break;
                case Line1:
                    text.append("Line1: ").append(f.value).append("\n");
                    break;
                case Line2:
                    text.append("Line2: ").append(f.value).append("\n");
                    break;
                case Line3:
                    text.append("Line3: ").append(f.value).append("\n");
                    break;
            }
        }
        tv_result.setText(text.toString());
    }

    private void showDocument(@NonNull PDF417Data data) {
        TextView tv_result = findViewById(R.id.tv_result);
        tv_result.setText(new String(data.barcodeData));
    }

    private void showDocument(@NonNull ZXingData data) {
        TextView tv_result = findViewById(R.id.tv_result);
        tv_result.setText(new String(data.barcodeData));
    }

    private void showDefaultScanView() {
        if (checkCameraPermissions()) {
            MultiScanActivity.build(this)
                    .withComponent(PDF417Component.build()
                            .withLicenseKey(LIC_KEY_PDF417)
                            .complete())
                    .withComponent(MRZComponent.build()
                            .withLicenseKey(LIC_KEY_MRZ)
                            .complete())
                    .withComponent(ZXingComponent.build()
                            .withFormats(ZXingComponent.Format.values())
                            .complete())
                    .start(SCAN_ACTIVITY_CODE);
        } else {
            requestCameraPermissions(REQUEST_CAMERA_PERMISSIONS_DEFAULT);
        }
    }

    private void showCustomScanView() {
        if (checkCameraPermissions()) {
            MultiScanActivity.build(this)
                    .withComponent(PDF417Component.build()
                            .withLicenseKey(LIC_KEY_PDF417)
                            .complete())
                    .withComponent(MRZComponent.build()
                            .withLicenseKey(LIC_KEY_MRZ)
                            .complete())
                    .withComponent(ZXingComponent.build()
                            .withFormats(ZXingComponent.Format.values())
                            .complete())
                    .withCustomActivity(CustomScanActivity.class)
                    .start(SCAN_ACTIVITY_CODE);
        } else {
            requestCameraPermissions(REQUEST_CAMERA_PERMISSIONS_CUSTOM);
        }
    }

    private boolean checkCameraPermissions() {
        int status = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return (status == PackageManager.PERMISSION_GRANTED);
    }

    private void requestCameraPermissions(int requestCode) {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                requestCode);
    }
}
