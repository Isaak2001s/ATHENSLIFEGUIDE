package com.athens.lifeguide.ui.places

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.athens.lifeguide.R
import com.athens.lifeguide.data.models.Place
import com.athens.lifeguide.data.models.PlaceType
import com.athens.lifeguide.databinding.FragmentPlacesBinding
import com.athens.lifeguide.databinding.ItemPlaceBinding

class PlacesFragment : Fragment() {

    private var _b: FragmentPlacesBinding? = null
    private val b get() = _b!!
    private val vm: PlacesViewModel by viewModels()
    private lateinit var adapter: PlaceAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentPlacesBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PlaceAdapter { vm.toggleFavorite(it) }
        b.recycler.layoutManager = LinearLayoutManager(requireContext())
        b.recycler.adapter = adapter

        setupTabs()
        observeVm()
    }

    override fun onResume() {
        super.onResume()
        vm.switchTab(vm.currentTab.value ?: PlacesTab.PARKS)
    }

    private fun setupTabs() {
        b.tabParks.setOnClickListener   { vm.switchTab(PlacesTab.PARKS) }
        b.tabSquares.setOnClickListener { vm.switchTab(PlacesTab.SQUARES) }
        b.tabTransit.setOnClickListener { vm.switchTab(PlacesTab.TRANSIT) }
        b.tabFavorites.setOnClickListener { vm.switchTab(PlacesTab.FAVORITES) }
    }
    private fun observeVm() {
        vm.currentTab.observe(viewLifecycleOwner) { tab -> highlightTab(tab) }
        vm.places.observe(viewLifecycleOwner)     { adapter.submitList(it) }
        vm.toastMsg.observe(viewLifecycleOwner)   { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                vm.toastMsg.value = null
            }
        }
    }

    private fun highlightTab(tab: PlacesTab) {
        val active   = requireContext().getColor(R.color.primary)
        val inactive = requireContext().getColor(R.color.text_secondary)
        b.tabParks.setTextColor(    if (tab == PlacesTab.PARKS)     active else inactive)
        b.tabSquares.setTextColor(  if (tab == PlacesTab.SQUARES)   active else inactive)
        b.tabTransit.setTextColor(  if (tab == PlacesTab.TRANSIT)   active else inactive)
        b.tabFavorites.setTextColor(if (tab == PlacesTab.FAVORITES) active else inactive)

        val selBg   = R.drawable.bg_tab_selected
        val unselBg = 0
        b.tabParks.setBackgroundResource(    if (tab == PlacesTab.PARKS)     selBg else unselBg)
        b.tabSquares.setBackgroundResource(  if (tab == PlacesTab.SQUARES)   selBg else unselBg)
        b.tabTransit.setBackgroundResource(  if (tab == PlacesTab.TRANSIT)   selBg else unselBg)
        b.tabFavorites.setBackgroundResource(if (tab == PlacesTab.FAVORITES) selBg else unselBg)
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

// ── Adapter ───────────────────────────────────────────────────────────────────

class PlaceAdapter(private val onFav: (Place) -> Unit) :
    RecyclerView.Adapter<PlaceAdapter.VH>() {

    private val list = mutableListOf<Place>()

    fun submitList(newList: List<Place>) {
        list.clear(); list.addAll(newList); notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount() = list.size
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(list[pos])

    inner class VH(private val b: ItemPlaceBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(p: Place) {
            b.tvEmoji.text       = p.type.emoji
            b.tvName.text        = p.name
            b.tvType.text        = p.type.label
            b.tvDescription.text = p.description.ifBlank { "" }
            b.tvDescription.visibility = if (p.description.isBlank()) View.GONE else View.VISIBLE

            if (p.lines.isNotEmpty()) {
                b.tvLines.visibility = View.VISIBLE
                b.tvLines.text = "Γραμμές: ${p.lines.joinToString(" · ")}"
            } else {
                b.tvLines.visibility = View.GONE
            }

            b.btnFav.text = if (p.isFavorite) "★" else "☆"
            b.btnFav.setTextColor(
                Color.parseColor(if (p.isFavorite) "#FFB300" else "#6B7280")
            )
            b.btnFav.setOnClickListener { onFav(p) }
            b.accent.setBackgroundColor(Color.parseColor(p.type.colorHex))
        }
    }
}
