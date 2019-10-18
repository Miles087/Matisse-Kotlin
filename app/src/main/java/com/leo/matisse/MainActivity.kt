package com.leo.matisse

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.matisse.Matisse
import com.matisse.MimeTypeManager
import com.matisse.compress.CompressHelper
import com.matisse.compress.FileUtil
import com.matisse.entity.CaptureStrategy
import com.matisse.entity.ConstValue
import com.matisse.listener.Consumer
import com.matisse.listener.OnCheckedListener
import com.matisse.listener.OnSelectedListener
import com.matisse.utils.Platform
import com.matisse.widget.CropImageView
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import java.text.DecimalFormat
import java.util.concurrent.Callable
import kotlin.math.log10
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_media_store.setOnClickListener {
            RxPermissions(this@MainActivity)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .subscribe {
                    if (!it) {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.permission_request_denied,
                            Toast.LENGTH_LONG
                        ).show()
                        return@subscribe
                    }
                    Matisse.from(this@MainActivity)
                        .choose(MimeTypeManager.ofAll())
                        .countable(true)
                        .capture(true)
                        .isCrop(true)
                        .cropStyle(CropImageView.Style.CIRCLE)
                        .maxSelectable(3)
                        .setStatusIsDark(true)
                        .theme(R.style.CustomMatisseStyle)
                        .captureStrategy(
                            CaptureStrategy(
                                true,
                                "${Platform.getPackageName(this@MainActivity)}.fileprovider"
                            )
                        )
                        .thumbnailScale(0.8f)
                        .gridExpectedSize(resources.getDimensionPixelSize(R.dimen.grid_expected_size))
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .imageEngine(Glide4Engine())
                        .setOnSelectedListener(object : OnSelectedListener {
                            override fun onSelected(uriList: List<Uri>, pathList: List<String>) {
                                // DO SOMETHING IMMEDIATELY HERE
                                Log.e("onSelected", "onSelected: pathList=$pathList")
                            }
                        })
                        .setNoticeConsumer(object : Consumer<String> {
                            override fun accept(params: String) {
                                showToast(params)
                            }
                        })
                        .setOnCheckedListener(object : OnCheckedListener {
                            override fun onCheck(isChecked: Boolean) {
                                // DO SOMETHING IMMEDIATELY HERE
                                Log.e("isChecked", "onCheck: isChecked=$isChecked")
                            }
                        })
                        .forResult(ConstValue.REQUEST_CODE_CHOOSE)
                }
        }

        btn_media_multi.setOnClickListener {
            RxPermissions(this@MainActivity)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .subscribe {
                    if (!it) {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.permission_request_denied,
                            Toast.LENGTH_LONG
                        ).show()
                        return@subscribe
                    }
                    Matisse.from(this@MainActivity)
                        .choose(MimeTypeManager.ofAll(), true)
                        .countable(false)
                        .capture(true)
                        .isCrop(true)
                        .cropStyle(CropImageView.Style.CIRCLE)
                        .setStatusIsDark(false)
                        .theme(R.style.CustomMatisseStyle)
                        .captureStrategy(
                            CaptureStrategy(
                                true,
                                "${Platform.getPackageName(this@MainActivity)}.fileprovider"
                            )
                        )
                        .maxSelectable(1)
                        .thumbnailScale(0.8f)
                        .gridExpectedSize(resources.getDimensionPixelSize(R.dimen.grid_expected_size))
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .imageEngine(Glide4Engine())
                        .setOnSelectedListener(object : OnSelectedListener {
                            override fun onSelected(uriList: List<Uri>, pathList: List<String>) {
                                // DO SOMETHING IMMEDIATELY HERE
                                Log.e("onSelected", "onSelected: pathList=$pathList")
                            }
                        })
                        .setNoticeConsumer(object : Consumer<String> {
                            override fun accept(params: String) {
                                showToast(params)
                            }
                        })
                        .setOnCheckedListener(object : OnCheckedListener {
                            override fun onCheck(isChecked: Boolean) {
                                // DO SOMETHING IMMEDIATELY HERE
                                Log.e("isChecked", "onCheck: isChecked=$isChecked")
                            }
                        })
                        .forResult(ConstValue.REQUEST_CODE_CHOOSE)
                }
        }
    }

    private fun showToast(value: String) {
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) return

        if (requestCode == ConstValue.REQUEST_CODE_CHOOSE && resultCode == Activity.RESULT_OK) {
            var string = ""
            val uriList = Matisse.obtainResult(data)
            val strList = Matisse.obtainPathResult(data)

            uriList.forEach {
                string += it.toString() + "\n"
            }

            string += "\n"

            strList.forEach {
                string += it + "\n"
            }

            // 原文件
            val file = FileUtil.getFileByPath(Matisse.obtainPathResult(data)[0])

            // 压缩后的文件         （多个文件压缩可以循环压缩）
            val file1 = CompressHelper.getDefault(applicationContext)?.compressToFile(file)
            string += getReadableFileSize(file.length()) + " PK " +
                    getReadableFileSize(file1?.length() ?: 0)
            string = "\n\n$string"

            text.text = "\n\n$string"
        }
    }

    private fun getReadableFileSize(size: Long): String {
        if (size <= 0) return "0"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#")
            .format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }
}
