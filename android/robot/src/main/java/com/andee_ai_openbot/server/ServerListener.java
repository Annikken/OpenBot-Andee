package com.andee_ai_openbot.server;

import java.util.Set;

public interface ServerListener {
  void onAddModel(String model);

  void onRemoveModel(String model);

  void onConnectionEstablished(String ipAddress);

  void onServerListChange(Set<String> servers);
}
