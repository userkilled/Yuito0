package net.accelf.yuito

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.connyduck.calladapter.networkresult.NetworkResult
import at.connyduck.calladapter.networkresult.onFailure
import at.connyduck.calladapter.networkresult.onSuccess
import com.keylesspalace.tusky.R
import com.keylesspalace.tusky.databinding.ItemDrawerFooterBinding
import com.keylesspalace.tusky.entity.Instance
import com.keylesspalace.tusky.util.BindingHolder
import com.mikepenz.materialdrawer.model.AbstractDrawerItem

class FooterDrawerItem : AbstractDrawerItem<FooterDrawerItem, BindingHolder<ItemDrawerFooterBinding>>() {

    override val type = R.id.instanceData

    override val layoutRes: Int
        get() = throw UnsupportedOperationException()

    private lateinit var binding: ItemDrawerFooterBinding

    override fun generateView(ctx: Context, parent: ViewGroup): View {
        binding = ItemDrawerFooterBinding.inflate(LayoutInflater.from(ctx))
        binding.instanceData.setTag(com.mikepenz.materialdrawer.R.id.material_drawer_item, this)
        return binding.root
    }

    override fun bindView(holder: BindingHolder<ItemDrawerFooterBinding>, payloads: List<Any>) {
        super.bindView(holder, payloads)

        holder.itemView.setPadding(0, 0, 0, 0)
        binding.instanceData.setTextColor(binding.instanceData.hintTextColors)
    }

    override fun getViewHolder(v: View): BindingHolder<ItemDrawerFooterBinding> = throw UnsupportedOperationException()

    fun setInstance(instance: NetworkResult<Instance>) {
        instance
            .onSuccess {
                binding.instanceData.text = listOf(it.title, it.domain, it.version).joinToString("\n")
            }
            .onFailure {
                binding.instanceData.text = binding.root.context.getString(R.string.instance_data_failed)
            }
    }
}
