package com.cinestream.live;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<List<Channel>> channels = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final XtreamClient xtreamClient = new XtreamClient();

    public LiveData<List<Channel>> getChannels() {
        return channels;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadData() {
        isLoading.setValue(true);
        xtreamClient.fetchCredentials(new XtreamClient.CredentialsCallback() {
            @Override
            public void onSuccess(Credential credential) {
                fetchCategoriesAndChannels();
            }

            @Override
            public void onError(String error) {
                // Handle error, maybe expose it via another LiveData
                isLoading.setValue(false);
            }
        });
    }

    private void fetchCategoriesAndChannels() {
        xtreamClient.fetchLiveCategories(new XtreamClient.CategoriesCallback() {
            @Override
            public void onSuccess(List<Category> categoryList) {
                categories.postValue(categoryList);
                xtreamClient.fetchLiveStreams(new XtreamClient.ChannelsCallback() {
                    @Override
                    public void onSuccess(List<Channel> channelList) {
                        channels.postValue(channelList);
                        isLoading.postValue(false);
                    }

                    @Override
                    public void onError(String error) {
                        // Handle error
                        isLoading.postValue(false);
                    }
                });
            }

            @Override
            public void onError(String error) {
                // Handle error
                isLoading.postValue(false);
            }
        });
    }

    public XtreamClient getXtreamClient() {
        return xtreamClient;
    }

    public Credential getCredential() {
        return xtreamClient.getCurrentCredential();
    }
}
