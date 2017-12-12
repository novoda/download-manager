package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;

class RoomV2DatabaseFiles implements V2DatabaseFiles {

    private final RoomFileDao roomFileDao;

    RoomV2DatabaseFiles(RoomFileDao roomFileDao) {
        this.roomFileDao = roomFileDao;
    }

    @Override
    public List<String> fileNames() {
        List<RoomFile> roomFilesList = roomFileDao.loadAllFiles();
        List<String> stringFilesList = new ArrayList<>();
        
        for (RoomFile roomFile : roomFilesList) {
            stringFilesList.add(roomFile.name);
        }
        return stringFilesList;
    }
}
