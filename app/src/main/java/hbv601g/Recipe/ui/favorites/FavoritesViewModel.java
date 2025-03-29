package hbv601g.Recipe.ui.favorites;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FavoritesViewModel extends ViewModel {

    private final MutableLiveData<String> text = new MutableLiveData<>();

    public LiveData<String> getText() {
        return text;
    }
}
