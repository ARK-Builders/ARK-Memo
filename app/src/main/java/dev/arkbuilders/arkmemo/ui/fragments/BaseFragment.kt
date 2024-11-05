package dev.arkbuilders.arkmemo.ui.fragments

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {
    abstract fun onBackPressed()
}
