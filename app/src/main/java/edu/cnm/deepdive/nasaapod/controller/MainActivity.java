package edu.cnm.deepdive.nasaapod.controller;

import android.Manifest;
import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import edu.cnm.deepdive.android.DateTimePickerFragment;
import edu.cnm.deepdive.android.DateTimePickerFragment.Mode;
import edu.cnm.deepdive.nasaapod.R;
import edu.cnm.deepdive.nasaapod.viewmodel.MainViewModel;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private static final int EXTERNAL_STORAGE_PERMISSIONS_REQUEST = 1000;

  private MainViewModel viewModel;
  private NavController navController;
  private ProgressBar loading;
  private Calendar calendar;
  private BottomNavigationView navigator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    checkPermissions(permission.READ_EXTERNAL_STORAGE, permission.WRITE_EXTERNAL_STORAGE);
    loading = findViewById(R.id.loading);
    setupNavigation();
    setupViewModel();
    setupCalendarPicker();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (requestCode == EXTERNAL_STORAGE_PERMISSIONS_REQUEST) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

      } else {

      }
    }
  }

  public void loadApod(Date date) {
    setProgressVisibility(View.VISIBLE);
    viewModel.setApodDate(date);
  }

  public void setProgressVisibility(int visibility) {
    loading.setVisibility(visibility);
  }

  public void showToast(String message) {
    setProgressVisibility(View.GONE);
    Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
    toast.setGravity(Gravity.BOTTOM, 0,
        getResources().getDimensionPixelOffset(R.dimen.toast_vertical_margin));
    toast.show();
  }

  private void checkPermissions(String... permissions) {
    List<String> permissionsToRequest = new LinkedList<>();
    List<String> permissionsToExplain = new LinkedList<>();
    for (String permission : permissions) {
      if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
        permissionsToRequest.add(permission);
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
          permissionsToExplain.add(permission);
        }
      } else {

      }
    }
    if (!permissionsToExplain.isEmpty()) {
    } else if (!permissionsToRequest.isEmpty()) {
      ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]),
          EXTERNAL_STORAGE_PERMISSIONS_REQUEST);
    }
  }

  private void setupViewModel() {
    viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    viewModel.getApod().observe(this, (apod) -> {
      calendar.setTime(apod.getDate());
      if (navController.getCurrentDestination().getId() != R.id.navigation_image) {
        navigator.setSelectedItemId(R.id.navigation_image);
        navController.navigate(R.id.navigation_image);
      }
    });
    viewModel.getThrowable().observe(this, (throwable) -> {
      if (throwable != null) {
        showToast(getString(R.string.error_message, throwable.getMessage()));
      }
    });
    getLifecycle().addObserver(viewModel);
  }

  private void setupNavigation() {
    NavOptions options = new NavOptions.Builder()
        .setPopUpTo(R.id.navigation_map, true)
        .build();
    AppBarConfiguration appBarConfiguration =
        new AppBarConfiguration.Builder(R.id.navigation_image, R.id.navigation_history)
            .build();
    navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    navigator = findViewById(R.id.navigator);
    navigator.setOnNavigationItemSelectedListener((item) -> {
      navController.navigate(item.getItemId(), null, options);
      return true;
    });
  }

  private void setupCalendarPicker() {
    calendar = Calendar.getInstance();
    FloatingActionButton calendarFab = findViewById(R.id.calendar_fab);
    calendarFab.setOnClickListener((v) -> {
      DateTimePickerFragment fragment = new DateTimePickerFragment();
      fragment.setCalendar(calendar);
      fragment.setMode(Mode.DATE);
      fragment.setOnChangeListener((cal) -> loadApod(cal.getTime()));
      fragment.show(getSupportFragmentManager(), fragment.getClass().getName());
    });
  }

}
