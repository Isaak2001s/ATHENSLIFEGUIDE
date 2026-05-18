package com.athens.lifeguide.ui.favorites

import android.graphics.*
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.*
import com.athens.lifeguide.data.db.FavoriteEntity
import com.athens.lifeguide.databinding.FragmentFavoritesBinding
import com.athens.lifeguide.databinding.ItemFavoriteBinding

class FavoritesFragment : Fragment() {

    private var _b: FragmentFavoritesBinding? = null
    private val b get() = _b!!
    private val vm: FavoritesViewModel by viewModels()
    private lateinit var adapter: FavAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentFavoritesBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FavAdapter { vm.remove(it) }
        b.recycler.layoutManager = LinearLayoutManager(requireContext())
        b.recycler.adapter = adapter

        attachSwipeToDelete()
        observeVm()
    }

    override fun onResume() { super.onResume(); vm.load() }

    private fun observeVm() {
        vm.favorites.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            b.tvEmpty.visibility    = if (list.isEmpty()) View.VISIBLE else View.GONE
            b.recycler.visibility   = if (list.isEmpty()) View.GONE  else View.VISIBLE
        }
        vm.toastMsg.observe(viewLifecycleOwner) { msg ->
            msg?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                vm.toastMsg.value = null }
        }
    }

    private fun attachSwipeToDelete() {
        val paint = Paint().apply { color = Color.parseColor("#C62828") }
        val cb = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder,
                                t: RecyclerView.ViewHolder) = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) =
                vm.remove(adapter.getItem(vh.adapterPosition))
            override fun onChildDraw(
                c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder,
                dX: Float, dY: Float, state: Int, active: Boolean
            ) {
                val v = vh.itemView
                c.drawRect(v.right + dX, v.top.toFloat(),
                    v.right.toFloat(), v.bottom.toFloat(), paint)
                // Draw trash emoji
                val txt = "🗑"
                val tp = Paint().also { it.textSize = 52f }
                c.drawText(txt, v.right - 80f, v.top + (v.height / 2f) + 18f, tp)
                super.onChildDraw(c, rv, vh, dX, dY, state, active)
            }
        }
        ItemTouchHelper(cb).attachToRecyclerView(b.recycler)
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

// ── Adapter ───────────────────────────────────────────────────────────────────

class FavAdapter(private val onDelete: (FavoriteEntity) -> Unit) :
    RecyclerView.Adapter<FavAdapter.VH>() {

    private val list = mutableListOf<FavoriteEntity>()

    fun submitList(newList: List<FavoriteEntity>) {
        list.clear(); list.addAll(newList); notifyDataSetChanged()
    }

    fun getItem(pos: Int) = list[pos]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = list.size
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(list[pos])

    inner class VH(private val b: ItemFavoriteBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(f: FavoriteEntity) {
            val emoji = when (f.placeType) {
                "park"   -> "🌳"
                "square" -> "⛲"
                "metro"  -> "🚇"
                "tram"   -> "🚊"
                "bus"    -> "🚌"
                "aqi"    -> "💨"
                else     -> "📍"
            }
            val typeLabel = when (f.placeType) {
                "park"   -> "Πάρκο"
                "square" -> "Πλατεία"
                "metro"  -> "Σταθμός Μετρό"
                "tram"   -> "Στάση Τραμ"
                "bus"    -> "Στάση Λεωφορείου"
                "aqi"    -> "Σταθμός Αέρα"
                else     -> f.placeType
            }
            b.tvEmoji.text       = emoji
            b.tvName.text        = f.placeName
            b.tvType.text        = typeLabel
            b.tvDescription.text = f.description.ifBlank { "—" }
            b.tvCoords.text      = "${"%.4f".format(f.latitude)}°N, ${"%.4f".format(f.longitude)}°E"
            b.btnDelete.setOnClickListener { onDelete(f) }

            val accentColor = when (f.placeType) {
                "park"   -> "#2E7D32"; "square" -> "#1565C0"
                "metro"  -> "#6A1B9A"; "tram"   -> "#E65100"
                "aqi"    -> "#37474F"; else     -> "#607D8B"
            }
            b.accent.setBackgroundColor(Color.parseColor(accentColor))
        }
    }
}
