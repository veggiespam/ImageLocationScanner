package com.veggiespam.imagelocationscanner;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.scanner.scancheck.ScanCheckType;

import burp.api.montoya.logging.Logging;

public class BurpStartup implements BurpExtension
{
    @Override
    public void initialize(MontoyaApi api)
    {
        api.extension().setName("Image Location Scanner");

        Logging logging = api.logging();

        logging.logToOutput("Image Location Scanner extension initialized.");

        api.scanner().registerPassiveScanCheck(new BurpPassiveScan(), ScanCheckType.PER_REQUEST);

        // api.scanner().registerActiveScanCheck(new MyActiveScanCheck(), ScanCheckType.PER_INSERTION_POINT);
    }
}