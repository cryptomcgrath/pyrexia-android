package com.cryptomcgrath.pyrexia

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.databinding.ViewDataBinding
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.RxStore
import com.edwardmcgrath.blueflux.core.State
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/**
 * Defines a function used to bind view models of type DiffableItem
 *
 * When invoked, the function will take an instance of DiffableItem
 * and binding it to the view (binding) which is accessed through
 * its enclosing BindFunViewHolder
 *
 */
typealias BindFun = (DiffableItem) -> Unit

/**
 * A RecyclerView.ViewHolder that uses a function of type BindFun to perform the view binding
 */
class BindFunViewHolder(val binding: ViewDataBinding, private val bindFun: BindFun): RecyclerView.ViewHolder(binding.root)  {
    fun bind(diffableItem: DiffableItem) {
        bindFun.invoke(diffableItem)
        binding.executePendingBindings()
    }
}

/**
 * DiffableItemAdapter is a RecyclerView.Adapter that
 *
 * 1) Uses a List<DiffableItem> for its items
 * @see itemModels
 *
 * 2) Uses an AsyncListDiffer to hold/submit/diff the list
 * @see differ
 *
 * 3) Uses a BindFunViewHolder to perform view holder binding to the items in the list
 * @see BindFunViewHolder
 **
 */
abstract class DiffableItemAdapter: RecyclerView.Adapter<BindFunViewHolder>() {
    protected val itemModels: List<DiffableItem>
        get() = differ.currentList

    /**
     * You should override with your list of different view types,
     * each of which must implement DiffableItem
     *
     * i.e.
     *
     * override val viewTypes = listOf(
     *     ClubHeaderDiffableItem::class.java,
     *     DateHeaderDiffableItem::class.java,
     *     PickupSlotDiffableItem::class.java)
     */
    protected abstract val viewTypes: List<Class<out DiffableItem>>

    /**
     * you should override with a list differ implementation
     *
     * i.e.
     *
     * override val differ = AsyncListDiffer(this, DIFF_CALLBACK)
     */
    protected abstract val differ: AsyncListDiffer<DiffableItem>

    override fun getItemCount(): Int = itemModels.size

    override fun getItemViewType(position: Int): Int = viewTypes.indexOf(itemModels[position].javaClass)

    override fun onBindViewHolder(holder: BindFunViewHolder, position: Int) = holder.bind(itemModels[position])

    companion object {
        @JvmStatic
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DiffableItem>() {
            override fun areItemsTheSame(oldItem: DiffableItem, newItem: DiffableItem): Boolean {
                return oldItem.areItemsTheSame(newItem)
            }

            override fun areContentsTheSame(oldItem: DiffableItem, newItem: DiffableItem): Boolean {
                return oldItem.areContentsTheSame(newItem)
            }
        }
    }
}

/**
 * RxStoreAdapter is a DiffableItemAdapter that
 *
 * Upon attachment to recycler view, observes an RxStore's State T
 * and continually renders its items and any changes to the State will
 * cause a new list to be submitted to the list differ.
 *
 * @param store The RxStore which holds the state that this adapter uses to create its items
 *
 * @param debounceTimeMillis Throttles the recyclerview ui diffs so as to only perform a diff if
 * the state has not changed within the debounceTimeMillis.  This creates a more fluid ui experience by
 * preventing successive ui renderings when there are multiple state change emissions within the time window.
 *
 * @see RxStore
 * @see DiffableItemAdapter
 *
 */
abstract class RxStoreAdapter<T: State>(val store: RxStore<T>): DiffableItemAdapter() {
    protected val disposables = CompositeDisposable()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        store.stateStream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = { state ->
                            differ.submitList(buildList(state))
                        }, onError = {
                        }
                )
                .addTo(disposables)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposables.clear()
    }

    /**
     * buildList
     *
     * @param state The state which should be used to build the list
     *
     * @return List<DiffableItem> List of items to be submitted to the list differ
     */
    abstract fun buildList(state: T): List<DiffableItem>
}


