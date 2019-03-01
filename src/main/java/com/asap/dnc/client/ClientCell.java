package com.asap.dnc.client;

import com.asap.dnc.core.Cell;

public class ClientCell extends Cell {

    public ClientCell(int nRows, int nCols, int ctrlReq) {
        super(nRows, nCols, ctrlReq);
        System.out.println("Creating cell in client grid...");
    }

    // TODO: Implement fillCell()
    @Override
    public boolean fillCell(int rowIdx, int colIdx, int player) {
        return true;
    }
}
