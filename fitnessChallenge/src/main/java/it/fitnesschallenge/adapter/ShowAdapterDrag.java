/**
 * Questa classe implementa lo spostamento tra le card della RecyclerView i nomi dei metodi sono
 * esplicativi
 */
package it.fitnesschallenge.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class ShowAdapterDrag extends ItemTouchHelper.Callback {

    private final ItemTouchHelperContract mItemTouchHelperContract;

    public ShowAdapterDrag(ItemTouchHelperContract mItemTouchHelperContract) {
        this.mItemTouchHelperContract = mItemTouchHelperContract;
    }

    public interface ItemTouchHelperContract {
        void onRowMoved(int fromPosition, int toPosition);

        void onRowSelected(ShowAdapter.ViewHolder viewHolder);

        void onRowClear(ShowAdapter.ViewHolder viewHolder);
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Lascio vuoto in quanto non permetto lo swipe su gli elementi della recycler
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        mItemTouchHelperContract.onRowMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE)
            if (viewHolder instanceof ShowAdapter.ViewHolder) {
                ShowAdapter.ViewHolder showViewHolder = (ShowAdapter.ViewHolder) viewHolder;
                mItemTouchHelperContract.onRowSelected(showViewHolder);
            }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (viewHolder instanceof ShowAdapter.ViewHolder) {
            ShowAdapter.ViewHolder showViewHolder = (ShowAdapter.ViewHolder) viewHolder;
            mItemTouchHelperContract.onRowClear(showViewHolder);
        }
    }
}
