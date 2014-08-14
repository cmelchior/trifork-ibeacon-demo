package com.trifork.ibeacon.detectors;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;

/**
 * @author Christian Melchior
 *
 * See http://stackoverflow.com/questions/18906988/what-is-the-ibeacon-bluetooth-profile for futher
 * info.
 */
public class IBeaconParser extends BeaconParser {

    public IBeaconParser() {
        super();
        this.setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
    }

    @Override
    public byte[] constructAdvertismentPackage(Beacon beacon, int measuredPower) {
        byte[] advertismentPackage = new byte[25];
        advertismentPackage[0] = (byte) 0x4C;  // Apple ID, Big Endian
        advertismentPackage[1] = (byte) 0x00;
        advertismentPackage[2] = (byte) 0x02;  // Identifies advertisement as iBeacon
        advertismentPackage[3] = (byte) 0x15;
        System.arraycopy(uuidToBytes(beacon.getId1().toString()), 0, advertismentPackage, 4, 16); // UUID
        System.arraycopy(uint16ToBytes(beacon.getId2().toInt()), 0, advertismentPackage, 20, 2); // Major
        System.arraycopy(uint16ToBytes(beacon.getId3().toInt()), 0, advertismentPackage, 22, 2); // Minor
        advertismentPackage[24] = int8ToByte(measuredPower); // Measured power at index 24
        return advertismentPackage;
    }
}
