package dev.arkbuilders.arkmemo.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import coil.load
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.DialogDonateQrBinding
import dev.arkbuilders.arkmemo.ui.viewmodels.QRViewModel
import dev.arkbuilders.arkmemo.ui.views.toast
import dev.arkbuilders.arkmemo.utils.copyToClipboard


class DonateDialog(private val walletAddress: String,
                   private val title: String,
                   private val onPositiveClick: (() -> Unit)? = null,
                   private val onCloseClicked: (() -> Unit)? = null,
): DialogFragment() {

    companion object {
        val TAG: String = DonateDialog::class.java.name
    }
    private lateinit var binding: DialogDonateQrBinding
    private val qrViewModel: QRViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogDonateQrBinding.inflate(inflater)
        initViews()
        return binding.root
    }

    private fun initViews() {

        dialog?.setCanceledOnTouchOutside(false)

        binding.ivClose.setOnClickListener {
            onCloseClicked?.invoke()
            dismiss()
        }

        binding.tvTitle.text = title

        binding.layoutDownloadQr.setOnClickListener {
            onPositiveClick?.invoke()
            dismiss()
        }

        binding.tvAddress.text = walletAddress
        binding.layoutCopy.setOnClickListener {
            context?.copyToClipboard(getString(R.string.setting_donate_wallet_clipboard_label),
                walletAddress)
        }

        initQRImage()
    }

    private fun initQRImage() {
        qrViewModel.generateQRCode(walletAddress) { bitmap ->
            binding.ivQr.load(bitmap)
            binding.layoutDownloadQr.setOnClickListener {
                qrViewModel.saveQRCodeImage(walletAddress, bitmap) { path ->
                    context?.let { ctx ->
                        toast(ctx, getString(R.string.toast_save_qr_success, path))
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun getTheme(): Int {
        return R.style.MemoDialog
    }

}