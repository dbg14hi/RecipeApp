package hbv601g.Recipe.ui.favorites;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel class for the Favorites Fragment.
 */
public class FavoritesViewModel extends ViewModel {
    private final MutableLiveData<String> text = new MutableLiveData<>();

    /**
     * Returns live data for observing text change
     *
     * @return Livedata object.
     */
    public LiveData<String> getText() {
        return text;
    }
}
