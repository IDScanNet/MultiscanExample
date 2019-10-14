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

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.idscan.components.android.multiscan.MultiScanActivity;
import net.idscan.components.android.multiscan.common.DocumentData;
import net.idscan.components.android.multiscan.components.mrz.MRZComponent;
import net.idscan.components.android.multiscan.components.mrz.MRZData;
import net.idscan.components.android.multiscan.components.pdf417.PDF417Component;
import net.idscan.components.android.multiscan.components.pdf417.PDF417Data;

public class CustomScanActivity extends MultiScanActivity {
    private static final String _PREFS_NAME = "CustomScanActivitySettings";

    private int _number_of_cameras = 0;
    private int _current_camera = 0;
    private DocumentData _result;

    private Button _btn_confirm;
    private TextView _tv_scanned_data;
    private ImageView _iv_info;

    private ToneGenerator _beep;

    private Handler _handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _beep = new ToneGenerator(AudioManager.STREAM_ALARM, 50);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _beep.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        _handler.removeCallbacks(_clearResult);
    }

    @Override
    protected int selectCamera(int numberOfCameras) {
        _number_of_cameras = numberOfCameras;

        SharedPreferences pref = getSharedPreferences(_PREFS_NAME, Context.MODE_PRIVATE);
        _current_camera = pref.getInt("camera", 0);

        if (_current_camera >= _number_of_cameras) {
            _current_camera = 0;
            pref.edit()
                    .putInt("camera", _current_camera)
                    .apply();
        }

        return _current_camera;
    }

    @Override
    protected View getViewFinder(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.custom_viewfinder, null);

        View old_vf = super.getViewFinder(inflater);
        if (old_vf != null) {
            FrameLayout old_vf_layout = v.findViewById(R.id.old_vf);
            ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            old_vf_layout.addView(old_vf, p);
        }

        _tv_scanned_data = v.findViewById(R.id.tv_scanned_data);
        _iv_info = v.findViewById(R.id.iv_info);
        _btn_confirm = v.findViewById(R.id.btn_confirm);
        _btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(_result);
            }
        });
        _btn_confirm.setVisibility(View.VISIBLE);
        v.findViewById(R.id.btn_next_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Select next camera.
                _current_camera++;
                if (_current_camera >= _number_of_cameras)
                    _current_camera = 0;

                if (setCamera(_current_camera)) {
                    // Save the last selected camera.
                    getSharedPreferences(_PREFS_NAME, Context.MODE_PRIVATE).edit()
                            .putInt("camera", _current_camera)
                            .apply();
                }
            }
        });

        v.findViewById(R.id.btn_flash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFlashState(!getFlashState());
            }
        });

        return v;
    }

    @Override
    protected void onData(@NonNull DocumentData result) {
        if (!result.equals(_result)) {
            _result = result;

            MRZData mrzData = MRZComponent.extractDataFromDocument(result);
            PDF417Data pdf417Data = PDF417Component.extractDataFromDocument(result);

            if (mrzData != null) {
                showDocument(mrzData);
            }

            if (pdf417Data != null) {
                showDocument(pdf417Data);
            }

            _btn_confirm.setVisibility(View.VISIBLE);
            _beep.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        }

        _handler.removeCallbacks(_clearResult);
        _handler.postDelayed(_clearResult, 2500);
    }

    private void showDocument(@NonNull PDF417Data data) {
        _tv_scanned_data.setText(new String(data.barcodeData));
    }

    private void showDocument(@NonNull MRZData data) {

        MRZData.MRZField line1 = data.fields.get(MRZData.MRZFieldType.Line1);
        MRZData.MRZField line2 = data.fields.get(MRZData.MRZFieldType.Line2);
        MRZData.MRZField line3 = data.fields.get(MRZData.MRZFieldType.Line3);

        String sb = "";
        if (line1 != null) {
            sb = sb + line1.value + "\n";
        }
        if (line2 != null) {
            sb = sb + line2.value + "\n";
        }
        if (line3 != null) {
            sb = sb + line3.value + "\n";
        }
        _tv_scanned_data.setText(sb);
    }

    private Runnable _clearResult = new Runnable() {
        @Override
        public void run() {
            _result = null;
            _tv_scanned_data.setText("");
        }
    };
}
