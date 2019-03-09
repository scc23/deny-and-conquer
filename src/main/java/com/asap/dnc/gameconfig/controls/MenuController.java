package com.asap.dnc.gameconfig.controls;

import com.asap.dnc.gameconfig.GameConfig;
import com.asap.dnc.network.gameconfig.ConnectionResponseHandler;
import com.asap.dnc.network.gameconfig.client.ClientConnection;

public interface MenuController {

    public boolean onGameHost();

    public boolean onGameJoin(String hostAddress);

    public  void setConnectionResponseHandler(ConnectionResponseHandler connectionResponseHandler);

    public void setGameConfig(GameConfig gameConfig);

    public void startGame();
}
