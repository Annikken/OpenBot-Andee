package com.andee_openbot.main;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easypermissions.EasyPermissions;
import com.andee_openbot.OpenBotApplication;

import com.andee_openbot.R;
import com.andee_openbot.databinding.FragmentBluetoothBinding;
import com.andee_openbot.vehicle.Vehicle;

public class BluetoothFragment extends Fragment {
  private RecyclerView rv;
  private FragmentBluetoothBinding binding;
  private Vehicle vehicle;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    vehicle = OpenBotApplication.vehicle;
    binding = FragmentBluetoothBinding.inflate(inflater, container, false);
    rv = binding.rv;
    showDevicesByRv();
    return binding.getRoot();
  }

  private void showDevicesByRv() {
    rv.setLayoutManager(new LinearLayoutManager(getActivity()));
    SparseArray<int[]> res = new SparseArray<>();
    res.put(
        R.layout.ble_listview_tv,
        new int[] {R.id.ble_name, R.id.ble_address, R.id.ble_connection_state});
    vehicle.setBleAdapter(
        new ScanDeviceAdapter(requireActivity(), vehicle.getDeviceList(), res),
            (itemView, position) -> {
              vehicle.stopScan();
              ProgressBar pb = requireView().findViewById(R.id.progress_bar);
              TextView tv = requireView().findViewById(R.id.btn_refresh);
              pb.setVisibility(View.INVISIBLE);
              tv.setVisibility(View.VISIBLE);
              BleDevice device = vehicle.getDeviceList().get(position);
              if (vehicle.getBleDevice() == null
                  || vehicle.getBleDevice().address.equals(device.address)) {
                vehicle.setBleDevice(device);
              }
              vehicle.toggleConnection(position, device);
            });
    rv.setAdapter(vehicle.getBleAdapter());
  }

  @Override
  public void onViewCreated(@NonNull View view, @NonNull Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    // for most devices whose version is over Android6,scanning may need GPS permission

    EasyPermissions.with(requireActivity())
        .request(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT)
        .autoRetryWhenUserRefuse(true, null)
        .result(
                (grantAll, results) -> checkPermission(grantAll));

  }

  private void checkPermission(boolean grantAll) {
    TextView tv = requireView().findViewById(R.id.btn_refresh);
    if (grantAll) {
      if (!BleManager.isBluetoothOn()) BleManager.toggleBluetooth(true);
      tv.setOnClickListener(
              view -> startScan());
      startScan();
    } else {
      Toast.makeText(
              requireActivity().getApplicationContext(),
              "Please go to settings to grant location and Near by Devices permission manually",
              Toast.LENGTH_LONG)
          .show();
    }
  }

  private void startScan() {
    ProgressBar pb = requireView().findViewById(R.id.progress_bar);
    TextView tv = requireView().findViewById(R.id.btn_refresh);
    vehicle.startScan();
    pb.setVisibility(View.VISIBLE);
    tv.setVisibility(View.INVISIBLE);
    new Handler(Looper.getMainLooper())
        .postDelayed(
                () -> {
                  pb.setVisibility(View.INVISIBLE);
                  tv.setVisibility(View.VISIBLE);
                },
            4000);
  }

//  private boolean isGpsOn() {
//    LocationManager locationManager =
//        (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
//    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    vehicle.stopScan();
  }
}
