package com.athens.lifeguide.ui.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.athens.lifeguide.R
import com.athens.lifeguide.data.models.AqiLevel
import com.athens.lifeguide.data.models.AqiStation
import com.athens.lifeguide.data.models.Session
import com.athens.lifeguide.databinding.FragmentHomeBinding
import com.athens.lifeguide.utils.AqiAdvice
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private val vm: HomeViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentHomeBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Options menu for logout
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inf: MenuInflater) =
                inf.inflate(R.menu.menu_main, menu)
            override fun onMenuItemSelected(item: MenuItem): Boolean {
                if (item.itemId == R.id.action_logout) {
                    (requireActivity() as MainActivity).logout(); return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        b.tvWelcome.text = "Γεια σου, ${Session.username}! 👋"
        b.tvDate.text    = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("el", "GR")).format(Date())

        b.swipe.setOnRefreshListener { vm.refresh() }
        b.btnRefresh.setOnClickListener { vm.refresh() }

        vm.isRefreshing.observe(viewLifecycleOwner) { b.swipe.isRefreshing = it }
        vm.aqiResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = ::bindAqi,
                onFailure = {
                    Toast.makeText(requireContext(),
                        "Δεν φορτώθηκαν δεδομένα AQI: ${it.message}", Toast.LENGTH_LONG).show()
                    bindDemoAqi()
                }
            )
        }
    }

    private fun bindAqi(s: AqiStation) {
        val level = s.level
        b.cardAqi.visibility = View.VISIBLE

        // Header colour
        b.headerAqi.setBackgroundColor(level.color())
        val textCol = if (s.aqi <= 100) Color.BLACK else Color.WHITE
        b.tvAqiNum.setTextColor(textCol)
        b.tvAqiLabel.setTextColor(textCol)
        b.tvAqiEmoji.setTextColor(textCol)

        b.tvAqiNum.text   = s.aqi.toString()
        b.tvAqiLabel.text = level.labelGr
        b.tvAqiEmoji.text = level.emoji
        b.tvStation.text  = "📍 ${s.name}"
        b.tvAdvice.text   = AqiAdvice.get(s.aqi)
        b.tvUpdated.text  = "Ενημέρωση: ${s.updatedAt ?: "—"}"

        // Progress bar
        val pct = (s.aqi.coerceIn(0, 300) * 100 / 300)
        b.aqiBar.progress   = pct
        b.aqiBar.progressTintList = ColorStateList.valueOf(level.color())

        // Components
        b.tvPm25.text = s.pm25?.let { "PM2.5  ${"%.1f".format(it)} μg/m³" } ?: "PM2.5  —"
        b.tvPm10.text = s.pm10?.let { "PM10   ${"%.1f".format(it)} μg/m³" } ?: "PM10   —"
        b.tvO3.text   = s.o3?.let   { "O₃     ${"%.1f".format(it)} μg/m³" } ?: "O₃     —"
        b.tvNo2.text  = s.no2?.let  { "NO₂    ${"%.1f".format(it)} μg/m³" } ?: "NO₂    —"
    }

    private fun bindDemoAqi() {
        bindAqi(AqiStation(0, "Αθήνα (demo)", 37.98, 23.73, 55))
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
