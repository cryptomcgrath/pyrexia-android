package com.cryptomcgrath.pyrexia.thermostat

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import com.cryptomcgrath.pyrexia.BindFunViewHolder
import com.cryptomcgrath.pyrexia.CentralState
import com.cryptomcgrath.pyrexia.RxStoreAdapter
import com.cryptomcgrath.pyrexia.databinding.CycleInfoItemBinding
import com.cryptomcgrath.pyrexia.databinding.HistoryChartItemBinding
import com.cryptomcgrath.pyrexia.databinding.HistoryInfoItemBinding
import com.cryptomcgrath.pyrexia.databinding.PropaneItemBinding
import com.cryptomcgrath.pyrexia.databinding.StatEnabledItemBinding
import com.cryptomcgrath.pyrexia.databinding.StatModeItemBinding
import com.cryptomcgrath.pyrexia.databinding.ThermostatItemBinding
import com.cryptomcgrath.pyrexia.databinding.ThermostatItemLoadingBinding
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.statlist.StatDiffableItem
import com.cryptomcgrath.pyrexia.statlist.StatLoadingDiffableItem
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.RxStore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

internal class ThermostatAdapter(private val context: Context,
                                 store: RxStore<CentralState>,
                                 private val dispatcher: Dispatcher,
                                 private val pyDevice: PyDevice,
                                 private val statId: Int
): RxStoreAdapter<CentralState>(store) {
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
                    it.getDeviceState(pyDevice).historyPages.sortedBy { page ->
                        page.points.firstOrNull()?.actionTs
                    }.filter { page ->
                        page.statId == statId
                    }.map { page ->
                        page.points
                    }.flatten().distinctBy {
                        it.actionTs
                    }
                }.distinctUntilChanged().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        holder.binding.pointsChart.setSeriesData(it.toSeries(store.state.getDeviceState(pyDevice).stats.firstOrNull {
                            it.program.id == statId
                        }?.program?.mode))
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

    override fun buildList(state: CentralState): List<DiffableItem> {
        val items = mutableListOf<DiffableItem>()
        state.getDeviceState(pyDevice)?.let { deviceState ->
            if (deviceState.loading) {
                items += StatLoadingDiffableItem()
            } else {
                deviceState.stats.firstOrNull {
                    it.program.id == statId
                }?.let { stat ->
                    items += StatDiffableItem(stat, pyDevice, dispatcher, deviceState.updating)
                    items += StatModeDiffableItem(mode = stat.program.mode)
                    items += PropaneDiffableItem(
                        dispatcher = dispatcher,
                        pyDevice = pyDevice,
                        stat = stat,
                        totalRun = stat.control.totalRun,
                        runCapacity = stat.control.runCapacity
                    )
                    items += StatEnabledDiffableItem(
                        dispatcher = dispatcher,
                        enabled = stat.program.enabled,
                        pyDevice = pyDevice,
                        statId = stat.program.id
                    )

                    items += HistoryChartDiffableItem(store)

                    //items += HistoryInfoDiffableItem(state.historyOldtoNew)

                    //items += createCycleInfoItems(
                    //    context = context,
                    //    history = state.historyOldtoNew,
                    //    mode = it.program.mode)
                }
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
                                dispatcher.post(ThermostatEvent.RequestHistoryBefore(pyDevice, statId, timeStamp.toInt()))
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

private const val TAG="ThermostatAdapter"