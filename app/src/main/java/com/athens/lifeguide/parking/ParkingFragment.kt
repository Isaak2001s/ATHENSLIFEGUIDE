package com.athens.lifeguide.ui.parking

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.athens.lifeguide.R
import com.athens.lifeguide.data.db.ParkingAreaEntity
import com.athens.lifeguide.data.db.ReservationEntity
import com.athens.lifeguide.databinding.FragmentParkingBinding
import com.athens.lifeguide.databinding.ItemParkingBinding

class ParkingFragment : Fragment() {

    private var _b: FragmentParkingBinding? = null
    private val b get() = _b!!
    private val vm: ParkingViewModel by viewModels()

    private lateinit var areasAdapter: ParkingAreaAdapter
    private lateinit var reservationsAdapter: ReservationAdapter
    private var currentFilter = "all" // "all" | "active" | "cancelled"
    private var allReservations = listOf<ReservationEntity>()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentParkingBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupTabs()
        setupFilters()
        observeVm()
    }

    private fun setupAdapters() {
        areasAdapter = ParkingAreaAdapter(
            onReserve = { area ->
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Κράτηση Θέσης")
                    .setMessage("Θέλετε να κρατήσετε θέση στο «${area.name}» για 15 λεπτά;\n\nΤιμή: €${area.pricePerHour}/ώρα\nΔιαθέσιμες: ${area.availableSpots}/${area.totalSpots}")
                    .setPositiveButton("Κράτηση") { _, _ -> vm.reserve(area) }
                    .setNegativeButton("Ακύρωση", null)
                    .show()
            },
            onFav = { area ->
                Toast.makeText(requireContext(), "⭐ «${area.name}» στα αγαπημένα!", Toast.LENGTH_SHORT).show()
            }
        )
        b.recyclerAreas.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerAreas.adapter = areasAdapter

        reservationsAdapter = ReservationAdapter { res ->
            if (res.status == "active") {
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Ακύρωση Κράτησης")
                    .setMessage("Είστε σίγουροι ότι θέλετε να ακυρώσετε την κράτηση στο «${res.parkingAreaName}»;")
                    .setPositiveButton("Ναι, ακύρωση") { _, _ -> vm.cancelReservation(res) }
                    .setNegativeButton("Όχι", null)
                    .show()
            }
        }
        b.recyclerReservations.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerReservations.adapter = reservationsAdapter

        b.swipe.setOnRefreshListener { vm.load() }
    }

    private fun setupTabs() {
        b.btnTabSpots.setOnClickListener { showSpots() }
        b.btnTabBookings.setOnClickListener { showBookings() }
    }

    private fun showSpots() {
        b.recyclerAreas.visibility = View.VISIBLE
        b.bookingsContainer.visibility = View.GONE
        // Styling
        b.btnTabSpots.setBackgroundColor(requireContext().getColor(R.color.primary))
        b.btnTabSpots.setTextColor(Color.WHITE)
        b.btnTabBookings.setBackgroundColor(Color.TRANSPARENT)
        b.btnTabBookings.setTextColor(requireContext().getColor(R.color.primary))
    }

    private fun showBookings() {
        b.recyclerAreas.visibility = View.GONE
        b.bookingsContainer.visibility = View.VISIBLE
        // Styling
        b.btnTabBookings.setBackgroundColor(requireContext().getColor(R.color.primary))
        b.btnTabBookings.setTextColor(Color.WHITE)
        b.btnTabSpots.setBackgroundColor(Color.TRANSPARENT)
        b.btnTabSpots.setTextColor(requireContext().getColor(R.color.primary))
        applyFilter()
    }

    private fun setupFilters() {
        b.btnFilterAll.setOnClickListener { setFilter("all") }
        b.btnFilterActive.setOnClickListener { setFilter("active") }
        b.btnFilterCancelled.setOnClickListener { setFilter("cancelled") }
    }

    private fun setFilter(filter: String) {
        currentFilter = filter
        val active = requireContext().getColor(R.color.primary)
        val inactive = requireContext().getColor(R.color.text_secondary)

        b.btnFilterAll.setTextColor(if (filter == "all") active else inactive)
        b.btnFilterAll.setBackgroundResource(if (filter == "all") R.drawable.bg_tab_selected else 0)
        b.btnFilterActive.setTextColor(if (filter == "active") active else inactive)
        b.btnFilterActive.setBackgroundResource(if (filter == "active") R.drawable.bg_tab_selected else 0)
        b.btnFilterCancelled.setTextColor(if (filter == "cancelled") active else inactive)
        b.btnFilterCancelled.setBackgroundResource(if (filter == "cancelled") R.drawable.bg_tab_selected else 0)

        applyFilter()
    }

    private fun applyFilter() {
        val filtered = when (currentFilter) {
            "active" -> allReservations.filter { it.status == "active" }
            "cancelled" -> allReservations.filter { it.status == "cancelled" }
            else -> allReservations
        }

        val headerText = when (currentFilter) {
            "active" -> "Ενεργές Κρατήσεις"
            "cancelled" -> "Ακυρωμένες Κρατήσεις"
            else -> "Όλες οι Κρατήσεις"
        }
        val headerColor = when (currentFilter) {
            "active" -> "#4CAF50"
            "cancelled" -> "#F44336"
            else -> "#48CAE4"
        }

        b.tvReservationsHeader.text = headerText
        b.tvReservationsHeader.setTextColor(Color.parseColor(headerColor))

        if (filtered.isEmpty()) {
            b.recyclerReservations.visibility = View.GONE
            b.tvReservationsHeader.visibility = View.GONE
            b.tvEmptyBookings.visibility = View.VISIBLE
        } else {
            b.recyclerReservations.visibility = View.VISIBLE
            b.tvReservationsHeader.visibility = View.VISIBLE
            b.tvEmptyBookings.visibility = View.GONE
            reservationsAdapter.submitList(filtered)
        }
    }

    private fun observeVm() {
        vm.state.observe(viewLifecycleOwner) { state ->
            b.swipe.isRefreshing = false
            when (state) {
                is ParkingUiState.Loading -> b.swipe.isRefreshing = true
                is ParkingUiState.Ready -> {
                    areasAdapter.submitList(state.areas)
                    allReservations = state.reservations
                    applyFilter()
                }
                is ParkingUiState.Error -> {
                    Toast.makeText(requireContext(), state.msg, Toast.LENGTH_LONG).show()
                }
            }
        }

        vm.toastMsg.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                vm.toastMsg.value = null
            }
        }
    }

    override fun onResume() { super.onResume(); vm.load() }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

// ── Parking Areas Adapter ─────────────────────────────────────────────────

class ParkingAreaAdapter(
    private val onReserve: (ParkingAreaEntity) -> Unit,
    private val onFav: (ParkingAreaEntity) -> Unit = {}
) :
    RecyclerView.Adapter<ParkingAreaAdapter.VH>() {

    private val list = mutableListOf<ParkingAreaEntity>()

    fun submitList(newList: List<ParkingAreaEntity>) {
        list.clear(); list.addAll(newList); notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemParkingBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = list.size
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(list[pos])

    inner class VH(private val b: ItemParkingBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(area: ParkingAreaEntity) {
            b.tvName.text = area.name
            b.tvAddress.text = area.address
            b.tvSpots.text = "${area.availableSpots}/${area.totalSpots} θέσεις"
            b.tvPrice.text = "€${area.pricePerHour}/ώρα"

            val levelText = when (area.occupancyLevel) {
                "low" -> "Χαμηλή"
                "medium" -> "Μέτρια"
                "high" -> "Υψηλή"
                else -> area.occupancyLevel
            }
            b.tvOccupancy.text = levelText

            val color = when (area.occupancyLevel) {
                "low" -> "#4CAF50"
                "medium" -> "#FFC107"
                "high" -> "#F44336"
                else -> "#607D8B"
            }
            b.accent.setBackgroundColor(Color.parseColor(color))

            if (area.availableSpots > 0) {
                b.btnReserve.isEnabled = true
                b.btnReserve.text = "Κράτηση (15 λεπτά)"
                b.btnReserve.setOnClickListener { onReserve(area) }
            } else {
                b.btnReserve.isEnabled = false
                b.btnReserve.text = "Πλήρες"
            }

            b.btnFav.text = "☆"
            b.btnFav.setOnClickListener {
                val isFav = b.btnFav.text == "★"
                b.btnFav.text = if (isFav) "☆" else "★"
                onFav(area)
            }
        }
    }
}

// ── Reservations Adapter (improved) ───────────────────────────────────────

class ReservationAdapter(private val onCancel: (ReservationEntity) -> Unit) :
    RecyclerView.Adapter<ReservationAdapter.VH>() {

    private val list = mutableListOf<ReservationEntity>()

    fun submitList(newList: List<ReservationEntity>) {
        list.clear(); list.addAll(newList); notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val card = com.google.android.material.card.MaterialCardView(parent.context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
            setCardBackgroundColor(Color.parseColor("#161B22"))
            radius = 24f
            cardElevation = 0f
            strokeWidth = 0
        }
        val layout = android.widget.LinearLayout(parent.context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
        }
        card.addView(layout)
        return VH(card, layout)
    }

    override fun getItemCount() = list.size
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(list[pos])

    inner class VH(
        private val card: com.google.android.material.card.MaterialCardView,
        private val layout: android.widget.LinearLayout
    ) : RecyclerView.ViewHolder(card) {

        fun bind(r: ReservationEntity) {
            layout.removeAllViews()
            val ctx = layout.context
            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())

            // Name + status badge
            val headerLayout = android.widget.LinearLayout(ctx).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val tvName = android.widget.TextView(ctx).apply {
                text = "🅿️ ${r.parkingAreaName}"
                setTextColor(Color.parseColor("#E6EDF3"))
                textSize = 15f
                layoutParams = android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            headerLayout.addView(tvName)

            val isActive = r.status == "active"
            val badgeColor = if (isActive) "#4CAF50" else "#9E9E9E"
            val badgeText = if (isActive) "ΕΝΕΡΓΗ" else "ΑΚΥΡΩΜΕΝΗ"

            val tvBadge = android.widget.TextView(ctx).apply {
                text = badgeText
                setTextColor(Color.parseColor(badgeColor))
                textSize = 11f
                setPadding(16, 4, 16, 4)
                setBackgroundColor(Color.parseColor(badgeColor + "22"))
            }
            headerLayout.addView(tvBadge)
            layout.addView(headerLayout)

            // Time info
            val tvTime = android.widget.TextView(ctx).apply {
                text = "Κράτηση: ${dateFormat.format(java.util.Date(r.reservedAt))}"
                setTextColor(Color.parseColor("#8B949E"))
                textSize = 12f
                setPadding(0, 8, 0, 0)
            }
            layout.addView(tvTime)

            val tvExpiry = android.widget.TextView(ctx).apply {
                text = "Λήξη: ${dateFormat.format(java.util.Date(r.expiresAt))}"
                setTextColor(Color.parseColor("#8B949E"))
                textSize = 12f
            }
            layout.addView(tvExpiry)

            val tvPrice = android.widget.TextView(ctx).apply {
                text = "€${r.pricePerHour}/ώρα"
                setTextColor(Color.parseColor("#48CAE4"))
                textSize = 13f
                setPadding(0, 8, 0, 0)
            }
            layout.addView(tvPrice)

            // Cancel button only for active
            if (isActive) {
                val btn = com.google.android.material.button.MaterialButton(ctx).apply {
                    text = "Ακύρωση Κράτησης"
                    setTextColor(Color.parseColor("#F44336"))
                    setBackgroundColor(Color.TRANSPARENT)
                    strokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336"))
                    strokeWidth = 2
                    cornerRadius = 16
                    isAllCaps = false
                    val lp = android.widget.LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    lp.topMargin = 16
                    layoutParams = lp
                    setOnClickListener { onCancel(r) }
                }
                layout.addView(btn)
            }
        }
    }
}