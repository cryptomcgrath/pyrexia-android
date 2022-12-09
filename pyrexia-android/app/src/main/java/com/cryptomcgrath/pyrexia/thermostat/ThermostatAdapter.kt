package com.cryptomcgrath.pyrexia.thermostat

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import com.cryptomcgrath.pyrexia.BindFunViewHolder
import com.cryptomcgrath.pyrexia.RxStoreAdapter
import com.cryptomcgrath.pyrexia.databinding.CycleInfoItemBinding
import com.cryptomcgrath.pyrexia.databinding.HistoryChartItemBinding
import com.cryptomcgrath.pyrexia.databinding.HistoryInfoItemBinding
import com.cryptomcgrath.pyrexia.databinding.PropaneItemBinding
import com.cryptomcgrath.pyrexia.databinding.StatEnabledItemBinding
import com.cryptomcgrath.pyrexia.databinding.StatModeItemBinding
import com.cryptomcgrath.pyrexia.databinding.ThermostatItemBinding
import com.cryptomcgrath.pyrexia.databinding.ThermostatItemLoadingBinding
import com.cryptomcgrath.pyrexia.statlist.StatDiffableItem
import com.cryptomcgrath.pyrexia.statlist.StatLoadingDiffableItem
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.RxStore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy

internal class ThermostatAdapter(private val context: Context,
                                 store: RxStore<ThermostatState>,
                                 private val dispatcher: Dispatcher
): RxStoreAdapter<ThermostatState>(store) {
    override val viewTypes: List<Class<out DiffableItem>> =
        listOf(
            StatDiffableItem::class.java,
            StatModeDiffableItem::class.java,
            StatEnabledDiffableItem::class.java,
            PropaneDiffableItem::class.java,
            StatLoadingDiffableItem::class.java,
            HistoryChartDiffableItem::class.java,
            HistoryInfoDiffableItem::class.java,
            CycleInfoDiffableItem::class.java
        )

    private var historyDisposable: Disposable? = null

    override val differ: AsyncListDiffer<DiffableItem> = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onViewAttachedToWindow(holder: BindFunViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder.binding is HistoryChartItemBinding) {
            holder.binding.model?.let { model ->
                historyDisposable = model.store.stateStream.map {
                    it.historyOldtoNew
                }.observeOn(AndroidSchedulers.mainThread())

                    .subscribeBy(
                    onNext = {
                        holder.binding.pointsChart.setSeriesData(it.toSeries(context))
                        Log.d(TAG, "addSeries ${it.size}")
                    }, onError = {
                        // ignore
                    }
                )
            }

        }
    }

    override fun onViewDetachedFromWindow(holder: BindFunViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder.binding is HistoryChartItemBinding) {
            historyDisposable?.dispose()
            historyDisposable = null
        }
    }

    override fun buildList(state: ThermostatState): List<DiffableItem> {
        val items = mutableListOf<DiffableItem>()
        if (state.isLoading) {
            items += StatLoadingDiffableItem()
        } else {
            state.current?.let {
                items += StatDiffableItem(state.current, dispatcher)
                items += StatModeDiffableItem(mode = state.current.program.mode)
                items += PropaneDiffableItem(
                    dispatcher = dispatcher,
                    totalRun = state.current.control.totalRun,
                    runCapacity = state.current.control.runCapacity
                )
                items += StatEnabledDiffableItem(dispatcher, state.current.program.enabled, state.current.program.id)

                items += HistoryChartDiffableItem(store)

                items += HistoryInfoDiffableItem(state.historyOldtoNew)

                items += createCycleInfoItems(
                    context = context,
                    history = state.historyOldtoNew,
                    mode = it.program.mode)
            }
        }
        return items
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindFunViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewTypes[viewType]) {
            StatDiffableItem::class.java -> {
                val binding = ThermostatItemBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as StatDiffableItem
                }
            }

            StatModeDiffableItem::class.java -> {
                val binding = StatModeItemBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as StatModeDiffableItem
                }
            }

            StatEnabledDiffableItem::class.java -> {
                val binding = StatEnabledItemBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as StatEnabledDiffableItem
                }
            }

            PropaneDiffableItem::class.java -> {
                val binding = PropaneItemBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as PropaneDiffableItem
                }
            }

            StatLoadingDiffableItem::class.java -> {
                val binding = ThermostatItemLoadingBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as StatLoadingDiffableItem
                }
            }

            HistoryChartDiffableItem::class.java -> {
                val binding = HistoryChartItemBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    val model = it as HistoryChartDiffableItem
                    binding.model = model

                    binding.pointsChart.apply {
                        listener = object: PointsChart.Listener {
                            override fun onMoreDataRequest(timeStamp: Long) {
                                dispatcher.post(ThermostatEvent.RequestMoreHistory(timeStamp))
                            }
                        }
                    }
                }
            }

            HistoryInfoDiffableItem::class.java -> {
                val binding = HistoryInfoItemBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as HistoryInfoDiffableItem
                }
            }

            CycleInfoDiffableItem::class.java -> {
                val binding = CycleInfoItemBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as CycleInfoDiffableItem
                }
            }

            else -> throw IllegalStateException("unknown view type")
        }
    }
}